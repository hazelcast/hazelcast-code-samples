import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IQueue;
import com.hazelcast.quorum.QuorumException;

public class ClusterQuorum {

    public static void main(String[] args) throws Exception {
        final HazelcastInstance instance1 = Hazelcast.newHazelcastInstance();
        final HazelcastInstance instance2 = Hazelcast.newHazelcastInstance();
        final IQueue<String> queue = instance1.getQueue("queueWithQuorum");
        final ILock lock = instance1.getLock("lockWithQuorum");

        // Quorum will succeed
        System.out.println("Quorum is satisfied, so the following methods will not throw an exception");
        queue.add("we have the quorum");

        final String value = queue.poll();
        System.out.println("Fetched '" + value + "' from the queue");

        System.out.println("Lock operates as expected");
        lock.lock();
        System.out.println("The lock is locked : " + lock.isLocked());
        lock.unlock();

        // Quorum will fail
        System.out.println("Shutdown one instance, so there won't be enough members for quorum presence");
        instance2.getLifecycleService().shutdown();
        // wait for a moment to detect that cluster fell apart
        Thread.sleep(1000);

        System.out.println("The following queue and lock operations will fail");
        try {
            queue.add("will not succeed");
        } catch (QuorumException expected) {
            System.out.println("Queue operation failed with expected QuorumException: " + expected.getMessage());
        }

        try {
            lock.lock();
        } catch (QuorumException expected) {
            System.out.println("Lock operation failed with expected QuorumException: " + expected.getMessage());
        }

        Hazelcast.shutdownAll();
    }
}
