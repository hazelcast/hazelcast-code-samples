package hazelcast.platform.labs.airline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientConnectionStrategyConfig;
import com.hazelcast.config.SSLConfig;
import com.hazelcast.core.HazelcastInstance;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Static methods that help with connecting to a Hazelcast cluster.
 *
 * If a Viridian cluster name is passed to the connect method, it will attempt to access
 * cluster credentials inside the CLC vault on the local file system under the
 * ~/.hazelcast directory (or %HOMEDRIVE%:%HOMEPATH%\AppData\Roaming\Hazelcast on
 * Windows).  The cluster name you pass to this method must match the one passed to "clc viridian import-config"
 * or to "clc config import". NOTE: This method requires that the configuration be imported using CLC
 * version 5.3.3 or later.  It does not work with non-production Viridian environments.
 *
 * There is also a connect method that takes no arguments.  This method will first
 * inspect the VIRIDIAN_SECRETS_DIR environment variable and, if present, will
 * attempt to connect to a Viridian cluster using credentials from the environment
 * using the variables described below.
 *
 * VIRIDIAN_SECRETS_DIR  The unzipped contents of the file downloaded from the "Advanced" configuration tab
 * VIRIDIAN_CLUSTER_ID
 * VIRIDIAN_DISCOVERY_TOKEN
 * VIRIDIAN_PASSWORD
 *
 * For non-Viridian connections that are not secured (e.g. local development clusters), use the connect method
 * with no arguments and provide the following environment variables instead of the 4 above.
 *
 * HZ_SERVERS  A comma separated list of members (e.g. member1:5701,member2:5701)
 * HZ_CLUSTER_NAME  The cluster name
 *
 * There are also 2 methods, "configureViridianClientFromEnvironment" and
 * "configureViridianClientFromLocalVault" that take a ClientConfig instance as a parameter.  If
 * you wish to add non-default client configurations, you can use one of these methods and
 * pass a ClientConfig that has already been configured.  These methods will use the passed configuration
 * as a starting point and only overwrite the necessary configuration options.
 *
 */
public class ConnectionHelper {
    public static final String VIRIDIAN_SECRETS_DIR_PROP = "VIRIDIAN_SECRETS_DIR";
    public static final String VIRIDIAN_CLUSTER_ID_PROP = "VIRIDIAN_CLUSTER_ID";
    public static final String VIRIDIAN_PASSWORD_PROP = "VIRIDIAN_PASSWORD";
    public static final String VIRIDIAN_DISCOVERY_TOKEN_PROP = "VIRIDIAN_DISCOVERY_TOKEN";
    private static final String HZ_SERVERS_PROP = "HZ_SERVERS";
    private static final String HZ_CLUSTER_NAME_PROP = "HZ_CLUSTER_NAME";

    public static  HazelcastInstance connect(){
        return connect(null);
    }
    public static HazelcastInstance connect(String clusterName){
        String message;
        ClientConfig clientConfig = new ClientConfig();
        if (clusterName != null ){
            configureViridianClientFromLocalVault(clientConfig, clusterName);
            message = "Connected to Viridian Cluster: " + clusterName;
        } else {
            if (ConnectionHelper.viridianConfigPresent()){
                ConnectionHelper.configureViridianClientFromEnvironment(clientConfig);
                message = "Connected to Viridian Cluster: " +
                        System.getenv(ConnectionHelper.VIRIDIAN_CLUSTER_ID_PROP);
            } else {
                String hzServersProp = getRequiredEnv(HZ_SERVERS_PROP);
                String []hzServers = hzServersProp.split(",");
                for (int i = 0; i < hzServers.length; ++i) hzServers[i] = hzServers[i].trim();

                String hzClusterName = getRequiredEnv(HZ_CLUSTER_NAME_PROP);

                clientConfig.setClusterName(hzClusterName);
                for(String server: hzServers) clientConfig.getNetworkConfig().addAddress(server);
                message = "Connected to cluster [" + hzClusterName + "] at " + hzServersProp;
            }
        }
        clientConfig.getConnectionStrategyConfig().setAsyncStart(false);
        clientConfig.getConnectionStrategyConfig().setReconnectMode(ClientConnectionStrategyConfig.ReconnectMode.ON);

        HazelcastInstance hzClient = HazelcastClient.newHazelcastClient(clientConfig);
        System.out.println(message);
        return hzClient;
    }

    private static String getRequiredEnv(String envVarName){
        String result = System.getenv(envVarName);
        if (result == null)
            throw new RuntimeException("Required environment variable (" + envVarName + ") was not provided.");

        return result;
    }

    /*
     * Looks for Viridian configuration in the environment
     */
    public static boolean viridianConfigPresent(){
        String secretsDir = System.getenv(VIRIDIAN_SECRETS_DIR_PROP);
        return secretsDir != null;
    }

    public static void configureViridianClientFromLocalVault(ClientConfig clientConfig, String clusterName){
        File homeDir = new File(System.getProperty("user.home"));
        String os = System.getProperty("os.name");
        File hazelcastHomeDir;
        if (os.toLowerCase().contains("windows")){
            hazelcastHomeDir = new File(new File(new File(homeDir, "AppData"), "Roaming"), "Hazelcast");
        } else {
            hazelcastHomeDir = new File(homeDir, ".hazelcast");
        }
        if (!hazelcastHomeDir.isDirectory())
            throw new RuntimeException("The Hazelcast settings directory, " + hazelcastHomeDir.getAbsolutePath() + " does not exist");

        File clusterConfigDir = new File(new File(hazelcastHomeDir, "configs"), clusterName);
        if (!clusterConfigDir.isDirectory())
            throw new RuntimeException("Did not find expected configuration at " + clusterConfigDir.getAbsolutePath());

        File configFile = new File(clusterConfigDir, "config.json");

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root =  mapper.readTree(configFile);
            String clusterId = root.get("cluster").get("name").asText();
            String discoveryToken = root.get("cluster").get("discovery-token").asText();
            String apiBase = root.get("cluster").get("api-base").asText();
            String password = root.get("ssl").get("key-password").asText();
            configureViridian(clusterId, discoveryToken, password, clusterConfigDir.getAbsolutePath(), apiBase, clientConfig);
        } catch(IOException x){
            throw new RuntimeException("An error occurred while parsing " + configFile.getAbsolutePath(), x);
        }
    }

    public static void configureViridianClientFromEnvironment(ClientConfig clientConfig){
        String secretsDir = getRequiredEnv(VIRIDIAN_SECRETS_DIR_PROP);
        String password = getRequiredEnv(VIRIDIAN_PASSWORD_PROP);
        String clusterId = getRequiredEnv(VIRIDIAN_CLUSTER_ID_PROP);
        String discoveryToken = getRequiredEnv(VIRIDIAN_DISCOVERY_TOKEN_PROP);
        configureViridian(clusterId, discoveryToken, password, secretsDir, null, clientConfig);
    }

    public static void configureViridian(
            String clusterId,
            String discoveryToken,
            String password,
            String secretsDir,
            String apiBase,
            ClientConfig clientConfig){
        if (apiBase == null)
            apiBase = "https://api.viridian.hazelcast.com";

        File configDir = new File(secretsDir);
        if (!configDir.isDirectory()){
            throw new RuntimeException("Could not initialize Viridian connection because the given secrets directory (" + secretsDir + ") does not exist or is not a directory.");
        }

        File keyStoreFile = new File(configDir, "client.keystore");
        File trustStoreFile = new File(configDir, "client.truststore");
        if (!keyStoreFile.isFile() || !keyStoreFile.canRead()){
            throw new RuntimeException("The keystore file (" + keyStoreFile.getPath() +") was not found or could not be read.");
        }
        if (!trustStoreFile.isFile() || !trustStoreFile.canRead()){
            throw new RuntimeException("The truststore file (" + trustStoreFile.getPath() +") was not found or could not be read.");
        }

        Properties props = new Properties();
        props.setProperty("javax.net.ssl.keyStore", keyStoreFile.getPath());
        props.setProperty("javax.net.ssl.keyStorePassword", password);
        props.setProperty("javax.net.ssl.trustStore", trustStoreFile.getPath());
        props.setProperty("javax.net.ssl.trustStorePassword", password);

        clientConfig.getNetworkConfig().setSSLConfig(new SSLConfig().setEnabled(true).setProperties(props));
        clientConfig.getNetworkConfig().getCloudConfig().setEnabled(true).setDiscoveryToken(discoveryToken);
        clientConfig.setProperty("hazelcast.client.cloud.url", apiBase);
        clientConfig.setClusterName(clusterId);
    }
}
