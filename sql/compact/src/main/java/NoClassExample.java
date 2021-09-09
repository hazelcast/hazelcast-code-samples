import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.nio.serialization.GenericRecord;
import com.hazelcast.sql.SqlResult;
import com.hazelcast.sql.SqlRow;
import com.hazelcast.sql.SqlService;

import java.util.Map;
import java.util.Set;

/**
 * Demonstrates usage of Compact(BETA) format with SQL Service without an existing DTO class
 */
public class NoClassExample {

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
        config.getJetConfig().setEnabled(true);
        config.getSerializationConfig().getCompactSerializationConfig().setEnabled(true);
        HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(config);
        IMap<Integer, GenericRecord> myMap = hazelcast.getMap("myMap");

        SqlService sqlService = hazelcast.getSql();

        sqlService.execute("CREATE MAPPING myMap ( "
                + "__key INT , "
                + "name VARCHAR ,"
                + "surname VARCHAR,"
                + "id INT ) "
                + "TYPE IMap "
                + "OPTIONS ("
                + "    'keyFormat' = 'int', "
                + "    'valueFormat' = 'compact', "
                + "    'valueCompactTypeName' = 'person' )");

        sqlService.execute("INSERT INTO myMap (__key, name, surname, id) VALUES (1, ?, ?, 1)", "John", "Stone");

        //Query map with sql
        SqlResult sqlRows = sqlService.execute("SELECT * FROM myMap");
        for (SqlRow sqlRow : sqlRows) {
            System.out.println(sqlRow);
        }

        //read all entries from the map
        //Since we don't have the class in our local, GenericRecord is returned instead
        Set<Map.Entry<Integer, GenericRecord>> entries = myMap.entrySet();
        for (Map.Entry<Integer, GenericRecord> entry : entries) {
            System.out.println(entry);
        }

        hazelcast.shutdown();
    }
}
