package com.hazelcast.namespace.staticconfig.jar;

import com.hazelcast.config.Config;
import com.hazelcast.config.UserCodeNamespaceConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.io.File;
import java.net.URL;

public class Member {

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.getNamespacesConfig().setEnabled(true);

        //construct a namespace configuration
        UserCodeNamespaceConfig userCodeNamespaceConfig = new UserCodeNamespaceConfig();
        userCodeNamespaceConfig.setName("ucn1");
        userCodeNamespaceConfig.addJar(getJarURL(), "jar_id");
        config.getNamespacesConfig().addNamespaceConfig(userCodeNamespaceConfig);

        //referencing the new UserCodeNamespaceConfig in a map config
        config.getMapConfig("map1").setUserCodeNamespace("ucn1");

        HazelcastInstance member = Hazelcast.newHazelcastInstance(config);
        //putting key/value in the map that client can execute on
        member.getMap("map1").put("key", 0);
    }


    /**
     * Helper code to load the example jar from outside the classpath
     * @return the URL of the jar.
     */
    private static URL getJarURL() throws Exception {
        String basePath = System.getProperty("user.dir");
        String directoryPath = basePath + "/src/main/class/usercodenamespaces/";

        File jar = new File(directoryPath + "IncrementingEntryProcessor.jar");
        return jar.toURI().toURL();
    }
}
