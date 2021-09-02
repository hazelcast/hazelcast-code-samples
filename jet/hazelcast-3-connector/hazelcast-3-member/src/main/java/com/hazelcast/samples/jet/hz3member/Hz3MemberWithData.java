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

package com.hazelcast.samples.jet.hz3member;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Hz3MemberWithData {

    public static void main(String[] args) {
        Config config = new Config();
        config.getNetworkConfig().setPort(3210);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);

        loadTickersToMap(hz);
    }

    private static void loadTickersToMap(HazelcastInstance hz) {
        IMap<String, String> tickers = hz.getMap("tickers");
        if (tickers.isEmpty()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Hz3MemberWithData.class.getResourceAsStream("/nasdaqlisted.txt"), UTF_8))) {
                reader.lines()
                      .skip(1)
                      .map(l -> l.split("\\|"))
                      .forEach(parts -> tickers.put(parts[0], parts[1]));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Loaded " + tickers.size() + " tickers.");
        } else {
            System.out.println("Map already contains " + tickers.size() + " tickers.");
        }
    }
}