import com.hazelcast.config.CompactSerializationConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.sql.SqlResult;
import com.hazelcast.sql.SqlRow;
import com.hazelcast.sql.SqlService;

import java.util.Map;
import java.util.Set;

/**
 * Demonstrates usage of Compact(BETA) format with SQL Service with an existing DTO class
 */
public class WithExistingClassExample {

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
        CompactSerializationConfig compactSerializationConfig = config.getSerializationConfig().getCompactSerializationConfig();
        compactSerializationConfig.setEnabled(true);
        config.getJetConfig().setEnabled(true);
        HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(config);

        IMap<Integer, Person> myMap = hazelcast.getMap("myMap");
        myMap.put(1, new Person(1, "John", "Stone"));

        SqlService sqlService = hazelcast.getSql();

        sqlService.execute("CREATE MAPPING myMap ( "
                + "__key INT, "
                + "name VARCHAR ,"
                + "surname VARCHAR,"
                + "id INT ) "
                + "TYPE IMap "
                + "OPTIONS ("
                + "    'keyFormat'='int', "
                + "    'valueFormat'='compact', "
                + "    'valueCompactTypeName'='" + Person.class.getName() + "' ) ");

        sqlService.execute("INSERT INTO myMap (__key, name, surname, id) VALUES (2, ?, ?, 2)", "Jack", "Sparrow");

        //Query map with sql
        SqlResult sqlRows = sqlService.execute("SELECT * FROM myMap WHERE id = 2");
        for (SqlRow sqlRow : sqlRows) {
            System.out.println(sqlRow);
        }

        //read all entries from the map
        Set<Map.Entry<Integer, Person>> entries = myMap.entrySet();
        for (Map.Entry<Integer, Person> entry : entries) {
            System.out.println(entry);
        }

        hazelcast.shutdown();
    }
}
