package your.company.name;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * <p>The entry point of the application.
 * </p>
 * <p>We ask the user via the command line for the
 * connection details for their Hazelcast Cloud cluster.
 * </p>
 * <p>Then we load some test data.
 * </p>
 * <p>Then run the business logic.
 * </p>
 * <p>Finally disconnect.
 * </p>
 */
public class Application {

    private static HazelcastInstance hazelcastClient;

    public static void main(String[] args) throws Exception {
        /* Ask the user for the necessary fields
         */
        InputReader inputReader = new InputReader();
        String clusterName =
                inputReader.read(InputReader.CLUSTER_NAME);
        String clusterPassword =
                inputReader.read(InputReader.CLUSTER_PASSWORD);
        String clusterDiscoveryToken =
                inputReader.read(InputReader.CLUSTER_DISCOVERY_TOKEN);
        inputReader.close();

        if (clusterName.length() == 0 || clusterPassword.length() == 0
            || clusterDiscoveryToken.length() == 0) {
            System.exit(0);
        }

        /* Configure and create the Hazelcast client
         */
        ClientConfig clientConfig = ApplicationConfig.clientConfig(
                clusterName,
                clusterPassword,
                clusterDiscoveryToken
                );

        System.out.println("--------------------------------------------------");
        System.out.println("Starting Hazelcast client");
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
        System.out.println("Stopping Hazelcast client");
        System.out.println("--------------------------------------------------");
        hazelcastClient.shutdown();
    }

}
