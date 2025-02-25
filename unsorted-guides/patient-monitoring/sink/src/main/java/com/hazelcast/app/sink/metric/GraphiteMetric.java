package com.hazelcast.app.sink.metric;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.python.core.*;

import java.io.Serializable;

public class GraphiteMetric implements Serializable {
	private static final Logger LOGGER = LogManager.getLogger("GraphiteMetric");

	private static final long serialVersionUID = 3386724407622870440L;

	private final PyString metric;
	private final PyObject value;
	private final PyInteger timeStamp;

	public GraphiteMetric(PyString metricIn, PyString valueIn, PyInteger timeStampIn) {
		metric = metricIn;
		value = valueIn;
		timeStamp = timeStampIn;
	}

	public GraphiteMetric(PyString metricIn, PyInteger valueIn, PyInteger timeStampIn) {
		metric = metricIn;
		value = valueIn;
		timeStamp = timeStampIn;
	}

	public GraphiteMetric(PyString metricIn, PyFloat valueIn, PyInteger timeStampIn) {
		metric = metricIn;
		value = valueIn;
		timeStamp = timeStampIn;
	}

	public PyList getAsList() {
		PyList pyList = new PyList();
		try {
			PyTuple pyTuple = getAsItem();
			pyList.add(pyTuple);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		}
		return pyList;
	}

	public PyTuple getAsItem() {
		return new PyTuple(metric, new PyTuple(timeStamp, value));
	}

}