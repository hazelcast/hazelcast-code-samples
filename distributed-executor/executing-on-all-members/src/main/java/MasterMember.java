import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

public class MasterMember {

    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        Map<String, Integer> map = hz.getMap("map");
        for (int i = 0; i < 5; i++) {
            map.put(UUID.randomUUID().toString(), 1);
        }
        IExecutorService executor = hz.getExecutorService("executor");

        Map<Member, Future<Integer>> result = executor.submitToAllMembers(new SumTask());
        int sum = 0;
        for (Future<Integer> future : result.values()) {
            sum += future.get();
        }

        System.out.println("Result: " + sum);
    }
}
