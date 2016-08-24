import com.hazelcast.cluster.ClusterState;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class ClusterFrozenState {

    public static void main(String[] args) {
        System.setProperty("hazelcast.phone.home.enabled", "false");

        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance();
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance();

        System.out.println("Instance-1 Cluster State: " + instance1.getCluster().getClusterState());
        System.out.println("Instance-2 Cluster State: " + instance2.getCluster().getClusterState());

        instance2.getCluster().changeClusterState(ClusterState.FROZEN);
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

        // a new instance cannot join to a frozen cluster
        try {
            Hazelcast.newHazelcastInstance();
        } catch (IllegalStateException expected) {
            System.err.println("New node cannot join to the cluster: " + expected);
        }

        Hazelcast.shutdownAll();
    }
}
