import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicLong;
import com.hazelcast.cp.lock.ICondition;
import com.hazelcast.cp.lock.ILock;

public class NotifyMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IAtomicLong counter = hz.getAtomicLong("counter");
        ILock lock = hz.getLock("lock");
        ICondition isOneCondition = lock.newCondition("isOne");
        lock.lock();
        try {
            counter.set(1);
            isOneCondition.signalAll();
        } finally {
            lock.unlock();
        }
        System.out.println("Value changed");
    }
}
