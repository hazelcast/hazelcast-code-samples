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
import com.hazelcast.core.HazelcastInstance;
import mergepolicies.UserContextMergePolicy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.hazelcast.examples.helper.CommonUtils.assertEquals;
import static mergepolicies.UserContextMergePolicy.TRUTH_PROVIDER_ID;

/**
 * Shows the split-brain healing via the {@link UserContextMergePolicy}.
 */
public final class UserContextMergePolicyExample extends AbstractMapMergePolicyExample {

    private UserContextMergePolicyExample() {
        super(UserContextMergePolicy.class);
    }

    @Override
    protected Config config() {
        // we use the user context to provide access to our TruthProvider in the merge policy
        ConcurrentMap<String, Object> userContext = new ConcurrentHashMap<String, Object>();
        userContext.put(TRUTH_PROVIDER_ID, new ExampleTruthProvider());

        return super.config()
                .setUserContext(userContext);
    }

    @Override
    void duringSplitBrain(HazelcastInstance[] firstBrain, HazelcastInstance[] secondBrain) {
        map2.put("key1", 23);
        map2.put("key2", 42);
        map2.put("key3", 1337);

        System.out.println("========================== Map size (larger cluster): " + map1.size());

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

        assertEquals(1, map1.size());
        assertEquals(1, map2.size());

        assertEquals(42, map1.get("key2"));
        assertEquals(42, map2.get("key2"));
    }

    public static void main(String[] args) throws Exception {
        UserContextMergePolicyExample mergePolicyExample = new UserContextMergePolicyExample();
        mergePolicyExample.run();
    }

    private class ExampleTruthProvider implements UserContextMergePolicy.TruthProvider {

        @Override
        public boolean isMergeable(Object mergingValue, Object existingValue) {
            return mergingValue instanceof Integer && (Integer) mergingValue == 42;
        }
    }
}
