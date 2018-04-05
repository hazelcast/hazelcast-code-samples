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
import mergepolicies.ComposedHitsAndCreationTimeMergePolicy;

import static com.hazelcast.examples.helper.CommonUtils.assertEquals;
import static com.hazelcast.examples.helper.CommonUtils.sleepMillis;

/**
 * Shows the split-brain healing via the {@link ComposedHitsAndCreationTimeMergePolicy}.
 */
public final class ComposedHitsAndCreationTimeMapMergePolicyExample extends AbstractMapMergePolicyExample {

    private ComposedHitsAndCreationTimeMapMergePolicyExample() {
        super(ComposedHitsAndCreationTimeMergePolicy.class);
    }

    @Override
    void duringSplitBrain(HazelcastInstance[] firstBrain, HazelcastInstance[] secondBrain) {
        // putting the older values
        map1.put("key1", "large-value1");

        map2.put("key2", "small-value2");
        map2.put("key3", "small-value3");

        sleepMillis(500);

        // putting the newer values
        map1.put("key2", "large-value2");
        map1.put("key3", "large-value3");

        map2.put("key1", "small-value1");

        // increasing the hits
        map1.get("key2");
        map1.get("key2");
        map1.get("key2");

        map2.get("key3");
        map2.get("key3");
        map2.get("key3");

        System.out.println("========================== Map size (larger cluster): " + map1.size());
        System.out.println("========================== Value for \"key1\" (larger cluster): " + map1.get("key1"));
        System.out.println("========================== Value for \"key2\" (larger cluster): " + map1.get("key2"));
        System.out.println("========================== Value for \"key3\" (larger cluster): " + map1.get("key3"));

        System.out.println("========================== Map size (smaller cluster): " + map2.size());
        System.out.println("========================== Value for \"key1\" (smaller cluster): " + map2.get("key1"));
        System.out.println("========================== Value for \"key2\" (smaller cluster): " + map2.get("key2"));
        System.out.println("========================== Value for \"key3\" (smaller cluster): " + map2.get("key3"));
    }

    @Override
    void afterSplitBrain(HazelcastInstance[] instances) {
        System.out.println("========================== Map size (merged cluster): " + map1.size());
        System.out.println("========================== Value for \"key1\" (merged cluster): " + map1.get("key1"));
        System.out.println("========================== Value for \"key2\" (merged cluster): " + map1.get("key2"));
        System.out.println("========================== Value for \"key3\" (merged cluster): " + map1.get("key3"));

        assertEquals(3, map1.size());
        assertEquals(3, map2.size());

        assertEquals("large-value1", map1.get("key1"));
        assertEquals("large-value1", map2.get("key1"));

        assertEquals("large-value2", map1.get("key2"));
        assertEquals("large-value2", map2.get("key2"));

        assertEquals("small-value3", map1.get("key3"));
        assertEquals("small-value3", map2.get("key3"));
    }

    public static void main(String[] args) throws Exception {
        ComposedHitsAndCreationTimeMapMergePolicyExample mergePolicyExample
                = new ComposedHitsAndCreationTimeMapMergePolicyExample();
        mergePolicyExample.run();
    }
}
