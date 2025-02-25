package sample.com.hazelcast.clientfailover;

import java.util.Properties;
import java.util.Random;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientFailoverConfig; // <1>
import com.hazelcast.config.SSLConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.map.IMap;
import com.hazelcast.sql.SqlResult;
import com.hazelcast.sql.SqlRow;
import com.hazelcast.sql.SqlService;


// tag::intro-comments[]
/**
 * This is a boilerplate application that configures a client 
 * to connect to a Hazelcast Cloud cluster.
 * See: https://docs.hazelcast.com/cloud/get-started
 * 
 * The numbers in angled brackets are used in the documentation 
 * to call out and explain important steps.
 * 
 * The comments marked tag:: are also used to include and exclude 
 * snippets in the docs.
 */
// end::intro-comments[]

public class ClientWithSsl {

    public static void main(String[] args) throws Exception {

        ClientFailoverConfig clientFailoverConfig = new ClientFailoverConfig(); // <2>
        clientFailoverConfig.addClientConfig(getPrimaryClientConfig());
        clientFailoverConfig.addClientConfig(getSecondaryClientConfig());

        HazelcastInstance client = HazelcastClient.newHazelcastFailoverClient(clientFailoverConfig);

        System.out.println("Connection Successful!");

        nonStopMapExample(client); // <3>

        client.shutdown();

        System.exit(0);

    }

    // <4>
    private static ClientConfig getPrimaryClientConfig() throws Exception {
        ClassLoader classLoader = ClientWithSsl.class.getClassLoader();
        Properties props = new Properties();
        props.setProperty("javax.net.ssl.keyStore", classLoader.getResource("client.keystore").toURI().getPath());
        props.setProperty("javax.net.ssl.keyStorePassword", "YOUR_KEYSTORE_PASSWORD");
        props.setProperty("javax.net.ssl.trustStore",
            classLoader.getResource("client.truststore").toURI().getPath());
        props.setProperty("javax.net.ssl.trustStorePassword", "YOUR_KEYSTORE_PASSWORD");
        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().setSSLConfig(new SSLConfig().setEnabled(true).setProperties(props));
        config.getNetworkConfig().getCloudConfig()
            .setDiscoveryToken("YOUR_DISCOVERY_TOKEN")
            .setEnabled(true);
        config.setClusterName("YOUR_CLUSTER_ID");
        config.getConnectionStrategyConfig()
            .getConnectionRetryConfig()
            .setClusterConnectTimeoutMillis(10000); // <5>
        config.setProperty("hazelcast.client.cloud.url", "https://api.cloud.hazelcast.com");

        return config;
        }
    
    // <6>
    private static ClientConfig getSecondaryClientConfig() throws Exception {
        ClassLoader classLoader = ClientWithSsl.class.getClassLoader();
       Properties props = new Properties();
        props.setProperty("javax.net.ssl.keyStore", classLoader.getResource("client2.keystore").toURI().getPath()); // <7>
        props.setProperty("javax.net.ssl.keyStorePassword", "YOUR_KEYSTORE_PASSWORD");
        props.setProperty("javax.net.ssl.trustStore",
            classLoader.getResource("client2.truststore").toURI().getPath()); // <7>
        props.setProperty("javax.net.ssl.trustStorePassword", "YOUR_KEYSTORE_PASSWORD");
        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().setSSLConfig(new SSLConfig().setEnabled(true).setProperties(props));
        config.getNetworkConfig().getCloudConfig()
           .setDiscoveryToken("YOUR_DISCOVERY_TOKEN")
           .setEnabled(true);
        config.setClusterName("YOUR_CLUSTER_ID");
        config.getConnectionStrategyConfig().getConnectionRetryConfig().setClusterConnectTimeoutMillis(10000);
        return config;
        }

    /**
     * This example shows how to work with Hazelcast maps, where the map is
     * updated continuously.
     *
     * @param client - a {@link HazelcastInstance} client.
     */
    private static void nonStopMapExample(HazelcastInstance client) {
        System.out.println("Now the map named 'map' will be filled with random entries.");

        IMap<String, String> map = client.getMap("map");
        Random random = new Random();
        int iterationCounter = 0;
        while (true) {
            int randomKey = random.nextInt(100_000);
            map.put("key-" + randomKey, "value-" + randomKey); // Replaced by exception handling
            map.get("key-" + random.nextInt(100_000));
            if (++iterationCounter == 10) {
                iterationCounter = 0;
                System.out.println("Current map size: " + map.size());
            }
        }
    }
}
