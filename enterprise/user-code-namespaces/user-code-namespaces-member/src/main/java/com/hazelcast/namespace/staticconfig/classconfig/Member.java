package com.hazelcast.namespace.staticconfig.classconfig;

import com.hazelcast.config.Config;
import com.hazelcast.config.UserCodeNamespaceConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class Member {

    public static void main(String[] args) {
        Config config = new Config();
        config.getNamespacesConfig().setEnabled(true);

        // construct a namespace configuration
        UserCodeNamespaceConfig userCodeNamespaceConfig = new UserCodeNamespaceConfig();
        userCodeNamespaceConfig.setName("ucn1");
        userCodeNamespaceConfig.addClass(loadClass("usercodenamespaces.IncrementingEntryProcessor"));
        config.getNamespacesConfig().addNamespaceConfig(userCodeNamespaceConfig);

        // referencing the new UserCodeNamespaceConfig in a map config
        config.getMapConfig("map1").setUserCodeNamespace("ucn1");

        HazelcastInstance member = Hazelcast.newHazelcastInstance(config);
        // putting key/value in the map that client can execute on
        member.getMap("map1").put("key", 0);
    }

    /**
     * Helper code to load the class from outside the class path.
     * @return the Class
     */
    private static Class loadClass(String className) {
        String basePath = System.getProperty("user.dir");
        String directoryPath = basePath + "/src/main/class/";

        File directory = new File(directoryPath);
        try {
            URL url = directory.toURI().toURL();
            ClassLoader parentClassLoader = Member.class.getClassLoader();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{url}, parentClassLoader);
            return classLoader.loadClass(className);
        } catch (MalformedURLException | ClassNotFoundException e) {
            throw new RuntimeException("Error loading class", e);
        }
    }
}
