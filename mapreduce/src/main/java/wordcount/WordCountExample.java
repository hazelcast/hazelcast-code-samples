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

package wordcount;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import tutorials.impl.ToStringPrettyfier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Map;

import static java.util.Collections.singletonList;

public class WordCountExample {

    private static final String[] DATA_RESOURCES_TO_LOAD = {"text1.txt", "text2.txt", "text3.txt"};

    private static final String MAP_NAME = "articles";

    public static void main(String[] args) throws Exception {
        // prepare Hazelcast cluster
        HazelcastInstance hazelcastInstance = buildCluster(3);

        try {
            // read data
            fillMapWithData(hazelcastInstance);

            JobTracker tracker = hazelcastInstance.getJobTracker("default");

            IMap<String, String> map = hazelcastInstance.getMap(MAP_NAME);
            KeyValueSource<String, String> source = KeyValueSource.fromMap(map);

            Job<String, String> job = tracker.newJob(source);
            ICompletableFuture<Map<String, Integer>> future = job
                    .mapper(new TokenizerMapper())
                    // activate Combiner to add combining phase!
                    // .combiner(new WordcountCombinerFactory())
                    .reducer(new WordcountReducerFactory())
                    .submit();


            System.out.println(ToStringPrettyfier.toString(future.get()));
        } finally {
            // shutdown cluster
            Hazelcast.shutdownAll();
        }
    }

    static String cleanWord(String word) {
        return word.replaceAll("[^A-Za-z0-9]", "");
    }

    private static HazelcastInstance buildCluster(int memberCount) {
        Config config = new Config();
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.getJoin().getMulticastConfig().setEnabled(false);
        networkConfig.getJoin().getTcpIpConfig().setEnabled(true);
        networkConfig.getJoin().getTcpIpConfig().setMembers(singletonList("127.0.0.1"));

        HazelcastInstance[] hazelcastInstances = new HazelcastInstance[memberCount];
        for (int i = 0; i < memberCount; i++) {
            hazelcastInstances[i] = Hazelcast.newHazelcastInstance(config);
        }
        return hazelcastInstances[0];
    }

    private static void fillMapWithData(HazelcastInstance hazelcastInstance) throws Exception {
        IMap<String, String> map = hazelcastInstance.getMap(MAP_NAME);
        for (String file : DATA_RESOURCES_TO_LOAD) {
            InputStream is = WordCountExample.class.getResourceAsStream("/wordcount/" + file);
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            map.put(file, sb.toString());

            is.close();
            reader.close();
        }
    }
}
