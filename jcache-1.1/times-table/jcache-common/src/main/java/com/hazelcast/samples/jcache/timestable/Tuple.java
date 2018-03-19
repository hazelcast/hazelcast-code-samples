package com.hazelcast.samples.jcache.timestable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * The key of the times table entry.
 * <p>
 * So key "{@code (5,6)}" should have the corresponding
 * value "{@code 30}".
 */
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("serial")
@Data
public class Tuple implements Comparable<Tuple>, Serializable {

    private int operand1;
    private int operand2;

    /**
     * Simple numeric ordering on tuples. Assumes no overflow
     */
    @Override
    public int compareTo(Tuple that) {
        if (this.operand1 == that.getOperand1()) {
            return this.operand2 - that.operand2;
        } else {
            return this.operand1 - that.operand1;
        }
    }
}
