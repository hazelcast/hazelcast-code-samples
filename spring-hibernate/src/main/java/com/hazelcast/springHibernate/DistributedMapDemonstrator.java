package com.hazelcast.springHibernate;

/**
 * Created by Esref Ozturk <esrefozturk93@gmail.com> on 17.07.2014.
 */

public class DistributedMapDemonstrator {

	private	DistributedMapService distributedMapService;

	public void demonstrate() {				
		System.out.println("Received Entry : 1 => " + distributedMapService.getCustomerMap().get("1"));
        System.out.println("Received Entry : 2 => " + distributedMapService.getCustomerMap().get("2"));
	}

	public void setDistributedMapService(DistributedMapService distributedMapService) {
		this.distributedMapService = distributedMapService;
	}
	
}
