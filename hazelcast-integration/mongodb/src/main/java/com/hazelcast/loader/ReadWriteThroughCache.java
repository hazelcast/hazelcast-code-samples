/*
 *
 *  Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.hazelcast.loader;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.io.FileNotFoundException;

/**
 * Starter application for read-through / write-through example with Hazelcast and MongoDB.
 *
 * Connection details should be interred in `hazelcast.xml` under MapStore config for IMap
 * Properties includes: connection url, database and collection names
 *
 * @author Viktor Gamov on 11/2/15.
 *         Twitter: @gamussa
 */
public class ReadWriteThroughCache {

    public static void main(String[] args) throws FileNotFoundException {
        HazelcastInstance instance = Hazelcast.newHazelcastInstance();
        IMap<String, Supplement> supplements = instance.getMap("supplements");
        System.out.println(supplements.size());

        supplements.set("1", new Supplement("bcaa", 10));
        supplements.set("2", new Supplement("protein", 100));
        supplements.set("3", new Supplement("glucosamine", 200));

        System.out.println(supplements.size());

        supplements.evictAll();

        System.out.println(supplements.size());

        supplements.loadAll(true);

        System.out.println(supplements.size());
    }
}
