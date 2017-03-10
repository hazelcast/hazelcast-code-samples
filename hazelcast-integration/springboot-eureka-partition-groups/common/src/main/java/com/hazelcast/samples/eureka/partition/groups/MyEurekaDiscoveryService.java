package com.hazelcast.samples.eureka.partition.groups;

import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import com.hazelcast.spi.discovery.integration.DiscoveryService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static com.hazelcast.spi.partitiongroup.PartitionGroupMetaData.PARTITION_GROUP_HOST;
import static com.hazelcast.spi.partitiongroup.PartitionGroupMetaData.PARTITION_GROUP_ZONE;

/**
 * <P>A mechanism 
 * TODO:
 */
@Component
@Slf4j
public class MyEurekaDiscoveryService implements DiscoveryService {

	@Autowired
	private DiscoveryClient discoveryClient;

    private HashMap<String, Object> memberMetadata = new HashMap<>();

    /**
     * FIXME
     * @return XXX
     */
	@Override
	public Iterable<DiscoveryNode> discoverNodes() {
		List<DiscoveryNode> nodes = new ArrayList<>();

		log.info("\n--------------------------------------------------------------------------------");
		log.info("Hazelcast lookup to Eureka : start");
		
        populateMemberMetadata();

        // A mildly unnecessary lambda, to ensure we're not on the dark ages of Java 7
		this.discoveryClient.getInstances(Constants.CLUSTER_NAME).forEach(
				(ServiceInstance serviceInstance) -> {
					try {
						
						String host = serviceInstance.getMetadata().get("instanceHost");
						String port = serviceInstance.getMetadata().get("instancePort");
						
						log.info(" -> DiscoveryNode {}:{}", host, port);

						// START : Meta Data extra
						boolean noOtherMetaData = true;
						for (String key : serviceInstance.getMetadata().keySet()) {
							if (!key.equals("instanceHost") && !key.equals("instancePort")) {
								log.info("  ->  -> Other metadata '{}'=='{}'",
										key, serviceInstance.getMetadata().get(key));
								noOtherMetaData = false;
							}
						}
						if (noOtherMetaData) {
							log.info("  ->  -> No other metadata");
						}
						// END : Meta Data extra

						Address address = new Address(host, Integer.valueOf(port));

						DiscoveryNode discoveryNode = 
								new SimpleDiscoveryNode(address);
						
						nodes.add(discoveryNode);
						
					} catch (Exception e) {
						log.error("discoverNodes()", e);
					}
					
				});

		log.info("Hazelcast lookup to Eureka : end. Found {} item{}",
				nodes.size(), (nodes.size()==1 ? "" : "s"));
		log.info("--------------------------------------------------------------------------------\n");
		
		return nodes;
	}

    /**
     * FIXME
     */
    private void populateMemberMetadata() {
        final ServiceInstance localServiceInstance = discoveryClient.getLocalServiceInstance();
        final Map<String, String> metadata = localServiceInstance.getMetadata();
        final String host1 = localServiceInstance.getHost();
        memberMetadata.put(PARTITION_GROUP_ZONE, metadata.get(Constants.HAZELCAST_ZONE_METADATA_KEY));
        memberMetadata.put(PARTITION_GROUP_HOST, host1);
    }


    /**
     * FIXME
     * @return XXX
     */
    @Override
    public Map<String, Object> discoverLocalMetadata() {
        // TODO: add check if it's a member or a client
        if (memberMetadata.size() == 0) {
            populateMemberMetadata();
        }
        log.info("Partition groups metadata: {}", memberMetadata);
        return memberMetadata;
    }

	/**
	 * <P>Part of the interface, but not used.
	 * </P>
	 */
	@Override
	public void start() {
	}
	
	/**
	 * <P>Part of the interface, but not used.
	 * </P>
	 */
	@Override
	public void destroy() {
	}

}
