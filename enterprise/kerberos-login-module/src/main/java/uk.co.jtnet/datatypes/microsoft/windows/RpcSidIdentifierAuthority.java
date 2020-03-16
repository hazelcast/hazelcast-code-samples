package uk.co.jtnet.datatypes.microsoft.windows;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RpcSidIdentifierAuthority {

	private static final byte[] NULL_SID_AUTHORITY = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	private static final byte[] WORLD_SID_AUTHORITY = {0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
	private static final byte[] LOCAL_SID_AUTHORITY = {0x00, 0x00, 0x00, 0x00, 0x00, 0x02};
	private static final byte[] CREATOR_SID_AUTHORITY = {0x00, 0x00, 0x00, 0x00, 0x00, 0x03};
	private static final byte[] NON_UNIQUE_AUTHORITY = {0x00, 0x00, 0x00, 0x00, 0x00, 0x04};
	private static final byte[] SECURITY_NT_AUTHORITY = {0x00, 0x00, 0x00, 0x00, 0x00, 0x05};
	private static final byte[] SECURITY_APP_PACKAGE_AUTHORITY = {0x00, 0x00, 0x00, 0x00, 0x00, 0x0F};
	private static final byte[] SECURITY_MANDATORY_LABEL_AUTHORITY = {0x00, 0x00, 0x00, 0x00, 0x00, 0x10};
	private static final byte[] SECURITY_SCOPED_POLICY_ID_AUTHORITY = {0x00, 0x00, 0x00, 0x00, 0x00, 0x11};
	private static final byte[] SECURITY_AUTHENTICATION_AUTHORITY = {0x00, 0x00, 0x00, 0x00, 0x00, 0x12};

	private byte[] sidIdentifierAuthority = new byte[6];

	public RpcSidIdentifierAuthority(byte[] sidIdentifierAuthority){
		this.sidIdentifierAuthority = sidIdentifierAuthority;
	}

	public byte[] getSidIdentifierAuthority() {
		return sidIdentifierAuthority;
	}

	public void setSidIdentifierAuthority(byte[] sidIdentifierAuthority) {
		this.sidIdentifierAuthority = sidIdentifierAuthority;
	}



	/*The SID string format syntax, a format commonly used for a string representation of the SID type (as specified in section 2.4.2), is described by the following ABNF syntax, as specified in [RFC5234].
	SID= "S-1-" IdentifierAuthority 1*SubAuthority
	IdentifierAuthority= IdentifierAuthorityDec / IdentifierAuthorityHex
	  ; If the identifier authority is < 2^32, the
	  ; identifier authority is represented as a decimal 
	  ; number
	  ; If the identifier authority is >= 2^32,
	  ; the identifier authority is represented in 
	  ; hexadecimal
	IdentifierAuthorityDec =  1*10DIGIT
	  ; IdentifierAuthorityDec, top level authority of a 
	  ; security identifier is represented as a decimal number
	IdentifierAuthorityHex = "0x" 12HEXDIG
	  ; IdentifierAuthorityHex, the top-level authority of a
	  ; security identifier is represented as a hexadecimal number
	 */

	@Override
	public String toString() {
		//Check if zeros in the first two elements of the array. If so then it is just 32bit.
		String str = new String();
		if (sidIdentifierAuthority[0] == 0x00 && sidIdentifierAuthority[1] == 0x00) {
			ByteBuffer bb = ByteBuffer.wrap(sidIdentifierAuthority);
			//bb.order(ByteOrder.LITTLE_ENDIAN);  //not sure about this
			bb.position(2); //first two bytes of the 6 are zero so skip
			long l = (long)bb.getInt(); //remaining 4 bytes read into an int then cast to long
			str = Long.toString(l);
		} else {
			StringBuilder sb = new StringBuilder(sidIdentifierAuthority.length * 2 + 2);
			sb.append("0x");
			for(byte b: sidIdentifierAuthority)
				sb.append(String.format("%02x", b & 0xff));
			str = sb.toString();
		}
		return str;
	}




}

