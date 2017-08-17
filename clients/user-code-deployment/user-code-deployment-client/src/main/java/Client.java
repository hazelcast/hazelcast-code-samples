import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientUserCodeDeploymentConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class Client {

    public static void main(String[] args) throws InterruptedException {
        ClientConfig clientConfig = new ClientConfig();
        ClientUserCodeDeploymentConfig clientUserCodeDeploymentConfig = new ClientUserCodeDeploymentConfig();
        clientUserCodeDeploymentConfig.addClass("IncrementingEntryProcessor");
        clientUserCodeDeploymentConfig.setEnabled(true);
        clientConfig.setUserCodeDeploymentConfig(clientUserCodeDeploymentConfig);

        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

        IncrementingEntryProcessor incrementingEntryProcessor = new IncrementingEntryProcessor();
        int keyCount = 10;
        IMap<Integer, Integer> map = client.getMap("sample map");

        for (int i = 0; i < keyCount; i++) {
            map.put(i, 0);
        }
        map.executeOnEntries(incrementingEntryProcessor);

        for (int i = 0; i < keyCount; i++) {
            System.out.println(map.get(i));
        }
    }
}
