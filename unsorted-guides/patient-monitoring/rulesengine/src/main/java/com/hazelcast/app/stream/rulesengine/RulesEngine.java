package com.hazelcast.app.stream.rulesengine;

import com.hazelcast.app.common.connection.ClientConnection;
import com.hazelcast.app.common.data.person.patient.Patient;
import com.hazelcast.app.common.data.person.patient.PatientUtils;
import com.hazelcast.app.common.data.person.profile.Profile;
import com.hazelcast.app.common.data.person.profile.ProfileUtils;
import com.hazelcast.app.common.resource.Resource;
import com.hazelcast.app.sink.Result;
import com.hazelcast.app.sink.metric.GraphiteMetric;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.function.FunctionEx;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.*;
import com.hazelcast.jet.python.PythonServiceConfig;
import com.hazelcast.jet.python.PythonTransforms;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.python.core.*;
import org.python.core.finalization.FinalizablePyObjectDerived;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class RulesEngine {
	private static final Logger LOGGER = LogManager.getLogger("RulesEngine");

	private static final String DELIMITER = ",";

	public RulesEngine(
			HazelcastInstance hazelcastInstanceIn,
			String hazelcastIpAddressIn,
			String patientMapIn
	) {
		Resource resource = Resource.getInstance();

		String resultMap = resource.getResultMap();
		String projectPath = resource.getProjectPath();
		String sinkIpAddress = resource.getSinkIpAddress()[0];
		String sinkPort = resource.getSinkPort()[0];
		String rulesEnginePath = resource.getRulesEnginePath();
		String rulesEngineSource = resource.getRulesEngineSource();
		String rulesEngineJob = resource.getRulesEngineJob();

		try {
			Pipeline pipeline = createRulesEnginePipeline(
					hazelcastIpAddressIn,
					patientMapIn,
					resultMap,
					projectPath,
					sinkIpAddress,
					sinkPort,
					rulesEnginePath,
					rulesEngineSource
			);
			submitRulesEngineJob(hazelcastInstanceIn, rulesEngineJob, pipeline);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
	}

	protected static Entry<String, List<String>> makeResultEntry(String resultIn) {
		String[] results = resultIn.split("=");
		String key = results[0];
		List<String> value = new ArrayList<>();
		try {
			value.add(results[1]);
			value.add(results[2]);
			value.add(results[3]);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
			return null;
		}
		return new SimpleImmutableEntry<>(key, value);
	}

	protected FunctionEx<Entry<String, Patient>, String> formatForPython() {
		return entry -> entry.getKey() + DELIMITER +
				entry.getValue().getPatientId() + DELIMITER +
				entry.getValue().getBreathing() + DELIMITER +
				entry.getValue().getConsciousness() + DELIMITER +
				entry.getValue().getHeart() + DELIMITER +
				entry.getValue().getOxygenSaturationOne() + DELIMITER +
				entry.getValue().getOxygenSaturationTwo() + DELIMITER +
				entry.getValue().getRespiratory() + DELIMITER +
				entry.getValue().getSystolicBloodPressure() + DELIMITER +
				entry.getValue().getTemperature();
	}

	public Pipeline createRulesEnginePipeline(
			String hazelcastIpAddressIn,
			String patientMapIn,
			String resultMapIn,
			String projectPathIn,
			String processorNameIn,
			String processorPortIn,
			String rulesEnginePathIn,
			String rulesEngineSourceIn
	) {
		String rulesEngineFullPath = projectPathIn.concat(rulesEnginePathIn);
		Pipeline pipeline = Pipeline.create();

		try {
			StreamStage<Entry<String, Patient>> streamStage = pipeline.readFrom(Sources.<String, Patient>mapJournal(patientMapIn, JournalInitialPosition.START_FROM_CURRENT)).withoutTimestamps();
			StreamStage<String> fromPython = streamStage.map(formatForPython()).apply(PythonTransforms.mapUsingPython(new PythonServiceConfig().setBaseDir(rulesEngineFullPath).setHandlerModule(rulesEngineSourceIn))).setLocalParallelism(1);
			fromPython.map(RulesEngine::makeResultEntry).writeTo(Sinks.logger());
			fromPython.map(RulesEngine::makeResultEntry).writeTo(Sinks.map(resultMapIn));
			fromPython.map(RulesEngine::makeResultEntry).writeTo(Result.buildGraphiteSinkResults(hazelcastIpAddressIn, processorNameIn, processorPortIn));
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
		return pipeline;
	}

	public void submitRulesEngineJob(
			HazelcastInstance hazelcastInstanceIn,
			String rulesEngineJobIn,
			Pipeline pipelineIn
	) {
		try {
			JobConfig jobConfig = new JobConfig().setName(rulesEngineJobIn);
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
					, RulesEngine.class
					, ProfileUtils.class
					, Profile.class
					, PatientUtils.class
					, Patient.class
					, ClientConnection.class
					, Resource.class
					, Main.class
			);
			hazelcastInstanceIn.getJet().newJob(pipelineIn, jobConfig);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
	}

}