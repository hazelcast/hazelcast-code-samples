package example;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.nio.serialization.genericrecord.GenericRecord;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class ProcessAll implements HazelcastInstanceAware, Serializable, Callable {

    private HazelcastInstance hazelcastInstance;

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public Object call() {
        IQueue<Object> productsQueue = hazelcastInstance.getQueue("productsQueue");

        int totalNumberOfItems = 0;
        while (true) {
            GenericRecord poll = (GenericRecord) productsQueue.poll();
            if (poll == null) {
                break;
            }
            int quantity = poll.getInt32("quantity");
            totalNumberOfItems += quantity;
        }
        return totalNumberOfItems;
    }
}
