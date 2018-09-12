package com.hazelcast.pcf.integration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.SSLConfig;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.context.annotation.Bean;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@SpringBootApplication
public class Application {

    // TODO provide valid license
    public static final String LICENSE_KEY = "YOUR_LICENSE_KEY";
    private static ClientConfig clientConfig;
    private static File tsFile;
    private static final String PASSWORD = "123456";

    @Bean
    public HazelcastInstance hazelcastClient() {
        Properties clientSslProps = new Properties();
        clientSslProps.setProperty("trustStore", tsFile.getAbsolutePath());
        clientSslProps.setProperty("trustStorePassword", PASSWORD);
        clientConfig.getNetworkConfig().setSSLConfig(new SSLConfig().setEnabled(true).setProperties(clientSslProps));
        clientConfig.setLicenseKey(LICENSE_KEY);
        return HazelcastClient.newHazelcastClient(clientConfig);
    }

    public static void extractKSTS() throws IOException {
        System.out.println("extracting truststore");
        InputStream tsStream = Application.class.getResourceAsStream("/truststore");
        tsFile = copyToTemp(tsStream, "truststore");
    }

    private static File copyToTemp(InputStream stream, String name) throws IOException {
        File f = File.createTempFile(name, ".dat");
        System.out.println("writing to " + f.getAbsolutePath());
        FileOutputStream fos = new FileOutputStream(f);
        FileCopyUtils.copy(stream, fos);
        fos.close();
        return f;
    }

    public static void main(String[] args) throws IOException {
        extractKSTS();
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
