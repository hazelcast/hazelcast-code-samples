import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.map.IMap;
import com.hazelcast.partition.MigrationListener;
import com.hazelcast.partition.MigrationState;
import com.hazelcast.partition.ReplicaMigrationEvent;

public class BouncingSingleMaxIdleSimulator extends SingleMaxIdleSimulator {
    private static final boolean USE_NORMAL_GETS_DURING_MIGRATION = true;
    private static volatile boolean useNormalGetsA;
    private static volatile boolean useNormalGetsB;

    private static volatile boolean migratingA;
    private static volatile boolean migratingB;

    public static void main(String[] args) {
        AbstractMaxIdleSimulator simulator = new BouncingSingleMaxIdleSimulator();
        simulator.simulate();
    }

    private static class MigrationListenerImplA implements MigrationListener {

        @Override
        public void migrationStarted(MigrationState migrationState) {
            if (USE_NORMAL_GETS_DURING_MIGRATION) {
                useNormalGetsA = true;
            }
            migratingA = true;
            if (loggerA != null) {
                loggerA.info(">> Migration started " + migrationState);
            }
        }

        @Override
        public void migrationFinished(MigrationState migrationState) {
            if (USE_NORMAL_GETS_DURING_MIGRATION) {
                useNormalGetsA = false;
            }
            migratingA = false;
            if (loggerA != null) {
                loggerA.info(">> Migration finished " + migrationState);
            }
        }

        @Override
        public void replicaMigrationCompleted(ReplicaMigrationEvent replicaMigrationEvent) {

        }

        @Override
        public void replicaMigrationFailed(ReplicaMigrationEvent replicaMigrationEvent) {

        }
    }

    private static class MigrationListenerImplB implements MigrationListener {

        @Override
        public void migrationStarted(MigrationState migrationState) {
            if (USE_NORMAL_GETS_DURING_MIGRATION) {
                useNormalGetsB = true;
            }
            migratingB = true;
            if (loggerB != null) {
                loggerB.info(">> Migration started " + migrationState);
            }
        }

        @Override
        public void migrationFinished(MigrationState migrationState) {
            if (USE_NORMAL_GETS_DURING_MIGRATION) {
                useNormalGetsB = false;
            }
            migratingB = false;
            if (loggerB != null) {
                loggerB.info(">> Migration finished " + migrationState);
            }
        }

        @Override
        public void replicaMigrationCompleted(ReplicaMigrationEvent replicaMigrationEvent) {

        }

        @Override
        public void replicaMigrationFailed(ReplicaMigrationEvent replicaMigrationEvent) {

        }
    }

    @Override
    void modifyConfig(Config config) {
        super.modifyConfig(config);
        if (config.getClusterName().equals("A")) {
            config.addListenerConfig(new ListenerConfig(MigrationListenerImplA.class.getName()));
        } else if (config.getClusterName().equals("B")) {
            config.addListenerConfig(new ListenerConfig(MigrationListenerImplB.class.getName()));
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    String simulateGet(IMap<String, String> map, String key, boolean mapInA) {
        if ((mapInA && useNormalGetsA) || (!mapInA && useNormalGetsB)) {
            return map.get(key);
        }

        return super.simulateGet(map, key, mapInA);
    }

    @Override
    boolean throwIfNonExpiredDataIsMissing() {
        return false;
    }

    @Override
    boolean bounceMembers() {
        return true;
    }

    @Override
    boolean migrating() {
        return migratingA || migratingB;
    }
}
