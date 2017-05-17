package com.hazelcast.samples.tricolor;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * The client submits a callable, this does some logging
 * so we have visibility of which server or servers it is
 * running on.
 */
public class MyClient implements Runnable {

    private static Logger log;

    private CountDownLatch countDownLatch;

    /**
     * Set the logging configuration to use the provided logfile.
     *
     * @param logFile        Should be on the classpath
     * @param countDownLatch For synchronisation
     */
    public MyClient(String logFile, CountDownLatch countDownLatch) {
        Configurator.initialize(logFile, getClass().getClassLoader(), "classpath:" + logFile);
        log = LoggerFactory.getLogger(getClass());
        this.countDownLatch = countDownLatch;
    }

    /**
     * Start a client, do something, shutdown.
     */
    @Override
    public void run() {
        log.info("run() START");

        HazelcastInstance hazelcastInstance = null;

        try {
            // wait til servers are up before starting client
            log.info("Client is waiting for {} servers", countDownLatch.getCount());
            countDownLatch.await();
            log.info("Starting client");

            // start client
            ClientConfig clientConfig = new XmlClientConfigBuilder("hazelcast-client.xml").build();
            clientConfig.setClassLoader(getClass().getClassLoader());
            hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);

            IExecutorService executorService = hazelcastInstance.getExecutorService("default");
            Callable<String> myCallable = new MyCallable();

            log.info("Submitting callable");
            Map<Member, Future<String>> results = executorService.submitToAllMembers(myCallable);
            for (Future<String> result : results.values()) {
                log.info("Result from '{}'", result.get());
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
}
