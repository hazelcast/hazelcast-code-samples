package member;

import com.hazelcast.config.Config;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.IOException;

public class CustomSerializerSample {

    static class CustomSerializable {
        public String value;

        CustomSerializable(String value) {
            this.value = value;
        }
    }

    static class CustomSerializer implements StreamSerializer<CustomSerializable> {

        @Override
        public int getTypeId() {
            return 10;
        }

        @Override
        public void destroy() {

        }

        @Override
        public void write(ObjectDataOutput out, CustomSerializable object) throws IOException {
            byte[] bytes = object.value.getBytes("utf8");
            out.writeInt(bytes.length);
            out.write(bytes);
        }

        @Override
        public CustomSerializable read(ObjectDataInput in) throws IOException {
            int len = in.readInt();
            byte[] bytes = new byte[len];
            in.readFully(bytes);
            return new CustomSerializable(new String(bytes, "utf8"));
        }
    }

    public static void main(String[] args) {
        Config config = new Config();
        config.getSerializationConfig().addSerializerConfig(new SerializerConfig()
                .setImplementation(new CustomSerializer())
                .setTypeClass(CustomSerializable.class));

        // Start the Embedded Hazelcast Cluster Member.
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        //CustomSerializer will serialize/deserialize CustomSerializable objects
        hz.shutdown();
    }
}
