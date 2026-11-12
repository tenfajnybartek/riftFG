package net.dzikoysk.funnyguilds.feature.command.user;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import net.dzikoysk.funnycommands.stereotypes.FunnyCommand;
import net.dzikoysk.funnycommands.stereotypes.FunnyComponent;
import net.dzikoysk.funnyguilds.feature.command.AbstractFunnyCommand;
import net.dzikoysk.funnyguilds.feature.command.GuildCommandPermission;
import net.dzikoysk.funnyguilds.feature.command.HasGuildPermission;
import net.dzikoysk.funnyguilds.guild.Guild;
import net.dzikoysk.funnyguilds.shared.bukkit.ItemUtils;
import net.dzikoysk.funnyguilds.shared.bukkit.LocationUtils;
import net.dzikoysk.funnyguilds.user.User;
import net.dzikoysk.funnyguilds.user.UserCache;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import static net.dzikoysk.funnyguilds.feature.command.DefaultValidation.when;

@FunnyComponent
public final class BaseCommand extends AbstractFunnyCommand {

    @FunnyCommand(
            name = "${user.base.name}",
            aliases = "${user.base.aliases}",
            description = "${user.base.description}",
            permission = "funnyguilds.base",
            acceptsExceeded = true,
            playerOnly = true
    )
    public void execute(Player player, @HasGuildPermission(GuildCommandPermission.BASE) User member, Guild guild) {
        when(!this.config.regionsEnabled, config -> config.regionsDisabled);
        when(!this.config.baseEnable, config -> config.baseTeleportationDisabled);
        when(member.getCache().getTeleportation() != null, config -> config.baseIsTeleportation);

        List<ItemStack> requiredItems = player.hasPermission("funnyguilds.vip.base")
                ? Collections.emptyList()
                : this.config.baseItems;

        if (!ItemUtils.playerHasEnoughItems(player, requiredItems, config -> config.baseItems)) {
            return;
        }


        if (this.config.baseDelay.isZero()) {
            this.completeTeleport(player, member, guild, requiredItems);
            return;
        }

        Duration time = player.hasPermission("funnyguilds.vip.baseTeleportTime")
                ? this.config.baseDelayVip
                : this.config.baseDelay;

        Location before = player.getLocation();
        Instant teleportStart = Instant.now();
        UserCache cache = member.getCache();

        cache.setTeleportation(Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            if (!player.isOnline()) {
                cache.getTeleportation().cancel();
                cache.setTeleportation(null);
                return;
            }

            if (!LocationUtils.equals(player.getLocation(), before)) {
                cache.getTeleportation().cancel();
                this.messageService.getMessage(config -> config.baseMove)
                        .receiver(member)
                        .send();
                cache.setTeleportation(null);
                return;
            }

            if (Duration.between(teleportStart, Instant.now()).compareTo(time) >= 0) {
                cache.getTeleportation().cancel();
                this.completeTeleport(player, member, guild, requiredItems);
                cache.setTeleportation(null);
            }
        }, 0L, 10L));

        this.messageService.getMessage(config -> config.baseDontMove)
                .receiver(member)
                .with("{TIME}", time.getSeconds())
                .send();
    }

    private void completeTeleport(Player player, User member, Guild guild, List<ItemStack> cost) {
        if (!member.getGuild().contentEquals(guild) || this.guildManager.findByUuid(guild.getUUID()).isEmpty()
                || !guild.hasHome() || !ItemUtils.playerHasEnoughItems(player, cost, config -> config.baseItems)) {
            return;
        }
        if (!ItemUtils.takeItems(player, cost)) {
            return;
        }
        boolean teleported;
        try {
            teleported = player.isOnline() && player.teleport(guild.getHome().get());
        }
        catch (RuntimeException exception) {
            ItemUtils.returnItems(player, ItemUtils.toArray(cost));
            throw exception;
        }
        if (!teleported) {
            ItemUtils.returnItems(player, ItemUtils.toArray(cost));
            return;
        }
        this.messageService.getMessage(config -> config.baseTeleport).receiver(member).send();
    }

}
