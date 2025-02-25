package com.hazelcast.guide;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

@ApplicationScoped
public class HazelcastManager {

    @Inject
    HazelcastInstance instance;

    String put(Integer key, String value) {
        String oldValue = getDistributedMap().put(key, value);
        return String.format("Put: %s. Old value was: %s\n", value, oldValue == null ? "null" : oldValue);
    }

    String get(Integer key) {
        String value = getDistributedMap().get(key);
        return String.format("{ %d : %s }\n", key, value == null ? "null" : value);
    }

    String list() {
        StringBuilder result = new StringBuilder(String.format("Size: %d\n", getDistributedMap().size()));
        getDistributedMap().forEach( (k, v) -> result.append(String.format("{ %d: %s }\n", k, v)));
        return result.toString();
    }

    private IMap<Integer,String> getDistributedMap() {
        return instance.getMap(HazelcastApplication.MAP_NAME);
    }

}
