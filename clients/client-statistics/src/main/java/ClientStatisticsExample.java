/*
 * Copyright (c) 2008-2017, Hazelcast, Inc. All Rights Reserved.
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

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class ClientStatisticsExample {
    public static void main(String[] args) {
        HazelcastInstance server = Hazelcast.newHazelcastInstance();
        HazelcastInstance client = HazelcastClient.newHazelcastClient();

        IMap<Integer, Integer> map = client.getMap("articlesObject");
        map.put(2, 500);

        map.get(2);

        try {
            // sleep twice the statistics collection time as configured at the hazelcast-client.xml
            Thread.sleep(2 * 5 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Statistics is now populated at the member side, so you can see it e.g. in Management Center.

        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }
}
