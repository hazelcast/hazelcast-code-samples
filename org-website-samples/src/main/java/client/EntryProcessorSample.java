package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientUserCodeDeploymentConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.AbstractEntryProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public class EntryProcessorSample {

    public static class IncEntryProcessor extends AbstractEntryProcessor<String, Integer> implements IdentifiedDataSerializable {
        public static final int FACTORY_ID = 1;
        public static final int CLASS_ID = 9;

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {

        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {

        }

        @Override
        public int getFactoryId() {
            return FACTORY_ID;
        }

        @Override
        public int getId() {
            return CLASS_ID;
        }

        @Override
        public Object process(Map.Entry<String, Integer> entry) {
            // Get the value passed
            int oldValue = entry.getValue();
            // Update the value
            int newValue = oldValue + 1;
            // Update the value back to the entry stored in the Hazelcast Member this EntryProcessor is running on.
            entry.setValue(newValue);
            // No need to return anything back to the caller, we can return whatever we like here.
            return null;
        }
    }

    public static void main(String[] args) {
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        // Get the Distributed Map from Cluster.
        IMap<String, Integer> map = hz.getMap("my-distributed-map");
        // Put the integer value of 0 into the Distributed Map
        Integer replacedValue = map.put("key", 0);
        // Run the IncEntryProcessor class on the Hazelcast Cluster Member holding the key called "key"
        Object returnValueFromIncEntryProcessor = map.executeOnKey("key", new IncEntryProcessor());
        // Show that the IncEntryProcessor updated the value.
        System.out.println("new value:" + map.get("key"));
        // Shutdown the Hazelcast Cluster Member
        hz.shutdown();
    }
}