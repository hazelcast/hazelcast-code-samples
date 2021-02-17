package example;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.GenericRecord;
import com.hazelcast.nio.serialization.GenericRecordBuilder;

import java.util.Collection;

import static com.hazelcast.query.Predicates.sql;

public class ClientWithoutDomainObjectExample {

    public static void main(String[] args) {
        ClassDefinition classDefinition = new ClassDefinitionBuilder(1, 2)
                .addStringField("name")
                .addIntField("age")
                .build();

        ClientConfig clientConfig = new ClientConfig();
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

        IMap<Integer, Object> map = client.getMap("products");

        GenericRecord wally = GenericRecordBuilder.portable(classDefinition)
                .setString("name", "Wally")
                .setInt("age", 30)
                .build();

        map.put(1, wally);

        GenericRecord kermit = GenericRecordBuilder.portable(classDefinition)
                .setString("name", "Kermit The Frog")
                .setInt("age", 12)
                .build();

        map.put(2, kermit);

        Collection<Object> values = map.values(sql("age > 10"));

        // prints
        // Kermit The Frog
        // Wally
        for (Object value : values) {
            GenericRecord record = (GenericRecord) value;
            System.out.println(record.getString("name"));
        }

        client.shutdown();
    }
}
