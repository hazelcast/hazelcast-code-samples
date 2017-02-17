/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
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

import com.hazelcast.scheduledexecutor.StatefulTask;

import java.io.Serializable;
import java.util.Map;

public class EchoTask
        implements Runnable, StatefulTask<String, Integer>, Serializable {

    private static final String COUNTER_KEY = "Counter";

    private final String msg;

    private transient int counter;

    public EchoTask(String msg) {
        this.msg = msg;
    }

    @Override
    public void run() {
        System.out.println("Running: " + msg + " with count: " + counter);
    }

    @Override
    public void save(Map<String, Integer> map) {
        map.put(COUNTER_KEY, ++counter);
    }

    @Override
    public void load(Map<String, Integer> map) {
        counter = map.containsKey(COUNTER_KEY) ? map.get(COUNTER_KEY) : 0;
    }
}
