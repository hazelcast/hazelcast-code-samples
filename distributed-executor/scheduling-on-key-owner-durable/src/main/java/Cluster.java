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

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;
import com.hazelcast.scheduledexecutor.IScheduledFuture;

import java.util.concurrent.TimeUnit;

import static com.hazelcast.examples.helper.HazelcastUtils.generateKeyOwnedBy;

public class Cluster {

    public static void main(String[] args) throws Exception {
        HazelcastInstance[] instances = new HazelcastInstance[3];
        instances[0] = Hazelcast.newHazelcastInstance();
        instances[1] = Hazelcast.newHazelcastInstance();
        instances[2] = Hazelcast.newHazelcastInstance();

        String key = generateKeyOwnedBy(instances[1]);
        IScheduledExecutorService scheduler = instances[0].getScheduledExecutorService("scheduler");
        IScheduledFuture<String> future = scheduler.scheduleOnKeyOwner(new EchoTask("My Task"), key, 5, TimeUnit.SECONDS);

        instances[1].getLifecycleService().terminate();

        Object result = future.get();
        System.out.println("Result: " + result);
        future.dispose();

        Hazelcast.shutdownAll();
    }
}
