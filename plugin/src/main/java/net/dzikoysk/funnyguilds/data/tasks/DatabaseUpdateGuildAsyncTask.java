package net.dzikoysk.funnyguilds.data.tasks;

import net.dzikoysk.funnyguilds.FunnyGuilds;
import net.dzikoysk.funnyguilds.data.DataModel;
import net.dzikoysk.funnyguilds.data.database.SQLDataModel;
import net.dzikoysk.funnyguilds.data.database.serializer.DatabaseGuildSerializer;
import net.dzikoysk.funnyguilds.data.database.serializer.DatabaseRegionSerializer;
import net.dzikoysk.funnyguilds.data.database.serializer.DatabaseUserSerializer;
import net.dzikoysk.funnyguilds.data.flat.FlatDataModel;
import net.dzikoysk.funnyguilds.data.flat.seralizer.FlatGuildSerializer;
import net.dzikoysk.funnyguilds.data.flat.seralizer.FlatRegionSerializer;
import net.dzikoysk.funnyguilds.data.flat.seralizer.FlatUserSerializer;
import net.dzikoysk.funnyguilds.guild.Guild;
import net.dzikoysk.funnyguilds.shared.FunnyTask.SyncFunnyTask;

public class DatabaseUpdateGuildAsyncTask extends SyncFunnyTask {

    private final DataModel dataModel;
    private final Guild guild;

    public DatabaseUpdateGuildAsyncTask(DataModel dataModel, Guild guild) {
        this.dataModel = dataModel;
        this.guild = guild;
    }

    @Override
    public void execute() {
        FunnyGuilds.getInstance().getDataPersistenceHandler().requestSave();
    }

}
