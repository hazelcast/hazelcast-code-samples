package com.hazelcast.cache.wanreplication.filter;

import com.hazelcast.cache.CacheEntryView;
import com.hazelcast.cache.wan.filter.CacheWanEventFilter;
import com.hazelcast.enterprise.wan.WanFilterEventType;

/**
 * Sample {@link CacheWanEventFilter} implementation.
 * It simply allows all events to be replicated, no filtering is applied.
 */
public class SampleCacheWanEventFilter implements CacheWanEventFilter {

    @Override
    public boolean filter(String s, CacheEntryView cacheEntryView, WanFilterEventType wanFilterEventType) {
        return false;
    }
}
