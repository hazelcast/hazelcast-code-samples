/*
 * Copyright (c) 2008-2014, Hazelcast, Inc. All Rights Reserved.
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
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapStore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LoadAll {

    public static void main(String[] args) {
        final int numberOfEntriesToAdd = 1000;
        final String mapName = LoadAll.class.getCanonicalName();

        final Config config = createNewConfig(mapName);
        final HazelcastInstance node = Hazelcast.newHazelcastInstance(config);

        final IMap<Integer, Integer> map = node.getMap(mapName);

        populateMap(map, numberOfEntriesToAdd);

        System.out.printf("# Map store has %d elements\n", numberOfEntriesToAdd);

        map.evictAll();

        System.out.printf("# After evictAll map size\t: %d\n", map.size());

        map.loadAll(true);

        System.out.printf("# After loadAll map size\t: %d\n", map.size());
    }

    private static void populateMap(IMap<Integer, Integer> map, int itemCount) {
        for (int i = 0; i < itemCount; i++) {
            map.put(i, i);
        }
    }

    private static Config createNewConfig(String mapName) {
        final SimpleStore simpleStore = new SimpleStore();
        XmlConfigBuilder configBuilder = new XmlConfigBuilder();
        Config config = configBuilder.build();
        MapConfig mapConfig = config.getMapConfig(mapName);
        MapStoreConfig mapStoreConfig = new MapStoreConfig();
        mapStoreConfig.setImplementation(simpleStore);
        mapStoreConfig.setWriteDelaySeconds(0);
        mapConfig.setMapStoreConfig(mapStoreConfig);
        return config;
    }


    private static class SimpleStore implements MapStore {
        private ConcurrentMap store = new ConcurrentHashMap();

        @Override
        public void store(Object key, Object value) {
            store.put(key, value);
        }

        @Override
        public void storeAll(Map map) {
            final Set<Map.Entry> entrySet = map.entrySet();
            for (Map.Entry entry : entrySet) {
                final Object key = entry.getKey();
                final Object value = entry.getValue();
                store(key, value);
            }

        }

        @Override
        public void delete(Object key) {

        }

        @Override
        public void deleteAll(Collection keys) {

        }

        @Override
        public Object load(Object key) {
            return store.get(key);
        }

        @Override
        public Map loadAll(Collection keys) {
            final Map map = new HashMap();
            for (Object key : keys) {
                final Object value = load(key);
                map.put(key, value);
            }
            return map;
        }

        @Override
        public Set loadAllKeys() {
            return store.keySet();
        }
    }


}
