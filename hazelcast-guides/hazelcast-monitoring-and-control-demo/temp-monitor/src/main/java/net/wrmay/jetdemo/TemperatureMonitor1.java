package net.wrmay.jetdemo;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.aggregate.AggregateOperations;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.contrib.http.HttpListenerSources;
import com.hazelcast.jet.datamodel.KeyedWindowResult;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.datamodel.Tuple4;
import com.hazelcast.jet.pipeline.*;

public class TemperatureMonitor1 {

    private static String categorizeTemp(double temp, int warningLimit, int criticalLimit){
        String result;
        if (temp > (double) criticalLimit)
            result = "red";
        else if (temp > (double) warningLimit)
            result = "orange";
        else
            result = "green";

        return result;
    }

    public static Pipeline createPipeline(String logDir){
        Pipeline pipeline = Pipeline.create();

        StreamSource<MachineStatus> machineStatusEventSource =
                HttpListenerSources.httpListener(8080, MachineStatus.class);

        // create a stream of MachineStatus events from the map journal, use the timestamps embedded in the events
        StreamStage<MachineStatus> statusEvents = pipeline.readFrom(machineStatusEventSource)
                .withTimestamps(MachineStatus::getTimestamp, 2000)
                .setName("machine status events");


        statusEvents = LoggingService.tee(statusEvents, "status event logger", logDir, entry -> "NEW EVENT FOR " + entry.getSerialNum());

        // sink to the machine_status map
        statusEvents.writeTo(Sinks.map(Names.STATUS_MAP_NAME, MachineStatus::getSerialNum, status -> String.valueOf(status.getBitTemp() )));

        return pipeline;
    }
    public static void main(String []args){
        if (args.length != 1){
            System.err.println("Please provide the log output directory as the first argument");
            System.exit(1);
        }

        String logDir = args[0];

        Pipeline pipeline = createPipeline(logDir);
        pipeline.setPreserveOrder(true);

        JobConfig jobConfig = new JobConfig();
        jobConfig.setName("Temperature Monitor");
        HazelcastInstance hz = Hazelcast.bootstrappedInstance();
        hz.getJet().newJob(pipeline, jobConfig);
    }
}
