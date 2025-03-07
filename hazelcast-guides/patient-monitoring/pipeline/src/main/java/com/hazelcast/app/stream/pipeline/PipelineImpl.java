package com.hazelcast.app.stream.pipeline;

import com.hazelcast.app.common.connection.ClientConnection;
import com.hazelcast.app.common.data.person.patient.Patient;
import com.hazelcast.app.common.data.person.patient.PatientUtils;
import com.hazelcast.app.common.data.person.profile.Profile;
import com.hazelcast.app.common.data.person.profile.ProfileUtils;
import com.hazelcast.app.common.resource.Resource;
import com.hazelcast.app.sink.Result;
import com.hazelcast.app.sink.metric.GraphiteMetric;
import com.hazelcast.app.stream.pipeline.entryprocessor.EntryProcessorImpl;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamStage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.python.core.*;
import org.python.core.finalization.FinalizablePyObjectDerived;

import java.util.Map.Entry;
import java.util.Properties;

import static com.hazelcast.function.Functions.entryKey;
import static com.hazelcast.jet.Util.entry;

public class PipelineImpl {
    private static final Logger LOGGER = LogManager.getLogger("PipelineImpl");
    private static String jobConfigName;
    private static String mbIpPort;
    private final HazelcastInstance hazelcastInstance;

    public PipelineImpl(
            HazelcastInstance hazelcastInstanceIn,
            String hazelcastIpAddressIn,
            String patientMapIn,
            String topicMapNameIn,
            String jobConfigNameIn,
            String processorNameIn,
            String processorPortIn,
            String mbIpPortIn
    ) {
        hazelcastInstance = hazelcastInstanceIn;
        jobConfigName = jobConfigNameIn;
        mbIpPort = mbIpPortIn;

        try {
            Pipeline pipeline = createPipeline(hazelcastIpAddressIn, patientMapIn, topicMapNameIn, processorNameIn, processorPortIn);
            submitJob(pipeline);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info(e.getMessage());
        }
    }

    public static Pipeline createPipeline(
            String hazelcastIpAddressIn,
            String patientMapIn,
            String topicMapNameIn,
            String processorNameIn,
            String processorPortIn
    ) {
        //  Initialize an empty pipeline
        Pipeline pipeline = Pipeline.create();

        try {
            //  Read from the Message Bus (Apache Kafka)
            StreamStage<Entry<String, String>> streamStage = pipeline.readFrom(
                            KafkaSources.<String, String>kafka(kafkaProps(), topicMapNameIn))
                    .withoutTimestamps()
                    .map(r -> entry(r.getKey(), r.getValue()));
            //  Write to the Patient Map
            streamStage.writeTo(Sinks.mapWithEntryProcessor(
                    patientMapIn, entryKey(), e -> new EntryProcessorImpl(topicMapNameIn, e.getValue(), new Patient(e.getKey()))
            ));
            streamStage.writeTo(Sinks.map(topicMapNameIn));
            // Sink is designated to send results
            streamStage.writeTo(Result.buildGraphiteSink(hazelcastIpAddressIn, topicMapNameIn, processorNameIn, processorPortIn));
            streamStage.writeTo(Sinks.logger());
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info(e.getMessage());
        }
        return pipeline;
    }

    public static Properties kafkaProps() {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, mbIpPort);
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, jobConfigName);
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        return props;
    }

    public void submitJob(Pipeline pipelineIn) {
        try {
            JobConfig jobConfig = new JobConfig().setName(jobConfigName);
            jobConfig.addClass(
                    Traverseproc.class
                    , Slotted.class
                    , PyTuple.class
                    , PyString.class
                    , PySequenceList.class
                    , PySequence.class
                    , PyObjectDerived.class
                    , PyObject.class
                    , PyList.class
                    , PyException.class
                    , PyBaseString.class
                    , FinalizablePyObjectDerived.class
                    , BufferProtocol.class
                    , GraphiteMetric.class
                    , Result.class
                    , EntryProcessorImpl.class
                    , PipelineImpl.class
                    , ProfileUtils.class
                    , Profile.class
                    , PatientUtils.class
                    , Patient.class
                    , ClientConnection.class
                    , Resource.class
                    , Main.class
            );
            hazelcastInstance.getJet().newJob(pipelineIn, jobConfig);
//            hazelcastInstance.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info(e.getMessage());
        }
    }

}