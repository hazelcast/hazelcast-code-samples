import com.hazelcast.config.Config;
import com.hazelcast.config.SSLConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.io.File;
import java.util.concurrent.BlockingQueue;

/**
 * keytool -genkey -alias hazelcast -keyalg RSA -keypass password -keystore hazelcast.ks -storepass password
 * keytool -export -alias hazelcast -file hazelcast.cer -keystore hazelcast.ks -storepass password
 * keytool -import -v -trustcacerts -alias hazelcast -keypass password -file hazelcast.cer -keystore hazelcast.ts -storepass password
 */
public class Member {
    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.keyStore", new File("hazelcast.ks").getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStore", new File("hazelcast.ts").getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", "password");

        Config config = new Config();
        config.getNetworkConfig().setSSLConfig(new SSLConfig().setEnabled(true));

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        BlockingQueue<String> queue = hz.getQueue("queue");
        System.out.println("Full member up");
        for (; ; )
            System.out.println(queue.take());
    }
}
