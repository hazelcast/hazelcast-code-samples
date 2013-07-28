import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

public class ClientMember {

    public static void main(String[] args) throws Exception {
        ClientConfig clientConfig = new ClientConfig().addAddress("127.0.0.1");
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        Counter counter = client.getDistributedObject(CounterService.NAME, "counter" +0);
        System.out.println(counter.inc(10));
    }
}
