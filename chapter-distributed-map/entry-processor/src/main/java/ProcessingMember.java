import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

import java.util.Map;

public class ProcessingMember {
    public static void main(String[] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap map = hz.getMap("theMap");
        map.put("peter", "onzin");
        map.executeOnKey("peter", new EntryProcessor() {
            @Override
            public Object process(Map.Entry entry) {
                System.out.println("entry.key:" + entry.getKey() + " entry.value:" + entry.getValue());
                return true;
            }

            @Override
            public EntryBackupProcessor getBackupProcessor() {
                return null;
            }
        });
    }
}
