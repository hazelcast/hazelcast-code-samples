package com.hazelcast.cache.wanreplication.filter;

import com.hazelcast.cache.CacheEntryView;
import com.hazelcast.cache.wan.filter.CacheWanEventFilter;
import com.hazelcast.enterprise.wan.WanFilterEventType;

/**
 * Sample {@link CacheWanEventFilter} implementation.
 * It filters entries if their value starts with "filter".
 */
public class SampleCacheWanEventFilter implements CacheWanEventFilter<String, String> {

    @Override
    public boolean filter(String s, CacheEntryView<String, String> cacheEntryView, WanFilterEventType wanFilterEventType) {
        if (cacheEntryView.getValue().startsWith("filter")) {
            return true;
        }
        return false;
    }
}
