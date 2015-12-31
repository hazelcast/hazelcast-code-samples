package com.hazelcast.map.wanreplication.filter;

import com.hazelcast.core.EntryView;
import com.hazelcast.enterprise.wan.WanFilterEventType;
import com.hazelcast.map.wan.filter.MapWanEventFilter;

/**
 * Sample {@link MapWanEventFilter} implementation.
 * It simply allows all events to be replicated, no filtering is applied.
 */
public class SampleMapWanEventFilter implements MapWanEventFilter {
    @Override
    public boolean filter(String s, EntryView entryView, WanFilterEventType wanFilterEventType) {
        return false;
    }
}
