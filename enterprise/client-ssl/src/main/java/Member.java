import com.hazelcast.config.Config;
import com.hazelcast.config.SSLConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.io.File;
import java.util.concurrent.BlockingQueue;

public class Member {

    public static void main(String[] args) throws Exception {
        Config config = new Config();

        // please set your enterprise license key to make the sample work
        config.setLicenseKey("YOUR_ENTERPRISE_LICENSE_KEY");

        SSLConfig sslConfig = new SSLConfig();
        sslConfig.setEnabled(true);
        sslConfig.setFactoryClassName("com.hazelcast.nio.ssl.BasicSSLContextFactory");
        sslConfig.setProperty("keyStore", new File("enterprise/client-ssl/hazelcast.ks").getAbsolutePath());
        sslConfig.setProperty("keyStorePassword", "password");
        sslConfig.setProperty("javax.net.ssl.trustStore", new File("enterprise/client-ssl/hazelcast.ts").getAbsolutePath());
        config.getNetworkConfig().setSSLConfig(sslConfig);

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        System.out.println("Hazelcast Member instance is running!");

        BlockingQueue<String> queue = hz.getQueue("queue");
        for (; ; ) {
            System.out.println(queue.take());
        }
    }
}
