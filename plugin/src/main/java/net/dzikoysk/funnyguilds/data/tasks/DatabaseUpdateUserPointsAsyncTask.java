package net.dzikoysk.funnyguilds.data.tasks;

import net.dzikoysk.funnyguilds.data.database.serializer.DatabaseUserSerializer;
import net.dzikoysk.funnyguilds.shared.FunnyTask.SyncFunnyTask;
import net.dzikoysk.funnyguilds.FunnyGuilds;
import net.dzikoysk.funnyguilds.user.User;

public class DatabaseUpdateUserPointsAsyncTask extends SyncFunnyTask {

    private final User user;

    public DatabaseUpdateUserPointsAsyncTask(User user) {
        this.user = user;
    }

    @Override
    public void execute() {
        FunnyGuilds.getInstance().getDataPersistenceHandler().requestSave();
    }

}
