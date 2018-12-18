import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.ClientListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class AttributesUsage {
    public static void main(String[] args) {


        Config config = new Config();
        JoinConfig join = config.getNetworkConfig().getJoin();
        join.getTcpIpConfig().setEnabled(true);
        join.getMulticastConfig().setEnabled(false);
        final HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        instance.getClientService().addClientListener(new ClientListener() {
            @Override
            public void clientConnected(com.hazelcast.core.Client client) {
                System.out.println("Client : " + client.getName() + " is connected to member: " + instance.getName());
                System.out.println("Client : " + client.getName() + " is connected with attributes " + client.getAttributes());

            }

            @Override
            public void clientDisconnected(com.hazelcast.core.Client client) {

            }
        });

        ClientConfig clientConfig = new ClientConfig();
        //setting instance name and attributes
        clientConfig.setInstanceName("SampleClientName");
        clientConfig.setAttribute("role", "admin");
        clientConfig.setAttribute("region", "foo");

        HazelcastClient.newHazelcastClient(clientConfig);


        //client opened with xml config. See ./resources/hazelcast-client.xml
        HazelcastClient.newHazelcastClient();


    }
}