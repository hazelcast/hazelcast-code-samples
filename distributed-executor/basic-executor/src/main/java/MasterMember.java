import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

public class MasterMember {

    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IExecutorService executor = hz.getExecutorService("executor");

        for (int i = 1; i <= 1; i++) {
            Thread.sleep(1000);
            System.out.println("Producing echo task: " + i);
            executor.execute(new EchoTask("" + i));
        }
        System.out.println("MasterMember finished!");

        executor.execute(new EchoTask("foo"));
        executor.shutdown();
    }
}
