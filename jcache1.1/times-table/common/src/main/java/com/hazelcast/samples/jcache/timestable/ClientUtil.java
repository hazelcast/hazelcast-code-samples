package com.hazelcast.samples.jcache.timestable;

import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;

/**
 * <p>Common utilities for all flavours of clients.
 * </p>
 */
public class ClientUtil {

    /**
     * <p>JCache configuration for the "{@code timestable}" cache.
     * The key is {@link Tuple} holding the input to the times
     * table argument (eg. the pair {@code 5} and {@code 6}) and
     * the value is the resulting number.
     * </p>
     * <p>Add a cache listener so we can observe when items are
     * actually added to the cache, as this helps to prove when
     * caching is happening and when it is not.
     * </p>
     *
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static MutableConfiguration timesTableConfiguration() {
        MutableConfiguration<Tuple, Integer> mutableConfiguration = new MutableConfiguration<>();

        mutableConfiguration.setTypes(Tuple.class, Integer.class);

        CacheEntryListenerConfiguration cacheEntryListenerConfiguration
        = new MutableCacheEntryListenerConfiguration(
                // Factory, FilterFactory, is old value required?, is synchronous?
                new MyCacheListenerFactory(), null, false, false);

        mutableConfiguration.addCacheEntryListenerConfiguration(cacheEntryListenerConfiguration);

        return mutableConfiguration;
    }
}
