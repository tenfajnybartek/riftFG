package net.dzikoysk.funnyguilds.feature.command.admin;

import net.dzikoysk.funnycommands.stereotypes.FunnyCommand;
import net.dzikoysk.funnyguilds.data.DataModel;
import net.dzikoysk.funnyguilds.data.database.SQLDataModel;
import net.dzikoysk.funnyguilds.data.database.serializer.DatabaseGuildSerializer;
import net.dzikoysk.funnyguilds.data.database.serializer.DatabaseRegionSerializer;
import net.dzikoysk.funnyguilds.data.flat.FlatDataModel;
import net.dzikoysk.funnyguilds.event.SimpleEventHandler;
import net.dzikoysk.funnyguilds.event.guild.GuildPreRenameEvent;
import net.dzikoysk.funnyguilds.event.guild.GuildRenameEvent;
import net.dzikoysk.funnyguilds.feature.command.AbstractFunnyCommand;
import net.dzikoysk.funnyguilds.feature.command.GuildValidation;
import net.dzikoysk.funnyguilds.guild.Guild;
import net.dzikoysk.funnyguilds.shared.FunnyIOUtils;
import net.dzikoysk.funnyguilds.user.User;
import org.bukkit.command.CommandSender;
import org.panda_lang.utilities.inject.annotations.Inject;
import static net.dzikoysk.funnyguilds.feature.command.DefaultValidation.when;

public final class NameCommand extends AbstractFunnyCommand {

    @Inject
    public DataModel dataModel;

    @FunnyCommand(
            name = "${admin.name.name}",
            permission = "funnyguilds.admin",
            completer = "guilds:3",
            acceptsExceeded = true
    )
    public void execute(CommandSender sender, String[] args) {
        when(args.length < 1, config -> config.generalNoTagGiven);
        when(args.length < 2, config -> config.adminNoNewNameGiven);

        Guild guild = GuildValidation.requireGuildByTag(args[0]);
        when(!this.config.nameRegex.matches(args[1]) || args[1].length() < this.config.createNameMinLength || args[1].length() > this.config.createNameLength, config -> config.createOLName);
        when(this.guildManager.nameExists(args[1]), config -> config.createNameExists);

        User admin = AdminUtils.getAdminUser(sender);

        String oldName = guild.getName();
        if (!SimpleEventHandler.handle(new GuildPreRenameEvent(AdminUtils.getCause(admin), admin, guild, oldName, args[1]))) {
            return;
        }

        String oldRegion = guild.getRegion().map(net.dzikoysk.funnyguilds.guild.Region::getName).orNull();
        Runnable removeOldRegion = this.dataModel instanceof SQLDataModel
                ? guild.getRegion().map(DatabaseRegionSerializer::prepareDelete).orElseGet(() -> {})
                : () -> {};
        guild.getRegion().peek(region -> region.setName(args[1]));
        guild.setName(args[1]);
        Runnable snapshot = this.dataModel.prepareSave(false);
        if (this.dataModel instanceof SQLDataModel) {
            this.plugin.getDataPersistenceHandler().submitWrite(() -> this.plugin.getDatabase().get().transaction(() -> {
                snapshot.run();
                removeOldRegion.run();
            }));
        }
        else {
            Runnable removeOldFiles = net.dzikoysk.funnyguilds.data.PendingDeletion.prepare(
                    this.plugin, "guild", guild.getUUID().toString(), oldName, oldRegion);
            this.plugin.getDataPersistenceHandler().submitWrite(() -> {
                snapshot.run();
                removeOldFiles.run();
            });
        }
        this.messageService.getMessage(config -> config.adminNameChanged)
                .with("{GUILD}", guild.getName())
                .receiver(sender)
                .send();

        SimpleEventHandler.handle(new GuildRenameEvent(AdminUtils.getCause(admin), admin, guild, oldName, args[1]));
    }

}
