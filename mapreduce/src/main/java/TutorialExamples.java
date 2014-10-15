/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
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
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import csv.ReaderHelper;
import tutorials.*;

import java.util.Arrays;

/**
 * State collection: http://statetable.com/
 * Person samples: http://www.briandunning.com/sample-data/
 * Crime statistics: http://hci.stanford.edu/jheer/workshop/data/
 */
public class TutorialExamples {

    public static void main(String[] args)
            throws Exception {

        // Prepare Hazelcast cluster
        HazelcastInstance hazelcastInstance = buildCluster(2);

        // Read CSV data
        ReaderHelper.read(hazelcastInstance);

        try {
            // Execute tutorials.Tutorial
            Tutorial tutorial = new Tutorial1();
            // tutorials.Tutorial tutorial = new Tutorial2();
            // tutorials.Tutorial tutorial = new Tutorial3();
            // tutorials.Tutorial tutorial = new Tutorial4();
            // tutorials.Tutorial tutorial = new Tutorial5();
            // tutorials.Tutorial tutorial = new Tutorial6();
            // tutorials.Tutorial tutorial = new Tutorial7();
            tutorial.execute(hazelcastInstance);

        } finally {
            // Shutdown cluster
            Hazelcast.shutdownAll();
        }
    }

    private static HazelcastInstance buildCluster(int memberCount) {
        Config config = new Config();
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.getJoin().getMulticastConfig().setEnabled(false);
        networkConfig.getJoin().getTcpIpConfig().setEnabled(true);
        networkConfig.getJoin().getTcpIpConfig().setMembers(Arrays.asList(new String[]{"127.0.0.1"}));

        HazelcastInstance[] hazelcastInstances = new HazelcastInstance[memberCount];
        for (int i = 0; i < memberCount; i++) {
            hazelcastInstances[i] = Hazelcast.newHazelcastInstance(config);
        }
        return hazelcastInstances[0];
    }
}
