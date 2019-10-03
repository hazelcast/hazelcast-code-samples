import com.hazelcast.partition.MigrationListener;
import com.hazelcast.partition.MigrationState;
import com.hazelcast.partition.ReplicaMigrationEvent;

import java.util.concurrent.CountDownLatch;

public class MonitoringMigrationListener implements MigrationListener {

    private final CountDownLatch completionLatch = new CountDownLatch(1);

    @Override
    public void migrationStarted(MigrationState state) {
        System.out.println("Migration Started: " + state);
    }

    @Override
    public void migrationFinished(MigrationState state) {
        System.out.println("Migration Finished: " + state);
        completionLatch.countDown();
    }

    @Override
    public void replicaMigrationCompleted(ReplicaMigrationEvent event) {
        System.out.println("Replica Migration Completed: " + event);
    }

    @Override
    public void replicaMigrationFailed(ReplicaMigrationEvent event) {
        System.out.println("Replica Migration Failed: " + event);
    }

    public void awaitUtilCompletion() throws InterruptedException {
        completionLatch.await();
    }
}
