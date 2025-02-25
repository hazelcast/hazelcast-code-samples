package hazelcast.platform.labs.machineshop.solutions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.aggregate.AggregateOperations;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.datamodel.KeyedWindowResult;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.datamodel.Tuple4;
import com.hazelcast.jet.kafka.KafkaSinks;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.*;
import com.hazelcast.nio.serialization.genericrecord.GenericRecord;
import com.hazelcast.nio.serialization.genericrecord.GenericRecordBuilder;
import hazelcast.platform.labs.machineshop.domain.MachineEvent;
import hazelcast.platform.labs.machineshop.domain.Names;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

/*
 **************** SOLUTION ****************
 */

public class TemperatureMonitorPipelineSolution {


    /*
     * Used for stateful filtering.  Must be serializable as it is included in stream snapshots
     */
    public static class CurrentState implements Serializable {
        private String color;

        public CurrentState(){
            color="";
        }

        public String getColor(){
            return color;
        }

        public void setColor(String color){
            this.color = color;
        }

    }

    private static String categorizeTemp(double temp, short warningLimit, short criticalLimit){
        String result;
        if (temp > (double) criticalLimit)
            result = "red";
        else if (temp > (double) warningLimit)
            result = "orange";
        else
            result = "green";

        return result;
    }

    public static GenericRecord machineStatus(String serialNum, short averageBitTemp10s, long timestamp){
        return GenericRecordBuilder.compact(Names.MACHINE_STATUS_TYPE_NAME)
                .setString("serialNumber", serialNum)
                .setInt16("averageBitTemp10s", averageBitTemp10s)
                .setInt64("eventTime", timestamp)
                .build();
    }

    /*
     * Write your Pipeline here.
     *
     * DataStructureDefinitions
     *
     *   GenericRecord of MachineStatus;
     *     GenericRecord machineStatusSummary;
     *     String serialNum = machineStatusSummary.getString("serialNum);
     *     short averageBitTemp10s = machineStatusSummary.getInt16("averageBitTemp10s");
     *
     *   GenericRecord of MachineProfile
     *     GenericRecord profile;
     *     String serialNum = profile.getString("serialNum");
     *     String location = profile.getString("location");
     *     String block = profile.getString("block");
     *     short warningTemp = profile.getInt16("warningTemp");
     *     short criticalTemp = profile.getInt16("criticalTemp");
     *
     *   The incoming events are json encoded.  An example is shown below
     *      {
	 *         "serialNum": "UVQ438",
	 *         "eventTime": 1713994242415,
	 *         "bitRPM": 10000,
	 *         "bitTemp": 147,
	 *         "bitPositionX": 0,
	 *         "bitPositionY": 0,
	 *         "bitPositionZ": 0
	 *      }
     *
     * Useful References:
     *    https://docs.hazelcast.org/docs/latest/javadoc/com/hazelcast/jet/pipeline/StreamStage.html
     */
    public static Pipeline createPipeline(Properties kafkaConnectionProps,
                                          String telemetryTopicName,
                                          String controlsTopicName){
        Pipeline pipeline = Pipeline.create();

        /*
         * Set up the Kafka Source this Pipeline
         */
        StreamSource<Map.Entry<String, String>> telemetryTopic =
                KafkaSources.kafka(kafkaConnectionProps, telemetryTopicName);


        /*
         * Read events from the telemetry topic.  The key, which will be used to partition the consumers, is
         * the machine serial number and the value is the json-encoded event
         */
        StreamStage<Map.Entry<String, String>> rawEvents =
                pipeline.readFrom(telemetryTopic).withNativeTimestamps(2000).setName("read telemetry events");

        /*
         * Parse the JSON value into a MachineEvent
         *
         *
         * We want to be able to process 1000's of events per second, or more.  We don't want to create an instance of
         * ObjectMapper every time we parse or format json. Instead, we use a "Service" to create one instance
         * on each node that will be shared by all processors.  This is done via the "mapUsingService" method.
         * See: https://docs.hazelcast.com/hazelcast/latest/pipelines/transforms#mapusingservice
         *
         * INPUT: Map.Entry<String, String>
         *        The key is the machine serial number and the value is a JSON formatted MachineEvent
         *
         * OUTPUT: MachineEvent
         *
         */
        ServiceFactory<?, ObjectMapper> jsonParserFactory = ServiceFactories.sharedService((ctx) -> new ObjectMapper());
        StreamStage<MachineEvent> machineEvents = rawEvents.mapUsingService(jsonParserFactory,
                        (mapper, event) -> mapper.readValue(event.getValue(), MachineEvent.class))
                .setName("parse json");


        /*
         * Group the events by serial number. For each serial number, compute the average temperature over a 10s
         * tumbling window.
         *
         * INPUT: MachineEvent
         *
         * OUTPUT: KeyedWindowResult<String, Double>
         *
         * The general template for aggregation looks like this:
         *
         * StreamStage<KeyedWindowResult<String, Double>> averageTemps = statusEvents.groupingKey( GET KEY LAMBDA )
         *                                  .window( WINDOW DEFINITION )
         *                                  .aggregate(AggregateOperations.averagingLong( GET BIT TEMP LAMBDA);
         *
         * For available Window Definitions and Aggregations, see:
         *   https://docs.hazelcast.org/docs/latest/javadoc/com/hazelcast/jet/pipeline/WindowDefinition.html
         *   https://docs.hazelcast.org/docs/latest/javadoc/com/hazelcast/jet/aggregate/AggregateOperations.html
         *
         */
        StreamStage<KeyedWindowResult<String, Double>> averageTemps = machineEvents
                .groupingKey(MachineEvent::getSerialNum)
                .window(WindowDefinition.tumbling(10000))
                .aggregate(AggregateOperations.averagingLong(MachineEvent::getBitTemp))
                .setName("Average Temp");


        /*
         * Write the averageTemperatures to the "machine_status" map as MachineStatus GenericRecords
         *
         * INPUT: KeyedWindowResult<String, Double>
         *
         * OUTPUT: None - this is a sink
         *
         * Use the Sinks.map variant that takes a map name, a key extractor function and a value extractor function.
         *    The value extractor needs to build a GenericRecord.  The "machineStatus" method above has been
         *    provided for this purpose.
         *
         * Useful References:
         *   https://docs.hazelcast.org/docs/latest/javadoc/com/hazelcast/jet/datamodel/KeyedWindowResult.html
         *   https://docs.hazelcast.org/docs/latest/javadoc/com/hazelcast/jet/pipeline/Sinks.html#map(java.lang.String,com.hazelcast.function.FunctionEx,com.hazelcast.function.FunctionEx)
         *
         */
        averageTemps.writeTo(Sinks.map(Names.MACHINE_STATUS_MAP_NAME,
                                        KeyedWindowResult::getKey,
                                        kwr -> machineStatus(kwr.getKey(), kwr.getValue().shortValue(), kwr.end())));


        /*
         * Look up the machine profile for this machine from the machine_profiles map.  Output a
         * 4-tuple (serialNum, avg temp, warning temp, critical_temp)
         *
         * INPUT: StreamStage<KeyedWindowResult<String, Double>>
         *        streamStage.getKey() is the serial number
         *        streamStage.getValue() is the averageTemperature over the window.
         *
         * OUTPUT: Tuple4<String, Double, Short, Short>
         *         The members of the Tuple4 are: (serial number, average temp, warning temp, critical temp)
         *         The last 2 values are looked up from the machine_profiles map using the mapUsingIMap method.
         *
         * We would like for the map lookup to be local which means each event needs to be routed to the
         * machine that owns that machine_profile entry.  This is accomplished by setting a grouping key
         * using the groupingKey method.  The groupingKey method returns a  StreamStageWithKey.  Since the
         * key is already known, StreamStageWithKey.mapUsingImap will automatically use it to do the lookup on the map.
         * As opposed to StreamStage.mapUsingIMap, you do not need to supply a "getKey" function.  Instead, you supply
         * a BiFunction which takes the input even and the value returned from the map lookup and returns a new event.
         *
         * In this case, the value in the machine_profiles map is a GenericRecord of a MachineProfile.  For
         * the available field names, see the comment at the top of this class.
         *
         * The general form is:
         *
         * StreamStage<Tuple4<String,Double,Short,Short>> temperaturesAndLimits
         *      = averageTemps
         *           .groupingKey( GET KEY LAMBDA)
         *           ,mapUsingIMap( Names.PROFILE_MAP_NAME, (w, p) -> LAMBDA RETURNING Tuple4)
         *
         * where p is a MachineProfile GenericRecord
         *       w is the KeyedWindowResult from the averageTemps stage.
         *
         * See:
         *   "mapUsingIMap" in https://docs.hazelcast.org/docs/latest/javadoc/com/hazelcast/jet/pipeline/StreamStageWithKey.html
         *    https://docs.hazelcast.org/docs/latest/javadoc/com/hazelcast/jet/datamodel/KeyedWindowResult.html
         *    https://docs.hazelcast.org/docs/latest/javadoc/com/hazelcast/jet/datamodel/Tuple4.html
         */
        StreamStage<Tuple4<String, Double, Short, Short>> temperaturesAndLimits =
                averageTemps.groupingKey(KeyedWindowResult::getKey)
                        .<GenericRecord, Tuple4<String, Double, Short, Short>>mapUsingIMap(Names.PROFILE_MAP_NAME,
                (window, mp) -> Tuple4.tuple4(window.getKey(), window.getValue(), mp.getInt16("warningTemp"), mp.getInt16("criticalTemp")))
                .setName("Lookup Temp Limits");

        /*
         * Using a simple "map" stage, categorize the temperature as "green", "red" or "orange" and
         * return a Tuple2 (serialNum, color).
         *
         * INPUT: Tuple4<String,Double,Short,Short) i.e.  (serialNum, avg temp, warning temp, critical_temp)
         * OUTPUT: Tuple2<String,String> i.e. (serialNumber, red/orange/green)
         *
         * See:
         *   the "categorizeTemp" function at the top of this class
         *   "map" in https://docs.hazelcast.org/docs/latest/javadoc/com/hazelcast/jet/pipeline/StreamStage.html
         */
        StreamStage<Tuple2<String,String>> labels =
                temperaturesAndLimits.map(tuple -> Tuple2.tuple2(tuple.f0(), categorizeTemp(tuple.f1(), tuple.f2(), tuple.f3())))
                .setName("Apply Label");

        /*
         * We  want to write to the output map only if the current color has changed.  This prevents flooding the
         * down stream listeners with irrelevant events.  We can use   StreamStageWithKey.filterStateful to do this.
         * The filter will remember the last value for each key.
         *
         * INPUT: Tuple2<String,String>  i.e. (serialNumber, red/orange/green)
         * OUTPUT: Tuple2<String,String>  i.e. (serialNumber, red/orange/green)
         *         OR nothing if there is no change relative to the previous event with the same serial number
         *
         * The CurrentState class in this file should be used to hold the remembered value.
         *
         * The solution will look like this:
         *
         * StreamStage<Tuple2<String,String>> changedLabels =
         *   labels.groupingKey(GET SERIAL NUM LAMBDA)
         *         .filterStateful(CurrentState::new, (cs, event) -> FILTER LAMBDA)
         *
         * Where cs is the instance of CurrentState related to this key
         *       event is the Tuple2 input event
         *
         * Note:
         *    When the incoming value is not equal to the previous value, don't forget to update the CurrentState
         *    object with the new value!
         *
         * See:
         *    "filterStateful" in https://docs.hazelcast.org/docs/latest/javadoc/com/hazelcast/jet/pipeline/StreamStageWithKey.html
         */
        StreamStage<Tuple2<String,String>> changedLabels =
                labels.groupingKey(Tuple2::f0)
                        .filterStateful(CurrentState::new,
                                (cs, label) -> {
                                    boolean same = cs.getColor().equals(label.f1());
                                    if (!same) cs.setColor(label.f1());
                                    return !same;
                                }).setName("Label Changes");

        /*
         * Finally, we can write the status out to the machine_controls Kafka topic.  Tuple2<K,V> also implements
         * Map.Entry<K,V> so we can just supply it directly to the Sink that was declared above.
         *
         * INPUT: Tuple2<String,String>  i.e. (serialNumber, red/orange/green)
         * OUTPUT: None
         *
         * Create a Sink for the "machine_controls" topic using  KafkaSinks.kafka (see reference) then finish the
         * pipeline with writing changedLabels to it.
         *
         * See:
         *    https://docs.hazelcast.org/docs/latest/javadoc/com/hazelcast/jet/kafka/KafkaSinks.html
         */
        Sink<Map.Entry<String,String>> sink = KafkaSinks.kafka(kafkaConnectionProps, controlsTopicName);
        changedLabels.writeTo(sink);

        return pipeline;
    }
    /*
     * Three command line arguments are expected, in this order
     *  KAFKA_BOOTSTRAP_SERVERS
     *  MACHINE_EVENTS_TOPIC      (the name of the input topic)
     *  MACHINE_CONTROLS_TOPIC    (the name of the output topic)
     */
    public static void main(String []args){
        if (args.length != 3){
            throw new RuntimeException("Expected 3 arguments (KAFKA_BOOTSTRAP_SERVERS, MACHINE_EVENTS_TOPIC, MACHINE_CONTROLS_TOPIC) but found " + args.length);
        }

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", args[0]);
        props.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("auto.offset.reset", "latest");

        Pipeline pipeline = createPipeline(props, args[1], args[2]);
        /*
         * We need to preserve order in this pipeline because of the change detection step
         */
        pipeline.setPreserveOrder(true);

        JobConfig jobConfig = new JobConfig();
        jobConfig.setName("Temperature Monitor");
        HazelcastInstance hz = Hazelcast.bootstrappedInstance();
        hz.getJet().newJob(pipeline, jobConfig);
    }
}
