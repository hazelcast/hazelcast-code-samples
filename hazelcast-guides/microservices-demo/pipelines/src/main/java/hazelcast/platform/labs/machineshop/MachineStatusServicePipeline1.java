package hazelcast.platform.labs.machineshop;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.datamodel.Tuple3;
import com.hazelcast.jet.pipeline.*;
import com.hazelcast.nio.serialization.genericrecord.GenericRecord;
import com.hazelcast.nio.serialization.genericrecord.GenericRecordBuilder;

import java.util.Map;

public class MachineStatusServicePipeline1 {

    public static Pipeline createPipeline(String inputMapName, String outputMapName){
        Pipeline pipeline = Pipeline.create();

        /*
         * Read a status request from the input map
         *
         * INPUT: None
         * OUTPUT: A Map.Entry<String,String> representing a request.  The key is the request id and
         *         the value is the requested serial number
         */
        StreamStage<Map.Entry<String,String>> requests = pipeline
                .readFrom(Sources.<String, String>mapJournal(inputMapName, JournalInitialPosition.START_FROM_CURRENT))
                .withoutTimestamps();

        /*
         * Look up the current average temperature in the machine status IMap and return it along
         * with all the information in the request
         *
         * INPUT: Map.Entry<String,String> (see above)
         * OUTPUT: A Tuple with (requestId, serialNum, averageTemp)
         */
        StreamStage<Tuple3<String,String, Short>> rawdata = requests
                .groupingKey(Map.Entry::getValue)
                .<GenericRecord, Tuple3<String, String, Short>>mapUsingIMap(
                        Names.STATUS_SUMMARY_MAP_NAME,
                        (request, profile) -> Tuple3.tuple3(
                                request.getKey(),
                                request.getValue(),
                                profile != null ? profile.getInt16("averageBitTemp10s") : -1 ));

        /*
         * Change the Tuple3 from the previous step and turn it into a Tuple2 with the first item
         * being the requestId and the second being a GenericRecord representation of the
         * StatusServiceResponse
         *
         * INPUT: Tuple3<String, String, short> (requestId, serialNumber, averageTemp)
         * OUTPUT: None
         */
        rawdata.map((raw) -> Tuple2.tuple2(
                raw.f0(),
                GenericRecordBuilder.compact("hazelcast.platform.labs.machineshop.domain.StatusServiceResponse")
                        .setString("serialNumber", raw.f1())
                        .setInt16("averageBitTemp10s", raw.f2())
                        .setInt16("warningTemp", (short) 0)
                        .setInt16("criticalTemp", (short) 0)
                        .setString("status", "unknown")
                        .build())).writeTo(Sinks.map(outputMapName));


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
