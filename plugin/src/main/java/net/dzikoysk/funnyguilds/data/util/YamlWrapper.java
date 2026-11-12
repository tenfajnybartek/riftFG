package net.dzikoysk.funnyguilds.data.util;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public final class YamlWrapper extends YamlConfiguration {

    private final File file;

    public YamlWrapper(File file) {
        this(file, true);
    }

    public YamlWrapper(File file, boolean loadExisting) {
        this.file = file;
        if (loadExisting && file.exists()) {
            try {
                super.load(file);
            }
            catch (Exception exception) {
                throw new IllegalStateException("Cannot read YAML; existing data was preserved: " + file, exception);
            }
        }
    }

    @Override
    public void save(@NotNull File file) {
        write(file, this.saveToString());
    }

    public void save() {
        this.save(this.file);
    }

    /** Capture YAML on its owning thread; the worker only performs IO. */
    public Runnable snapshot(Runnable onSuccess) {
        String content = this.saveToString();
        return () -> {
            write(this.file, content);
            onSuccess.run();
        };
    }

    private static void write(File file, String content) {
        try {
            AtomicFiles.write(file.toPath(), content);
        }
        catch (IOException exception) {
            throw new UncheckedIOException("Failed to save YAML: " + file, exception);
        }
    }
}
