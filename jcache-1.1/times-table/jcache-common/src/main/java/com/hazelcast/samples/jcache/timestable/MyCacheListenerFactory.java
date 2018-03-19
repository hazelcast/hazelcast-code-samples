package com.hazelcast.samples.jcache.timestable;

import javax.cache.configuration.Factory;

/**
 * Factory to create cache listeners, in this case return
 * the static one each time.
 */
@SuppressWarnings({"serial", "rawtypes"})
public class MyCacheListenerFactory implements Factory<MyCacheListener> {

    private static MyCacheListener myCacheListener = new MyCacheListener();

    @Override
    public MyCacheListener create() {
        return myCacheListener;
    }
}
