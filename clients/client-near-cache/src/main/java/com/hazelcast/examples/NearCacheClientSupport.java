package com.hazelcast.examples;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

abstract class NearCacheClientSupport extends NearCacheSupport {

    protected static HazelcastInstance initCluster() {
        serverInstance = Hazelcast.newHazelcastInstance();
        return HazelcastClient.newHazelcastClient();
    }

    protected static HazelcastInstance[] initCluster(int clusterSize) {
        serverInstance = Hazelcast.newHazelcastInstance();

        HazelcastInstance[] instances = new HazelcastInstance[clusterSize];
        for (int i = 0; i < clusterSize; i++) {
            instances[i] = HazelcastClient.newHazelcastClient();
        }
        return instances;
    }
}
