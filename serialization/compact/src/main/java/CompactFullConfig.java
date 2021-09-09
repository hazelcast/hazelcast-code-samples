import com.hazelcast.config.CompactSerializationConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;

import java.util.Map;

/**
 * Demonstrates how to use Compact(BETA) serialization from java in a polyglot setup where hazelcast clients written in
 * different languages needs to be used.
 */
public class CompactFullConfig {

    public static void main(String[] args) {
        Config config = new Config();
        //This config is needed only during BETA phase.
        CompactSerializationConfig compactSerializationConfig = config.getSerializationConfig().getCompactSerializationConfig();
        compactSerializationConfig.setEnabled(true);
        //Here we register a typename and a serializer against the PersonDTO class so that any client from any language
        //can use same typename abd field names to match
        compactSerializationConfig.register(PersonDTO.class, "person", new CompactSerializer<PersonDTO>() {
            @Override
            public PersonDTO read(CompactReader compactReader) {
                int age = compactReader.readInt("age");
                String name = compactReader.readString("name");
                String surname = compactReader.readString("surname");
                return new PersonDTO(name, surname, age);
            }

            @Override
            public void write(CompactWriter compactWriter, PersonDTO personDTO) {
                compactWriter.writeInt("age", personDTO.getAge());
                compactWriter.writeString("name", personDTO.getName());
                compactWriter.writeString("surname", personDTO.getSurname());
            }
        });
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        Map<String, PersonDTO> map = hz.getMap("map");

        map.put("Peter", new PersonDTO("Peter", "Stone", 45));

        PersonDTO person = map.get("Peter");
        System.out.println(person);

        Hazelcast.shutdownAll();
    }
}
