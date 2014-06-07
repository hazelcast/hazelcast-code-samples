import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

public class ClientMember {

    public static void main(String[] args) throws Exception {
        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        Counter counter = client.getDistributedObject(CounterService.NAME, "counter" + 0);
        System.out.println(counter.inc(10));
    }
}
