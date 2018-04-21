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

package configvalidation;

import com.hazelcast.config.AtomicLongConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.InvalidConfigurationException;
import com.hazelcast.config.MergePolicyConfig;
import com.hazelcast.config.ReplicatedMapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import mergepolicies.ComposedHitsAndCreationTimeMergePolicy;

/**
 * Shows that {@link ComposedHitsAndCreationTimeMergePolicy}
 * just works on data structures which provide hit statistics.
 */
public class ComposedHitsAndCreationTimeExample {

    public static void main(String[] args) {
        MergePolicyConfig mergePolicyConfig = new MergePolicyConfig()
                .setPolicy(ComposedHitsAndCreationTimeMergePolicy.class.getName());

        ReplicatedMapConfig mapConfig = new ReplicatedMapConfig("default")
                .setMergePolicyConfig(mergePolicyConfig);

        AtomicLongConfig atomicLongConfig = new AtomicLongConfig("default")
                .setMergePolicyConfig(mergePolicyConfig);

        Config config = new Config()
                .addReplicatedMapConfig(mapConfig)
                .addAtomicLongConfig(atomicLongConfig);

        try {
            HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);

            // this works, since ReplicatedMap provides hits and creation time
            hazelcastInstance.getReplicatedMap("myReplicatedMap");

            try {
                hazelcastInstance.getAtomicLong("myAtomicLong");
            } catch (InvalidConfigurationException expected) {
                System.out.println("IAtomicLong doesn't provide the required hit statistics: " + expected.getMessage());
            }
        } finally {
            Hazelcast.shutdownAll();
        }
    }
}
