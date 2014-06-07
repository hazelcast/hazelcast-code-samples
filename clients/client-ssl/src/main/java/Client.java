import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import java.io.File;
import java.util.concurrent.BlockingQueue;

public class Client {
    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.keyStore", new File("hazelcast.ks").getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStore", new File("hazelcast.ts").getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", "password");

        ClientConfig config = new ClientConfig();
        config.addAddress("127.0.0.1");
        //config.getSocketOptions().setSocketFactory(new SSLSocketFactory());

        HazelcastInstance client = HazelcastClient.newHazelcastClient(config);
        BlockingQueue<String> queue = client.getQueue("queue");
        queue.put("Hello!");
        System.out.println("Message send by client!");
        System.exit(0);
    }
}
