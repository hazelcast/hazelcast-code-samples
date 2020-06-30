package example;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.GenericRecord;

import java.io.IOException;
import java.util.Collection;

import static com.hazelcast.query.Predicates.sql;

public class ClientWithoutDomainObjectExample {

    public static void main(String[] args) throws IOException {
        ClassDefinition classDefinition = new ClassDefinitionBuilder(1, 2).addUTFField("name").addIntField("age").build();

        ClientConfig clientConfig = new ClientConfig();
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

        IMap<Integer, Object> map = client.getMap("products");

        GenericRecord wally = GenericRecord.Builder.portable(classDefinition)
                .writeUTF("name", "Wally")
                .writeInt("age", 30).build();

        map.put(1, wally);

        GenericRecord kermit = GenericRecord.Builder.portable(classDefinition)
                .writeUTF("name", "Kermit The Frog")
                .writeInt("age", 12).build();

        map.put(2, kermit);

        Collection<Object> values = map.values(sql("age > 10"));

        //prints
        // Kermit The Frog
        // Wally
        for (Object value : values) {
            GenericRecord record = (GenericRecord) value;
            System.out.println(record.readUTF("name"));
        }

    }
}
