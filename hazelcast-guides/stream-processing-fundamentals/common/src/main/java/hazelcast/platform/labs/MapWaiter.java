package hazelcast.platform.labs;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

import java.util.concurrent.atomic.AtomicBoolean;


public class MapWaiter<K, V> {

    private final IMap<K, V> map;
    private final K awaitKey;
    private final V awaitVal;

    private final AtomicBoolean done;

    public MapWaiter(IMap<K, V> map, K key, V val) {
        this.map = map;
        this.awaitKey = key;
        this.awaitVal = val;
        this.done = new AtomicBoolean(false);
    }


    public boolean waitForSignal(long timeoutMs) {
        boolean result = false;
        map.addEntryListener(new AwaitedEntryListener(), awaitKey, true);
        V val = map.get(awaitKey);
        if (val != null && val.equals(awaitVal)) {
            System.out.println("Awaited value is present.");
            result = true;
        } else {
            synchronized (done) {
                try {
                    System.out.println("Waiting...");
                    done.wait(timeoutMs);
                    if (done.get()) {
                        result = true;
                        System.out.println("Awaited key arrived.");
                    } else {
                        result = false;
                        System.out.println("Awaited key never arrived. Wait timed out.");
                    }
                } catch (InterruptedException ix) {
                    // this is main, we will be exiting either way
                    System.out.println("Interrupted while waiting.");
                    result = false;
                }
            }
        }
        return result;
    }

    public class AwaitedEntryListener implements EntryAddedListener<K, V>, EntryUpdatedListener<K, V> {
        private void handleEvent(EntryEvent<K, V> event) {
            if (event.getKey().equals(awaitKey) && event.getValue().equals(awaitVal)) {
                synchronized (done) {
                    done.set(true);
                    done.notify();
                }
            }
        }

        @Override
        public void entryAdded(EntryEvent<K, V> event) {
            handleEvent(event);
        }

        @Override
        public void entryUpdated(EntryEvent<K, V> event) {
            handleEvent(event);
        }
    }
}
