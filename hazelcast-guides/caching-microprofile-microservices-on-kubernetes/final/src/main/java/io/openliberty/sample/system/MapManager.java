package io.openliberty.sample.system;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import com.hazelcast.core.HazelcastInstance;


@ApplicationScoped
public class MapManager {

    @Inject
    HazelcastInstance instance;

    //Map<String,String> keyValueStore = new ConcurrentHashMap<>();

    private Map<String,String> retrieveMap() {
        return instance.getMap("map");
    }

    public String put(String key,String value) {
        retrieveMap().put(key, value);
        return value;

    }

    public String get(String key) {
        String value = retrieveMap().get(key);
        return value;
    }


}