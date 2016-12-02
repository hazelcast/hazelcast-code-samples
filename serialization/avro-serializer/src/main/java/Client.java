import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.HazelcastInstance;
import hazelcast.avro.Person;

import java.util.Map;

public class Client {
    public static void main(String[] args) {
        final ClientConfig clientConfig = new ClientConfig();
        SerializerConfig serializationConfig = new SerializerConfig();
        serializationConfig.setImplementation(new PersonAvroSerializer());
        serializationConfig.setTypeClass(Person.class);
        clientConfig.getSerializationConfig().getSerializerConfigs().add(serializationConfig);

        final HazelcastInstance hazelcastClient = HazelcastClient.newHazelcastClient(clientConfig);
        Map<String, Person> map = hazelcastClient.getMap("map");
        Person person = map.get("Peter");
        System.out.println(person);

    }
}
