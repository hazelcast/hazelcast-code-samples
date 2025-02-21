import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class Client {

    private Client() {
    }

    public static void main(String[] args) throws InterruptedException {
        HazelcastInstance hazelcastClient = HazelcastClient.newHazelcastClient();
        IMap<Integer, String> test = hazelcastClient.getMap("test");
        for (int i=0; i < 1000000; i++) {
            test.put(i, "item" + i);
            System.out.println(test.get(i));
            Thread.sleep(1000);
        }
        hazelcastClient.shutdown();
    }

}
