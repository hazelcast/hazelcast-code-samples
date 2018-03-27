package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

import java.io.IOException;

public class IdentifiedDataSerializableSample {

    public static class Employee implements IdentifiedDataSerializable {

        private static final int CLASS_ID = 100;

        public int id;
        public String name;

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            id = in.readInt();
            name = in.readUTF();
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeInt(id);
            out.writeUTF(name);
        }

        @Override
        public int getFactoryId() {
            return SampleDataSerializableFactory.FACTORY_ID;
        }

        @Override
        public int getId() {
            return CLASS_ID;
        }
    }

    public static class SampleDataSerializableFactory implements DataSerializableFactory {
        public static final int FACTORY_ID = 1000;

        @Override
        public IdentifiedDataSerializable create(int typeId) {
            if (typeId == 100) {
                return new Employee();
            }
            return null;
        }
    }

    public static void main(String[] args) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getSerializationConfig()
                .addDataSerializableFactory(SampleDataSerializableFactory.FACTORY_ID,
                        new SampleDataSerializableFactory());
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient(clientConfig);
        //Employee can be used here
        hz.shutdown();
    }
}
