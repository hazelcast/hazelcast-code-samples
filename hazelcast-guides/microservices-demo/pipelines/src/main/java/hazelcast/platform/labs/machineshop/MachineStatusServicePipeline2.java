package hazelcast.platform.labs.machineshop;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.datamodel.Tuple4;
import com.hazelcast.jet.datamodel.Tuple5;
import com.hazelcast.jet.pipeline.*;
import com.hazelcast.nio.serialization.genericrecord.GenericRecord;
import com.hazelcast.nio.serialization.genericrecord.GenericRecordBuilder;

import java.util.Map;

public class MachineStatusServicePipeline2 {

    private static String categorizeTemp(short temp, short warningLimit, short criticalLimit){
        String result;
        if (temp >  criticalLimit)
            result = "red";
        else if (temp >  warningLimit)
            result = "orange";
        else
            result = "green";

        return result;
    }

    /*
     * DataStructureDefinitions
     *
     *   GenericRecord of MachineStatusEvent;
     *     GenericRecord machineEvent;
     *     String serialNum = machineEvent.getString("serialNum);
     *     long eventTime = machineEvent.getInt64("eventTime");
     *     short bitTemp = machineEvent.getInt16("bitTemp");
     *
     *   GenericRecord of MachineProfile
     *     GenericRecord profile;
     *     String serialNum = profile.getString("serialNum");
     *     String location = profile.getString("location");
     *     String block = profile.getString("block");
     *     short warningTemp = profile.getInt16("warningTemp");
     *     short criticalTemp = profile.getInt16("criticalTemp");
     *
     * Useful References:
     *    https://docs.hazelcast.org/docs/5.2.0/javadoc/index.html?com/hazelcast/jet/pipeline/StreamStage.html
     */
    public static Pipeline createPipeline(String inputMapName, String outputMapName){
        Pipeline pipeline = Pipeline.create();

        /*
         * Read the request event from the input map
         *
         * Set the groupingKey to the serialNumber from the request so that the events will be
         * on the partition where the requested MachineProfile resides.
         *
         * Look up the MachineProfile by serialNumber
         *
         * OUTPUT: Tuple4 (requestId, serialNum, warningTemp, criticalTemp)
         */
        StreamStage<Tuple4<String, String, Short, Short>> limits =
                pipeline.readFrom(Sources.<String, String>mapJournal(
                        inputMapName, JournalInitialPosition.START_FROM_CURRENT))
                        .withoutTimestamps()
                        .groupingKey(Map.Entry::getValue)
                        .<GenericRecord,Tuple4<String, String,Short, Short>>mapUsingIMap(
                                Names.PROFILE_MAP_NAME,
                                (entry, machineProfile) -> Tuple4.tuple4(
                                        entry.getKey(),
                                        entry.getValue(),
                                        machineProfile != null ? machineProfile.getInt16("warningTemp") : 0,
                                        machineProfile != null ? machineProfile.getInt16("criticalTemp"): 0
                        ));

        /*
         * Now, look up the current average temperature in the status summary map and
         * add that to the end of the tuple.
         *
         * OUTPUT: Tuple5 (requestId, serialNum, warningTemp, criticalTemp, averageTemp)
         */
        StreamStage<Tuple5<String,String, Short, Short, Short>> rawdata =
                limits.<String, GenericRecord, Tuple5<String, String, Short, Short, Short>>mapUsingIMap(
                        Names.STATUS_SUMMARY_MAP_NAME,
                        Tuple4::f1,
                        (t4, summary) -> Tuple5.tuple5(
                                t4.f0(),
                                t4.f1(),
                                t4.f2(),
                                t4.f3(),
                                summary != null ? summary.getInt16("averageBitTemp10s") : -1
        ));

        /*
         * Package the Tuple5 into a GenericRecord representation of the StatusServiceResponse
         * Write it to the response map, using the original request id as the key.
         */
        rawdata.map(( event) ->{
            String sn = event.f1();
            Short warningTemp = event.f2();
            Short criticalTemp = event.f3();
            Short averageTemp =  event.f4();
            String status = categorizeTemp(averageTemp, warningTemp, criticalTemp);
           return Tuple2.tuple2(
                   event.f0(),
                   GenericRecordBuilder.compact("hazelcast.platform.labs.machineshop.domain.StatusServiceResponse")
                           .setString("serialNumber", sn)
                           .setInt16("averageBitTemp10s", averageTemp)
                           .setInt16("warningTemp", warningTemp)
                           .setInt16("criticalTemp", criticalTemp)
                           .setString("status", status).build());
        }).writeTo(Sinks.map(outputMapName));

        return pipeline;
    }
    public static void main(String []args){
        if (args.length != 2){
            System.err.println("Please supply exactly 2 parameters: input map name, output map name");
            System.exit(1);
        }
        Pipeline pipeline = createPipeline(args[0], args[1]);

        JobConfig jobConfig = new JobConfig();
        jobConfig.setName("MachineStatusService");
        HazelcastInstance hz = Hazelcast.bootstrappedInstance();
        hz.getJet().newJob(pipeline, jobConfig);
    }
}
