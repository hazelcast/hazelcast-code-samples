package io.openliberty.sample.system;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@ApplicationScoped
public class MapManager {

    Map<String,String> keyValueStore = new ConcurrentHashMap<>();

    private Map<String,String> retrieveMap() {
        return keyValueStore;
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