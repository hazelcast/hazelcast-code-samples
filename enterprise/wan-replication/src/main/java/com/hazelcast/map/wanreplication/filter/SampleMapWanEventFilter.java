package com.hazelcast.map.wanreplication.filter;

import com.hazelcast.core.EntryView;
import com.hazelcast.enterprise.wan.WanFilterEventType;
import com.hazelcast.map.wan.filter.MapWanEventFilter;

/**
 * Sample {@link MapWanEventFilter} implementation.
 * It filters entries if their value starts with "filter".
 */
public class SampleMapWanEventFilter implements MapWanEventFilter<String, String> {
    @Override
    public boolean filter(String s, EntryView<String, String> entryView, WanFilterEventType wanFilterEventType) {
        if (entryView.getValue().startsWith("filter")) {
            return true;
        }
        return false;
    }
}
