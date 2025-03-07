package org.examples.jet.snapshot;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.pipeline.JournalInitialPosition;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;

public class JobV2 {

    public static void main(String[] args) {
        var p = Pipeline.create();
        var transactionSource = Sources.mapJournal("transaction", JournalInitialPosition.START_FROM_OLDEST);
        var loggerSink = Sinks.logger();
        p.readFrom(transactionSource)
                .withIngestionTimestamps()
                .setName("Emit Transactions")
                .map(e -> {
                    System.out.printf("The transaction '%s' is being executed in 'job-v2'\n", e.getKey());
                    // execute the transaction
                    return String.format("[Job V2] transaction:'%s' payload:'%s'", e.getKey(), e.getValue());
                })
                .setName("Apply Transactions")
                .writeTo(loggerSink)
                .setName("Log Transactions");

        HazelcastInstance hz = Hazelcast.bootstrappedInstance();
        hz.getJet().newJob(p);
    }

}
