package net.dzikoysk.funnyguilds.data.tasks;

import net.dzikoysk.funnyguilds.data.database.serializer.DatabaseGuildSerializer;
import net.dzikoysk.funnyguilds.guild.Guild;
import net.dzikoysk.funnyguilds.shared.FunnyTask.SyncFunnyTask;
import net.dzikoysk.funnyguilds.FunnyGuilds;

public class DatabaseUpdateGuildPointsAsyncTask extends SyncFunnyTask {

    private final Guild guild;

    public DatabaseUpdateGuildPointsAsyncTask(Guild guild) {
        this.guild = guild;
    }

    @Override
    public void execute() {
        FunnyGuilds plugin = FunnyGuilds.getInstance();
        if (plugin.getGuildManager().findByUuid(this.guild.getUUID()).contentEquals(this.guild)) {
            plugin.getDataPersistenceHandler().requestSave();
        }
    }

}
