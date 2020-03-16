package uk.co.jtnet.datatypes.microsoft.windows;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

public class FileTime {

	/**
	 * FILETIME is a windows data structure.
	 * Ref: https://msdn.microsoft.com/en-us/library/windows/desktop/ms724284%28v=vs.85%29.aspx
	 * Ref: https://msdn.microsoft.com/en-us/library/windows/desktop/ms724284%28v=vs.85%29.aspx
	 * It contains two parts that are 32bit integers:
	 * 	dwLowDateTime
	 * 	dwHighDateTime
	 * We need to combine these two into one 64bit integer. This gives the number of 100 nano second period from January 1, 1601, Coordinated Universal Time (UTC)
	 * For Java date we need to convert this into the number of milliseconds from 1st January 1970
	 */

	private int lowOrder;
	private int highOrder;
	private Date fileDateTime;

	public FileTime(int lowOrder, int highOrder) throws IOException{
		this.lowOrder = lowOrder;
		this.highOrder = highOrder;
		convertToDate();
	}

	public Date getDate(){
		return fileDateTime;
	}

	private void convertToDate() throws IOException {      
		long lowOrderLong = ((long)lowOrder) & 0xffffffffL;
		long highOrderLong = ((long)highOrder) & 0xffffffffL;
		/* From the Microsoft documentation:
		 *  LogoffTime - If the session should not expire, this structure SHOULD have the dwHighDateTime member set to 0x7FFFFFFF and the dwLowDateTime member set to 0xFFFFFFFF. A recipient of the PAC SHOULD use this value as an indicator of when to warn the user that the allowed time is due to expire.
		 *  KickOffTime - If the client should not be logged off, this structure SHOULD have the dwHighDateTime member set to 0x7FFFFFFF and the dwLowDateTime member set to 0xFFFFFFFF.
		 *  PasswordMustChange - If the password will not expire, this structure MUST have the dwHighDateTime member set to 0x7FFFFFFF and the dwLowDateTime member set to 0xFFFFFFFF. 
		 * In these cases we will return null.
		 */
		if(lowOrderLong != 0x7fffffffL && highOrderLong != 0xffffffffL) {
			BigInteger lowOrderBigInt = BigInteger.valueOf(lowOrderLong);
			BigInteger highOrderBigInt = BigInteger.valueOf(highOrderLong);
			//Combined the 32bit integers into one 64bit. Need to shift by 32 as the numbers are for different orders of accuracy.
			BigInteger combinedBigInt = lowOrderBigInt.add(highOrderBigInt.shiftLeft(32));
			//Convert from number of 100 nano second intervals to number of milliseconds.
			combinedBigInt = combinedBigInt.divide(BigInteger.valueOf(10000L));
			//Convert from being based on January 1 1601 to January 1 1970
			combinedBigInt = combinedBigInt.add(BigInteger.valueOf(-11644473600000L));
			fileDateTime = new Date(combinedBigInt.longValue());
		} else {
			fileDateTime = null;
		}
	}

}
