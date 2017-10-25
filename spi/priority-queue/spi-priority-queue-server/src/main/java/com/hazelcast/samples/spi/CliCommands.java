package com.hazelcast.samples.spi;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.stereotype.Component;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CliCommands implements CommandMarker {
	
    @Autowired
    private HazelcastInstance hazelcastInstance;

	/**
	 * <p>Look up the {@link com.hazelcast.core.DistributedObject DistributedObject}
	 * that are currently defined in the cluster. As we are not
	 * accessing by name, this won't trigger the lazy creation.
	 * It only shows the ones currently present.
	 * </p>
	 */
    @CliCommand(value = "list",
			help = "List (but don't create) distributed objects")
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

			// If it's our queue, use one of the operations defined for it
			if (distributedObjectServiceName.equals(MyPriorityQueue.SERVICE_NAME)) {
				MyPriorityQueue<?> myPriorityQueue
					= (MyPriorityQueue<?>) distributedObject;

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
				(distributedObjects.size()==1 ? "": "s")
				);
		
		log.info("-----------------------");
	}
}
