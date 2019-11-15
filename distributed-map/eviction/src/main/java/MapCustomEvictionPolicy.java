/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryView;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.MapEvictionPolicyComparator;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.spi.eviction.EvictionPolicyComparator;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.String.format;
import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.concurrent.locks.LockSupport.parkNanos;

/**
 * This sample shows custom eviction policy usage for IMap. IMap uses a sampling based approach to evict entries.
 * For more info about sampling approach, please refer to hazelcast documentation.
 *
 * To plug a custom eviction policy
 * {@link com.hazelcast.config.EvictionConfig#setComparator(EvictionPolicyComparator)} is used
 * in this example. Alternatively, custom eviction policy's class name can be also configured declaratively.
 */
public class MapCustomEvictionPolicy {

    public static void main(String[] args) {
        Config config = new Config();
        MapConfig mapConfig = config.getMapConfig("test");
        mapConfig.getEvictionConfig()
                .setMaxSizePolicy(MaxSizePolicy.PER_NODE)
                .setSize(10000)
                .setComparator(new OddEvictor());

        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        IMap<Integer, Integer> map = instance.getMap("test");

        final Queue<Integer> oddKeys = new ConcurrentLinkedQueue<Integer>();
        final Queue<Integer> evenKeys = new ConcurrentLinkedQueue<Integer>();

        map.addEntryListener(new EntryEvictedListener<Integer, Integer>() {
            @Override
            public void entryEvicted(EntryEvent<Integer, Integer> event) {
                Integer key = event.getKey();
                if (key % 2 == 0) {
                    evenKeys.add(key);
                } else {
                    oddKeys.add(key);
                }
            }
        }, false);

        // wait some more time to receive evicted-events
        parkNanos(SECONDS.toNanos(5));

        for (int i = 0; i < 15000; i++) {
            map.put(i, i);
        }

        String msg = "IMap uses sampling based eviction. After eviction is completed, we are expecting "
                + "number of evicted-odd-keys should be greater than number of evicted-even-keys"
                + "\nNumber of evicted-odd-keys = %d, number of evicted-even-keys = %d";
        out.println(format(msg, oddKeys.size(), evenKeys.size()));

        instance.shutdown();
    }

    /**
     * Odd evictor tries to evict odd keys first.
     */
    private static class OddEvictor implements MapEvictionPolicyComparator<Integer, Integer> {

        @Override
        public int compare(EntryView<Integer, Integer> o1, EntryView<Integer, Integer> o2) {
            Integer key = o1.getKey();
            if (key % 2 != 0) {
                return -1;
            }

            return 1;
        }
    }
}
