package com.hazelcast.samples.jcache.timestable;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>The key of the times table entry.
 * </p>
 * <p>So key "{@code (5,6)}" should have the corresponding
 * value "{@code 30}".
 * </p> 
 */
@SuppressWarnings("serial")
@Data
public class Tuple implements Serializable {
	private int operand1;
	private int operand2;
}
