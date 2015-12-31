import com.hazelcast.cluster.memberselector.MemberSelectors;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

import java.util.concurrent.Future;

public class StartDataMember {

    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IExecutorService executor = hz.getExecutorService("executor");

        Future<Integer> future = executor.submit(new ComputationHeavyTask(), MemberSelectors.LITE_MEMBER_SELECTOR);

        System.out.println("Result: " + future.get());
    }
}
