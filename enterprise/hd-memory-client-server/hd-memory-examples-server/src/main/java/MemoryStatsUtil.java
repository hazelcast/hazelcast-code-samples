import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.internal.memory.DefaultMemoryStats;
import com.hazelcast.internal.memory.HazelcastMemoryManager;
import com.hazelcast.internal.memory.MemoryStats;
import com.hazelcast.internal.serialization.EnterpriseSerializationService;

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
