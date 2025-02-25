package hazelcast.platform.labs.machineshop;

import java.io.Closeable;
import java.io.IOException;

/**
 * Implements reference counting of a closeable thing.
 *
 * When the last reference is released, the thing will be closed.
 *
 * Instances of this class are thread safe but the actual underlying thing may or may not be.
 *
 * @param <T>
 */
public class CloseableRef<T extends Closeable> {
    private  T t;
    private int refCount = 0;

    public CloseableRef(T t){
        this.t = t;
    }
    public synchronized T get(){
        return t;
    }

    public synchronized void acquire(){
        refCount += 1;
    }

    public synchronized void release(){
        refCount -= 1;
        if (refCount == 0 && t != null) {
            try {
                t.close();
                t = null;
            } catch(IOException iox){
                throw new RuntimeException("Release of Closeable resource failed", iox);
            }
        }
    }
}
