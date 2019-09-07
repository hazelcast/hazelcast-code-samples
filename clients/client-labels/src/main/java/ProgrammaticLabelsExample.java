import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.Client;
import com.hazelcast.client.ClientListener;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class ProgrammaticLabelsExample {
    public static void main(String[] args) {
        Config config = new Config();
        JoinConfig join = config.getNetworkConfig().getJoin();
        join.getTcpIpConfig().setEnabled(true);
        join.getMulticastConfig().setEnabled(false);
        final HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        instance.getClientService().addClientListener(new ClientListener() {
            @Override
            public void clientConnected(Client client) {
                System.out.println("Client : " + client.getName() + " is connected to member: " + instance.getName());
                System.out.println("Client : " + client.getName() + " is connected with labels " + client.getLabels());

            }

            @Override
            public void clientDisconnected(Client client) {

            }
        });

        ClientConfig clientConfig = new ClientConfig();
        //setting client instance name and adding client labels
        clientConfig.setInstanceName("SampleClientName");
        clientConfig.addLabel("role admin");
        clientConfig.addLabel("region foo");

        HazelcastClient.newHazelcastClient(clientConfig);
    }
}
