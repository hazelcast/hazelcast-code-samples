package com.hazelcast.springHibernate;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import org.apache.log4j.Logger;

/**
 * Created by Esref Ozturk <esrefozturk93@gmail.com> on 17.07.2014.
 */

public class CustomerEntryListener implements EntryListener<String, Customer> {

	private static final Logger logger = Logger.getLogger(CustomerEntryListener.class);
	

	@Override
	public void entryAdded(EntryEvent<String, Customer> ee) {
		logger.debug("Entry is added. Member : " + ee.getMember() + ", Entry : "+ee.getOldValue() + ", Entry : " + ee.getValue());
	}


	@Override
	public void entryEvicted(EntryEvent<String, Customer> ee) {
		logger.debug("Entry is evicted. Member : " + ee.getMember() + ", Entry : "+ee.getOldValue() + ", Entry : " + ee.getValue());
	}


	@Override
	public void entryRemoved(EntryEvent<String, Customer> ee) {
		logger.debug("Entry is removed. Member : " + ee.getMember() + ", Entry : "+ee.getOldValue() + ", Entry : " + ee.getValue());
	}


	@Override
	public void entryUpdated(EntryEvent<String, Customer> ee) {
		logger.debug("Entry is updated. Member : " + ee.getMember() + ", Entry : "+ee.getOldValue() + ", Entry : " + ee.getValue());
	}	

}
