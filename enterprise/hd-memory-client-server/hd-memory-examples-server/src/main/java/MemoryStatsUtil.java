import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceImpl;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.instance.Node;
import com.hazelcast.memory.DefaultMemoryStats;
import com.hazelcast.memory.HazelcastMemoryManager;
import com.hazelcast.memory.MemoryStats;
import com.hazelcast.nio.serialization.EnterpriseSerializationService;
import com.hazelcast.util.ExceptionUtil;

import java.lang.reflect.Method;

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

    private static Node getNode(HazelcastInstance hz) {
        HazelcastInstanceImpl impl = getHazelcastInstanceImpl(hz);
        return impl != null ? impl.node : null;
    }

    private static HazelcastInstanceImpl getHazelcastInstanceImpl(HazelcastInstance hz) {
        HazelcastInstanceImpl impl = null;
        if (hz instanceof HazelcastInstanceProxy) {
            Method original;
            try {
                original = HazelcastInstanceProxy.class.getDeclaredMethod("getOriginal");
                original.setAccessible(true);
                impl = ((HazelcastInstanceImpl) original.invoke(hz));
            } catch (Exception e) {
                return ExceptionUtil.sneakyThrow(e);
            }
        } else if (hz instanceof HazelcastInstanceImpl) {
            impl = (HazelcastInstanceImpl) hz;
        }
        return impl;
    }
}
