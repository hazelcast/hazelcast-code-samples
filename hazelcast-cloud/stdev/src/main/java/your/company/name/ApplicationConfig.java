package your.company.name;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientUserCodeDeploymentConfig;
import com.hazelcast.client.spi.impl.discovery.HazelcastCloudDiscovery;
import com.hazelcast.client.spi.properties.ClientProperty;
import com.hazelcast.config.GroupConfig;

/**
 * <p>Produce the configuration for the Hazelcast client,
 * mainly just plug in the cluster authentication details
 * provided by the cloud cluster details page.
 * </p>
 * <p>We also here specify to make the {@link Customer} and
 * {@link TotalDifferenceSquaredCallable} classes
 * available to the cluster servers.
 * </p>
 */
public class ApplicationConfig {

    public static ClientConfig clientConfig(String clusterName, String clusterPassword, String clusterDiscoveryToken) {
        ClientConfig clientConfig = new ClientConfig();

        /* Set the name and password
         */
        GroupConfig groupConfig = new GroupConfig(clusterName, clusterPassword);
        clientConfig.setGroupConfig(groupConfig);

        /* Discovery token for the cloud
         */
        clientConfig.setProperty(ClientProperty.HAZELCAST_CLOUD_DISCOVERY_TOKEN.getName(),
          clusterDiscoveryToken);
        clientConfig.setProperty(HazelcastCloudDiscovery.CLOUD_URL_BASE_PROPERTY.getName(),
          "https://coordinator.hazelcast.cloud");

        /* Activate the ability for the client to push code to the servers
         */
        ClientUserCodeDeploymentConfig clientUserCodeDeploymentConfig = clientConfig.getUserCodeDeploymentConfig();
        clientUserCodeDeploymentConfig.setEnabled(true);

        /* These are the classes we wish the servers to have
         */
        clientUserCodeDeploymentConfig.addClass(TotalDifferenceSquaredCallable.class);
        clientUserCodeDeploymentConfig.addClass(Customer.class);

        return clientConfig;
    }

}
