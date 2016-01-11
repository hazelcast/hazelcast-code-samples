import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

public class Member {

    public static void main(String[] args) {
        EchoService echoService = new EchoService();

        Config config = new Config();
        config.getUserContext().put("echoService", echoService);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);

        IExecutorService executor = hz.getExecutorService("echoExecutor");
        executor.execute(new EchoTask("hello"));

        Hazelcast.shutdownAll();
    }
}
