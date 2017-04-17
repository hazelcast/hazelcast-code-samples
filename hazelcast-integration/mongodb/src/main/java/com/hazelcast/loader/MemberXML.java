package com.hazelcast.loader;

import com.hazelcast.core.HazelcastInstance;

import static com.hazelcast.core.Hazelcast.newHazelcastInstance;

/**
 * Starter application for read-through / write-through example with Hazelcast and MongoDB.
 * <p>
 * Connection details should be interred in `hazelcast.xml` under MapStore config for IMap
 * Properties includes: connection url, database and collection names
 *
 * @author Viktor Gamov on 11/2/15.
 *         Twitter: @gamussa
 */
public class MemberXML {
    public static void main(String[] args) {
        final HazelcastInstance hazelcastInstance = newHazelcastInstance();
        new ReadWriteThroughCache(hazelcastInstance).run();
    }
}
