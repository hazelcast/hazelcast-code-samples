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
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

public class OverrideTtl {

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
        config.getMapConfig("mapWithTtl").setTimeToLiveSeconds(1);

        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);

        // Entries in this map live until they are explicitly removed
        IMap<Integer, String> defaultMap = instance.getMap("map");
        // Entries in this map are removed after one second
        IMap<Integer, String> mapWithTtl = instance.getMap("mapWithTtl");

        defaultMap.put(1, "Permanent Value");
        mapWithTtl.put(1, "Disappearing Value");

        Thread.sleep(SECONDS.toMillis(2));

        System.out.println("Entry 1 in default map is " + defaultMap.get(1));
        // we should get null because entry is evicted after two seconds
        System.out.println("Entry 1 in map with ttl is " + mapWithTtl.get(1));

        // we set a custom ttl for this entry
        defaultMap.setTtl(1, 5, TimeUnit.SECONDS);

        // we put disappearing value again and but we override its ttl this time.
        mapWithTtl.put(1, "Disappearing Value");
        mapWithTtl.setTtl(1, 3, TimeUnit.SECONDS);

        Thread.sleep(SECONDS.toMillis(2));
        System.out.println("2 seconds passed");

        // both entries are present because their ttl time has not passed yet
        System.out.println("Entry 1 in default map is " + defaultMap.get(1));
        System.out.println("Entry 1 in map with ttl is " + mapWithTtl.get(1));

        Thread.sleep(SECONDS.toMillis(2));
        System.out.println("2 seconds passed");

        // both entries are erased because enough time has passed
        System.out.println("Entry 1 in default map is " + defaultMap.get(1));
        System.out.println("Entry 1 in map with ttl is " + mapWithTtl.get(1));
        instance.shutdown();
    }
}
