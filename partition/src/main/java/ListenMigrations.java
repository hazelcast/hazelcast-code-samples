import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class ListenMigrations {

    public static void main(String[] args) throws Exception {
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance();
        MonitoringMigrationListener listener = new MonitoringMigrationListener();
        instance1.getPartitionService().addMigrationListener(listener);

        IMap<Object, Object> map = instance1.getMap("test-map");
        // initialize partition assignments
        map.size();

        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance();

        listener.awaitUtilCompletion();

        Hazelcast.shutdownAll();
    }
}
