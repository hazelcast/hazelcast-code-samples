package com.hazelcast.examples.declarative;

import javax.cache.configuration.Factory;
import javax.cache.integration.CacheLoader;

public class DummyCacheLoaderFactory implements Factory<CacheLoader<String, String>> {

    @Override
    public CacheLoader<String, String> create() {
        return new DummyCacheLoader();
    }
}
