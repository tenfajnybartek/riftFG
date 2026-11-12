package net.dzikoysk.funnyguilds.nms.api;

import org.bukkit.Bukkit;

public final class NmsAccessorHolder {

    // Minecraft 26.3: version.json -> world_version.
    private static final int MINIMUM_DATA_VERSION = 5023;
    private static final String ACCESSOR_CLASS = "net.dzikoysk.funnyguilds.nms.v26_3.V26_3NmsAccessor";

    private NmsAccessorHolder() {
    }

    static NmsAccessor getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static NmsAccessor newAccessorInstance() {
        int dataVersion = Bukkit.getUnsafe().getDataVersion();
        requireSupportedDataVersion(dataVersion);
        if (dataVersion > MINIMUM_DATA_VERSION) {
            Bukkit.getLogger().warning("[riftFG] Server data version " + dataVersion
                    + " is newer than the Minecraft 26.3 adapter baseline " + MINIMUM_DATA_VERSION
                    + "; compatibility is not verified. Report any NMS errors with the exact server build.");
        }
        try {
            return (NmsAccessor) Class.forName(ACCESSOR_CLASS).getConstructor().newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not initialize the riftFG Minecraft 26.3 adapter", exception);
        }
    }

    static void requireSupportedDataVersion(int dataVersion) {
        if (dataVersion < MINIMUM_DATA_VERSION) {
            throw new IllegalStateException("riftFG requires Paper for Minecraft 26.3 or newer and Java 25. "
                    + "Unsupported server data version: " + dataVersion);
        }
    }

    private static final class InstanceHolder {
        private static final NmsAccessor INSTANCE = newAccessorInstance();
    }

}
