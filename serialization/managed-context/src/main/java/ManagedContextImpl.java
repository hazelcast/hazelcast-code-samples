import com.hazelcast.core.ManagedContext;

class ManagedContextImpl implements ManagedContext {

    @Override
    public Object initialize(Object obj) {
        if (obj instanceof DummyObject) {
            ((DummyObject) obj).trans = new Thread();
        }
        return obj;
    }
}
