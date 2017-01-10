package com.hazelcast.pcf.integration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class Application {

    private static ClientConfig clientConfig;

    @Bean
    public HazelcastInstance hazelcastClient() {
        return HazelcastClient.newHazelcastClient(clientConfig);
    }

    public static void main(String[] args) {
        String servicesJson = System.getenv("VCAP_SERVICES");
        if (servicesJson == null || servicesJson.isEmpty()) {
            System.err.println("No service found!!!");
            return;
        }
        BasicJsonParser parser = new BasicJsonParser();
        Map<String, Object> json = parser.parseMap(servicesJson);
        List hazelcast = (List) json.get("hazelcast");
        Map map = (Map) hazelcast.get(0);
        Map credentials = (Map) map.get("credentials");
        String groupName = (String) credentials.get("group_name");
        String groupPassword = (String) credentials.get("group_pass");
        List<String> members = (List<String>) credentials.get("members");

        clientConfig = new ClientConfig();
        GroupConfig groupConfig = clientConfig.getGroupConfig();
        groupConfig.setName(groupName).setPassword(groupPassword);
        ClientNetworkConfig networkConfig = clientConfig.getNetworkConfig();
        for (String member : members) {
            networkConfig.addAddress(member.replace('"', ' ').trim());
        }
        SpringApplication.run(Application.class, args);
    }
}
