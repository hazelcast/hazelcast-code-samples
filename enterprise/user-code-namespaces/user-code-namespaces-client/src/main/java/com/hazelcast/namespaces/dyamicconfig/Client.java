package com.hazelcast.namespaces.dyamicconfig;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.UserCodeNamespaceConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.EntryProcessor;
import usercodenamespaces.IncrementingEntryProcessor;

public class Client {

    public static void main(String[] args) {
        HazelcastInstance client = HazelcastClient.newHazelcastClient();

        EntryProcessor entryProcessor = new IncrementingEntryProcessor();
        UserCodeNamespaceConfig namespaceConfig = new UserCodeNamespaceConfig("ucn1");
        namespaceConfig.addClass(IncrementingEntryProcessor.class);

        //dynamically add the namespace config
        client.getConfig().getNamespacesConfig().addNamespaceConfig(namespaceConfig);
        //execute the entry processor
        client.getMap("map1").executeOnKey("key", entryProcessor);

        //will print incremented value
        System.out.println(client.getMap("map1").get("key"));
        client.shutdown();
    }
}
