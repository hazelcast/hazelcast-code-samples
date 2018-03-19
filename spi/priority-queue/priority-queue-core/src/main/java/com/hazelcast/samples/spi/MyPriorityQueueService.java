package com.hazelcast.samples.spi;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Create a service for our priority queue, just like
 * the map service, the queue service and all the built-ins.
 * <p>
 * This is a {@link com.hazelcast.spi.ManagedService ManagedService}
 * as Hazelcast manages it's lifecycle along with the Hazelcast
 * instance starting and stopping.
 * <p>
 * This is a {@link com.hazelcast.spi.RemoteService RemoteService}
 * as other Hazelcast instances can access it. Hence we return a proxy
 * to the real thing for them to work with.
 * <p>
 * This is <b>not</b> yet a
 * {@link com.hazelcast.spi.MigrationAwareService MigrationAwareService}.
 * It should be for more realistic use but would complicate the example
 * further.
 * <p>
 * <b>OPTIMISATION NOTE</b></p>The threading model of Hazelcast
 * is such that only one thread is responsible for a subset of partitions.
 * (Eg. {@code thread-1} looks after partitions 1 &amp; 2. {@code thread-2}
 * looks after partitions 3 &amp; 4. And so on.)
 * So no partition's operations are ever handled by two threads
 * concurrently. Hence we can use {@link java.util.PriorityQueue}
 * rather than {@link java.util.concurrent.PriorityBlockingQueue} and this
 * gives better performance.
 */
public class MyPriorityQueueService implements ManagedService, RemoteService {

    // See class comments, PriorityQueue not PriorityBlockingQueue
    private Map<String, PriorityQueue<?>> mapPriorityQueue = new ConcurrentHashMap<>();
    private NodeEngine nodeEngine;

    /**
     * {@link com.hazelcast.spi.ManagedService ManagedService}
     * <p>
     * Save the {@link com.hazelcast.spi.NodeEngine NodeEngine}
     * for later.
     *
     * @param nodeEngine The Hazelcast operation processor
     * @param properties {@code @NotNull} but not used
     */
    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
    }

    /**
     * {@link com.hazelcast.spi.ManagedService ManagedService}
     * <p>
     * Not used in this example.
     */
    @Override
    public void reset() {
    }

    /**
     * {@link com.hazelcast.spi.ManagedService ManagedService}
     * <p>
     * No action needed to shut down the service.
     *
     * @param terminate Ignored
     */
    @Override
    public void shutdown(boolean terminate) {
    }

    /**
     * {@link com.hazelcast.spi.RemoteService RemoteService}
     * <p>
     * Create a remotely accessible service instance.
     *
     * @param objectName The name for the priority queue
     * @return The priority queue with that name
     */
    @SuppressWarnings("rawtypes")
    @Override
    public DistributedObject createDistributedObject(String objectName) {
        return new MyPriorityQueueServiceProxy(objectName, this.nodeEngine, this);
    }

    /**
     * {@link com.hazelcast.spi.RemoteService RemoteService}
     * <p>
     * >No action needed to shut down the service instance.
     *
     * @param objectName A named priority queue
     */
    @Override
    public void destroyDistributedObject(String objectName) {
    }

    /**
     * Helper method to return a queue held in the service,
     * creating if necessary.
     *
     * @param name The queue name
     * @return The queue
     */
    protected PriorityQueue<?> getQueue(String name) {
        PriorityQueue<?> priorityQueue = this.mapPriorityQueue.get(name);

        if (priorityQueue == null) {
            priorityQueue = new PriorityQueue<>();
            this.mapPriorityQueue.put(name, priorityQueue);
        }

        return priorityQueue;
    }
}
