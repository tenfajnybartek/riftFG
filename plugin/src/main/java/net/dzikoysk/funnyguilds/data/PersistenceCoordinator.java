package net.dzikoysk.funnyguilds.data;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.ArrayDeque;
import java.util.Deque;

/** Serializes immutable write requests, including deletions, in capture order. */
public final class PersistenceCoordinator implements AutoCloseable {

    // Accessed only by the persistence worker. Failed deletions must stay ahead of later saves.
    private final Deque<Runnable> pending = new ArrayDeque<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "FunnyGuilds-persistence");
        thread.setDaemon(true);
        return thread;
    });

    public CompletableFuture<Void> submit(Runnable snapshot) {
        return CompletableFuture.runAsync(() -> {
            this.pending.addLast(snapshot);
            this.drain();
        }, this.executor);
    }

    public CompletableFuture<Void> retry() {
        return CompletableFuture.runAsync(this::drain, this.executor);
    }

    private void drain() {
        while (!this.pending.isEmpty()) {
            this.pending.getFirst().run();
            this.pending.removeFirst();
        }
    }

    @Override
    public void close() {
        this.executor.shutdown();
        try {
            // Never close the database while a write is still running.
            if (!this.executor.awaitTermination(60, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Persistence did not finish within 60 seconds; database remains open");
            }
        }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for persistence", exception);
        }
    }
}
