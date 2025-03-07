package org.hazelcast.jet.demo;

import com.hazelcast.core.HazelcastInstance;

public interface IOfflineDataLoader {

    enum DataSource {
        S3,
        LOCAL_FILE_SYSTEM
    }
    long loadData(String regionName, HazelcastInstance hzInstance, String path) ;
}
