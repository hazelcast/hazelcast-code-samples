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
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Hz3MemberNoData {

    public static void main(String[] args) {
        Config config = new Config();
        config.getNetworkConfig().setPort(3210);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);

        IScheduledExecutorService executor = hz.getScheduledExecutorService("printer");
        executor.scheduleAtFixedRate(new CheckMap(), 0, 1, TimeUnit.SECONDS);
    }

    private static class CheckMap implements Runnable, Serializable, HazelcastInstanceAware {

        private transient HazelcastInstance hz;
        int size;

        public CheckMap() {
            size = -1;
        }

        @Override
        public void run() {
            IMap<Object, Object> tickers = hz.getMap("tickers");
            int newSize = tickers.size();
            if (size != newSize) {
                size = newSize;
                System.out.println("Tickers map contains " + size + " tickers.");
            }
        }

        @Override
        public void setHazelcastInstance(HazelcastInstance hz) {
            this.hz = hz;
        }
    }
}