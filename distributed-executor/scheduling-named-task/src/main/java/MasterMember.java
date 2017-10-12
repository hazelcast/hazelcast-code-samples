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

import static com.hazelcast.scheduledexecutor.TaskUtils.named;

public class MasterMember {

    public static void main(String[] args) throws Exception {
        HazelcastInstance instance = Hazelcast.newHazelcastInstance();

        IScheduledExecutorService scheduler = instance.getScheduledExecutorService("scheduler");
        IScheduledFuture<String> future = scheduler.schedule(named("MyTask",
                new EchoTask("foobar")), 5, TimeUnit.SECONDS);

        Object result = future.get();
        System.out.println(future.getHandler().getTaskName() + " result: " + result);

        future.dispose();

        Hazelcast.shutdownAll();
    }
}
