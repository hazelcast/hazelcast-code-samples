package com.hazelcast.samples.jet.hz3connector;

import com.hazelcast.connector.Hz3Sources;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.BatchSource;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sink;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.map.IMap;
import com.hazelcast.spi.properties.ClusterProperty;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.Map.Entry;

public class CopyMapFromHz3Example {

    private static final String HZ3_CLIENT_CONFIG =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<hazelcast-client xmlns=\"http://www.hazelcast.com/schema/client-config\"\n"
                    + "                  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                    + "                  xsi:schemaLocation=\"http://www.hazelcast.com/schema/client-config\n"
                    + "                  http://www.hazelcast.com/schema/client-config/hazelcast-client-config-3.12" +
                    ".xsd\">\n"
                    + "\n"
                    + "    <network>\n"
                    + "        <cluster-members>\n"
                    + "            <address>127.0.0.1:3210</address>\n"
                    + "        </cluster-members>\n"
                    + "    </network>\n"
                    + "</hazelcast-client>\n";

    @Option(names = {"-s", "--source-map"}, description = "The source map to copy from", required = true)
    private String sourceMap;

    @Option(names = {"-t", "--target-map"}, description = "The target map to copy to", required = true)
    private String targetMap;

    public static void main(String[] args) {
        CopyMapFromHz3Example copySourceMap = new CopyMapFromHz3Example();
        new CommandLine(copySourceMap).parse(args);
        copySourceMap.run();
    }

    private void run() {
        System.setProperty(ClusterProperty.PROCESSOR_CUSTOM_LIB_DIR.getName(),
                new File("target/source").getAbsolutePath());

        HazelcastInstance hz = Hazelcast.bootstrappedInstance();

        BatchSource<Entry<Object, Object>> source = Hz3Sources.remoteMap(sourceMap, HZ3_CLIENT_CONFIG);
        Sink<Entry<Object, Object>> sink = Sinks.map(targetMap);

        Pipeline p = Pipeline.create();
        p.readFrom(source)
         .writeTo(sink);

        JobConfig config = new JobConfig();
        config.addCustomClasspath(source.name(), "hazelcast-3.12.12.jar");
        config.addCustomClasspath(source.name(), "hazelcast-client-3.12.12.jar");
        config.addCustomClasspath(source.name(), "hazelcast-3-connector-impl-5.0-SNAPSHOT.jar");

        Job job = hz.getJet().newJob(p, config);
        job.join();

        IMap<Object, Object> map = hz.getMap(targetMap);

        System.out.println("After copying all the items from the source map " +
                "the target map contains " + map.size() + " items in total.");
    }
}
