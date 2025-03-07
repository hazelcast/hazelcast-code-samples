package com.hazelcast.app.sink;

import com.hazelcast.app.common.data.person.patient.Patient;
import com.hazelcast.app.sink.metric.GraphiteMetric;
import com.hazelcast.jet.pipeline.Sink;
import com.hazelcast.jet.pipeline.SinkBuilder;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyString;
import org.python.modules.cPickle;

import java.io.BufferedOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map.Entry;

public class Result {

	public Result(){}

	public static Sink<Entry<String, String>> buildGraphiteSink(
			String hostIn,
			String topicMapNameIn,
			String sinkIpAddressIn,
			String sinkPortIn
	) {
		return SinkBuilder.sinkBuilder(hostIn, __ -> new BufferedOutputStream(
				new Socket(sinkIpAddressIn, Integer.parseInt(sinkPortIn)).getOutputStream())).
				<Entry<String, String>>receiveFn((bufferedOutputStream, entry) -> {
			GraphiteMetric graphiteMetric;
			PyString metric = new PyString(topicMapNameIn
					.concat(".")
					.concat(new Patient(entry.getKey().trim()).getFullProfile().trim())
					.concat(".")
					.concat(entry.getKey().trim())
			);
			PyInteger intValue = null;
			PyInteger timeStamp = new PyInteger(-1);
			switch (topicMapNameIn) {
				case "TEMPERATURE":
					PyFloat fltValue = new PyFloat(Float.parseFloat(entry.getValue()));
					graphiteMetric = new GraphiteMetric(metric, fltValue, timeStamp);
					break;
				case "BREATHING":
					// value 1 if patient is breathing using normal air if breathing using oxygen value will be 0
					if (entry.getValue().equalsIgnoreCase("air")) {
						intValue = new PyInteger(1);
					} else if (entry.getValue().equalsIgnoreCase("oxygen")) {
						intValue = new PyInteger(0);
					}
					graphiteMetric = new GraphiteMetric(metric, intValue, timeStamp);
					break;
				case "CONSCIOUSNESS":
					// value 1 if patient is in alert state for all other states value will be 0
					if (entry.getValue().equalsIgnoreCase("alert")) {
						intValue = new PyInteger(1);
					} else if (entry.getValue().equalsIgnoreCase("unalert")) {
						intValue = new PyInteger(0);
					}
					graphiteMetric = new GraphiteMetric(metric, intValue, timeStamp);
					break;
				default:
					intValue = new PyInteger(Integer.parseInt(entry.getValue()));
					graphiteMetric = new GraphiteMetric(metric, intValue, timeStamp);
					break;
			}
			PyString payload = cPickle.dumps(graphiteMetric.getAsList(), 2);
			byte[] header = ByteBuffer.allocate(Integer.BYTES).putInt(payload.__len__()).array();
			bufferedOutputStream.write(header);
			bufferedOutputStream.write(payload.toBytes());
		}).flushFn(BufferedOutputStream::flush).destroyFn(BufferedOutputStream::close).preferredLocalParallelism(1).build();
	}

	public static Sink<Entry<String, List<String>>> buildGraphiteSinkResults(
			String hostIn,
			String sinkIpAddressIn,
			String sinkPortIn
	) {
		return SinkBuilder.sinkBuilder(hostIn, __ -> new BufferedOutputStream(
				new Socket(sinkIpAddressIn, Integer.parseInt(sinkPortIn)).getOutputStream())).
				<Entry<String, List<String>>>receiveFn((bufferedOutputStream, entry) -> {
			GraphiteMetric graphiteMetric;
			PyString metric = new PyString("RESULTS"
					.concat(".")
					.concat(new Patient(entry.getKey().trim()).getFullProfile().trim())
					.concat(".")
					.concat(entry.getKey().trim())
			);
//			PyString value = new PyString(entry.getValue().get(0).trim());
			PyInteger value = new PyInteger(Integer.parseInt(entry.getValue().get(1).trim()));
//			PyString value = new PyString(entry.getValue().get(2).trim());
			PyInteger timeStamp = new PyInteger(-1);
			graphiteMetric = new GraphiteMetric(metric, value, timeStamp);
			PyString payload = cPickle.dumps(graphiteMetric.getAsList(), 2);
			byte[] header = ByteBuffer.allocate(Integer.BYTES).putInt(payload.__len__()).array();
			bufferedOutputStream.write(header);
			bufferedOutputStream.write(payload.toBytes());
		}).flushFn(BufferedOutputStream::flush).destroyFn(BufferedOutputStream::close).preferredLocalParallelism(1).build();
	}

}