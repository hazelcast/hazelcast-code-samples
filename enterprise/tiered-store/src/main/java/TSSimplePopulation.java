import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.LocalDeviceConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NativeMemoryConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.memory.Capacity;
import com.hazelcast.memory.MemoryUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static com.hazelcast.config.NativeMemoryConfig.MemoryAllocatorType.POOLED;
import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;
import static com.hazelcast.memory.MemoryUnit.GIGABYTES;

/**
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 * <p>
 * The sample shows how to configure an IMap backed by tiered store, and that it can be populated
 * and further used the same as HD IMap.
 */
public class TSSimplePopulation {

    public static void main(String[] args) {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(newConfig());

        Map<String, String> map = hazelcastInstance.getMap("map");
        map.put("1", "Tokyo");
        map.put("2", "Paris");
        map.put("3", "New York");

        System.out.println("Inserted " + map.size() + " entries into map backed by tiered store");

        hazelcastInstance.shutdown();
    }

    private static Config newConfig() {
        // Configure pooled memory
        Capacity memoryCapacity = Capacity.of(1, GIGABYTES);
        NativeMemoryConfig memoryConfig = new NativeMemoryConfig();
        memoryConfig.setEnabled(true);
        memoryConfig.setCapacity(memoryCapacity);
        memoryConfig.setAllocatorType(POOLED);

        // Create device config
        String deviceName = "local-device";
        Capacity diskCapacity = Capacity.of(128, GIGABYTES);
        LocalDeviceConfig localDeviceConfig = new LocalDeviceConfig()
            .setName("local-device")
            .setCapacity(diskCapacity)
            .setBaseDir(createNewFolder(deviceName));

        Config config = new Config();
        config.setNativeMemoryConfig(memoryConfig);
        config.setLicenseKey(ENTERPRISE_LICENSE_KEY);
        config.addDeviceConfig(localDeviceConfig);

        // Configure map backed by tiered store
        MapConfig mapConfig = new MapConfig();
        mapConfig.setName("default");
        mapConfig.setInMemoryFormat(InMemoryFormat.NATIVE);

        // Configure disk tier
        mapConfig.getTieredStoreConfig()
            .setEnabled(true)
            .getDiskTierConfig()
            .setEnabled(true)
            .setDeviceName(deviceName);

        // Configure memory tier
        mapConfig.getTieredStoreConfig().getMemoryTierConfig()
            .setCapacity(Capacity.of(8, MemoryUnit.MEGABYTES));

        config.addMapConfig(mapConfig);

        return config;
    }

    private static File createNewFolder(String deviceName) {
        try {
            String path = Paths.get(deviceName).toString();

            Path tempDirectory = Files.createTempDirectory(path);
            File file = tempDirectory.toFile();
            file.deleteOnExit();
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
