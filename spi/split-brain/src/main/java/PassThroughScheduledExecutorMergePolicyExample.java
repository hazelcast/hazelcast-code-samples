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
import com.hazelcast.config.MergePolicyConfig;
import com.hazelcast.config.ScheduledExecutorConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.instance.HazelcastInstanceFactory;

/**
 * Shows the merge policy configuration for the scheduled executor via the
 * {@link com.hazelcast.spi.merge.PassThroughMergePolicy}.
 */
public final class PassThroughScheduledExecutorMergePolicyExample {

    private static final String EXECUTOR_NAME = "myScheduledExecutor";

    public static void main(String[] args) {
        // for a built-in merge policy we can just provide the simple classname
        MergePolicyConfig mergePolicyConfig = new MergePolicyConfig()
                .setPolicy("PassThroughMergePolicy");


        final ScheduledExecutorConfig scheduledExecutorConfig = new ScheduledExecutorConfig()
                .setName(EXECUTOR_NAME)
                .setMergePolicyConfig(mergePolicyConfig);

        final Config config = new Config()
                //.setProperty("hazelcast.logging.type", "none")
                .addScheduledExecutorConfig(scheduledExecutorConfig);


        HazelcastInstanceFactory.newHazelcastInstance(config);

        Hazelcast.shutdownAll();
    }
}
