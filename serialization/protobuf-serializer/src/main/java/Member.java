import com.hazelcast.config.Config;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.config.XmlConfigBuilder;
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
        Config config = new Config();

        // configure hazelcast serializers to use provided serializer for protobuf
        final SerializerConfig personProtoSerializerConfig = new SerializerConfig();
        personProtoSerializerConfig.setTypeClass(Person.class);
        personProtoSerializerConfig.setImplementation(new PersonProtoSerializer());
        config.getSerializationConfig().addSerializerConfig(personProtoSerializerConfig);
        // end serialization config

        // uncomment following line if you wish to use xml config instead of programmatic
        //Config config = new XmlConfigBuilder().build();
        final HazelcastInstance hazelcastInstance = newHazelcastInstance(config);
        final IMap<Integer, Person> personsMap = hazelcastInstance.getMap("person");
        populate(personsMap);

        // test member-side serialization
        System.out.println("---- where email is support@hazelcast.com");
        System.out.println(personsMap.values(equal("email", "support@hazelcast.com")));

        System.out.println("---- where email is not support@hazelcast.com");
        System.out.println(personsMap.values(new SqlPredicate("email != support@hazelcast.com")));
    }

    private static void populate(IMap<Integer, Person> personsMap) {
        final Person person1 = Person.newBuilder().setId(1).setEmail("dude@hazelcast.com").setName("Dude").build();
        final Person person2 = Person.newBuilder().setId(2).setEmail("sales@hazelcast.com").setName("John Appleseed").build();
        final Person person3 = Person.newBuilder().setId(3).setEmail("support@hazelcast.com").setName("John Support").build();
        personsMap.set(person1.getId(), person1);
        personsMap.set(person2.getId(), person2);
        personsMap.set(person3.getId(), person3);
    }
}
