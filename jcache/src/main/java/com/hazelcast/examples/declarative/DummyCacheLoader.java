package com.hazelcast.examples.declarative;

import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import java.util.HashMap;
import java.util.Map;

public class DummyCacheLoader implements CacheLoader<String, String> {

    @Override
    public String load(String key) throws CacheLoaderException {
        return key;
    }

    @Override
    public Map<String, String> loadAll(Iterable<? extends String> keys) throws CacheLoaderException {
        HashMap<String, String> map = new HashMap<String, String>();
        for (String key : keys) {
            map.put(key, load(key));
        }
        return map;
    }
}
