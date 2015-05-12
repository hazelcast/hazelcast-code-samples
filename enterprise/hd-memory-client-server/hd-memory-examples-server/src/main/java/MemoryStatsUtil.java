import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceImpl;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.memory.DefaultMemoryStats;
import com.hazelcast.memory.MemoryStats;
import com.hazelcast.util.ExceptionUtil;

import java.lang.reflect.Method;

public class MemoryStatsUtil {

    public static MemoryStats getMemoryStats(HazelcastInstance hz) {
        try {
            if (hz instanceof HazelcastInstanceProxy) {
                Method original = HazelcastInstanceProxy.class.getDeclaredMethod("getOriginal");
                original.setAccessible(true);
                return ((HazelcastInstanceImpl) original.invoke(hz)).getMemoryStats();
            } else if (hz instanceof HazelcastInstanceImpl) {
                return ((HazelcastInstanceImpl) hz).getMemoryStats();
            }
        } catch (Exception e) {
            return ExceptionUtil.sneakyThrow(e);
        }
        return new DefaultMemoryStats();
    }
}
