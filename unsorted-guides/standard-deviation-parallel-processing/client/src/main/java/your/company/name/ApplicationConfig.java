package your.company.name;

import java.util.Properties;
import java.net.*;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.SSLConfig;

/**
 * <p>Produce the configuration for the Hazelcast client,
 * mainly just plug in the cluster authentication details
 * provided by the cluster details page.
 * </p>
 */
public class ApplicationConfig {

    public static ClientConfig clientConfig(String clusterName, String clusterDiscoveryToken, String keyStorePassword) throws Exception {
        ClientConfig clientConfig = new ClientConfig();
        ClassLoader classLoader = Application.class.getClassLoader();
        Properties props = new Properties();
        props.setProperty("javax.net.ssl.keyStore", 
        classLoader.getResource("client.keystore").toURI().getPath());
        props.setProperty("javax.net.ssl.trustStore",
        classLoader.getResource("client.truststore").toURI().getPath());
        props.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
        props.setProperty("javax.net.ssl.trustStorePassword", keyStorePassword);
        clientConfig.setClusterName(clusterName);
        clientConfig.getNetworkConfig().setSSLConfig(new SSLConfig().setEnabled(true).setProperties(props));
        clientConfig.getNetworkConfig().getCloudConfig()
            .setDiscoveryToken(clusterDiscoveryToken)
            .setEnabled(true);
        clientConfig.setProperty("hazelcast.client.cloud.url", "https://api.cloud.hazelcast.com");

        return clientConfig;
    }

}
