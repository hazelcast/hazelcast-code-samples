package member;

import com.hazelcast.config.Config;
import com.hazelcast.config.GlobalSerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.IOException;

public class GlobalSerializerSample {
    static class GlobalSerializer implements StreamSerializer<Object> {

        @Override
        public int getTypeId() {
            return 20;
        }

        @Override
        public void destroy() {

        }

        @Override
        public void write(ObjectDataOutput out, Object object) throws IOException {
            // out.write(MyFavoriteSerializer.serialize(object))
        }

        @Override
        public Object read(ObjectDataInput in) throws IOException {
            // return MyFavoriteSerializer.deserialize(in);
            return null;
        }
    }

    public static void main(String[] args) {
        Config config = new Config();
        config.getSerializationConfig().setGlobalSerializerConfig(
                new GlobalSerializerConfig().setImplementation(new GlobalSerializer())
        );
        // Start the Embedded Hazelcast Cluster Member.
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        //GlobalSerializer will serialize/deserialize all non-builtin types
        hz.shutdown();
    }
}
