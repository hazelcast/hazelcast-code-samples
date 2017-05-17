package com.hazelcast.samples.tricolor;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * A callable that does some logging and returns a String.
 * We run this on every server to display and return the
 * server's ID.
 */
@SuppressWarnings("serial")
public class MyCallable implements Callable<String>, HazelcastInstanceAware, Serializable {

    private static Logger log = LoggerFactory.getLogger(MyCallable.class);

    private HazelcastInstance hazelcastInstance;

    /**
     * Return the name of the current Hazelcast instance.
     */
    @Override
    public String call() throws Exception {
        String uuid = hazelcastInstance.getCluster().getLocalMember().getUuid();
        log.info("call() runs on {}", uuid);
        return uuid;
    }

    /**
     * When the callable runs, Hazelcast will inject the current
     * instance.
     *
     * @param arg0 The instance that execs the {@code run()} method.
     */
    @Override
    public void setHazelcastInstance(HazelcastInstance arg0) {
        this.hazelcastInstance = arg0;
    }
}
