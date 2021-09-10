import com.hazelcast.config.CompactSerializationConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

/**
 * Demonstrates how to use Compact(BETA) serialization with java only setup
 * If all hazelcast clients and members are java, then you can use Compact format without any config.
 */
public class CompactZeroConfig {

    public static void main(String[] args) {
        Config config = new Config();
        //This config is needed only during BETA phase.
        CompactSerializationConfig compactSerializationConfig = config.getSerializationConfig().getCompactSerializationConfig();
        compactSerializationConfig.setEnabled(true);
        //If PersonDTO were implementing Java Serializable interface, then you would need to add the following config do
        // that the object is serialized using the Compact serialization. Otherwise, it defaults to Java serialization.
        // compactSerializationConfig.register(PersonDTO.class);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        Map<String, PersonDTO> map = hz.getMap("map");

        map.put("Peter", new PersonDTO("Peter", "Stone", 35));

        PersonDTO person = map.get("Peter");
        System.out.println(person);

        Hazelcast.shutdownAll();
    }
}
