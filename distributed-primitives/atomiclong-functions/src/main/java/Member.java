import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IFunction;

public class Member {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IAtomicLong atomicLong = hz.getAtomicLong("counter");

        atomicLong.set(1);
        long result = atomicLong.apply(new Add2Function());
        System.out.println("apply.result:" + result);
        System.out.println("apply.value:" + atomicLong.get());

        atomicLong.set(1);
        atomicLong.alter(new Add2Function());
        System.out.println("alter.value:" + atomicLong.get());

        atomicLong.set(1);
        result = atomicLong.alterAndGet(new Add2Function());
        System.out.println("alterAndGet.result:" + result);
        System.out.println("alterAndGet.value:" + atomicLong.get());

        atomicLong.set(1);
        result = atomicLong.getAndAlter(new Add2Function());
        System.out.println("getAndAlter.result:" + result);
        System.out.println("getAndAlter.value:" + atomicLong.get());

        System.exit(0);

        for (; ; ) {
            long oldValue = atomicLong.get();
            long newValue = oldValue + 2;
            if (atomicLong.compareAndSet(oldValue, newValue)) {
                break;
            }
        }
    }

    private static class Add2Function implements IFunction<Long, Long> {
        @Override
        public Long apply(Long input) {
            return input + 2;
        }
    }
}
