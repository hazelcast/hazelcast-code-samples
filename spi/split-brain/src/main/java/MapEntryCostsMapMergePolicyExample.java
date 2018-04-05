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

import com.hazelcast.core.HazelcastInstance;
import mergepolicies.MapEntryCostsMergePolicy;

import static com.hazelcast.examples.helper.CommonUtils.assertEquals;

/**
 * Shows the split-brain healing via the {@link MapEntryCostsMergePolicy}.
 */
public final class MapEntryCostsMapMergePolicyExample extends AbstractMapMergePolicyExample {

    private MapEntryCostsMapMergePolicyExample() {
        super(MapEntryCostsMergePolicy.class);
    }

    @Override
    void duringSplitBrain(HazelcastInstance[] firstBrain, HazelcastInstance[] secondBrain) {
        map1.put("key", "value");
        map2.put("key", "bigger-value");

        System.out.println("========================== Map size (larger cluster): " + map1.size());
        System.out.println("========================== Value for \"key\" (larger cluster): " + map1.get("key"));

        System.out.println("========================== Map size (smaller cluster): " + map2.size());
        System.out.println("========================== Value for \"key\" (smaller cluster): " + map2.get("key"));
    }

    @Override
    void afterSplitBrain(HazelcastInstance[] instances) {
        System.out.println("========================== Map size (merged cluster): " + map1.size());
        System.out.println("========================== Value for \"key\" (merged cluster): " + map1.get("key"));

        assertEquals(1, map1.size());
        assertEquals(1, map2.size());

        assertEquals("bigger-value", map1.get("key"));
        assertEquals("bigger-value", map2.get("key"));
    }

    public static void main(String[] args) throws Exception {
        MapEntryCostsMapMergePolicyExample mergePolicyExample = new MapEntryCostsMapMergePolicyExample();
        mergePolicyExample.run();
    }
}
