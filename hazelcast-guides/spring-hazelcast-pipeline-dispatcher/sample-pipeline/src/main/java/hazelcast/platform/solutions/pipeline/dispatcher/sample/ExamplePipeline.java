package hazelcast.platform.solutions.pipeline.dispatcher.sample;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.pipeline.*;
import com.hazelcast.map.IMap;
import hazelcast.platform.solutions.pipeline.dispatcher.PipelineDispatcherFactory;
import hazelcast.platform.solutions.pipeline.dispatcher.internal.MultiVersionRequestRouter;
import hazelcast.platform.solutions.pipeline.dispatcher.internal.MultiVersionRequestRouterConfig;

import java.util.Collections;
import java.util.Map;

public class ExamplePipeline {
    public static void main(String[] args) {
        // event journal must be enabled on the request map but is not required for the response map
        Config hzConfig = new Config();
        hzConfig.getMapConfig("*_request").getEventJournalConfig().setEnabled(true);
        hzConfig.getJetConfig().setEnabled(true);

        // this will start daemon threads - the process will not exit
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(hzConfig);
        Runtime.getRuntime().addShutdownHook(new Thread(hz::shutdown));

        // load the routing map - initially route all requests for the "reverse" service to v1
        MultiVersionRequestRouterConfig reverseServiceConfig =
                new MultiVersionRequestRouterConfig(Collections.singletonList("v1"), Collections.singletonList(1.0f));
        hz.getMap(PipelineDispatcherFactory.ROUTER_CONFIG_MAP).put("reverse", reverseServiceConfig);

        // submit 2 jobs for service version 1 and 2
        hz.getJet().newJob(ExamplePipeline.createPipelineV1(
                "reverse_v1_request",
                "reverse_response"));
        hz.getJet().newJob(ExamplePipeline.createPipelineV2(
                "reverse_v2_request",
                "reverse_response"));
    }

    static Pipeline createPipelineV1(String requestMapName, String responseMapName) {
        Pipeline pipeline = Pipeline.create();

        StreamStage<Map.Entry<String, String>> requestMapEntries =
                pipeline.<Map.Entry<String, String>>readFrom(
                        Sources.mapJournal(requestMapName, JournalInitialPosition.START_FROM_OLDEST))
                        .withIngestionTimestamps();

        requestMapEntries.writeTo(Sinks.logger( entry -> "Process Request: " + entry.getKey()));

        StreamStage<Tuple2<String, String>> reversedStrings = requestMapEntries.map(entry -> Tuple2.tuple2(
                entry.getKey(),
                new StringBuilder(entry.getValue()).reverse().toString()));

        reversedStrings.writeTo(Sinks.map(responseMapName));

        return pipeline;
    }

    static Pipeline createPipelineV2(String requestMapName, String responseMapName) {
        Pipeline pipeline = Pipeline.create();

        StreamStage<Map.Entry<String, String>> requestMapEntries =
                pipeline.<Map.Entry<String, String>>readFrom(
                                Sources.mapJournal(requestMapName, JournalInitialPosition.START_FROM_OLDEST))
                        .withIngestionTimestamps();

        requestMapEntries.writeTo(Sinks.logger( entry -> "Process Request: " + entry.getKey()));

        StreamStage<Tuple2<String, String>> reversedStrings = requestMapEntries.map(entry -> Tuple2.tuple2(
                entry.getKey(),
                entry.getValue().toUpperCase()));

        reversedStrings.writeTo(Sinks.map(responseMapName));

        return pipeline;
    }
}
