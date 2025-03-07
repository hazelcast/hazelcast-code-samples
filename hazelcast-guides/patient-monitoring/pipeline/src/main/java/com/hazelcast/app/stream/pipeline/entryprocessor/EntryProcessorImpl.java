package com.hazelcast.app.stream.pipeline.entryprocessor;

import com.hazelcast.app.common.data.person.patient.Patient;
import com.hazelcast.map.EntryProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Map;

public class EntryProcessorImpl implements EntryProcessor<String, Patient, Patient>, Serializable {
	private static final Logger LOGGER = LogManager.getLogger("EntryProcessorImpl");

	private static final long serialVersionUID = 3386724407622870440L;

	String topicMapName;
	String value;
	Patient patient;

	public EntryProcessorImpl(String topicMapNameIn, String valueIn, Patient patientIn) {
		super();
		topicMapName = topicMapNameIn;
		value = valueIn;
		patient = patientIn;
	}

	@Override
	public Patient process(Map.Entry<String, Patient> entryIn) {
		Patient p = entryIn.getValue();

		try {
			if (p == null) {
				p = patient;
			}

			switch (topicMapName) {
				case "BREATHING":
					p.setBreathing(value);
					break;
				case "CONSCIOUSNESS":
					p.setConsciousness(value);
					break;
				case "HEART":
					p.setHeart(Integer.parseInt(value));
					break;
				case "OXYGEN-SATURATION-ONE":
					p.setOxygenSaturationOne(Integer.parseInt(value));
					break;
				case "OXYGEN-SATURATION-TWO":
					p.setOxygenSaturationTwo(Integer.parseInt(value));
					break;
				case "RESPIRATORY":
					p.setRespiratory(Integer.parseInt(value));
					break;
				case "SYSTOLIC-BLOOD-PRESSURE":
					p.setSystolicBloodPressure(Integer.parseInt(value));
					break;
				case "TEMPERATURE":
					p.setTemperature(Float.parseFloat(value));
					break;
			}
			entryIn.setValue(p);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
		return p;
	}

}