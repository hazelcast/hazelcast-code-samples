package com.hazelcast.samples.spi;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Three commands for testing - write data to the queues,
 * read data from the queues, and list what is in the queues.
 */
@Component
@Slf4j
public class CliCommands implements CommandMarker {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    /**
     * Look up the {@link com.hazelcast.core.DistributedObject DistributedObject}
     * that are currently defined in the cluster. As we are not
     * accessing by name, this won't trigger the lazy creation.
     * It only shows the ones currently present.
     */
    @CliCommand(value = "list", help = "List (but don't create) distributed objects")
    public void list() throws Exception {
        log.info("-----------------------");

        Collection<DistributedObject> distributedObjects
                = this.hazelcastInstance.getDistributedObjects();

        // Find the distributed queue a different way than by name
        for (DistributedObject distributedObject : distributedObjects) {

            String distributedObjectName = distributedObject.getName();
            String distributedObjectServiceName = distributedObject.getServiceName();

            log.info("Distributed Object, name '{}', service '{}'",
                    distributedObjectName,
                    distributedObjectServiceName
            );
            log.trace("Distributed Object, name '{}', class '{}'",
                    distributedObjectName,
                    distributedObject.getClass().getName()
            );

            // If it's our queue, use one of the operations defined for it
            if (distributedObjectServiceName.equals(MyPriorityQueue.SERVICE_NAME)) {
                MyPriorityQueue<?> myPriorityQueue = (MyPriorityQueue<?>) distributedObject;

                log.info(" -> queue size {}", myPriorityQueue.size());
            }

            if (distributedObject instanceof IQueue) {
                IQueue<?> iQueue
                        = (IQueue<?>) distributedObject;

                log.info(" -> queue size {}", iQueue.size());
            }
        }

        if (distributedObjects.size() > 0) {
            log.info("-----------------------");
        }

        log.info("[{} distributed object{}]",
                distributedObjects.size(),
                (distributedObjects.size() == 1 ? "" : "s")
        );

        log.info("-----------------------");
    }

    /**
     * Read data from the queues.
     */
    @SuppressWarnings("unchecked")
    @CliCommand(value = "read", help = "Read orders from IQueue and MyPriorityQueue")
    public void read() throws Exception {

        // Normal IQueue
        IQueue<Order> vanilla = this.hazelcastInstance.getQueue("vanilla");

        log.info("Queue '{}' has size {}",
                vanilla.getName(), vanilla.size());

        for (int i = 0; !vanilla.isEmpty(); i++) {
            log.info("Item {} => {}", i, vanilla.poll());
        }

        // MyPriorityQueue
        DistributedObject distributedObject
                = this.hazelcastInstance.getDistributedObject(MyPriorityQueue.SERVICE_NAME, "strawberry");
        MyPriorityQueue<Order> strawberry = (MyPriorityQueue<Order>) distributedObject;

        log.info("Queue '{}' has size {}",
                strawberry.getName(), strawberry.size());

        int max = strawberry.size();
        for (int i = 0; i < max; i++) {
            log.info("Item {} => {}", i, strawberry.poll());
        }
    }

    /**
     * Write data to the queues, same data to both kinds.
     */
    @SuppressWarnings("unchecked")
    @CliCommand(value = "write", help = "Write orders into IQueue and MyPriorityQueue")
    public void write() throws Exception {
        // Normal IQueue
        List<Order> orders = TestData.createOrders();

        IQueue<Order> vanilla = this.hazelcastInstance.getQueue("vanilla");
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            log.info("Item {} => {}", i, order);
            vanilla.put(order);
        }
        log.info("Wrote {} into queue '{}', queue size now {}",
                orders.size(), vanilla.getName(), vanilla.size());

        // A distributed object of MyPriorityQueue type
        DistributedObject distributedObject
                = this.hazelcastInstance.getDistributedObject(MyPriorityQueue.SERVICE_NAME, "strawberry");
        MyPriorityQueue<Order> strawberry = (MyPriorityQueue<Order>) distributedObject;

        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            log.info("Item {} => {}", i, order);
            strawberry.offer(order);
        }
        log.info("Wrote {} into queue '{}', queue size now {}",
                orders.size(), strawberry.getName(), strawberry.size());
    }
}
