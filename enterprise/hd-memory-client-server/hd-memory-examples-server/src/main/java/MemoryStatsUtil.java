import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceImpl;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.instance.Node;
import com.hazelcast.memory.DefaultMemoryStats;
import com.hazelcast.memory.MemoryManager;
import com.hazelcast.memory.MemoryStats;
import com.hazelcast.nio.serialization.EnterpriseSerializationService;
import com.hazelcast.util.ExceptionUtil;

import java.lang.reflect.Method;

public class MemoryStatsUtil {

    public static MemoryStats getMemoryStats(HazelcastInstance hz) {
        Node node = getNode(hz); // or another way for getting "Node" over "HazelcastInstance"
        if (node != null) {
            EnterpriseSerializationService serializationService =
                (EnterpriseSerializationService) node.getSerializationService();
            MemoryManager memoryManager = serializationService.getMemoryManager();
            return memoryManager.getMemoryStats();
        } else
            return new DefaultMemoryStats();
    }

    public static Node getNode(HazelcastInstance hz) {
        HazelcastInstanceImpl impl = getHazelcastInstanceImpl(hz);
        return impl != null ? impl.node : null;
    }

    public static HazelcastInstanceImpl getHazelcastInstanceImpl(HazelcastInstance hz) {
        HazelcastInstanceImpl impl = null;
        if (hz instanceof HazelcastInstanceProxy) {
            Method original = null;
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
