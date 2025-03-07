package org.hazelcast.jet.demo.util;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.SSLConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.hazelcast.jet.demo.FlightTelemetry;

import java.net.URISyntaxException;
import java.util.Properties;

public class HazelcastConnection implements java.io.Serializable {

    private static HazelcastInstance hazelcastInstance;
    private static String instanceMode;
    private static String clientKeyStore;
    private static String clientKeyStorePassword;
    private static String clientTrustStore;
    private static String clientTrustStorePassword;
    private static String clusterName;
    private static String viridianDiscoveryToken;
    private static String viridianURI;

    public enum InstanceMode {
        VIRIDIAN,
        EMBEDDED,
        BOOTSTRAP
    }

    public static String getInstanceMode() {
        return instanceMode;
    }

    public static void setInstanceMode(String instanceMode) {
        HazelcastConnection.instanceMode = instanceMode;
    }

    public static String getClientKeyStore() {
        return clientKeyStore;
    }

    public static void setClientKeyStore(String clientKeyStore) {
        HazelcastConnection.clientKeyStore = clientKeyStore;
    }

    public static String getClientKeyStorePassword() {
        return clientKeyStorePassword;
    }

    public static void setClientKeyStorePassword(String clientKeyStorePassword) {
        HazelcastConnection.clientKeyStorePassword = clientKeyStorePassword;
    }

    public static String getClientTrustStore() {
        return clientTrustStore;
    }

    public static void setClientTrustStore(String clientTrustStore) {
        HazelcastConnection.clientTrustStore = clientTrustStore;
    }

    public static  String getClientTrustStorePassword() {
        return clientTrustStorePassword;
    }

    public static void setClientTrustStorePassword(String clientTrustStorePassword) {
        HazelcastConnection.clientTrustStorePassword = clientTrustStorePassword;
    }

    public static String getClusterName() {
        return clusterName;
    }

    public static void setClusterName(String clusterName) {
        HazelcastConnection.clusterName = clusterName;
    }

    public static String getViridianDiscoveryToken() {
        return viridianDiscoveryToken;
    }

    public static void setViridianDiscoveryToken(String viridianDiscoveryToken) {
        HazelcastConnection.viridianDiscoveryToken = viridianDiscoveryToken;
    }

    public static String getViridianURI() {
        return viridianURI;
    }

    public static void setViridianURI(String viridianURI) {
        HazelcastConnection.viridianURI = viridianURI;
    }


    public static HazelcastInstance createHazelcastInstance(String instanceMode)
            throws URISyntaxException, InstantiationException {

        setInstanceMode(instanceMode);

        if (instanceMode != null) {
            if (instanceMode.equalsIgnoreCase(InstanceMode.BOOTSTRAP.toString())) {
                System.out.println("Returning bootstrapped instance");
                hazelcastInstance = Hazelcast.bootstrappedInstance();
            } else if (instanceMode.equalsIgnoreCase(InstanceMode.VIRIDIAN.toString())) {
                throw new InstantiationException("Cannot instantiate a cloud connection without client certificates and discovery tokens.");
            } else if (instanceMode.equalsIgnoreCase(InstanceMode.EMBEDDED.toString())) {
                System.out.println("Returning bootstrapped instance");
                hazelcastInstance = Hazelcast.newHazelcastInstance();
            } else {
                throw new InstantiationException("Cannot instantiate a connection unrecognised instance mode [" + instanceMode + "].");
            }
        }

        return hazelcastInstance;
    }

    public static HazelcastInstance createHazelcastInstance(String instanceMode, String clientKeyStore, String clientKeyStorePassword,
                                                            String clientTrustStore, String clientTrustStorePassword, String clusterName,
                                                            String viridianDiscoveryToken, String viridianURI)
            throws URISyntaxException , InstantiationException {

        setInstanceMode(instanceMode);
        setClientKeyStore(clientKeyStore);
        setClientKeyStorePassword(clientKeyStorePassword);
        setClientTrustStore(clientTrustStore);
        setClientTrustStorePassword(clientTrustStorePassword);
        setClusterName(clusterName);
        setViridianDiscoveryToken(viridianDiscoveryToken);
        setViridianURI(viridianURI);

        if (instanceMode != null) {
            if (!instanceMode.equalsIgnoreCase(InstanceMode.VIRIDIAN.toString())) {
                // Pass to other implementation
                hazelcastInstance = createHazelcastInstance(instanceMode);
            } else {
                ClassLoader classLoader = FlightTelemetry.class.getClassLoader();
                Properties props = new Properties();
                props.setProperty("javax.net.ssl.keyStore", classLoader.getResource(clientKeyStore).toURI().getPath());
                props.setProperty("javax.net.ssl.keyStorePassword", clientKeyStorePassword);
                props.setProperty("javax.net.ssl.trustStore",
                        classLoader.getResource(clientTrustStore).toURI().getPath());
                props.setProperty("javax.net.ssl.trustStorePassword", clientTrustStorePassword);
                ClientConfig config = new ClientConfig();
                config.getNetworkConfig().setSSLConfig(new SSLConfig().setEnabled(true).setProperties(props));
                config.getNetworkConfig().getCloudConfig()
                        .setDiscoveryToken(viridianDiscoveryToken)
                        .setEnabled(true);
                config.setProperty("hazelcast.client.cloud.url", viridianURI);
                config.setClusterName(clusterName);

                hazelcastInstance = HazelcastClient.newHazelcastClient(config);
            }
        }

        return hazelcastInstance;
    }
}
