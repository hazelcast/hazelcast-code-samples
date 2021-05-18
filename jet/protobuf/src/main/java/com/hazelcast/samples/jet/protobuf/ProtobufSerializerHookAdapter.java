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

package com.hazelcast.samples.jet.protobuf;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.test.TestSources;
import com.hazelcast.jet.protobuf.Messages.Product;
import com.hazelcast.jet.protobuf.ProtobufSerializerHook;

/**
 * Demonstrates the usage of Protobuf serializer hook adapter.
 * <p>
 * {@link ProductSerializerHook} is discovered & registered via
 * 'META-INF/services/com.hazelcast.SerializerHook' and then used to
 * serialize local {@link com.hazelcast.collection.IList} items.
 */
public class ProtobufSerializerHookAdapter {

    private static final String LIST_NAME = "products";

    private HazelcastInstance hz;

    public static void main(String[] args) {
        new ProtobufSerializerHookAdapter().go();
    }

    private void go() {
        try {
            setup();
            JetInstance jet = hz.getJetInstance();
            jet.newJob(buildPipeline()).join();
        } finally {
            Hazelcast.shutdownAll();
        }
    }

    private void setup() {
        hz = Hazelcast.bootstrappedInstance();
    }

    private static Pipeline buildPipeline() {
        Pipeline p = Pipeline.create();

        p.readFrom(TestSources.items("jam", "marmalade"))
         .map(name -> Product.newBuilder().setName(name).build())
         .writeTo(Sinks.list(LIST_NAME));

        return p;
    }

    @SuppressWarnings("unused")
    private static class ProductSerializerHook extends ProtobufSerializerHook<Product> {

        private static final int TYPE_ID = 17;

        ProductSerializerHook() {
            super(Product.class, TYPE_ID);
        }
    }
}
