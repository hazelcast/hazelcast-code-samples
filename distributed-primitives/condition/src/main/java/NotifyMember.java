import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.ICondition;
import com.hazelcast.core.ILock;

public class NotifyMember {

    public static void main(String[] args) throws InterruptedException {
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
