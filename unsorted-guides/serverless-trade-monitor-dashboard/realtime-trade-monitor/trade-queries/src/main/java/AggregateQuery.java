import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.function.FunctionEx;
import com.hazelcast.function.SupplierEx;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.accumulator.MutableReference;
import com.hazelcast.jet.aggregate.AggregateOperation;
import com.hazelcast.jet.aggregate.AggregateOperation1;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.config.ProcessingGuarantee;
import com.hazelcast.jet.datamodel.Tuple3;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamStage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import com.hazelcast.jet.kafka.KafkaSources;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import static com.hazelcast.jet.aggregate.AggregateOperations.allOf;
import static com.hazelcast.jet.aggregate.AggregateOperations.counting;
import static com.hazelcast.jet.aggregate.AggregateOperations.summingLong;

public class AggregateQuery {

    public static final String TOPIC = "trades";

    public static void aggregateQuery(HazelcastInstance hzInstance, String servers) throws IOException {
        try {
            JobConfig query1config = new JobConfig()
                    .setProcessingGuarantee(ProcessingGuarantee.EXACTLY_ONCE)
                    .setName("AggregateQuery")
                    .addClass(TradeJsonDeserializer.class)
                    .addClass(Trade.class)
                    .addClass(AggregateQuery.class);

            JetService jetService = hzInstance.getJet();
            jetService.newJobIfAbsent(createPipeline(servers), query1config);

        } finally {
            Hazelcast.shutdownAll();
        }
    }

    private static Pipeline createPipeline(String servers) throws IOException {
        Pipeline p = Pipeline.create();

        StreamStage<Trade> source =
                p.readFrom(KafkaSources.<String, Trade, Trade>kafka(kafkaSourceProps(servers),
                        ConsumerRecord::value, TOPIC))
                        .withoutTimestamps();


        StreamStage<Entry<String, Tuple3<Long, Long, Integer>>> aggregated =
                source
                        .groupingKey(Trade::getSymbol)
                        .rollingAggregate(allOf(
                                counting(),
                                summingLong(trade -> trade.getPrice() * trade.getQuantity()),
                                latestValue(trade -> trade.getPrice())
                        ))
                        .setName("aggregate by symbol");

        // write results to IMDG IMap
        aggregated.writeTo(Sinks.map("query1_Results"));
        // write results to Kafka topic
//        aggregated
//                .drainTo(KafkaSinks.kafka(kafkaSinkProps(servers), "query1_Results"));
        return p;
    }

    private static Properties kafkaSourceProps(String servers) throws IOException {
        Properties props = new Properties();
        props.load(AggregateQuery.class.getResourceAsStream("kafka.properties"));
        if (!servers.isEmpty()) {
            props.setProperty("bootstrap.servers", servers);
        }
        props.setProperty("auto.offset.reset", "earliest");
        props.setProperty("key.deserializer", StringDeserializer.class.getName());
        props.setProperty("value.deserializer", TradeJsonDeserializer.class.getName());
        return props;
    }

    private static <T, R> AggregateOperation1<T, ?, R> latestValue(FunctionEx<T, R> toValueFn) {
        return AggregateOperation.withCreate((SupplierEx<MutableReference<R>>) MutableReference::new)
                .<T>andAccumulate((ref, t) -> ref.set(toValueFn.apply(t)))
                .andExportFinish(MutableReference::get);
    }
}
