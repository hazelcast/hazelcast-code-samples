package hazelcast.platform.labs.jet.connectors;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamSource;

public class DirectoryWatcherTestPipeline {
    /*
     * Expects 1 or 2 arguments: directory and (optionally) suffix
     */
    public static void main(String []args){
        if (args.length < 1){
            System.err.println("Please provide the name of the directory to watch.");
            System.exit(1);
        }

        String dir = args[0];
        String suffix = null;
        if (args.length > 1) suffix = args[1];

        HazelcastInstance hz = Hazelcast.bootstrappedInstance();

        Pipeline pipeline = createPipeline(dir, suffix);
        pipeline.setPreserveOrder(false);   // nothing in here requires order
        JobConfig config = new JobConfig();
        config.setName("Watch: " + dir);
        Job job = hz.getJet().newJob(pipeline);
    }

    private static Pipeline createPipeline(String dir, String suffix){
        Pipeline pipeline = Pipeline.create();
        StreamSource<Tuple2<DirectoryWatcher.EventType, String>> changeEvents =
                DirectoryWatcherSourceBuilder.newDirectoryWatcher(dir, suffix);
        pipeline.readFrom(changeEvents).withoutTimestamps().writeTo(Sinks.logger());
        return pipeline;
    }
}
