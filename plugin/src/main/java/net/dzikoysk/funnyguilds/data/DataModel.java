package net.dzikoysk.funnyguilds.data;

import java.sql.SQLException;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import net.dzikoysk.funnyguilds.data.database.serializer.DatabaseUserSerializer;
import net.dzikoysk.funnyguilds.data.database.serializer.DatabaseGuildSerializer;
import net.dzikoysk.funnyguilds.data.database.serializer.DatabaseRegionSerializer;
import net.dzikoysk.funnyguilds.data.flat.seralizer.FlatUserSerializer;
import net.dzikoysk.funnyguilds.data.flat.seralizer.FlatGuildSerializer;
import net.dzikoysk.funnyguilds.data.flat.seralizer.FlatRegionSerializer;
import net.dzikoysk.funnyguilds.Entity;
import net.dzikoysk.funnyguilds.FunnyGuilds;
import net.dzikoysk.funnyguilds.config.PluginConfiguration;
import net.dzikoysk.funnyguilds.data.database.SQLDataModel;
import net.dzikoysk.funnyguilds.data.flat.FlatDataModel;
import net.dzikoysk.funnyguilds.guild.Guild;
import net.dzikoysk.funnyguilds.guild.GuildManager;
import net.dzikoysk.funnyguilds.guild.Region;
import net.dzikoysk.funnyguilds.guild.RegionManager;

public interface DataModel {

    void load() throws SQLException;

    void save(boolean ignoreNotChanged);

    /** Capture on the primary thread; returned work does not read live entities. */
    default Runnable prepareSave(boolean ignoreNotChanged) {
        if (!org.bukkit.Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Persistence snapshots must be captured on the primary thread");
        }
        FunnyGuilds plugin = FunnyGuilds.getInstance();
        List<Runnable> writes = new ArrayList<>();
        boolean sql = this instanceof SQLDataModel;
        plugin.getUserManager().getUsers().stream()
                .filter(user -> !ignoreNotChanged || user.wasChanged())
                .forEach(user -> writes.add(sql ? DatabaseUserSerializer.prepare(user) : FlatUserSerializer.prepare(user)));
        plugin.getGuildManager().getGuilds().stream()
                .filter(guild -> !ignoreNotChanged || guild.wasChanged())
                .forEach(guild -> writes.add(sql ? DatabaseGuildSerializer.prepare(guild) : FlatGuildSerializer.prepare(guild)));
        if (plugin.getPluginConfiguration().regionsEnabled) {
            plugin.getRegionManager().getRegions().stream()
                    .filter(region -> !ignoreNotChanged || region.wasChanged())
                    .forEach(region -> writes.add(sql ? DatabaseRegionSerializer.prepare(region) : FlatRegionSerializer.prepare(region)));
        }
        return () -> {
            if (sql) {
                // The enclosing transaction rolls everything back on the first failed statement.
                writes.forEach(Runnable::run);
                return;
            }
            IllegalStateException failure = null;
            for (Runnable write : writes) {
                try {
                    write.run();
                }
                catch (RuntimeException exception) {
                    if (failure == null) {
                        failure = new IllegalStateException("Some records were not saved; they remain dirty for retry");
                    }
                    if (failure.getSuppressed().length < 20) {
                        failure.addSuppressed(exception);
                    }
                }
            }
            if (failure != null) {
                throw failure;
            }
        };
    }

    default void validateLoadedData() {
        FunnyGuilds plugin = FunnyGuilds.getInstance();
        GuildManager guildManager = plugin.getGuildManager();
        RegionManager regionManager = plugin.getRegionManager();

        if (!plugin.getPluginConfiguration().regionsEnabled) {
            return;
        }

        Set<String> guilds = Entity.names(guildManager.getGuilds());
        Set<String> regions = Entity.names(regionManager.getRegions());

        List<String> conflicts = new ArrayList<>();

        for (Guild guild : guildManager.getGuilds()) {
            if (regions.contains(guild.getName())) {
                guilds.remove(guild.getName());
                continue;
            }

            conflicts.add("Guild without region: " + guild.getName());
        }

        guilds = Entity.names(guildManager.getGuilds());
        regions = Entity.names(regionManager.getRegions());

        for (Region region : regionManager.getRegions()) {
            if (guilds.contains(region.getName())) {
                regions.remove(region.getName());
                continue;
            }

            conflicts.add("Region without guild: " + region.getName());
        }

        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Data conflicts; no records deleted: " + String.join(", ", conflicts));
        }
    }

    static DataModel create(FunnyGuilds plugin, PluginConfiguration.DataModel modelType) {
        if (modelType.isSQL()) {
            return new SQLDataModel(plugin);
        }

        return new FlatDataModel(plugin);
    }

}
