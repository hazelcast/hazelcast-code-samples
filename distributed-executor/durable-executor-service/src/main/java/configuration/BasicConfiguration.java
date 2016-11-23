/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
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

package configuration;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.durableexecutor.DurableExecutorService;
import com.hazelcast.durableexecutor.DurableExecutorServiceFuture;

public class BasicConfiguration {

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.getDurableExecutorConfig("exec")
                .setCapacity(200)
                .setDurability(2)
                .setPoolSize(8);

        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        DurableExecutorService executorService = instance.getDurableExecutorService("exec");

        DurableExecutorServiceFuture<?> durableExecutor = executorService.submit(new EchoTask("DurableExecutor"));
        durableExecutor.get();

        Hazelcast.shutdownAll();
    }
}
