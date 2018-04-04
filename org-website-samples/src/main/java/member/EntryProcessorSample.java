package member;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.AbstractEntryProcessor;

import java.util.Map;

public class EntryProcessorSample {
    public static class IncEntryProcessor extends AbstractEntryProcessor<String, Integer> {
        @Override
        public Object process(Map.Entry<String, Integer> entry) {
            // Get the value passed
            int oldValue = entry.getValue();
            // Update the value
            int newValue = oldValue + 1;
            // Update the value back to the entry stored in the Hazelcast Member this EntryProcessor is running on.
            entry.setValue(newValue);
            // No need to return anything back to the caller
            return null;
        }
    }

    public static void main(String[] args) {
        // Start the Embedded Hazelcast Cluster Member. No Config object is passed so using defaults.
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        // Get the Distributed Map from Cluster.
        IMap<String, Integer> map = hz.getMap("my-distributed-map");
        // Put the integer value of 0 into the Distributed Map
        map.put("key", 0);
        // Run the IncEntryProcessor class on the Hazelcast Cluster Member holding the key called "key"
        map.executeOnKey("key", new IncEntryProcessor());
        // Show that the IncEntryProcessor updated the value.
        System.out.println("new value:" + map.get("key"));
        // Shutdown the Hazelcast Cluster Member
        hz.shutdown();
    }
}
