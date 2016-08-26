import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.Node;
import com.hazelcast.memory.DefaultMemoryStats;
import com.hazelcast.memory.HazelcastMemoryManager;
import com.hazelcast.memory.MemoryStats;
import com.hazelcast.nio.serialization.EnterpriseSerializationService;

import static com.hazelcast.examples.helper.HazelcastUtils.getNode;

class MemoryStatsUtil {

    static MemoryStats getMemoryStats(HazelcastInstance hz) {
        // use this method or another way for getting "Node" from a "HazelcastInstance"
        Node node = getNode(hz);
        if (node != null) {
            EnterpriseSerializationService serializationService =
                    (EnterpriseSerializationService) node.getSerializationService();
            HazelcastMemoryManager memoryManager = serializationService.getMemoryManager();
            return memoryManager.getMemoryStats();
        } else {
            return new DefaultMemoryStats();
        }
    }
}
