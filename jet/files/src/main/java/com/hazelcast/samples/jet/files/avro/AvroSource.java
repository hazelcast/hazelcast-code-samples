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

package com.hazelcast.samples.jet.files.avro;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.function.SupplierEx;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Util;
import com.hazelcast.jet.avro.AvroSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.map.IMap;
import org.apache.avro.io.DatumReader;
import org.apache.avro.reflect.ReflectDatumReader;

import java.nio.file.Paths;

/**
 * Demonstrates reading Apache Avro files from a directory and populating IMap
 * Run {@link AvroSink} first to create necessary Apache Avro files directory.
 */
public class AvroSource {

    private HazelcastInstance hz;

    private static Pipeline buildPipeline() {
        Pipeline p = Pipeline.create();

        p.readFrom(AvroSources.filesBuilder(AvroSink.DIRECTORY_NAME, (SupplierEx<DatumReader<User>>) ReflectDatumReader::new)
                              //Both Jet members share the same local file system
                              .sharedFileSystem(true)
                              .build())
         .map(user -> Util.entry(user.getUsername(), user))
         .writeTo(Sinks.map(AvroSink.MAP_NAME));
        return p;
    }

    public static void main(String[] args) throws Exception {
        new AvroSource().go();
    }

    private void go() {
        try {
            init();
            JetService jet = hz.getJet();
            jet.newJob(buildPipeline()).join();

            IMap<String, User> map = hz.getMap(AvroSink.MAP_NAME);
            System.out.println("Map Size: " + map.size());
            map.forEach((key, value) -> System.out.println(key + " - " + value));
        } finally {
            Hazelcast.shutdownAll();
        }
    }

    private void init() {
        if (!Paths.get(AvroSink.DIRECTORY_NAME).toFile().exists()) {
            System.out.println("Avro files directory does not exist, please run "
                    + AvroSink.class.getSimpleName() + " first to create it.");
            System.exit(0);
        }
        hz = Hazelcast.bootstrappedInstance();
    }

}
