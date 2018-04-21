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

import com.hazelcast.config.AtomicReferenceConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.InvalidConfigurationException;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MergePolicyConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicReference;

/**
 * Shows that {@link mergepolicies.AtomicReferenceMergeIntegerValuesMergePolicy}
 * just works on {@link IAtomicReference} data structures.
 */
public class AtomicReferenceMergeIntegerValuesMergePolicyExample {

    public static void main(String[] args) {
        MergePolicyConfig mergePolicyConfig = new MergePolicyConfig()
                .setPolicy(mergepolicies.AtomicReferenceMergeIntegerValuesMergePolicy.class.getName());

        MapConfig mapConfig = new MapConfig("default")
                .setMergePolicyConfig(mergePolicyConfig);

        AtomicReferenceConfig atomicReferenceConfig = new AtomicReferenceConfig("default")
                .setMergePolicyConfig(mergePolicyConfig);

        Config config = new Config()
                .addMapConfig(mapConfig)
                .addAtomicReferenceConfig(atomicReferenceConfig);

        try {
            HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);

            // this works, since it's an IAtomicReference
            hazelcastInstance.getAtomicReference("myAtomicReference");

            try {
                hazelcastInstance.getMap("myMap");
            } catch (InvalidConfigurationException expected) {
                System.out.println("The configured merge policy is just suitable for IAtomicReference: "
                        + expected.getMessage());
            }
        } finally {
            Hazelcast.shutdownAll();
        }
    }
}
