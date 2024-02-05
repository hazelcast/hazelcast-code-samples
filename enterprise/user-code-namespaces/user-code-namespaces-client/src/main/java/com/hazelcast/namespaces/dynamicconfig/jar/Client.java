package com.hazelcast.namespaces.dynamicconfig.jar;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.UserCodeNamespaceConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.EntryProcessor;
import usercodenamespaces.IncrementingEntryProcessor;

import java.io.File;
import java.net.URL;
public class Client {

    public static void main(String[] args) throws Exception {
        HazelcastInstance client = HazelcastClient.newHazelcastClient();

        EntryProcessor entryProcessor = new IncrementingEntryProcessor();
        UserCodeNamespaceConfig namespaceConfig = new UserCodeNamespaceConfig("ucn1");
        namespaceConfig.addJar(getJarURL(), "jar_id");

        // dynamically add the namespace config
        client.getConfig().getNamespacesConfig().addNamespaceConfig(namespaceConfig);
        // execute the entry processor
        client.getMap("map1").executeOnKey("key", entryProcessor);

        // will print incremented value
        System.out.println(client.getMap("map1").get("key"));
        client.shutdown();
    }

    /**
     * Helper code to load the example jar from outside the classpath
     * @return the URL of the jar.
     */
    private static URL getJarURL() throws Exception {
        String basePath = System.getProperty("user.dir");
        String directoryPath = basePath + "/src/main/java/com/hazelcast/namespaces/";

        File jar = new File(directoryPath + "IncrementingEntryProcessor.jar");
        return jar.toURI().toURL();
    }
}
