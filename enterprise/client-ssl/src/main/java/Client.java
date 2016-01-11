import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.SSLConfig;
import com.hazelcast.core.HazelcastInstance;

import java.io.File;
import java.util.concurrent.BlockingQueue;

public class Client {

    public static void main(String[] args) throws Exception {
        ClientConfig clientConfig = new ClientConfig();

        // please set your enterprise license key to make the sample work
        clientConfig.setLicenseKey("YOUR_ENTERPRISE_LICENSE_KEY");

        clientConfig.getNetworkConfig().addAddress("127.0.0.1");
        SSLConfig sslConfig = new SSLConfig();
        sslConfig.setEnabled(true);
        sslConfig.setFactoryClassName("com.hazelcast.nio.ssl.BasicSSLContextFactory");
        sslConfig.setProperty("keyStore", new File("enterprise/client-ssl/hazelcast.ks").getAbsolutePath());
        sslConfig.setProperty("keyStorePassword", "password");
        sslConfig.setProperty("javax.net.ssl.trustStore", new File("enterprise/client-ssl/hazelcast.ts").getAbsolutePath());
        clientConfig.getNetworkConfig().setSSLConfig(sslConfig);

        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        System.out.println(clientConfig.toString());

        BlockingQueue<String> queue = client.getQueue("queue");
        queue.put("Hello!");
        System.out.println("Message sent by Hazelcast Client!");

        HazelcastClient.shutdownAll();
    }
}
