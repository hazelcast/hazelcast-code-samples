package hazelcast.platform.labs.image.similarity.solution;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.pipeline.*;
import com.hazelcast.jet.python.PythonServiceConfig;
import com.hazelcast.jet.python.PythonTransforms;
import com.hazelcast.vector.VectorValues;
import com.hazelcast.vector.jet.VectorSinks;
import hazelcast.platform.labs.image.similarity.EmbeddingServiceCodec;
import hazelcast.platform.labs.jet.connectors.DirectoryWatcher;
import hazelcast.platform.labs.jet.connectors.DirectoryWatcherSourceBuilder;

/*
 *
 */
public class ImageIngestPipelineSolution {


    public static void main(String []args){
        // TODO make argument handling prettier
        if (args.length != 5){
            System.err.println("Please provide the following 5 parameters: inputDir, fileSuffix (no glob!), www_server, pythonServiceDir, pythonServiceModule");
            System.exit(1);
        }

        String inputDir = args[0];
        String fileSuffix = args[1];
        String wwwServer = args[2];
        String pythonServiceDir = args[3];
        String pythonServiceModule = args[4];

        HazelcastInstance hz = Hazelcast.bootstrappedInstance();

        Pipeline pipeline = createPipeline(inputDir, fileSuffix, wwwServer, pythonServiceDir, pythonServiceModule);
        pipeline.setPreserveOrder(false);   // nothing in here requires order
        JobConfig config = new JobConfig();
        config.setName("Ingest: " + inputDir);
        Job job = hz.getJet().newJob(pipeline);
    }

    /*
     * Will encode jpg format image files stored in dir. The source could be run on any nodes
     * so all nodes need to have access to "dir" whether through a copy or a shared file  system.
     *
     * The source will run on a random node and emit events containing information about files
     * that have been added to the watched directory.
     *
     * These change events will be redistributed to all nodes so that the task of encoding can be shared.
     *
     * The python embedding service will issue a HTTP GET to retrieve the new image and generate a
     * vector encoding for it.
     */
    private static Pipeline createPipeline(
            String dir,
            String suffix,
            String wwwServer,
            String pythonServiceBaseDir,
            String pythonServiceModule){
        Pipeline pipeline = Pipeline.create();

        // 1. Read change events from file system
        StreamSource<Tuple2<DirectoryWatcher.EventType, String>> directoryWatcher =
                DirectoryWatcherSourceBuilder.newDirectoryWatcher(dir, suffix);
        StreamStage<Tuple2<DirectoryWatcher.EventType, String>> changeEvents =
                pipeline.readFrom(directoryWatcher).withoutTimestamps().setName("Watch for Changes");

        // 2. Redistribute to all cluster nodes
        changeEvents = changeEvents.rebalance();
        
        // 3. Prepare input - emits URLS that can be used to retrieve changed images
        StreamStage<String> imageURLS =
                changeEvents.map( t2 -> wwwServer + "/" + t2.f1()).setName("Prepare Input");

        // 4. Compute embeddings in Python - emits String containing json encoded embedding vector
        PythonServiceConfig pythonService =
                new PythonServiceConfig().setBaseDir(pythonServiceBaseDir).setHandlerModule(pythonServiceModule);
        StreamStage<String> outputs =
                imageURLS.apply(PythonTransforms.mapUsingPython(pythonService))
                        .setLocalParallelism(2)  // reserve some cores for gc
                        .setName("Compute Embedding");

        // 5. Parse output - emits a tuple of ( image url, image embedding )
        ServiceFactory<?, EmbeddingServiceCodec> postProcessor =
                ServiceFactories.sharedService(ctx -> new EmbeddingServiceCodec());
        StreamStage<Tuple2<String, float[]>> vectors =
                outputs.mapUsingService(postProcessor, EmbeddingServiceCodec::decodeOutput).setName("Parse Output");

        // 6. Store embeddings in vector collection
        Sink<Tuple2<String, float[]>> vectorCollection =
                VectorSinks.vectorCollection(
                        "images",    // collection name
                        Tuple2::f0,               // key
                        Tuple2::f0,               // val
                        t -> VectorValues.of(t.f1()));  // vector

        vectors.writeTo(vectorCollection);
        vectors.writeTo(Sinks.logger( t2 -> "Stored embedding for " + t2.f0()));

        return pipeline;
    }

}
