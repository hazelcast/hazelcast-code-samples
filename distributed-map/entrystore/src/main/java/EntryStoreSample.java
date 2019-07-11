import com.hazelcast.config.Config;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.concurrent.TimeUnit;

public class EntryStoreSample {

    public static void main(String[] args) throws InterruptedException {

        DummyDatabaseConnection dummyDatabaseConnection = new DummyDatabaseConnection();
        Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        MapStoreConfig mapStoreConfig = new MapStoreConfig();
        mapStoreConfig
                .setEnabled(true)
                .setImplementation(new DummyEntryStore(dummyDatabaseConnection));
        config.getMapConfig("db-backed-map").setMapStoreConfig(mapStoreConfig);

        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(config);

        IMap<String, String> dbBackedMap = instance1.getMap("db-backed-map");


        System.out.println("Putting an entry with one minute time to live seconds.\n"
                + "map.put(\"one-minute-key\", \"one-minute-value\", 1, TimeUnit.MINUTES)");
        dbBackedMap.put("one-minute-key", "one-minute-value", 1, TimeUnit.MINUTES);
        System.out.println("Shutting down the only hazelcast instance. In-memory data will be lost");
        instance1.shutdown();
        System.out.println("Instance is shutdown");

        System.out.println("Starting a new Hazelcast instance.");
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(config);
        IMap<String, String> dbBackedMap2 = instance2.getMap("db-backed-map");

        System.out.println("map.get(\"one-minute-key\"):");
        System.out.println(dbBackedMap2.get("one-minute-key"));
        System.out.println("This value was loaded by entry loader. Its initial expiration time still applies.");
        System.out.println("Waiting 60 seconds for the entry to expire.");

        // wait 60 seconds for key to expire
        Thread.sleep(60000);

        System.out.println("map.get(\"one-minute-key\"):");
        System.out.println(dbBackedMap2.get("key"));


    }
}
