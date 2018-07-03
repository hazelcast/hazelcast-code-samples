package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.GlobalSerializerConfig;
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
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getSerializationConfig().setGlobalSerializerConfig(
                new GlobalSerializerConfig().setImplementation(new GlobalSerializer())
        );
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient(clientConfig);
        //GlobalSerializer will serialize/deserialize all non-builtin types
        hz.shutdown();
    }
}
