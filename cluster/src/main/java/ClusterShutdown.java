import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class ClusterShutdown {

    public static void main(String[] args) {
        final HazelcastInstance instance1 = Hazelcast.newHazelcastInstance();
        final HazelcastInstance instance2 = Hazelcast.newHazelcastInstance();

        System.out.println("Instance-1 members: " + instance1.getCluster().getMembers());
        System.out.println("Instance-2 members: " + instance2.getCluster().getMembers());

        // shutdown cluster
        instance2.getCluster().shutdown();

        System.out.println("Instance-1: Is running?: " + instance1.getLifecycleService().isRunning());
        System.out.println("Instance-2: Is running?: " + instance2.getLifecycleService().isRunning());
    }
}
