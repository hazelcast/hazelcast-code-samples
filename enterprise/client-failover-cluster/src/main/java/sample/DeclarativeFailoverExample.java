/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
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

package sample;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;

public class DeclarativeFailoverExample {

    public static void main(String[] args) {

        HazelcastInstance client = HazelcastClient.newHazelcastFailoverClient(null);

        //Client will log similar to the following line at INFO level by default:
        //INFO: hz.client_0 [cluster1] [3.12] HazelcastClient 3.12 (20190205 - 3af10e3) is CLIENT_CHANGED_CLUSTER
        // user can listen the cluster change and take action
        client.getLifecycleService().addLifecycleListener(event -> {
            if (LifecycleEvent.LifecycleState.CLIENT_CHANGED_CLUSTER == event.getState()) {
                System.out.println("Client has switched to a new cluster");
            }
        });
    }
}
