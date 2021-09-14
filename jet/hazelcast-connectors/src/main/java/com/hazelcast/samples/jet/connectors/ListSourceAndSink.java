/*
 * Copyright (c) 2008-2021, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.samples.jet.connectors;

import com.hazelcast.collection.IList;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;

import java.util.ArrayList;

/**
 * Demonstrates the usage of Hazelcast IList as source and sink with the
 * Pipeline API. It takes the contents of a list of integers, maps them to
 * strings, and dumps the results into another list. You may notice that
 * the items in the destination list are disordered &mdash; this is due to
 * the parallelism of the mapping pipeline. If you comment it out, the order
 * will be preserved.
 */
public class ListSourceAndSink {

    private static final int ITEM_COUNT = 10;
    private static final String INPUT_LIST = "inputList";
    private static final String RESULT_LIST = "resultList";

    public static void main(String[] args) {
        Config config = new Config();
        config.getJetConfig().setEnabled(true);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        JetService jet = hz.getJet();

        try {
            IList<Integer> inputList = hz.getList(INPUT_LIST);
            for (int i = 0; i < ITEM_COUNT; i++) {
                inputList.add(i);
            }

            Pipeline p = Pipeline.create();
            p.readFrom(Sources.<Integer>list(INPUT_LIST))
             .map(i -> "item" + i)
             .writeTo(Sinks.list(RESULT_LIST));

            jet.newJob(p).join();

            IList<String> outputList = hz.getList(RESULT_LIST);
            System.out.println("Result list items: " + new ArrayList<>(outputList));
        } finally {
            Hazelcast.shutdownAll();
        }
    }
}
