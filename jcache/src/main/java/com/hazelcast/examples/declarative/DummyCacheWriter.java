package com.hazelcast.examples.declarative;

import javax.cache.Cache;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DummyCacheWriter implements CacheWriter<String, String> {

    private final Map<String, String> map = new HashMap<String, String>();

    @Override
    public void write(Cache.Entry<? extends String, ? extends String> entry) throws CacheWriterException {
        map.put(entry.getKey(), entry.getValue());
    }

    @Override
    public void writeAll(Collection<Cache.Entry<? extends String, ? extends String>> entries) throws CacheWriterException {
        for (Cache.Entry<? extends String, ? extends String> entry : entries) {
            write(entry);
        }
    }

    @Override
    public void delete(Object key) throws CacheWriterException {
        map.remove(key);
    }

    @Override
    public void deleteAll(Collection<?> keys) throws CacheWriterException {
        for (Object key : keys) {
            delete(key);
        }
    }
}
