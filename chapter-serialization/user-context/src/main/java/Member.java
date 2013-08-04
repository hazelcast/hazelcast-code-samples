import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class Member {
    public static void main(String[] args){
        EchoService echoService = new EchoService();

        Config config = new Config();
        config.getUserContext().put("echoService",echoService);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);

        hz.getExecutorService("echoExecutor").execute(new EchoTask("hello"));
    }
}
