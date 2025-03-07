package hazelcast.platform.labs.sandbox;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.*;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class Test {

    public static void main(String []args){
        Level LOG_LEVEL = Level.SEVERE;
        java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(LOG_LEVEL);
        for (Handler h : rootLogger.getHandlers()) {
            h.setLevel(LOG_LEVEL);
        }

        long time = System.currentTimeMillis();

        Event[] testEvents = new Event[]{
                new Event(time, "E00"),
                new Event(time += 100, "E01"),
                new Event(time += 100, "E02"),
                new Event(time += 100, "E03"),
                new Event(time += 100, "E04"),
                new Event(time += 100, "E05"),
                new Event(time + 100, "E06")
        };


        Config config = new Config();
        config.getJetConfig().setEnabled(true);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        Pipeline pipeline = Pipeline.create();

        pipeline.setPreserveOrder(true);

        pipeline.readFrom( testStreamSource(testEvents, 1)).withTimestamps(Event::getTimestamp, 2000)
                .mapUsingService(loggerServiceFactory("B"), LoggerService::map).setLocalParallelism(1)
                .mapUsingService(loggerServiceFactory("C"), LoggerService::map).setLocalParallelism(1)
                .writeTo(Sinks.noop());

        JobConfig jobConfig = new JobConfig();
        jobConfig.setName("test 1");

        Util.printHeader();
        Job job = hz.getJet().newJob(pipeline, jobConfig);

        try {
            Thread.sleep(10000);
        } catch(InterruptedException x){
            //
        }

        job.cancel();
        hz.shutdown();
    }

    public static StreamSource<Event> testStreamSource(Event []events, int instancesPerNode){
        return SourceBuilder.stream("Test Source", ctx ->
                        new TestEventSource<>(events, ctx.globalProcessorIndex(), ctx.totalParallelism()))
                .<Event>fillBufferFn( (src, buffer) -> {
                    Event e = src.next();
                    if (e != null) buffer.add(e);
                })
                .distributed(instancesPerNode)
                .build();
    }


    public static ServiceFactory<?, LoggerService<Event>> loggerServiceFactory(String name){
        return ServiceFactories.nonSharedService( ctx -> new LoggerService<>(name, ctx.globalProcessorIndex()));
    }

}
