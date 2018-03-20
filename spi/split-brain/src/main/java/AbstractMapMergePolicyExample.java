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
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.examples.helper.splitbrain.SplitBrainTestSupport;
import com.hazelcast.spi.merge.SplitBrainMergePolicy;

abstract class AbstractMapMergePolicyExample extends SplitBrainTestSupport {

    private static final String MAP_NAME = "myMap";

    IMap<Object, Object> map1;
    IMap<Object, Object> map2;

    private final Class<? extends SplitBrainMergePolicy> mergePolicyClass;

    private MergeLifecycleListener mergeLifecycleListener;

    AbstractMapMergePolicyExample(Class<? extends SplitBrainMergePolicy> mergePolicyClass) {
        this.mergePolicyClass = mergePolicyClass;
    }

    @Override
    protected Config config() {
        // for a custom merge policy we have to provide the FQCN, not just the simple classname
        MergePolicyConfig mergePolicyConfig = new MergePolicyConfig()
                .setPolicy(mergePolicyClass.getName());

        MapConfig mapConfig = new MapConfig()
                .setName(MAP_NAME)
                .setInMemoryFormat(InMemoryFormat.BINARY)
                .setBackupCount(1)
                .setAsyncBackupCount(0)
                .setMergePolicyConfig(mergePolicyConfig);

        return super.config()
                //.setProperty("hazelcast.logging.type", "none")
                .addMapConfig(mapConfig);
    }

    @Override
    protected void onBeforeSplitBrainCreated(HazelcastInstance[] instances) {
        System.out.println("========================== Cluster is created!");
    }

    @Override
    protected void onAfterSplitBrainCreated(HazelcastInstance[] firstBrain, HazelcastInstance[] secondBrain) {
        mergeLifecycleListener = new MergeLifecycleListener(secondBrain.length);
        for (HazelcastInstance instance : secondBrain) {
            instance.getLifecycleService().addLifecycleListener(mergeLifecycleListener);
        }

        map1 = firstBrain[0].getMap(MAP_NAME);
        map2 = secondBrain[0].getMap(MAP_NAME);

        System.out.println("========================== Cluster is split!");

        duringSplitBrain(firstBrain, secondBrain);
    }

    @Override
    protected void onAfterSplitBrainHealed(HazelcastInstance[] instances) {
        // wait until merge completes
        mergeLifecycleListener.await();

        System.out.println("========================== Cluster is merged!");

        afterSplitBrain(instances);
    }

    abstract void duringSplitBrain(HazelcastInstance[] firstBrain, HazelcastInstance[] secondBrain);

    abstract void afterSplitBrain(HazelcastInstance[] instances);
}
