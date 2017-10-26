package com.hazelcast.samples.jcache.timestable;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>The key of the times table entry.
 * </p>
 * <p>So key "{@code (5,6)}" should have the corresponding
 * value "{@code 30}".
 * </p> 
 */
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("serial")
@Data
public class Tuple implements Comparable<Tuple>, Serializable {
	private int operand1;
	private int operand2;
	
	
	/**
	 * <p>Simple numeric ordering on tuples. Assumes no overflow
	 * </p>
	 */
	@Override
	public int compareTo(Tuple that) {
		if (this.operand1==that.getOperand1()) {
			return this.operand2 - that.operand2;
		} else {
			return this.operand1 - that.operand1;
		}
	}
}
