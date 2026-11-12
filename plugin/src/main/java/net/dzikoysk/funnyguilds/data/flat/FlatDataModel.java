package net.dzikoysk.funnyguilds.data.flat;

import java.io.File;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import net.dzikoysk.funnyguilds.Entity.EntityType;
import net.dzikoysk.funnyguilds.FunnyGuilds;
import net.dzikoysk.funnyguilds.config.PluginConfiguration;
import net.dzikoysk.funnyguilds.data.DataModel;
import net.dzikoysk.funnyguilds.data.flat.seralizer.FlatGuildSerializer;
import net.dzikoysk.funnyguilds.data.flat.seralizer.FlatRegionSerializer;
import net.dzikoysk.funnyguilds.data.flat.seralizer.FlatUserSerializer;
import net.dzikoysk.funnyguilds.data.tasks.DatabaseFixAlliesAsyncTask;
import net.dzikoysk.funnyguilds.feature.scoreboard.ScoreboardGlobalUpdateSyncTask;
import net.dzikoysk.funnyguilds.guild.Guild;
import net.dzikoysk.funnyguilds.guild.GuildManager;
import net.dzikoysk.funnyguilds.guild.Region;
import net.dzikoysk.funnyguilds.guild.RegionManager;
import net.dzikoysk.funnyguilds.shared.FunnyIOUtils;
import net.dzikoysk.funnyguilds.user.User;
import net.dzikoysk.funnyguilds.user.UserManager;
import net.dzikoysk.funnyguilds.user.UserUtils;
import panda.std.Option;
import panda.std.Result;
import panda.std.stream.PandaStream;

public class FlatDataModel implements DataModel {

    private final FunnyGuilds plugin;
    private final PluginConfiguration pluginConfiguration;

    private final File usersFolderFile;
    private final File guildsFolderFile;
    private final File regionsFolderFile;

    public FlatDataModel(FunnyGuilds plugin) {
        this.plugin = plugin;
        this.pluginConfiguration = plugin.getPluginConfiguration();

        File dataFolder = plugin.getPluginDataFolder();
        this.usersFolderFile = new File(dataFolder, "users");
        this.guildsFolderFile = new File(dataFolder, "guilds");
        this.regionsFolderFile = new File(dataFolder, "regions");

        FlatPatcher.patch(plugin, this.usersFolderFile, this.guildsFolderFile, this.regionsFolderFile);
    }

    private Option<File> loadCustomFile(EntityType type, String name) {
        File fileFolder;

        switch (type) {
            case USER:
                fileFolder = this.usersFolderFile;
                break;
            case GUILD:
                fileFolder = this.guildsFolderFile;
                break;
            case REGION:
                fileFolder = this.regionsFolderFile;
                break;
            default:
                fileFolder = null;
        }

        if (fileFolder == null) {
            return Option.none();
        }

        java.nio.file.Path directory = fileFolder.toPath().toAbsolutePath().normalize();
        java.nio.file.Path target = directory.resolve(name + ".yml").normalize();
        if (!directory.equals(target.getParent())) {
            throw new IllegalArgumentException("Invalid data file name: " + name);
        }
        return Option.of(target.toFile());
    }

    public Option<File> getUserFile(User user) {
        return this.loadCustomFile(EntityType.USER, user.getUUID().toString());
    }

    public Option<File> getGuildFile(Guild guild) {
        return this.loadCustomFile(EntityType.GUILD, guild.getName());
    }

    public Option<File> getRegionFile(Region region) {
        return this.loadCustomFile(EntityType.REGION, region.getName());
    }

    @Override
    public void load() {
        net.dzikoysk.funnyguilds.data.PendingDeletion.replay(this.plugin, this);
        this.loadUsers();
        this.loadRegions();
        this.loadGuilds();

        this.validateLoadedData();
    }

    @Override
    public void save(boolean ignoreNotChanged) {
        this.prepareSave(ignoreNotChanged).run();
    }

    private void loadUsers() {
        UserManager userManager = this.plugin.getUserManager();
        userManager.clearUsers();

        File[] userFiles = this.usersFolderFile.listFiles();
        if (userFiles == null && this.usersFolderFile.exists()) {
            throw new IllegalStateException("Cannot read data directory: " + this.usersFolderFile);
        }
        if (userFiles == null || userFiles.length == 0) {
            FunnyGuilds.getPluginLogger().info("No users to load");
            return;
        }

        AtomicInteger deserializationErrors = new AtomicInteger();
        PandaStream.of(userFiles)
                .filter(file -> file.isFile() && file.getName().endsWith(".yml"))
                .map(file -> { if (file.length() == 0) { throw new IllegalStateException("Empty user file: " + file); } return file; })
                .map(file -> UserUtils.checkUserFile(this.pluginConfiguration, file).orThrow(() -> new IllegalStateException("Cannot load user file: " + file)))
                .forEach(file -> FlatUserSerializer.deserialize(file)
                        .onEmpty(deserializationErrors::incrementAndGet)
                );

        if (deserializationErrors.get() > 0) {
            throw new IllegalStateException("Users load errors: " + deserializationErrors.get());
        }

        FunnyGuilds.getPluginLogger().info("Loaded users: " + this.plugin.getUserManager().countUsers());
    }

    private void loadGuilds() {
        GuildManager guildManager = this.plugin.getGuildManager();
        guildManager.clearGuilds();

        File[] guildFiles = this.guildsFolderFile.listFiles();
        if (guildFiles == null && this.guildsFolderFile.exists()) {
            throw new IllegalStateException("Cannot read data directory: " + this.guildsFolderFile);
        }
        if (guildFiles == null || guildFiles.length == 0) {
            FunnyGuilds.getPluginLogger().info("No guilds to load");
            return;
        }

        AtomicInteger incorrectGuildsCount = new AtomicInteger();
        AtomicInteger ownerlessGuilds = new AtomicInteger();

        PandaStream.of(guildFiles)
                .filter(file -> file.isFile() && file.getName().endsWith(".yml"))
                .mapOpt(guildFile -> FlatGuildSerializer.deserialize(guildFile)
                        .onEmpty(incorrectGuildsCount::incrementAndGet))
                .filter(guild -> guild.getOwner() == null)
                .forEach(guild -> {
                    FunnyGuilds.getPluginLogger().error("Guild " + guild.getTag() + " has no owner!");
                    ownerlessGuilds.incrementAndGet();
                });

        long errors = incorrectGuildsCount.get() + ownerlessGuilds.get();
        if (errors > 0) {
            throw new IllegalStateException("Guild load errors: " + errors);
        }

        this.plugin.scheduleFunnyTasks(new DatabaseFixAlliesAsyncTask(guildManager));
        this.plugin.getIndividualNameTagManager().map(ScoreboardGlobalUpdateSyncTask::new).peek(this.plugin::scheduleFunnyTasks);

        FunnyGuilds.getPluginLogger().info("Loaded guilds: " + guildManager.countGuilds());
    }

    private void loadRegions() {
        if (!FunnyGuilds.getInstance().getPluginConfiguration().regionsEnabled) {
            FunnyGuilds.getPluginLogger().info("Regions are disabled and thus - not loaded");
            return;
        }

        RegionManager regionManager = this.plugin.getRegionManager();
        regionManager.clearRegions();

        File[] regionFiles = this.regionsFolderFile.listFiles(file -> file.isFile() && file.getName().endsWith(".yml"));
        if (regionFiles == null && this.regionsFolderFile.exists()) {
            throw new IllegalStateException("Cannot read data directory: " + this.regionsFolderFile);
        }
        if (regionFiles == null || regionFiles.length == 0) {
            FunnyGuilds.getPluginLogger().info("No regions to load");
            return;
        }

        AtomicInteger correctlyLoaded = new AtomicInteger();
        PandaStream.of(regionFiles)
                .mapOpt(FlatRegionSerializer::deserialize)
                .forEach(region -> {
                    regionManager.addRegion(region);
                    correctlyLoaded.incrementAndGet();
                });

        long errors = regionFiles.length - correctlyLoaded.get();
        if (errors > 0) {
            throw new IllegalStateException("Region load errors: " + errors);
        }

        FunnyGuilds.getPluginLogger().info("Loaded regions: " + regionManager.countRegions());
    }

}
