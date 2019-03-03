import com.hazelcast.config.Config;
import com.hazelcast.config.SSLConfig;
import com.hazelcast.config.ServerSocketEndpointConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class MemberB {

    public static void main(String[] args) {
        Config config = new Config();

        config.getAdvancedNetworkConfig().setEnabled(true);
        // advanced network configuration creates by default a member server socket on port 5701
        // in this example we only configure explicitly the WAN server socket endpoint
        config.getAdvancedNetworkConfig().addWanEndpointConfig(createWanEndpointConfig());

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
    }

    // This is the the passive side of WAN replication. A server socket is created that listens
    // for incoming connections. SSL configuration needs to be the same as active side to allow
    // connection establishment.
    private static ServerSocketEndpointConfig createWanEndpointConfig() {
        ServerSocketEndpointConfig endpointConfig = new ServerSocketEndpointConfig();
        endpointConfig.setName("active-wan")
                      .setSSLConfig(new SSLConfig()
                              .setEnabled(true)
                              .setProperty("trustStore", "truststore")
                              .setProperty("trustStorePassword", System.getenv("KEYSTORE_PASSWORD")));

        endpointConfig.setPort(8443)
                      .setPublicAddress("147.102.1.10:8443");

        return endpointConfig;
    }
}

