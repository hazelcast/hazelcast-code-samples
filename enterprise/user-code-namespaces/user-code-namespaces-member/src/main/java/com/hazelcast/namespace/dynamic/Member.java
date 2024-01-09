package com.hazelcast.namespace.dynamic;

import com.hazelcast.config.Config;
import com.hazelcast.config.UserCodeNamespaceConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class Member {

    public static void main(String[] args) {
        Config config = new Config();
        config.getNamespacesConfig().setEnabled(true);

        //construct a namespace configuration with no resources
        UserCodeNamespaceConfig userCodeNamespaceConfig = new UserCodeNamespaceConfig("ucn1");
        config.getNamespacesConfig().addNamespaceConfig(userCodeNamespaceConfig);

        //referencing the new UserCodeNamespaceConfig in a map config
        config.getMapConfig("map1").setUserCodeNamespace("ucn1");

        HazelcastInstance member = Hazelcast.newHazelcastInstance(config);
        //putting key/value in the map that client can execute on
        member.getMap("map1").put("key", 0);
    }
}
