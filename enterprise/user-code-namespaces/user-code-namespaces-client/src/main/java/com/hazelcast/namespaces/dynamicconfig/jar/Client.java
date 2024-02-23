package com.hazelcast.namespaces.dynamicconfig.jar;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.UserCodeNamespaceConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.EntryProcessor;
import usercodenamespaces.IncrementingEntryProcessor;

import java.io.File;

public class Client {

    public static void main(String[] args) throws Exception {
        HazelcastInstance client = HazelcastClient.newHazelcastClient();

        EntryProcessor entryProcessor = new IncrementingEntryProcessor();
        UserCodeNamespaceConfig namespaceConfig = new UserCodeNamespaceConfig("ucn1");
        String jarPath = "src/main/java/com/hazelcast/namespaces/IncrementingEntryProcessor.jar";
        namespaceConfig.addJar(new File(jarPath).toURI().toURL(), "jar_id");

        // dynamically add the namespace config
        client.getConfig().getNamespacesConfig().addNamespaceConfig(namespaceConfig);
        // execute the entry processor
        client.getMap("map1").executeOnKey("key", entryProcessor);

        // will print incremented value
        System.out.println(client.getMap("map1").get("key"));
        client.shutdown();
    }

}
