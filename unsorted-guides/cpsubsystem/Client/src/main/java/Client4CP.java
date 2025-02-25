import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPMap;

public final class Client4CP {
    public static void main(String[] args) throws Exception {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("127.0.0.1");
        clientConfig.getNetworkConfig().setSmartRouting(false);

        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

        CPMap<String, String> trektng = client.getCPSubsystem().getMap("trektng");
        trektng.set("1", "Jean-Luc Picard");
        trektng.set("2", "William Riker");
        trektng.set("3", "Data");
        trektng.set("4", "Beverly Crusher");
        trektng.set("5", "Deanna Troi");
        trektng.set("6", "Tasha Yar");
        trektng.set("7", "Worf son of Mogh");
        trektng.set("8", "Wesley Crusher");

        //System.out.println("They killed off" + trektng.get("6") + "in season one." );


        HazelcastClient.shutdownAll();
    }
}

