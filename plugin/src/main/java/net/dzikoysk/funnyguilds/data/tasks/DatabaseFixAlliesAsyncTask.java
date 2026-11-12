package net.dzikoysk.funnyguilds.data.tasks;

import net.dzikoysk.funnyguilds.guild.GuildManager;
import net.dzikoysk.funnyguilds.shared.FunnyTask.SyncFunnyTask;

public class DatabaseFixAlliesAsyncTask extends SyncFunnyTask {

    private final GuildManager guildManager;

    public DatabaseFixAlliesAsyncTask(GuildManager guildManager) {
        this.guildManager = guildManager;
    }

    @Override
    public void execute() {
        guildManager.getGuilds().forEach(guild -> {
            guild.getAllies().forEach(ally -> ally.addAlly(guild));
        });
    }

}
