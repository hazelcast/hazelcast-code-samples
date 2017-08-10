import com.hazelcast.cluster.ClusterState;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class ClusterNoMigrationState {

    public static void main(String[] args) {
        System.setProperty("hazelcast.phone.home.enabled", "false");

        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance();
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance();

        IMap<Object, Object> map = instance2.getMap("test-map");
        // initialize partition assignments
        map.size();

        System.out.println("Instance-1 Cluster State: " + instance1.getCluster().getClusterState());
        System.out.println("Instance-2 Cluster State: " + instance2.getCluster().getClusterState());

        instance2.getCluster().changeClusterState(ClusterState.NO_MIGRATION);
        System.out.println("Instance-1 Cluster State: " + instance1.getCluster().getClusterState());
        System.out.println("Instance-2 Cluster State: " + instance2.getCluster().getClusterState());

        // start a new instance
        HazelcastInstance instance3 = Hazelcast.newHazelcastInstance();

        System.out.println("Instance-3 Members: " + instance3.getCluster().getMembers());
        System.out.println("Instance-3 Cluster State: " + instance3.getCluster().getClusterState());

        Hazelcast.shutdownAll();
    }
}
