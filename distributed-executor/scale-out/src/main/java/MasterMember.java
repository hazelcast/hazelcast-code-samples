import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

import static com.hazelcast.examples.helper.CommonUtils.sleepSeconds;

public class MasterMember {

    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();

        IExecutorService executor = hz.getExecutorService("executor");
        for (int i = 1; i <= 1000; i++) {
            sleepSeconds(1);
            System.out.println("Producing echo task: " + i);
            executor.execute(new EchoTask("" + i));
        }

        System.out.println("MasterMember finished!");
    }
}
