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

package com.hazelcast.samples.jet.wordcount;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetMemberSelector;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;
import static com.hazelcast.function.Functions.wholeItem;
import static com.hazelcast.jet.Traversers.traverseArray;
import static com.hazelcast.jet.aggregate.AggregateOperations.counting;
import static java.util.Comparator.comparingLong;

/**
 * Demonstrates a simple Word Count job in the Pipeline API with member selector.
 * Inserts the text of The Complete Works of William Shakespeare into a Hazelcast
 * IMap, then lets Jet count the words in it and write its findings to
 * another IMap. The example looks at Jet's output and prints the 100 most
 * frequent words.
 */
public class WordCountWithComputeIsolation {

    private static final String BOOK_LINES = "bookLines";
    private static final String COUNTS = "counts";

    private static Pipeline buildPipeline() {
        Pattern delimiter = Pattern.compile("\\W+");
        Pipeline p = Pipeline.create();
        p.readFrom(Sources.<Long, String>map(BOOK_LINES))
                .flatMap(e -> traverseArray(delimiter.split(e.getValue().toLowerCase())))
                .filter(word -> !word.isEmpty())
                .groupingKey(wholeItem())
                .aggregate(counting())
                .writeTo(Sinks.map(COUNTS));
        return p;
    }

    public static void main(String[] args) {
        Config dataMemberConfig = new Config();
        dataMemberConfig.getJetConfig().setEnabled(true);
        dataMemberConfig.setLicenseKey(ENTERPRISE_LICENSE_KEY);

        Config liteMemberConfig = new Config();
        liteMemberConfig.getJetConfig().setEnabled(true);
        liteMemberConfig.setLicenseKey(ENTERPRISE_LICENSE_KEY);
        liteMemberConfig.setLiteMember(true);

        var hz = new HazelcastInstance[3];
        hz[0] = Hazelcast.newHazelcastInstance(dataMemberConfig);
        hz[1] = Hazelcast.newHazelcastInstance(liteMemberConfig);
        hz[2] = Hazelcast.newHazelcastInstance(liteMemberConfig);

        new WordCountWithComputeIsolation().go(hz);
    }

    private void go(HazelcastInstance[] hz) {
        try {
            init(hz);
            System.out.println("\nCounting words... ");
            long start = System.nanoTime();
            Pipeline p = buildPipeline();
            JetService jet = hz[0].getJet();
            // New in 5.5: use JetMemberSelector.ALL_LITE_MEMBERS to run the job on all lite members only.
            jet.newJobBuilder(p).withMemberSelector(JetMemberSelector.ALL_LITE_MEMBERS).start().join();
            System.out.println("done in " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " milliseconds.");
            Map<String, Long> results = hz[0].getMap(COUNTS);
            checkResults(results);
            printResults(results);
        } finally {
            Hazelcast.shutdownAll();
        }
    }

    private void init(HazelcastInstance[] hz) {
        System.out.println("Loading The Complete Works of William Shakespeare");
        try {
            long[] lineNum = {0};
            Map<Long, String> bookLines = new HashMap<>();
            InputStream stream = getClass().getResourceAsStream("/books/shakespeare-complete-works.txt");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                reader.lines().forEach(line -> bookLines.put(++lineNum[0], line));
            }
            hz[0].getMap(BOOK_LINES).putAll(bookLines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Long> checkResults(Map<String, Long> counts) {
        if (counts.get("the") != 27_843) {
            throw new AssertionError("Wrong count of 'the'");
        }
        System.out.println("Count of 'the' is valid");
        return counts;
    }

    private static void printResults(Map<String, Long> counts) {
        final int limit = 100;

        StringBuilder sb = new StringBuilder(String.format(" Top %d entries are:%n", limit));
        sb.append("/-------+---------\\\n");
        sb.append("| Count | Word    |\n");
        sb.append("|-------+---------|\n");
        counts.entrySet().stream()
                .sorted(comparingLong(Map.Entry<String, Long>::getValue).reversed())
                .limit(limit)
                .forEach(e -> sb.append(String.format("|%6d | %-8s|%n", e.getValue(), e.getKey())));
        sb.append("\\-------+---------/\n");

        System.out.println(sb.toString());
    }
}
