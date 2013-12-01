import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;

import java.lang.System;

public class Member {
    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IAtomicLong atomicLong = hz.getAtomicLong("counter");


        System.out.printf("Count is %s\n", atomicLong.get());
        System.exit(0);
    }

    private static class DoubleFunction implements Function<Long,Long>{

    }
}