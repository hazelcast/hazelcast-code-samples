/*
 * Copyright (c) 2008-2014, Hazelcast, Inc. All Rights Reserved.
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
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberSelector;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ForceLocalMemberToBeSafe {

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.getMemberAttributeConfig().setBooleanAttribute("EAST", true);

        HazelcastInstance node = Hazelcast.newHazelcastInstance(config);
        IExecutorService executorService = node.getExecutorService(ForceLocalMemberToBeSafe.class.getName());

        Future<Boolean> result = executorService.submit(new MemberSafe(), new MemberSelector() {
            @Override
            public boolean select(Member member) {
                Boolean east = member.getBooleanAttribute("EAST");
                return Boolean.TRUE.equals(east);
            }
        });

        System.out.printf("# Is forcing member to be safe is successful\t: %s\n", result.get());
    }

    private static class MemberSafe implements Callable<Boolean>, HazelcastInstanceAware, Serializable {

        private HazelcastInstance node;

        @Override
        public Boolean call() throws Exception {
            boolean safe = node.getPartitionService().forceLocalMemberToBeSafe(5, TimeUnit.SECONDS);
            return safe;
        }

        @Override
        public void setHazelcastInstance(HazelcastInstance node) {
            this.node = node;
        }
    }
}
