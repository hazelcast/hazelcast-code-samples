package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.SerializerConfig;
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
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getSerializationConfig().addSerializerConfig(new SerializerConfig()
                .setImplementation(new CustomSerializer())
                .setTypeClass(CustomSerializable.class));

        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient(clientConfig);
        //CustomSerializer will serialize/deserialize CustomSerializable objects
        hz.shutdown();
    }
}
