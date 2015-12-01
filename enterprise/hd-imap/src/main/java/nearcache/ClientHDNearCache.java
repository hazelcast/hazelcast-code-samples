package nearcache;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.EvictionConfig;
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

public class ClientHDNearCache {

    public static void main(String[] args) {
        // start server
        HazelcastInstance server = Hazelcast.newHazelcastInstance();

        // start client
        HazelcastInstance client = HazelcastClient.newHazelcastClient(newClientConfig("mapName"));

        IMap<String, String> map = client.getMap("mapName");
        for (int i = 0; i < 1000; i++) {
            map.put("key-" + i, "value-" + i);
        }

        // first `get` puts remote entry into near-cache
        for (int i = 0; i < 1000; i++) {
            map.get(i);
        }

        long ownedEntryCount = map.getLocalMapStats().getNearCacheStats().getOwnedEntryCount();
        System.out.println("Near cache includes " + ownedEntryCount + " entries");

        client.shutdown();
        server.shutdown();

    }

    private static ClientConfig newClientConfig(String mapName) {
        NativeMemoryConfig memoryConfig = new NativeMemoryConfig();
        memoryConfig.setEnabled(true);
        memoryConfig.setSize(new MemorySize(128, MEGABYTES));
        memoryConfig.setAllocatorType(STANDARD);

        NearCacheConfig nearCacheConfig = new NearCacheConfig();
        EvictionConfig evictionConfig = nearCacheConfig.getEvictionConfig();
        evictionConfig.setMaximumSizePolicy(USED_NATIVE_MEMORY_PERCENTAGE);
        evictionConfig.setSize(90);
        nearCacheConfig.setInMemoryFormat(NATIVE);
        nearCacheConfig.setInvalidateOnChange(true);
        nearCacheConfig.setCacheLocalEntries(true);
        nearCacheConfig.setName(mapName);

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setNativeMemoryConfig(memoryConfig);
        clientConfig.addNearCacheConfig(nearCacheConfig);
        return clientConfig;
    }
}
