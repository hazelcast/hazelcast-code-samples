package com.hazelcast.samples.tricolor;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Client;
import com.hazelcast.core.ClientListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Create a Hazelcast server, logging using the specified log file.
 */
public class MyServer implements Runnable {

    private static Logger log;

    private CountDownLatch countDownLatch;
    private HazelcastInstance hazelcastInstance;

    /**
     * Set the logging configuration to use the provided logfile.
     *
     * @param logFile Should be on the classpath
     */
    public MyServer(String logFile, CountDownLatch countDownLatch) throws Exception {
        Configurator.initialize(logFile, getClass().getClassLoader(), "classpath:" + logFile);
        log = LoggerFactory.getLogger(getClass());
        this.countDownLatch = countDownLatch;
    }

    /**
     * Start Hazelcast and listen for clients.
     * <p>
     * Keep running until the client finishes.
     */
    @Override
    public void run() {
        log.info("run() START");

        try {
            // start Hazelcast server
            Config config = new ClasspathXmlConfig("hazelcast.xml");
            config.setClassLoader(getClass().getClassLoader());
            hazelcastInstance = Hazelcast.newHazelcastInstance(config);

            // add a listener for the client
            MyClientListener myClientListener = new MyClientListener();
            hazelcastInstance.getClientService().addClientListener(myClientListener);

            // confirm ready for clients
            String uuid = hazelcastInstance.getCluster().getLocalMember().getUuid();
            log.info("Server {} is ready", uuid);
            countDownLatch.countDown();

            // stay running til the client is done
            while (!myClientListener.clientHasLeft) {
                TimeUnit.SECONDS.sleep(1L);
            }

        } catch (Exception exception) {
            log.error("run() exception", exception);
        }

        if (hazelcastInstance != null) {
            if (hazelcastInstance.getLifecycleService().isRunning()) {
                hazelcastInstance.shutdown();
            }
        }

        log.info("run() END");
    }

    /**
     * Listen for clients joining and leaving. There will be
     * only one. When it leaves we can shut down.
     */
    static class MyClientListener implements ClientListener {

        volatile boolean clientHasLeft;

        @Override
        public void clientConnected(Client arg0) {
        }

        /**
         * Set flag if client has shutdown.
         */
        @Override
        public void clientDisconnected(Client client) {
            clientHasLeft = true;
        }
    }
}
