import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Offloadable;
import com.hazelcast.core.ReadOnly;
import com.hazelcast.map.AbstractEntryProcessor;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * This example presents how to use the Offloadable and ReadOnly interfaces together with EntryProcessor.
 *
 * The optimization applies to the following methods only:
 * - executeOnKey(Object, EntryProcessor)
 * - submitToKey(Object, EntryProcessor)
 * - submitToKey(Object, EntryProcessor, ExecutionCallback)
 *
 * @see IMap#executeOnKey(Object, EntryProcessor)
 * @see IMap#submitToKey(Object, EntryProcessor)
 * @see IMap#submitToKey(Object, EntryProcessor, com.hazelcast.core.ExecutionCallback)
 */
public class OffloadableEntryProcessorDemo {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<String, Employee> employees = hz.getMap("employees");
        employees.put("John", new Employee(1000));
        employees.put("Mark", new Employee(1000));
        employees.put("Spencer", new Employee(1000));

        // EntryProcessor.process() will run on offloaded thread - a lock will be acquired for the given key
        String offloadedThread = (String) employees.executeOnKey("John", new OffloadableEntryProcessor());
        assertTrue(offloadedThread.contains("cached.thread"));

        // EntryProcessor.process() will run on offloaded thread - a lock won't be acquired for the given key, since
        // the EntryProcessor is ReadOnly
        offloadedThread = (String) employees.executeOnKey("John", new OffloadableReadOnlyEntryProcessor());
        assertTrue(offloadedThread.contains("cached.thread"));

        // EntryProcessor.process() will run on partition thread
        String partitionThread = (String) employees.executeOnKey("John", new ReadOnlyEntryProcessor());
        assertTrue(partitionThread.contains("partition-operation.thread"));

        // EntryProcessor.process() will run on partition thread
        partitionThread = (String) employees.executeOnKey("John", new OrdinaryEntryProcessor());
        assertTrue(partitionThread.contains("partition-operation.thread"));

        Hazelcast.shutdownAll();
    }

    /**
     * By default the EntryProcessor executes on a partition-thread. There is exactly one thread responsible for handling
     * each partition and each of such threads may handle more than one partition.
     *
     * The present design of EntryProcessor assumes users have fast user code execution of the process() method.
     * In the pathological case where they code that is very heavy and executes in multi-milliseconds, this may create a
     * bottleneck.
     *
     * In order to mitigate the above-mentioned problem an Offloadable interface has been introduced.
     * If an EntryProcessor implements the Offloadable interface the process() method will be executed in the executor
     * specified by the getExecutorName() method.
     *
     * Offloading will unblock the partition-thread allowing the user to profit from much higher throughput.
     * The key will be locked for the time-span of the processing in order to not generate a write-conflict.
     * In this case the threading looks as follows:
     * 1.) partition-thread (fetch & lock)
     * 2.) execution-thread (process)
     * 3.) partition-thread (set & unlock, or just unlock if no changes)
     *
     * The optimization applies to the following methods only:
     * - executeOnKey(Object, EntryProcessor)
     * - submitToKey(Object, EntryProcessor)
     * - submitToKey(Object, EntryProcessor, ExecutionCallback)
     *
     * @see IMap#executeOnKey(Object, EntryProcessor)
     * @see IMap#submitToKey(Object, EntryProcessor)
     * @see IMap#submitToKey(Object, EntryProcessor, com.hazelcast.core.ExecutionCallback)
     */
    private static class OffloadableEntryProcessor extends AbstractEntryProcessor<String, Employee>
            implements Offloadable {
        @Override
        public Object process(Map.Entry<String, Employee> entry) {
            Employee value = entry.getValue();
            value.incSalary(10);
            entry.setValue(value);

            // returns the name of thread it runs on
            return Thread.currentThread().getName();
        }

        @Override
        public String getExecutorName() {
            return OFFLOADABLE_EXECUTOR;
        }
    }

    /**
     * The same optimization applies to an EntryProcessor that implements Offloadable and ReadOnly interfaces.
     * By implementing ReadOnly the developer "promises" to the execute method that they will not modify the entry
     * within the EntryProcessor. It allows Hazelcast to optimize the execution path in a way that the
     * execution of the EntryProcessor will not wait until the lock on a specific key has been released.
     *
     * If the EntryProcessor implements the Offloadable and ReadOnly interfaces the processing will be offloaded to the
     * given ExecutorService allowing unblocking the partition-thread. Since the EntryProcessor is not supposed to do
     * any changes to the Entry the key will NOT be locked for the time-span of the processing. In this case the threading
     * looks as follows:
     * 1.) partition-thread (fetch & lock)
     * 2.) execution-thread (process)
     * In this case the EntryProcessor.getBackupProcessor() has to return null; otherwise an IllegalArgumentException
     * exception is thrown.
     *
     * The optimization applies to the following methods only:
     * - executeOnKey(Object, EntryProcessor)
     * - submitToKey(Object, EntryProcessor)
     * - submitToKey(Object, EntryProcessor, ExecutionCallback)
     *
     * @see IMap#executeOnKey(Object, EntryProcessor)
     * @see IMap#submitToKey(Object, EntryProcessor)
     * @see IMap#submitToKey(Object, EntryProcessor, com.hazelcast.core.ExecutionCallback)
     */
    private static class OffloadableReadOnlyEntryProcessor implements EntryProcessor<String, Employee>,
            Offloadable, ReadOnly {
        @Override
        public Object process(Map.Entry<String, Employee> entry) {
            Employee value = entry.getValue();
            value.incSalary(10);

            // returns the name of thread it runs on
            return Thread.currentThread().getName();
        }

        @Override
        public EntryBackupProcessor<String, Employee> getBackupProcessor() {
            // ReadOnly EntryProcessor has to return null, since it's just a read-only operation that will not be
            // executed on the backup
            return null;
        }

        @Override
        public String getExecutorName() {
            return OFFLOADABLE_EXECUTOR;
        }
    }

    /**
     * If the EntryProcessor implements only ReadOnly without implementing Offloadable the processing unit will not
     * be offloaded, however, the EntryProcessor will not wait for the lock to be acquired, since the EP will not
     * do any modifications.
     *
     * If the EntryProcessor implements ReadOnly and modifies the entry an UnsupportedOperationException
     * will be thrown.
     *
     * The optimization applies to the following methods only:
     * - executeOnKey(Object, EntryProcessor)
     * - submitToKey(Object, EntryProcessor)
     * - submitToKey(Object, EntryProcessor, ExecutionCallback)
     *
     * @see IMap#executeOnKey(Object, EntryProcessor)
     * @see IMap#submitToKey(Object, EntryProcessor)
     * @see IMap#submitToKey(Object, EntryProcessor, com.hazelcast.core.ExecutionCallback)
     */
    private static class ReadOnlyEntryProcessor implements EntryProcessor<String, Employee>,
            ReadOnly {
        @Override
        public Object process(Map.Entry<String, Employee> entry) {
            Employee value = entry.getValue();
            value.incSalary(10);

            // returns the name of thread it runs on
            return Thread.currentThread().getName();
        }

        @Override
        public EntryBackupProcessor<String, Employee> getBackupProcessor() {
            // ReadOnly EntryProcessor has to return null, since it's just a read-only operation that will not be
            // executed on the backup
            return null;
        }
    }

    /**
     * Ordinary EntryProcessor that will run on a partition-thread
     */
    private static class OrdinaryEntryProcessor extends AbstractEntryProcessor<String, Employee> {
        @Override
        public Object process(Map.Entry<String, Employee> entry) {
            Employee value = entry.getValue();
            value.incSalary(10);
            entry.setValue(value);

            // returns the name of thread it runs on
            return Thread.currentThread().getName();
        }
    }
}
