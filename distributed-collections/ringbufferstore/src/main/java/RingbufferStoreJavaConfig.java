import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.RingbufferConfig;
import com.hazelcast.config.RingbufferStoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import static com.hazelcast.config.InMemoryFormat.BINARY;
import static com.hazelcast.config.InMemoryFormat.OBJECT;

public class RingbufferStoreJavaConfig {

    public static void main(String[] args) throws Exception {
        Config config = new Config()
                .addRingBufferConfig(getConfig("object-ringbuffer", OBJECT, TheRingbufferObjectStore.class))
                .addRingBufferConfig(getConfig("binary-ringbuffer", BINARY, TheRingbufferBinaryStore.class));

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);

        hz.getRingbuffer("object-ringbuffer").add(new Item());
        hz.getRingbuffer("binary-ringbuffer").add(new Item());

        System.exit(0);
    }

    private static RingbufferConfig getConfig(String name, InMemoryFormat format, Class ringbufferStoreClass) {
        RingbufferStoreConfig objectStoreConfig = new RingbufferStoreConfig()
                .setClassName(ringbufferStoreClass.getName());

        return new RingbufferConfig(name)
                .setRingbufferStoreConfig(objectStoreConfig)
                .setInMemoryFormat(format);
    }
}
