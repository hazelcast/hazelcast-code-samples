package com.hazelcast.samples.spi;

import java.io.Serializable;
import java.util.UUID;

import lombok.Data;

/**
 * <p>An order in this example has an identified and a date
 * when it is due for delivery. Realistically it would have
 * more fields than this.</p>
 * <p>Orders are comparable so we can do them in the order
 * they need to be delivered rather than the order in which
 * they were created.
 * </p>
 * <p>Note the ordering implementation is flawed. An order
 * created on Thursday for Monday delivery will appear before
 * an order created on Thursday for Friday delivery. Only
 * the day is used not the week, and Monday comes before
 * Friday in the collating sequence.</p>
 * <p>It's just an example! You can enhance to fix if you like.</p>
 */
@SuppressWarnings("serial")
@Data
public class Order implements Comparable<Order>, Serializable {

	private UUID id;
	private int seqNo;
	private Day dueDate;
	
	/**
	 * <p>Sort only by day of the week</p>
	 */
	@Override
	public int compareTo(Order that) {
		return this.dueDate.compareTo(that.getDueDate());
	}
	
}
