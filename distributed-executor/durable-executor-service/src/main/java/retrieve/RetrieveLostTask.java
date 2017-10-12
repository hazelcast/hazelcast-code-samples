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

package retrieve;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.durableexecutor.DurableExecutorService;
import com.hazelcast.durableexecutor.DurableExecutorServiceFuture;

import java.util.concurrent.Future;

public class RetrieveLostTask {

    public static void main(String[] args) throws Exception {
        Hazelcast.newHazelcastInstance();

        HazelcastInstance client = HazelcastClient.newHazelcastClient();
        DurableExecutorService executorService = client.getDurableExecutorService("exec");
        DurableExecutorServiceFuture<String> future = executorService.submit(new BasicTask("DurableExecutor"));
        long taskId = future.getTaskId();
        client.shutdown();

        HazelcastInstance newClient = HazelcastClient.newHazelcastClient();
        DurableExecutorService newExecutorService = newClient.getDurableExecutorService("exec");
        Future<Object> retrieveResultFuture = newExecutorService.retrieveResult(taskId);
        Object result = retrieveResultFuture.get();
        System.out.println("Result: " + result);

        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }
}
