package hazelcast.platform.labs.image.similarity;

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
import hazelcast.platform.labs.jet.connectors.DirectoryWatcher;
import hazelcast.platform.labs.jet.connectors.DirectoryWatcherSourceBuilder;

/*
 *  This Pipeline will
 *  1. Watch a directory for the addition of new files
 *  2. Use a Python-based service to retrieve the new image over HTTP and compute the corresponding embedding.
 *  3. Write the resulting vectors into a Hazelcast vector collection
 */
public class ImageIngestPipeline {
    /*
     * This pipeline requires the following 5 arguments in the order given below
     *
     * inputDir         File system path to the directory to be watched.  Note that this path will be interpreted
     *                  on the Hazelcast servers.
     *
     * fileSuffix       The file type to watch.  This is not a "glob".  To watch ".jpg" files, simply pass ".jpg"
     *
     * wwwServer        The host name of the web server that hosts the images.  This value will be used on
     *                  the Hazelcast servers to construct a URL from which to retrieve new images for encoding.
     *
     * pythonServiceDir This is the directory on the client that contains the python embedding code.  When the
     *                  job is submitted, this directory will be sent to the Hazelcast servers.
     *
     * pythonServiceModule  The name of the python file (without .py) that contains the function that creates
     *                      the embeddings.  The function should be named "transform_list" and it should
     *                      take a list[str] for input and return a corresponding list[str] for output.
     */
    public static void main(String []args){
        if (args.length != 5){
            System.err.println("Please provide the following 5 parameters: inputDir, fileSuffix (no glob!), wwwServer, pythonServiceDir, pythonServiceModule");
            System.exit(1);
        }

        String inputDir = args[0];
        String fileSuffix = args[1];
        String wwwServer = args[2];
        String pythonServiceDir = args[3];
        String pythonServiceModule = args[4];

        // the code below is standard for jobs that
        HazelcastInstance hz = Hazelcast.bootstrappedInstance();

        Pipeline pipeline = createPipeline(inputDir, fileSuffix, wwwServer, pythonServiceDir, pythonServiceModule);
        pipeline.setPreserveOrder(false);   // nothing in here requires order
        JobConfig config = new JobConfig();
        config.setName("Ingest: " + inputDir);
        Job job = hz.getJet().newJob(pipeline);
    }

    /*
     * Complete this Pipeline
     *
     * Debugging Tips
     *
     *    At any stage, you can complete the pipeline by sinking the pipeline to a logging sink.
     *    You can then deploy the pipeline and observe the output (see  README.md for instructions).
     *
     *    To add a logging sink:   aStreamStage.writeTo(Sinks.logger())
     *
     *    You can also directly run this file in the debugger.  You will have to set up the run configuration as
     *    follows:
     *      set the HZ_LICENSEKEY environment variable to the value of your license (from .env)
     *      add the following JVM argument: -Dhazelcast.config=hazelcast.yaml
     *      pass the following command line arguments: www .jpg http://localhost:8000 image-embedding-service clip_image_encoder
     *
     *    To make the images available, you will need to run the www server: docker compose up -d wwww
     *  
     */
    private static Pipeline createPipeline(
            String dir,
            String suffix,
            String wwwServer,
            String pythonServiceBaseDir,
            String pythonServiceModule){
        Pipeline pipeline = Pipeline.create();

        /*
         * This custom source emits (event type, changed file name) tuples.  Currently, the
         * only event type returned is ADD.
         *
         * This source is non-distributed, which means it will run on one arbitrarily chosen node in the cluster.
         */
        StreamSource<Tuple2<DirectoryWatcher.EventType, String>> directoryWatcher =
                DirectoryWatcherSourceBuilder.newDirectoryWatcher(dir, suffix);
        StreamStage<Tuple2<DirectoryWatcher.EventType, String>> changeEvents =
                pipeline.readFrom(directoryWatcher).withoutTimestamps().setName("Watch for Changes");

        /*
         * TODO: The events all originate on a single server. Unless forced,  subsequent processing will
         *        remain on the same server.  In order to distribute the work, rebalance the stream.
         *        See: https://docs.hazelcast.org/docs/5.4.0/javadoc/com/hazelcast/jet/pipeline/StreamStage.html#rebalance()
         */
        StreamStage<Tuple2<DirectoryWatcher.EventType, String>> rebalancedEvents = null;

        /*
         * TODO: Change the format of the input to match what is expected by the python embedding
         *       service.  Use a "map" stage (https://docs.hazelcast.org/docs/5.4.0/javadoc/com/hazelcast/jet/pipeline/StreamStage.html#map(com.hazelcast.function.FunctionEx)
         *       to change the filename into a URL.
         *       Example: if the filename  = "myimage.jpg", the URL would be wwwServer + "/" + filename
         *
         *       Note that the input event is a Tuple2 (https://docs.hazelcast.org/docs/5.4.0/javadoc/com/hazelcast/jet/datamodel/Tuple2.html)
         *       The filename is in the second part of the tuple and can be accessed with tuple.f1()
         */
        StreamStage<String> inputs = null;

        /*
         * Now use mapUsingPython to call the python embedding service.
         * See the example here: https://docs.hazelcast.com/hazelcast/5.4/pipelines/python
         *
         * Notes: create the PythonServiceConfig with the following arguments
         *        baseDir: This is the name of the folder (on the client that submits the job) where the
         *                 python service code resides.  In this case, it is passed in to the createPipeline
         *                 method as an argument.
         *
         *        handlerModule: the name of the python file (without .py) containing the transform_list function.
         *                 This is also passed to the createPipeline method.
         *
         *        You can set the number of python instances to run on each node using the "setLocalParallelism"
         *        method of the StreamStage.  Set local parallelism to 2.
         *
         *        After creating a PythonServiceConfig, invoke the python service with code similar to
         *        the example below.
         *
         *        inputs.apply(PythonTransforms.mapUsingPython(myPythonServiceConfig).setLocalParallelism(2);
         */
        PythonServiceConfig pythonService = null;    // create the python service config
        StreamStage<String> outputs = null;          // call map using python

        /*
         * For debugging purposes, finish the pipeline with a logging sink and then test it by deploying
         * it per the instructions in the README.  Remove this sink in the final pipeline.
         */
        //outputs.writeTo(Sinks.logger());

        /*
         * Use mapUsingService to decode the output.  The python stage emits json encoded strings.
         * Use an instance of EmbeddingServiceCodec to translate the json into a (filename, float[]) 2-tuple.
         *
         * See: https://docs.hazelcast.com/hazelcast/5.4/pipelines/transforms#mapusingservice for
         * an example
         *
         * First use ServiceFactories.sharedService to create an instance of EmbeddingServiceCodec.  One instance
         * will be created on each node in the cluster.  Then use a mapUsingService stage to perform the decoding.
         * Your map method will be passed the shared instance of EmbeddingServiceCodec and an event.  You should
         * call embeddingCodec.decodeOutput(myEvent)
         */
        ServiceFactory<?, EmbeddingServiceCodec> postProcessor = null;
        StreamStage<Tuple2<String, float[]>> vectors = null;

        /*
         * Finally, create a vector collection sink and write the pipeline events to it.
         * See: https://docs.hazelcast.com/hazelcast/5.5-snapshot/integrate/vector-collection-connector#vector-collection-as-a-sink
         *
         * Call the VectorSinks.vectorCollection method with the following argument
         *      name of the collection (see hazelcast.yaml)
         *      function to extract the key - in this case, use tuple.f0() to return the URL of the image
         *      function to extract the value - pass the URL of the image as the value as well
         *      function to build an instance of VectorValue from the array of floats in tuple.f1()
         *
         *  You can construct a VectorValue instance with VectorValues.Of(an-array-of-floats)
         *
         * When you have created a vector sink, finish the pipeline by writing it to the sink.
         */
        Sink<Tuple2<String, float[]>> vectorCollectionSink = null;

        //vectors.writeTo(vectorCollectionSink);

        return pipeline;
    }

}
