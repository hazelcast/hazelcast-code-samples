import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.config.YamlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class ConfigLookup {
    public static void main(String[] args) {
        // XML takes precedence: loading member configuration hazelcast.xml from the classpath
        // despite the presence of hazelcast.yaml
        HazelcastInstance member = Hazelcast.newHazelcastInstance();
        // the member uses port 6000 as configured in hazelcast.xml
        member.shutdown();

        // loading configuration from hazelcast.yaml from the classpath without looking for XML files
        // by using YAML-specific config builder
        Config config = new YamlConfigBuilder().build();
        // the member uses port 7000 as configured in hazelcast-network-config.yaml
        member = Hazelcast.newHazelcastInstance(config);

        // loading the client configuration from hazelcast-client.yaml since no hazelcast-client.xml
        // is present on the classpath
        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        // the client is connected to the member running on port 7000

        client.shutdown();
        member.shutdown();
    }

}
