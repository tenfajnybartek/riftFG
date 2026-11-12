package net.dzikoysk.funnyguilds.feature.war;

import net.dzikoysk.funnyguilds.FunnyGuilds;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import net.dzikoysk.funnyguilds.nms.api.packet.PacketCallbacks;
import net.dzikoysk.funnyguilds.user.User;

public class WarPacketCallbacks implements PacketCallbacks {

    private final FunnyGuilds plugin;
    private final User user;
    private final AtomicBoolean attackQueued = new AtomicBoolean();
    private final AtomicBoolean infoQueued = new AtomicBoolean();

    public WarPacketCallbacks(FunnyGuilds plugin, User user) {
        this.plugin = plugin;
        this.user = user;
    }

    @Override
    public void handleRightClickEntity(int entityId, boolean isMainHand) {
        if (!isMainHand) {
            return;
        }

        this.enqueue(this.infoQueued, () -> new WarInfoAsyncTask(this.plugin, this.plugin.getGuildEntityHelper(), this.user, entityId));
    }

    @Override
    public void handleAttackEntity(int entityId, boolean isMainHand) {
        if (!isMainHand) {
            return;
        }

        this.enqueue(this.attackQueued,
                () -> new WarAttackAsyncTask(this.plugin.getFunnyServer(), this.plugin.getGuildEntityHelper(), this.user, entityId)
        );
    }

    private void enqueue(AtomicBoolean queued, Supplier<? extends Runnable> action) {
        if (!this.plugin.isEnabled() || !queued.compareAndSet(false, true)) {
            return;
        }
        try {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                try {
                    if (this.user.isOnline()) {
                        action.get().run();
                    }
                }
                finally {
                    queued.set(false);
                }
            });
        }
        catch (RuntimeException exception) {
            queued.set(false);
            if (this.plugin.isEnabled()) {
                throw exception;
            }
        }
    }

}
