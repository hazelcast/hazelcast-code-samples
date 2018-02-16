package com.hazelcast.samples.jcache.timestable;

import javax.cache.configuration.Factory;

/**
 * <p>Factory to create cache listeners, in this case return
 * the static one each time.
 * </p>
 */
@SuppressWarnings({ "serial", "rawtypes" })
public class MyCacheListenerFactory implements Factory<MyCacheListener> {

    private static MyCacheListener myCacheListener = new MyCacheListener();

    @Override
    public MyCacheListener create() {
        return myCacheListener;
    }

}
