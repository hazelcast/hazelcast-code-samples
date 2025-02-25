package your.company.name;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

/**
 * <p>The entry point of the application, which does the following
 * </p>
 * <ol>
 *    <li> Ask the user of the command prompt for the
 *    connection details of a Hazelcast Cloud cluster.
 *    </li>
 *    <li>Load some test data.
 *    </li>
 *    <li>Run the business logic.
 *    </li>
 *    <li>Disconnect from the cluster.
 *    </li>
 * </ol>
 */
public class Application {

    private static HazelcastInstance hazelcastClient;

    public static void main(String[] args) throws Exception {
        /* Ask the user for the necessary fields
         */
        InputReader inputReader = new InputReader();
        String clusterName =
                inputReader.read(InputReader.CLUSTER_NAME);
        String clusterDiscoveryToken =
                inputReader.read(InputReader.CLUSTER_DISCOVERY_TOKEN);
        String keyStorePassword = inputReader.read(InputReader.KEYSTORE_PASSWORD);
        inputReader.close();

        if (clusterName.length() == 0 ||
            clusterDiscoveryToken.length() == 0 ||
            keyStorePassword.length() == 0) {
            System.exit(0);
        }

        /* Configure and create the Hazelcast client
         */
        ClientConfig clientConfig = ApplicationConfig.clientConfig(
                clusterName,
                clusterDiscoveryToken,
                keyStorePassword
                );

        System.out.println("--------------------------------------------------");
        System.out.println("Starting the Hazelcast client");
        System.out.println("--------------------------------------------------");

        hazelcastClient = HazelcastClient.newHazelcastClient(clientConfig);

        IMap<Integer, Customer> iMap = hazelcastClient.getMap(Customer.class.getName());

        System.out.println("--------------------------------------------------");
        System.out.printf("Hazelcast client '%s', using map '%s'%n",
                hazelcastClient.getName(),
                iMap.getName()
                );
        System.out.println("--------------------------------------------------");

        /* Do the calculation
         */
        TestDataLoader.loadTestData(iMap);
        try {
            BusinessLogic.calculate(iMap, hazelcastClient);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        System.out.println("--------------------------------------------------");
        System.out.println("Disconnecting the Hazelcast client");
        System.out.println("--------------------------------------------------");
        hazelcastClient.shutdown();
    }

}
