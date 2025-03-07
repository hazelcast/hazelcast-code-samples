package org.example.run;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.test.TestSources;

public class App {
    public static void main(String[] args) {
        Pipeline p = Pipeline.create();
        p.readFrom(TestSources.itemStream(1))
                .withoutTimestamps()
                .writeTo(Sinks.logger());

        HazelcastInstance hz = Hazelcast.bootstrappedInstance();
        hz.getJet().newJob(p);
    }
}
