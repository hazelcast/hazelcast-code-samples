import com.hazelcast.collection.IQueue;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.splitbrainprotection.SplitBrainProtectionException;

/**
 * The term "split brain protection" simply refers to the count of members in the cluster required for an operation to succeed.
 * It does NOT refer to an implementation of Paxos or Raft protocols as used in many NoSQL and distributed systems.
 * The mechanism it provides in Hazelcast protects the user in case the number of nodes in a cluster drops below the
 * specified one.
 *
 * Hazelcast Split brain protection is supported in the following data-structures:
 * - IMap
 * - TransactionalMap
 * - ICache
 * - IQueue
 * - TransactionalQueue
 * - ISet
 * - TransactionalSet
 * - IList
 * - TransactionalList
 * - ReplicatedMap
 * - MultiMap
 * - IExecutorService
 * - DurableExecutorService
 * - IScheduledExecutorService
 * - Ringbuffer
 * - CardinalityEstimator
 */
public class ClusterSplitBrainProtection {

    public static void main(String[] args) throws Exception {
        final HazelcastInstance instance1 = Hazelcast.newHazelcastInstance();
        final HazelcastInstance instance2 = Hazelcast.newHazelcastInstance();
        final IQueue<String> queue = instance1.getQueue("queueWithSplitBrainProtection");

        // Split brain protection will succeed
        System.out.println("Split brain protection is satisfied, so the following methods will not throw an exception");
        queue.add("we have the split brain protection");

        final String value = queue.poll();
        System.out.println("Fetched '" + value + "' from the queue");

        // Split brain protection will fail
        System.out.println("Shutdown one instance, so there won't be enough members for split brain protection presence");
        instance2.getLifecycleService().shutdown();
        // wait for a moment to detect that cluster fell apart
        Thread.sleep(1000);

        System.out.println("The following queue and lock operations will fail");
        try {
            queue.add("will not succeed");
        } catch (SplitBrainProtectionException expected) {
            System.out.println("Queue operation failed with expected SplitBrainProtectionException: " + expected.getMessage());
        }

        Hazelcast.shutdownAll();
    }
}
