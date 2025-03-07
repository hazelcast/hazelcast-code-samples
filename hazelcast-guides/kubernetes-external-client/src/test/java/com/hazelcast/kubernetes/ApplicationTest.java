package com.hazelcast.kubernetes;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.KubernetesConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

class ApplicationTest {


    @Test
    void shouldConnectToHazelcastCluster() {
        String masterIp = System.getenv("KUBERNETES_MASTER").replaceAll("\u001B\\[[;\\d]*m", "");
        ClientConfig config = new ClientConfig();
        config.setInstanceName("dev");
        KubernetesConfig kubernetesConfig = new KubernetesConfig();
        kubernetesConfig.setEnabled(true);
        kubernetesConfig.setProperty("kubernetes-master", masterIp);
        kubernetesConfig.setProperty("namespace", "default");
        kubernetesConfig.setProperty("api-token", System.getenv("API_TOKEN"));
        kubernetesConfig.setProperty("ca-certificate", System.getenv("CA_CERTIFICATE"));
        kubernetesConfig.setUsePublicIp(true);
        ClientNetworkConfig networkConfig = new ClientNetworkConfig().setKubernetesConfig(kubernetesConfig);
        config.setNetworkConfig(networkConfig);

        HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient(config);
        IMap<String, String> map = hazelcastInstance.getMap("map");
        map.put("key", "value");

        assertEquals("value", map.get("key"));
    }
}