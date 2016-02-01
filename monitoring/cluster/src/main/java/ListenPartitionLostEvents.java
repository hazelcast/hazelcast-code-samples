/*
 * Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
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
import com.hazelcast.partition.PartitionLostEvent;
import com.hazelcast.partition.PartitionLostListener;

public class ListenPartitionLostEvents {

    public static void main(String[] args) {
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(null);
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(null);

        // initialize partitions
        instance1.getMap("map1").put(0, 0);

        instance1.getPartitionService().addPartitionLostListener(new PartitionLostListener() {
            @Override
            public void partitionLost(PartitionLostEvent event) {
                System.out.println("Instance2 has lost a partition for data with 0 backup! " + event);
            }
        });

        instance2.getLifecycleService().terminate();
    }
}
