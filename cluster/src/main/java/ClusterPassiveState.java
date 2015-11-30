import com.hazelcast.cluster.ClusterState;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class ClusterPassiveState {

    public static void main(String[] args) {
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance();
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance();

        System.out.println("Instance-1 Cluster State: " + instance1.getCluster().getClusterState());
        System.out.println("Instance-2 Cluster State: " + instance2.getCluster().getClusterState());

        IMap<Object, Object> map = instance2.getMap("test-map");
        // initialize partition assignments before taking cluster to the PASSIVE state
        map.size();

        instance2.getCluster().changeClusterState(ClusterState.PASSIVE);
        System.out.println("Instance-1 Cluster State: " + instance1.getCluster().getClusterState());
        System.out.println("Instance-2 Cluster State: " + instance2.getCluster().getClusterState());

        // shutdown 1st instance and start it back
        // it should re-join to the 2nd node
        instance1.shutdown();
        instance1 = Hazelcast.newHazelcastInstance();

        System.out.println("Instance-1 Members: " + instance1.getCluster().getMembers());
        System.out.println("Instance-2 Members: " + instance2.getCluster().getMembers());
        System.out.println("Instance-1 Cluster State: " + instance1.getCluster().getClusterState());
        System.out.println("Instance-2 Cluster State: " + instance2.getCluster().getClusterState());

        // readonly operations are allowed
        System.out.println("map.get() = " + map.get("key"));
        System.out.println("map.containsKey() = " + map.containsKey("key"));

        // non-readonly operations are NOT allowed
        try {
            map.put("key", "value");
        } catch (IllegalStateException e) {
            System.err.println("Cannot put! Cluster is in PASSIVE state! -> " + e);
        }

        Hazelcast.shutdownAll();
    }
}
