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
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MergePolicyConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.instance.HazelcastInstanceFactory;
import mergepolicies.UserContextMergePolicy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static mergepolicies.UserContextMergePolicy.TRUTH_PROVIDER_ID;

/**
 * Shows the split-brain healing via the {@link UserContextMergePolicy}.
 */
public final class UserContextMergePolicyExample {

    private static final String MAP_NAME = "myMap";

    public static void main(String[] args) throws Exception {
        // for a custom merge policy we have to provide the FQCN, not just the simple classname
        MergePolicyConfig mergePolicyConfig = new MergePolicyConfig()
                .setPolicy(UserContextMergePolicy.class.getName());

        MapConfig mapConfig = new MapConfig()
                .setName(MAP_NAME)
                .setInMemoryFormat(InMemoryFormat.BINARY)
                .setBackupCount(1)
                .setAsyncBackupCount(0)
                .setMergePolicyConfig(mergePolicyConfig);

        // we use the user context to provide access to our TruthProvider in the merge policy
        ConcurrentMap<String, Object> userContext = new ConcurrentHashMap<String, Object>();
        userContext.put(TRUTH_PROVIDER_ID, new ExampleTruthProvider());

        final Config config = new Config()
                //.setProperty("hazelcast.logging.type", "none")
                .addMapConfig(mapConfig)
                .setUserContext(userContext);


        HazelcastInstanceFactory.newHazelcastInstance(config);

        Hazelcast.shutdownAll();
    }

    private static class ExampleTruthProvider implements UserContextMergePolicy.TruthProvider {

        @Override
        public boolean isMergeable(Object mergingValue, Object existingValue) {
            return mergingValue instanceof Integer && (Integer) mergingValue == 42;
        }
    }
}
