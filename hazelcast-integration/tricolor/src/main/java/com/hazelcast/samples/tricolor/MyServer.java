package com.hazelcast.samples.tricolor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Client;
import com.hazelcast.core.ClientListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * <P>Create a Hazelcast server, logging using the specified log file.
 * </P>
 */
public class MyServer implements Runnable {

	private static Logger log;

	private CountDownLatch countDownLatch;
	private HazelcastInstance hazelcastInstance;
	private MyClientListener myClientListener;

	/**
	 * <P>Set the logging configuration to use the provided logfile.
	 * </P>
	 * 
	 * @param logFile Should be on the classpath
	 */
	public MyServer(String logFile, CountDownLatch countDownLatch) throws Exception {
		Configurator.initialize(logFile, this.getClass().getClassLoader(), "classpath:" + logFile);
		log = LoggerFactory.getLogger(this.getClass());
		this.countDownLatch = countDownLatch;
	}

	/**
	 * <P>Start Hazelcast and listen for clients.
	 * </P>
	 * <P>Keep running until the client finishes.
	 * </P>
	 */
	@Override
	public void run() {
		log.info("run() START");
		
		try {
			// Start Hazelcast server
			Config config = new ClasspathXmlConfig("hazelcast.xml");
			config.setClassLoader(getClass().getClassLoader());
			this.hazelcastInstance = Hazelcast.newHazelcastInstance(config);

			// Add a listener for the client
			this.myClientListener = new MyClientListener();
			this.hazelcastInstance.getClientService().addClientListener(myClientListener);

			// Confirm ready for clients
			String uuid = this.hazelcastInstance.getCluster().getLocalMember().getUuid();
			log.info("Server {} is ready", uuid);
			this.countDownLatch.countDown();
			
			// Stay running til the client is done
			while (!myClientListener.clientHasLeft) {
				TimeUnit.SECONDS.sleep(1L);
			}
			
		} catch (Exception exception) {
			log.error("run() exception", exception);
		}

		if (this.hazelcastInstance != null) {
			if (this.hazelcastInstance.getLifecycleService().isRunning()) {
				this.hazelcastInstance.shutdown();
			}
		}

		log.info("run() END");
	}

	
	/**
	 * <P>Listen for clients joining and leaving. There will be
	 * only one. When it leaves we can shut down.
	 * </P>
	 */
	static class MyClientListener implements ClientListener {

		boolean clientHasLeft = false;
		
		@Override
		public void clientConnected(Client arg0) {
		}

		/**
		 * <P>Set flag if client has shutdown.
		 * </P>
		 */
		@Override
		public void clientDisconnected(Client client) {
			clientHasLeft = true;
		}

	}
	
}
