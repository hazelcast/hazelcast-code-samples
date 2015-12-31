import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.NativeMemoryConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.memory.MemorySize;
import com.hazelcast.memory.MemoryUnit;

import java.util.Map;

import static com.hazelcast.config.MaxSizeConfig.MaxSizePolicy.PER_NODE;

public class HDEviction {

    private static final String LICENSE_KEY = "";
    private static final int MAX_ENTRY_COUNT = 1000;

    public static void main(String[] args) {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(newConfig());

        Map<String, String> map = hazelcastInstance.getMap("map");
        for (int i = 0; i < 10 * MAX_ENTRY_COUNT; i++) {
            map.put("key-" + i, "value-" + i);
        }

        System.out.println("Map size is " + map.size() + " and it is below the allowed max entry count " + MAX_ENTRY_COUNT);

        hazelcastInstance.shutdown();
    }

    public static Config newConfig() {
        MaxSizeConfig maxSizeConfig = new MaxSizeConfig();
        maxSizeConfig.setMaxSizePolicy(PER_NODE);
        maxSizeConfig.setSize(MAX_ENTRY_COUNT);

        MapConfig mapConfig = new MapConfig();
        mapConfig.setName("default");
        mapConfig.setInMemoryFormat(InMemoryFormat.NATIVE);
        mapConfig.setEvictionPolicy(EvictionPolicy.LRU);
        mapConfig.setMaxSizeConfig(maxSizeConfig);
        mapConfig.setMinEvictionCheckMillis(0);

        MemorySize memorySize = new MemorySize(128, MemoryUnit.MEGABYTES);
        NativeMemoryConfig memoryConfig = new NativeMemoryConfig();
        memoryConfig.setEnabled(true);
        memoryConfig.setSize(memorySize);
        memoryConfig.setAllocatorType(NativeMemoryConfig.MemoryAllocatorType.STANDARD);

        Config config = new Config();
        config.addMapConfig(mapConfig);
        config.setNativeMemoryConfig(memoryConfig);
        if (!LICENSE_KEY.isEmpty()) {
            config.setLicenseKey(LICENSE_KEY);
        }

        return config;
    }
}
