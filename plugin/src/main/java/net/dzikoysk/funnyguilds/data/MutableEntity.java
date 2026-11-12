package net.dzikoysk.funnyguilds.data;

import net.dzikoysk.funnyguilds.Entity;

public interface MutableEntity extends Entity {

    void markChanged();

    void markUnchanged();

    boolean wasChanged();

    default long getRevision() {
        return 0;
    }

    default void markSaved(long revision) {
        this.markUnchanged();
    }

}
