import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.SSLConfig;
import com.hazelcast.core.HazelcastInstance;

import java.io.File;
import java.util.concurrent.BlockingQueue;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;

/**
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class Client {

    public static void main(String[] args) throws Exception {
        SSLConfig sslConfig = new SSLConfig();
        sslConfig.setEnabled(true);
        sslConfig.setFactoryClassName("com.hazelcast.nio.ssl.BasicSSLContextFactory");
        sslConfig.setProperty("keyStore", new File("hazelcast.ks").getAbsolutePath());
        sslConfig.setProperty("keyStorePassword", "password");
        sslConfig.setProperty("javax.net.ssl.trustStore", new File("hazelcast.ts").getAbsolutePath());

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setLicenseKey(ENTERPRISE_LICENSE_KEY);
        clientConfig.getNetworkConfig().addAddress("127.0.0.1");
        clientConfig.getNetworkConfig().setSSLConfig(sslConfig);

        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        System.out.println(clientConfig.toString());

        BlockingQueue<String> queue = client.getQueue("queue");
        queue.put("Hello!");
        System.out.println("Message sent by Hazelcast Client!");

        HazelcastClient.shutdownAll();
    }
}
