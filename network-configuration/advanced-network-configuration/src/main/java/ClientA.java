import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

public class ClientA {

    public static void main(String[] args) {
        // configure Hazelcast client to connect to cluster A member addresses
        // as exposed in clients network
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("172.10.10.10:9000")
                    .addAddress("172.10.10.11:9000").addAddress("172.10.10.12:9000");

        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
    }
}
