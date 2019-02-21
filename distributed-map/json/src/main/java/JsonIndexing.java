import com.hazelcast.config.Config;
import com.hazelcast.config.MapIndexConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicates;

public class JsonIndexing {

    private static final int NUMBER_OF_ENTRIES = 400000;

    public static void main(String[] args) {

        Config config = new Config();
        config.getMapConfig("mapWithIndex").addMapIndexConfig(new MapIndexConfig("id", false));
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        IMap<Integer, HazelcastJsonValue> mapWithoutIndex = instance.getMap("mapWithoutIndex");
        IMap<Integer, HazelcastJsonValue> mapWithIndex = instance.getMap("mapWithIndex");

        for (int i = 0; i < NUMBER_OF_ENTRIES; i++) {
            mapWithIndex.put(i, new HazelcastJsonValue(generateJsonObject(i)));
            mapWithoutIndex.put(i, new HazelcastJsonValue(generateJsonObject(i)));
        }

        long beforeTimestamp = System.currentTimeMillis();
        mapWithoutIndex.keySet(Predicates.equal("id", 1));
        long elapsed = System.currentTimeMillis() - beforeTimestamp;

        System.out.println("Took " + elapsed + " ms to run the query without indexes.");

        beforeTimestamp = System.currentTimeMillis();
        mapWithIndex.keySet(Predicates.equal("id", 1));
        elapsed = System.currentTimeMillis() - beforeTimestamp;

        System.out.println("Took " + elapsed + " ms to run the query with indexes.");

        instance.shutdown();
    }

    private static String generateJsonObject(int id) {
        return "{ \"id\": " + id + "}";
    }
}
