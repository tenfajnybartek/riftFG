package net.dzikoysk.funnyguilds.data;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import net.dzikoysk.funnyguilds.FunnyGuilds;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class DataPersistenceHandler {

    private final FunnyGuilds plugin;
    private final PersistenceCoordinator coordinator = new PersistenceCoordinator();
    private final AtomicBoolean saving = new AtomicBoolean();
    private final AtomicBoolean retryNeeded = new AtomicBoolean();
    private boolean saveRequested;
    private BukkitTask dataPersistenceHandlerTask;

    public DataPersistenceHandler(FunnyGuilds plugin) {
        this.plugin = plugin;
    }

    public void startHandler() {
        this.stopHandler();
        long interval = this.plugin.getPluginConfiguration().dataInterval * 60L * 20L;
        this.dataPersistenceHandlerTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            if (this.retryNeeded.get()) {
                this.coordinator.retry().whenComplete((ignored, failure) -> {
                    this.retryNeeded.set(failure != null);
                    if (failure != null) { FunnyGuilds.getPluginLogger().error("Persistence retry failed", failure); }
                    else if (this.plugin.isEnabled()) {
                        Bukkit.getScheduler().runTask(this.plugin, () -> {
                            this.saveRequested = false;
                            this.requestSave();
                        });
                    }
                });
            }
            else if (!this.saving.get()) {
                this.save(false);
            }
        }, interval, interval);
    }

    public CompletableFuture<Void> submitWrite(Runnable snapshot) {
        return this.coordinator.submit(snapshot).whenComplete((ignored, failure) -> {
            if (failure != null) {
                this.retryNeeded.set(true);
                FunnyGuilds.getPluginLogger().error("Persistence failed; unsaved records remain dirty", failure);
            }
        });
    }

    /** Coalesce combat/create updates and never build an unbounded queue during an outage. */
    public void requestSave() {
        if (this.saveRequested || this.plugin.isDisabling()) { return; }
        this.saveRequested = true;
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            this.saveRequested = false;
            if (this.retryNeeded.get()) { return; }
            if (this.saving.get()) {
                this.saveRequested = true;
                return;
            }
            this.save(false);
        });
    }

    public CompletableFuture<Void> save(boolean full) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Capture persistence snapshots on the primary thread");
        }
        Runnable data = this.plugin.getDataModel().prepareSave(!full);
        Runnable invitations = this.plugin.getInvitationPersistenceHandler().prepareSave();
        this.saving.set(true);
        return this.submitWrite(() -> {
            try {
                data.run();
                invitations.run();
            }
            finally {
                this.saving.set(false);
            }
        }).whenComplete((ignored, failure) -> {
            if (failure == null && !this.plugin.isDisabling() && this.plugin.isEnabled()) {
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    if (this.saveRequested) {
                        this.saveRequested = false;
                        this.requestSave();
                    }
                });
            }
        });
    }

    public void stopHandler() {
        if (this.dataPersistenceHandlerTask != null) {
            this.dataPersistenceHandlerTask.cancel();
            this.dataPersistenceHandlerTask = null;
        }
    }

    public void reloadHandler() {
        this.startHandler();
    }

    public void close() {
        this.stopHandler();
        this.coordinator.close();
    }
}
