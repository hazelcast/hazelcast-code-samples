package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import java.io.IOException;
import java.util.Date;

public class PortableSerializableSample {
    public static class Customer implements Portable {
        public static final int CLASS_ID = 1;

        public String name;
        public int id;
        public Date lastOrder;

        @Override
        public int getFactoryId() {
            return SamplePortableFactory.FACTORY_ID;
        }

        @Override
        public int getClassId() {
            return CLASS_ID;
        }

        @Override
        public void writePortable(PortableWriter writer) throws IOException {
            writer.writeInt("id", id);
            writer.writeUTF("name", name);
            writer.writeLong("lastOrder", lastOrder.getTime());
        }

        @Override
        public void readPortable(PortableReader reader) throws IOException {
            id = reader.readInt("id");
            name = reader.readUTF("name");
            lastOrder = new Date(reader.readLong("lastOrder"));
        }
    }

    public static class SamplePortableFactory implements PortableFactory {

        public static final int FACTORY_ID = 1;

        @Override
        public Portable create(int classId) {
            if (classId == Customer.CLASS_ID) {
                return new Customer();
            }
            return null;
        }
    }

    public static void main(String[] args) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getSerializationConfig()
                .addPortableFactory(SamplePortableFactory.FACTORY_ID, new SamplePortableFactory());
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient(clientConfig);
        //Customer can be used here
        hz.shutdown();
    }
}
