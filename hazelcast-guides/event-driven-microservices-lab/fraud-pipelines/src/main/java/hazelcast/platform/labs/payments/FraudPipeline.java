package hazelcast.platform.labs.payments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.config.ProcessingGuarantee;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.datamodel.Tuple3;
import com.hazelcast.jet.kafka.KafkaSinks;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.*;
import hazelcast.platform.labs.payments.domain.Transaction;

import java.util.Map;
import java.util.Properties;

public class FraudPipeline {

    /*
     * Format a json string with the transaction_id and approval status
     * as shown below
     * {
     *    "transaction_id": "12345",
     *    "approved": true
     * }
     */
    public static String resultJson(String txnId, boolean approved){
        return "{ \"transaction_id\": \"" + txnId + "\", \"approved\": " + (approved ? "true" : "false") + "}";
    }

    private static Properties kafkaProperties(String bootstrapServers){
        Properties kafkaConnectionProps = new Properties();
        kafkaConnectionProps.setProperty("bootstrap.servers", bootstrapServers);
        kafkaConnectionProps.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaConnectionProps.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaConnectionProps.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaConnectionProps.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaConnectionProps.setProperty("auto.offset.reset", "earliest");
        return kafkaConnectionProps;
    }

    public static Pipeline createPipeline(String kafkaBootstrapServers, String inputTopic, String outputTopic){
        Pipeline pipeline = Pipeline.create();

        Properties kafkaProperties = kafkaProperties(kafkaBootstrapServers);

        /*
         * Create a Source to read from the input topic
         */
        StreamSource<Map.Entry<String, String>> source =
                KafkaSources.kafka(kafkaProperties, inputTopic);

        /*
         * Create a Sink to write to the output topic. This sink will expect a Map.Entry<K,V> with the
         * entry key specifying the message key and the entry value specifying the message value
         */
        Sink<Map.Entry<String, String>> sink = KafkaSinks.kafka(kafkaProperties, outputTopic);

        /*
         * We need to parse and format JSON. We use Jackson for that.  We don't want to create a new
         * instance of ObjectMapper every time an event is processed. Instead, we create a "service"
         * which Hazelcast will instantiate once (per node) and re-use during event processing.
         */
        ServiceFactory<?, ObjectMapper> jsonService = ServiceFactories.sharedService(ctx -> new ObjectMapper());

        /*
         * Read a stream of Map.Entry<String,String> from the stream. entry.key is the cc# and
         * entry.value is a json string similar to the one shown below
         *
         * {
         *   "card_number": "6771-8952-0704-5425",
         *   "transaction_id": "1710969754",
         *   "amount": 42,
         *   "merchant_id": "8222"
         * }
         *
         */
        StreamStage<Map.Entry<String, String>> cardTransactions =
                pipeline.readFrom(source).withIngestionTimestamps().setName("read topic");

        /*
         * Use the json service to parse the JSON message into an instance
         * of Transaction.
         */

        StreamStage<Transaction> transactions =
                cardTransactions.mapUsingService(jsonService, (svc, entry) -> svc.readValue(entry.getValue(), Transaction.class));


        /*
         * This stage returns the tuple (credit_card_number, transaction_id, true)
         */
        StreamStage<Tuple3<String, String, Boolean>> approvals =
                transactions.map(txn -> Tuple3.tuple3(txn.getCardNumber(), txn.getTransactionId(), true));

        // LAB 2: Modify the map operation above. The last item in the tuple should be false (not approved) if
        //        the transaction amount is over 5000


        /*
         * For each transaction, create a Map.Entry where the key is the credit card number and the value is
         * a piece of json that contains the transaction_id and the approval_status (see the "resultJson" method)
         *
         * Then write the entry directly to the output topic
         *
         * Note:the Hazelcast Tuple2 class implements Map.Entry
         */

        approvals.map(approval -> Tuple2.tuple2(
                approval.f0(),
                FraudPipeline.resultJson(approval.f1(), approval.f2()) )
        ).writeTo(sink);

        return pipeline;
    }

    // expects arguments: kafka bootstrap servers, input kafka topic, output kafka topic
    public static void main(String []args){
        if (args.length != 3){
            System.err.println("Please provide 3 arguments: kafka bootstrap servers, input kafka topic and output kafka topic");
            System.exit(1);
        }

        Pipeline pipeline = createPipeline(args[0], args[1], args[2]);
        pipeline.setPreserveOrder(false);   // nothing in here requires order
        JobConfig jobConfig = new JobConfig();
        jobConfig.setName("Fraud Checker");
        jobConfig.setProcessingGuarantee(ProcessingGuarantee.EXACTLY_ONCE);
        HazelcastInstance hz = Hazelcast.bootstrappedInstance();
        hz.getJet().newJob(pipeline, jobConfig);
    }
}
