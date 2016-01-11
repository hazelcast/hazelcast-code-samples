package com.hazelcast.examples.declarative;

import javax.cache.configuration.Factory;
import javax.cache.integration.CacheWriter;

public class DummyCacheWriterFactory implements Factory<CacheWriter<String, String>> {

    @Override
    public CacheWriter<String, String> create() {
        return new DummyCacheWriter();
    }
}
