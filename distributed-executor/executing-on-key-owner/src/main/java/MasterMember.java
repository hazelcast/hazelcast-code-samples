import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;

import java.util.Map;
import java.util.UUID;

public class MasterMember {

    public static void main(String[] args) throws Exception {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        Map<String, String> map = hz.getMap("map");
        for (int i = 0; i < 10; i++) {
            map.put(UUID.randomUUID().toString(), "");
        }

        IExecutorService executor = hz.getExecutorService("executor");
        for (String key : map.keySet()) {
            executor.executeOnKeyOwner(new VerifyTask(key), key);
        }
    }
}
