import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import java.util.concurrent.BlockingQueue;

public class Client {

    public void put(String string) throws Exception {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("127.0.0.1");

        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        System.out.println(clientConfig.toString());

        BlockingQueue<String> queue = client.getQueue("queue");
        queue.put(string);
        System.out.println("Message sent by Hazelcast Client!");

        HazelcastClient.shutdownAll();
    }

    public static void main(String[] args) throws Exception {
        new Client().put("Hello!");
    }
}
