import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NativeMemoryConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.memory.MemorySize;
import com.hazelcast.memory.MemoryUnit;

import java.util.Map;

public class HDSimplePopulation {

    private static final String LICENSE_KEY = "";

    public static void main(String[] args) {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(newConfig());

        Map<String, String> map = hazelcastInstance.getMap("map");
        map.put("1", "Tokyo");
        map.put("2", "Paris");
        map.put("3", "New York");

        System.out.println("Inserted " + map.size() + " entries into maps hd memory");

        hazelcastInstance.shutdown();
    }

    public static Config newConfig() {
        MapConfig mapConfig = new MapConfig();
        mapConfig.setName("default");
        mapConfig.setInMemoryFormat(InMemoryFormat.NATIVE);

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
