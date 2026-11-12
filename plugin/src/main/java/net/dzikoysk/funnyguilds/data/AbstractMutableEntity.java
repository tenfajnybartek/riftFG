package net.dzikoysk.funnyguilds.data;

import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractMutableEntity implements MutableEntity {

    private final AtomicLong revision = new AtomicLong(1);
    private final AtomicLong savedRevision = new AtomicLong();

    @Override
    public void markChanged() {
        this.revision.incrementAndGet();
    }

    @Override
    public void markUnchanged() {
        this.markSaved(this.getRevision());
    }

    @Override
    public boolean wasChanged() {
        return this.revision.get() != this.savedRevision.get();
    }

    @Override
    public long getRevision() {
        return this.revision.get();
    }

    @Override
    public void markSaved(long revision) {
        this.savedRevision.accumulateAndGet(revision, Math::max);
    }

}
