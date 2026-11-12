package net.dzikoysk.funnyguilds.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.dzikoysk.funnyguilds.FunnyGuilds;
import net.dzikoysk.funnyguilds.data.database.SQLDataModel;
import net.dzikoysk.funnyguilds.data.database.element.SQLBasicUtils;
import net.dzikoysk.funnyguilds.data.database.element.SQLNamedStatement;
import net.dzikoysk.funnyguilds.data.util.YamlWrapper;

/** A durable intent: unfinished deletions are replayed before entities are loaded. */
public final class PendingDeletion {

    private PendingDeletion() {
    }

    public static Runnable prepare(FunnyGuilds plugin, String kind, String key, String name, String region) {
        Map<String, String> entry = new LinkedHashMap<>();
        entry.put("kind", kind);
        entry.put("key", key);
        entry.put("name", name);
        entry.put("region", region == null ? "" : region);
        DataModel model = plugin.getDataModel();
        entry.put("storage", model instanceof SQLDataModel ? "SQL" : "FLAT");
        return () -> {
            File journal = new File(plugin.getPluginDataFolder(), "pending-deletions.yml");
            List<Map<?, ?>> entries = new ArrayList<>(new YamlWrapper(journal).getMapList("deletions"));
            if (!entries.contains(entry)) { entries.add(entry); }
            store(journal, entries);
            delete(plugin, model, entry);
            entries.remove(entry);
            store(journal, entries);
        };
    }

    public static void replay(FunnyGuilds plugin, DataModel model) {
        File journal = new File(plugin.getPluginDataFolder(), "pending-deletions.yml");
        if (!journal.exists()) { return; }
        List<Map<?, ?>> entries = new ArrayList<>(new YamlWrapper(journal).getMapList("deletions"));
        for (Map<?, ?> entry : List.copyOf(entries)) {
            delete(plugin, model, entry);
            entries.remove(entry);
            store(journal, entries);
        }
    }

    private static void store(File file, List<Map<?, ?>> entries) {
        YamlWrapper yaml = new YamlWrapper(file, false);
        yaml.set("deletions", entries);
        yaml.save();
    }

    private static void delete(FunnyGuilds plugin, DataModel model, Map<?, ?> entry) {
        String storage = model instanceof SQLDataModel ? "SQL" : "FLAT";
        if (entry.containsKey("storage") && !storage.equals(entry.get("storage"))) {
            throw new IllegalStateException("Deletion journal belongs to a different storage model; no data was deleted");
        }
        String kind = (String) entry.get("kind");
        String key = (String) entry.get("key");
        String name = (String) entry.get("name");
        String region = (String) entry.get("region");
        if (!"guild".equals(kind) && !"region".equals(kind)) { throw new IllegalStateException("Invalid deletion journal entry"); }
        if (model instanceof SQLDataModel sql) {
            plugin.getDatabase().get().transaction(() -> {
                SQLNamedStatement statement = SQLBasicUtils.getDelete("guild".equals(kind) ? sql.getGuildsTable() : sql.getRegionsTable());
                statement.set("guild".equals(kind) ? "uuid" : "name", key);
                statement.executeUpdate();
                if (region != null && !region.isEmpty()) {
                    SQLNamedStatement regionStatement = SQLBasicUtils.getDelete(sql.getRegionsTable());
                    regionStatement.set("name", region);
                    regionStatement.executeUpdate();
                }
            });
        }
        else {
            deleteFile(plugin, "guild".equals(kind) ? "guilds" : "regions", name);
            if (region != null && !region.isEmpty()) { deleteFile(plugin, "regions", region); }
        }
    }

    private static void deleteFile(FunnyGuilds plugin, String folder, String name) {
        java.nio.file.Path directory = plugin.getPluginDataFolder().toPath().resolve(folder).toAbsolutePath().normalize();
        java.nio.file.Path file = directory.resolve(name + ".yml").normalize();
        if (!file.getParent().equals(directory)) { throw new IllegalStateException("Invalid deletion target"); }
        try { Files.deleteIfExists(file); }
        catch (IOException exception) { throw new IllegalStateException("Cannot delete " + file, exception); }
    }
}
