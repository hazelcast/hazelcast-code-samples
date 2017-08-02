import com.hazelcast.config.Config;
import com.hazelcast.config.SSLConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.io.File;
import java.util.concurrent.BlockingQueue;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;

/**
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class Member {

    public static void main(String[] args) throws Exception {
        SSLConfig sslConfig = new SSLConfig();
        sslConfig.setEnabled(true);
        sslConfig.setFactoryClassName("com.hazelcast.nio.ssl.BasicSSLContextFactory");
        sslConfig.setProperty("keyStore", new File("hazelcast.ks").getAbsolutePath());
        sslConfig.setProperty("keyStorePassword", "password");
        sslConfig.setProperty("javax.net.ssl.trustStore", new File("hazelcast.ts").getAbsolutePath());

        Config config = new Config();
        config.setLicenseKey(ENTERPRISE_LICENSE_KEY);
        config.getNetworkConfig().setSSLConfig(sslConfig);

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        System.out.println("Hazelcast Member instance is running!");

        BlockingQueue<String> queue = hz.getQueue("queue");
        for (; ; ) {
            System.out.println(queue.take());
        }
    }
}
