package nearcache;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NativeMemoryConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.memory.MemorySize;

import static com.hazelcast.config.EvictionConfig.MaxSizePolicy.USED_NATIVE_MEMORY_PERCENTAGE;
import static com.hazelcast.config.InMemoryFormat.NATIVE;
import static com.hazelcast.config.NativeMemoryConfig.MemoryAllocatorType.STANDARD;
import static com.hazelcast.memory.MemoryUnit.MEGABYTES;

public class ServerHDNearCache {

    private static final String LICENSE_KEY = "";

    public static void main(String[] args) {
        HazelcastInstance node = Hazelcast.newHazelcastInstance(newConfig());
        IMap<String, String> map = node.getMap("default");
        for (int i = 0; i < 1000; i++) {
            map.put("key-" + i, "value-" + i);
        }

        // first `get` puts remote entry into near-cache
        for (int i = 0; i < 1000; i++) {
            map.get("key-" + i);
        }

        long ownedEntryCount = map.getLocalMapStats().getNearCacheStats().getOwnedEntryCount();
        System.out.println("Near cache includes " + ownedEntryCount + " entries");

        node.shutdown();
    }

    private static Config newConfig() {
        NearCacheConfig nearCacheConfig = new NearCacheConfig();
        EvictionConfig evictionConfig = nearCacheConfig.getEvictionConfig();
        evictionConfig.setMaximumSizePolicy(USED_NATIVE_MEMORY_PERCENTAGE);
        evictionConfig.setSize(90);
        nearCacheConfig.setInMemoryFormat(NATIVE);
        nearCacheConfig.setInvalidateOnChange(true);
        nearCacheConfig.setCacheLocalEntries(true);
        nearCacheConfig.setName("default");

        MapConfig mapConfig = new MapConfig();
        mapConfig.setName("default");
        mapConfig.setNearCacheConfig(nearCacheConfig);

        NativeMemoryConfig memoryConfig = new NativeMemoryConfig();
        memoryConfig.setEnabled(true);
        memoryConfig.setSize(new MemorySize(128, MEGABYTES));
        memoryConfig.setAllocatorType(STANDARD);

        Config config = new Config();
        config.addMapConfig(mapConfig);
        config.setNativeMemoryConfig(memoryConfig);
        if (!LICENSE_KEY.isEmpty()) {
            config.setLicenseKey(LICENSE_KEY);
        }

        return config;
    }
}
