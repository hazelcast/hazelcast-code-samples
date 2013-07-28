import com.hazelcast.core.QueueStore;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class TheQueueStore implements QueueStore<Integer> {

    @Override
    public void delete(Long key) {
        System.out.printf("delete");
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void store(Long key, Integer value) {
        System.out.printf("store");
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void storeAll(Map<Long, Integer> map) {
        System.out.printf("store all");
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteAll(Collection<Long> keys) {
        System.out.printf("deleteAll");
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Integer load(Long key) {
        System.out.printf("load");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<Long, Integer> loadAll(Collection<Long> keys) {
        System.out.printf("loadALl");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<Long> loadAllKeys() {
        System.out.printf("loadAllKeys");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
