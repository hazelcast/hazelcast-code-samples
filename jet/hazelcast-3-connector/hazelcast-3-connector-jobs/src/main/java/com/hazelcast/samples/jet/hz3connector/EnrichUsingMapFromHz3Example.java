package com.hazelcast.samples.jet.hz3connector;

import com.hazelcast.connector.Hz3Enrichment;
import com.hazelcast.connector.map.AsyncMap;
import com.hazelcast.connector.map.Hz3MapAdapter;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.function.BiFunctionEx;
import com.hazelcast.function.FunctionEx;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.datamodel.Tuple3;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.ServiceFactory;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.SourceBuilder;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.spi.properties.ClusterProperty;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.hazelcast.connector.Hz3Enrichment.hz3MapServiceFactory;
import static com.hazelcast.jet.datamodel.Tuple3.tuple3;

public class EnrichUsingMapFromHz3Example {

    private static final String HZ3_CLIENT_CONFIG =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<hazelcast-client xmlns=\"http://www.hazelcast.com/schema/client-config\"\n"
                    + "                  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                    + "                  xsi:schemaLocation=\"http://www.hazelcast.com/schema/client-config\n"
                    + "                  http://www.hazelcast.com/schema/client-config/hazelcast-client-config-3.12" +
                    ".xsd\">\n"
                    + "\n"
                    + "    <network>\n"
                    + "        <cluster-members>\n"
                    + "            <address>127.0.0.1:3210</address>\n"
                    + "        </cluster-members>\n"
                    + "    </network>\n"
                    + "</hazelcast-client>\n";

    public static void main(String[] args) {
        System.setProperty(ClusterProperty.PROCESSOR_CUSTOM_LIB_DIR.getName(),
                new File("target/source").getAbsolutePath());

        HazelcastInstance hz = Hazelcast.bootstrappedInstance();

        StreamSource<Trade> tradesSource = tradeStream(2, 2);

        ServiceFactory<Hz3MapAdapter, AsyncMap<String, String>> hz3MapSF =
                hz3MapServiceFactory("tickers", HZ3_CLIENT_CONFIG);

        ServiceFactory<String, Map<String, String>> sf = ServiceFactory
                .withCreateContextFn(context -> "context")
                .withCreateServiceFn((context, think) ->
                        new HashMap<>()
                );
        Pipeline p = Pipeline.create();
        BiFunctionEx<? super Map<String, String>, ? super Trade, Tuple3<String, String, Long>> mapFn =
                Hz3Enrichment.mapUsingIMap(
                        Trade::getTicker,
                        (trade, o) -> tuple3(o, trade.getTicker(), trade.getPrice())
                );

//        BiFunctionEx<? super Map<String, String>, ? super Trade, Tuple3<String, String, Long>> mapFn =
//                (kvMap, t) -> ((BiFunctionEx<? super Trade, ? super String, ? extends Tuple3<String, String, Long>>) (trade, o) -> tuple3(o, trade.getTicker(), trade.getPrice())).apply(t, kvMap.get(((FunctionEx<? super Trade, ? extends String>) Trade::getTicker).apply(t)));

        StreamStage<Tuple3<String, String, Long>> mapStage =
                p.readFrom(tradesSource)
                 .withoutTimestamps()
                 .mapUsingService(hz3MapSF,
                         mapFn
                 );
        mapStage.writeTo(Sinks.logger());

        JobConfig config = new JobConfig();
        config.addCustomClasspath(mapStage.name(), "hazelcast-3.12.12.jar");
        config.addCustomClasspath(mapStage.name(), "hazelcast-client-3.12.12.jar");
        config.addCustomClasspath(mapStage.name(), "hazelcast-3-connector-impl-5.0-SNAPSHOT.jar");
        hz.getJet().newJob(p, config);

    }

    public static StreamSource<Trade> tradeStream(int tradesPerSec, int maxLag) {
        return SourceBuilder
                .timestampedStream("trade-source",
                        x -> new TradeGenerator(tradesPerSec, maxLag))
                .fillBufferFn(TradeGenerator::generateTrades)
                .build();
    }

}
