import com.hazelcast.config.Config;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;

import static com.hazelcast.core.Hazelcast.newHazelcastInstance;
import static com.hazelcast.query.Predicates.equal;
import static com.hazelcast.serialization.PersonProtos.Person;

/**
 * Hazelcast Member with custom serialization registration
 *
 * @author Viktor Gamov on 5/12/17.
 *         Twitter: @gamussa
 */
public class Member {

    public static void main(String[] args) {
        // configure hazelcast serializers to use provided serializer for protobuf
        SerializerConfig personProtoSerializerConfig = new SerializerConfig()
                .setTypeClass(Person.class)
                .setImplementation(new PersonProtoSerializer());

        Config config = new Config();
        config.getSerializationConfig().addSerializerConfig(personProtoSerializerConfig);

        // uncomment the following line if you wish to use XML config instead of programmatic configuration
        // config = new com.hazelcast.config.XmlConfigBuilder().build();

        HazelcastInstance hazelcastInstance = newHazelcastInstance(config);
        IMap<Integer, Person> personsMap = hazelcastInstance.getMap("person");
        populate(personsMap);

        // test member-side serialization
        System.out.println("---- where email is support@hazelcast.com");
        System.out.println(personsMap.values(equal("email", "support@hazelcast.com")));

        System.out.println("---- where email is not support@hazelcast.com");
        System.out.println(personsMap.values(new SqlPredicate("email != support@hazelcast.com")));
    }

    private static void populate(IMap<Integer, Person> personsMap) {
        Person person1 = Person.newBuilder().setId(1).setEmail("dude@hazelcast.com").setName("Dude").build();
        Person person2 = Person.newBuilder().setId(2).setEmail("sales@hazelcast.com").setName("John Appleseed").build();
        Person person3 = Person.newBuilder().setId(3).setEmail("support@hazelcast.com").setName("John Support").build();

        personsMap.set(person1.getId(), person1);
        personsMap.set(person2.getId(), person2);
        personsMap.set(person3.getId(), person3);
    }
}
