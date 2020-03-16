package uk.co.jtnet.datatypes.microsoft.windows;

import java.io.IOException;

import uk.co.jtnet.encoding.ndr.NdrStream;

public class RpcUnicodeString {

	// Reference: https://msdn.microsoft.com/en-us/library/cc230365.aspx

	/* Length: The length, in bytes, of the string pointed to by the Buffer member, not including the terminating null character if any.
	 * The length MUST be a multiple of 2. The length SHOULD equal the entire size of the Buffer, in which case there is no terminating null character.
	 * Any method that accesses this structure MUST use the Length specified instead of relying on the presence or absence of a null character.
	 * length_is(Length/2)
	 */
	private short length;

	/* MaximumLength: The maximum size, in bytes, of the string pointed to by Buffer.
	 * The size MUST be a multiple of 2. If not, the size MUST be decremented by 1 prior to use.
	 * This value MUST not be less than Length.
	 * size_is(MaximumLength/2)
	 */
	private short maximumLength;

	/*Buffer: A pointer to a string buffer. If MaximumLength is greater than zero, the buffer MUST contain a non-null value.
	 */
	private int pointer;

	private String stringValue;

	public RpcUnicodeString() {
	}

	public RpcUnicodeString(String stringValue, int pointer) {
		this.stringValue = stringValue;
		this.length = (short)(stringValue.length() * 2);
		this.maximumLength = this.length;
		this.pointer = pointer;
	}

	public RpcUnicodeString(short length, short maximumLength, int pointer) {
		this.length = length;
		this.maximumLength = maximumLength;
		if (this.maximumLength % 2 != 0) {
			this.maximumLength -= 1;
		}

		if (this.length > this.maximumLength) {
			throw new IllegalArgumentException("The length parameter cannot be greater than maximumLength");
		}
		this.pointer = pointer;
	}

	public short getLength() {
		return length;
	}

	public void setLength(short length) {
		this.length = length;
	}

	public short getMaximumLength() {
		return maximumLength;
	}

	public void setMaximumLength(short maximumLength) {
		this.maximumLength = maximumLength;
	}

	public int getPointer() {
		return pointer;
	}

	public void setPointer(int pointer) {
		this.pointer = pointer;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public void deserializeString(NdrStream pacDataStream) throws IOException {
		String str = pacDataStream.readConformantAndVaryingString();

		//Valid extracted string matches the expected values.
		if (maximumLength > 0 && str == null){
			throw new IOException("Malformed RCP_UNICODE_STRING pointer. Maximum length greater than zero but extracted string is null");
		}
		int expectedLength = length / 2;
		int extractedLength = (str != null) ? str.length() : 0;
		if (extractedLength != expectedLength) {
			throw new IOException("Malformed RCP_UNICODE_STRING pointer. Extracted string length (" + extractedLength + ") does not match expected length (" + expectedLength + ").");
		}
		this.stringValue = str;
	}

}
