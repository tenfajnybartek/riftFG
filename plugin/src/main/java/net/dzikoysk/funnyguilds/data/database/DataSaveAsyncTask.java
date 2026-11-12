package net.dzikoysk.funnyguilds.data.database;

import net.dzikoysk.funnyguilds.data.DataModel;
import net.dzikoysk.funnyguilds.shared.FunnyTask.SyncFunnyTask;

public class DataSaveAsyncTask extends SyncFunnyTask {

    private final DataModel dataModel;
    private final boolean fullSave;

    public DataSaveAsyncTask(DataModel dataModel, boolean fullSave) {
        this.dataModel = dataModel;
        this.fullSave = fullSave;
    }

    @Override
    public void execute() {
        net.dzikoysk.funnyguilds.FunnyGuilds.getInstance().getDataPersistenceHandler().save(this.fullSave);
    }

}
