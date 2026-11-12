package net.dzikoysk.funnyguilds;

import net.dzikoysk.funnyguilds.config.PluginConfiguration;
import net.dzikoysk.funnyguilds.shared.FunnyIOUtils;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class FunnyGuildsVersionTest {

    private static final String COMMITS_URL = "https://api.github.com/repos/tenfajnybartek/riftFG/commits?sha=5.x&per_page=1";
    private static final String RELEASE_URL = "https://raw.githubusercontent.com/tenfajnybartek/riftFG/5.x/updater.txt";
    private static final String NIGHTLY_URL = "https://raw.githubusercontent.com/tenfajnybartek/riftFG/5.x/updater-nightly.txt";

    private MockedStatic<FunnyIOUtils> http;
    private MockedStatic<FunnyGuilds> pluginStatics;
    private PluginConfiguration config;
    private FunnyGuildsVersion version;
    private CommandSender sender;

    @BeforeEach
    void prepareUpdater() {
        this.http = mockStatic(FunnyIOUtils.class);
        this.pluginStatics = mockStatic(FunnyGuilds.class);
        FunnyGuilds plugin = mock(FunnyGuilds.class);
        this.config = mock(PluginConfiguration.class);
        this.config.updateInfo = true;
        this.config.updateNightlyInfo = true;
        when(plugin.getPluginConfiguration()).thenReturn(this.config);
        when(plugin.getDescription()).thenReturn(new PluginDescriptionFile(
                "FunnyGuilds", "5.0.0-SNAPSHOT Primrose-abcdef0", "net.dzikoysk.funnyguilds.FunnyGuilds"));
        Server server = mock(Server.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(1).run();
            return null;
        }).when(scheduler).runTaskAsynchronously(any(Plugin.class), any(Runnable.class));
        this.sender = mock(CommandSender.class);
        when(this.sender.hasPermission("funnyguilds.admin")).thenReturn(true);
        this.version = new FunnyGuildsVersion(plugin);
    }

    @AfterEach
    void closeMocks() {
        this.pluginStatics.close();
        this.http.close();
    }

    @Test
    void nightlyChecksTheForkAndDoesNotAnnounceTheInstalledCommit() {
        this.http.when(() -> FunnyIOUtils.getContent(COMMITS_URL)).thenReturn("[{\"sha\":\"abcdef0123456789\"}]");
        this.http.when(() -> FunnyIOUtils.getContent(NIGHTLY_URL)).thenReturn("5.0.0-SNAPSHOT Primrose\n");
        this.version.isNewAvailable(this.sender, false);
        this.http.verify(() -> FunnyIOUtils.getContent(COMMITS_URL));
        this.http.verify(() -> FunnyIOUtils.getContent(NIGHTLY_URL));
        this.pluginStatics.verifyNoInteractions();
    }

    @Test
    void releaseFeedWhitespaceDoesNotCauseAFalseUpdateNotice() {
        this.config.updateNightlyInfo = false;
        this.http.when(() -> FunnyIOUtils.getContent(RELEASE_URL)).thenReturn(" 5.0.0-SNAPSHOT Primrose\r\n");
        this.version.isNewAvailable(this.sender, false);
        this.http.verify(() -> FunnyIOUtils.getContent(RELEASE_URL));
        this.pluginStatics.verifyNoInteractions();
    }

    @Test
    void missingNightlyVersionDoesNotAnnounceAnIncompleteVersion() {
        this.http.when(() -> FunnyIOUtils.getContent(COMMITS_URL)).thenReturn("[{\"sha\":\"123456789abcdef0\"}]");
        this.http.when(() -> FunnyIOUtils.getContent(NIGHTLY_URL)).thenReturn("\n");
        this.version.isNewAvailable(this.sender, false);
        this.pluginStatics.verifyNoInteractions();
    }
}
