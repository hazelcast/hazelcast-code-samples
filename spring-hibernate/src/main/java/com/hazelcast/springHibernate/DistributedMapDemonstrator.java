package com.hazelcast.springHibernate;

import org.apache.log4j.Logger;

/**
 * Created by Esref Ozturk <esrefozturk93@gmail.com> on 17.07.2014.
 */

public class DistributedMapDemonstrator {

	private static final Logger logger = Logger.getLogger(DistributedMapDemonstrator.class);
	
	private	IDistributedMapService distributedMapService;


	public void demonstrate() {				
		logger.debug("Received Entry : 1 => " + distributedMapService.getCustomerMap().get("1"));
		logger.debug("Received Entry : 2 => " + distributedMapService.getCustomerMap().get("2"));
	}

	public void setDistributedMapService(IDistributedMapService distributedMapService) {
		this.distributedMapService = distributedMapService;
	}
	
}
