package uk.co.jtnet.encoding.ndr;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

import uk.co.jtnet.datatypes.microsoft.windows.FileTime;
import uk.co.jtnet.datatypes.microsoft.windows.KerbSidAndAttributes;
import uk.co.jtnet.datatypes.microsoft.windows.RpcSid;
import uk.co.jtnet.datatypes.microsoft.windows.RpcSidIdentifierAuthority;
import uk.co.jtnet.datatypes.microsoft.windows.RpcUnicodeString;

public class NdrStream {

	/** 
	 * Network Data Representation (NDR)
	 * Standards reference: http://pubs.opengroup.org/onlinepubs/9629399/chap14.htm
	 */

	public byte[] rawBytesArray;
	public ByteBuffer ndrByteStreamBuffer;
	public int streamSize;
	public int rpcTypeSerializationVerion;
	public int Endian;
	public int charEncoding;
	public int floatingPointRepresentation;
	public short headerLength;
	public int dataLength;

	public final static int BIG_ENDIAN = 0;
	public final static int LITTLE_ENDIAN = 1;
	public final static int ASCII = 0;
	public final static int EBCDIC = 1;
	public final static int IEEE = 0;
	public final static int VAX = 1;
	public final static int CRAY = 2;
	public final static int IBM = 3;

	public enum Align {
		SHORT(2),
		INT(4),
		LONG(8),
		CHAR(2),
		FLOAT(4),
		DOUBLE(8);
		private int bytes;
		private Align(int bytes) {
			this.bytes = bytes;
		}
	}

	public NdrStream(byte[] rawBytesArray) {
		this.rawBytesArray = rawBytesArray;
	}

	public void initializeStream() throws IOException{
		parseRPCCommonHeader();
	}

	public void parseRPCCommonHeader() throws IOException{
		//TODO need to get some standards compliant data to test this with
		/* The first 8 bytes comprise the Common RPC Header for type marshalling.
		 * The next 8 bytes comprise the RPC type marshalling private header for constructed types. 
		 */	
		if (rawBytesArray.length < 16){
			throw new IOException("Malformed NDR data stream. Not big enough to contain RPC headers and data");
		}
		/* Common RPC Header (https://msdn.microsoft.com/en-us/library/cc243890.aspx):
		 * - First Byte[0] - the protocol version
		 * - Must be a multiple of 8 bytes in length
		 * Within the RPC Common Header is the NDR Format Label:
		 *   - Second Byte[1]:
		 *     - Big or Little Endian (first 4 bits)
		 *     - ASCII or EBCDIC (next 4 bits)
		 *   - Third Byte[2] - the floating point representation
		 *   - The next two bytes in the header is filler and can be ignored
		 */
		rpcTypeSerializationVerion = rawBytesArray[0];
		byte secondByte = rawBytesArray[1];
		Endian = secondByte >> 4 & 0xF;
		charEncoding = secondByte & 0xF;
		floatingPointRepresentation = rawBytesArray[2];

		//TODO need to get some standards compliant data to test this with
		initializeStreamBuffer();

		//TODO - not sure for what the header total length is so guessing 8 for now. Need to look into this more
		headerLength = 8;
		//The header length must be a multiple of 8
		if (headerLength % 8 != 0){
			throw new IOException("Malformed Common RPC Header. Header length must be a multiple of 8 octets");
		}
		//Set the position according to the header length
		ndrByteStreamBuffer.position(headerLength);
		// We are now at the end of the Common RPC Header and at the beginning of the Private RPC Header (Ref: MS-RPCE section 2.2.6.2)
		parseRPCPrivateHeader();
	}

	public void parseRPCPrivateHeader() {
		//TODO need to get some standards compliant data to test this with
		/* We are now at the end of the Common RPC Header and at the beginning of the Private RPC Header
		 * Private RPC Header (Total 8 bytes)
		 * - First 4 bytes - data length
		 * - Second 4 bytes - filler and can be ignored
		 */
		dataLength = ndrByteStreamBuffer.getInt();
		//Shift over the remaining 4 bytes of filler of the Private RPC Header
		shiftPosition(4);
		//We are now at the start of the data
	}

	public void initializeStreamBuffer() throws IOException {
		//Set how to read the buffer according to the endianness indicated in the header.
		ndrByteStreamBuffer = ByteBuffer.wrap(rawBytesArray);
		if (Endian == LITTLE_ENDIAN){
			ndrByteStreamBuffer.order(ByteOrder.LITTLE_ENDIAN);
		} else if (Endian == BIG_ENDIAN){
			ndrByteStreamBuffer.order(ByteOrder.BIG_ENDIAN);
		} else {
			throw new IOException("Malformed NDR data");
		}
		streamSize = ndrByteStreamBuffer.remaining();
	}



	public int getStreamSize() {
		return streamSize;
	}

	public int getRpcTypeSerializationVerion() {
		return rpcTypeSerializationVerion;
	}

	public int getEndian() {
		return Endian;
	}

	public int getCharEncoding() {
		return charEncoding;
	}

	public short getHeaderLength() {
		return headerLength;
	}

	public int getDataLength() {
		return dataLength;
	}



	public void ensureAlignment(Align alignment) {
		int position = streamSize - ndrByteStreamBuffer.remaining();
		int shift = position & alignment.bytes - 1;
		if(alignment.bytes != 0 && shift != 0) {
			shiftPosition(alignment.bytes - shift);
		}
	}
	public void shiftPosition(int count){
		ndrByteStreamBuffer.position(ndrByteStreamBuffer.position() + count);
	}
	public byte[] readOffBytes (int numberBytes){
		byte[] dst = new byte[numberBytes];
		return ndrByteStreamBuffer.get(dst, 0, dst.length).array();
	}
	public void readIntoArray(byte[] byteArray) {
		ndrByteStreamBuffer.get(byteArray);
	}


	/**
	 * A Boolean is a logical quantity that assumes one of two values: TRUE or FALSE.
	 * NDR represents a Boolean as one octet. It represents a value of FALSE as a zero octet, an octet in which every bit is reset.
	 * It represents a value of TRUE as a non-zero octet, an octet in which one or more bits are set.
	 */
	public Boolean readBoolean(){
		byte octet = ndrByteStreamBuffer.get();
		if (octet == 0){
			return false;
		} else {
			return true;
		}
	}
	public char readChar(){
		ensureAlignment(Align.CHAR);
		return  ndrByteStreamBuffer.getChar();
	}

	public byte readByte(){
		return ndrByteStreamBuffer.get();
	}
	public byte readUninterpretedOctet(){
		return readByte();
	}
	public short readShort() {
		ensureAlignment(Align.SHORT);
		return ndrByteStreamBuffer.getShort();
	}
	public int readInt(){
		ensureAlignment(Align.INT);
		return ndrByteStreamBuffer.getInt();
	}
	public long readUnsignedInt(){
		return ((long)readInt()) & 0xffffffffL;
	}
	public long readLong(){
		ensureAlignment(Align.LONG);
		return ndrByteStreamBuffer.getLong();
	}
	public double readDouble(){
		ensureAlignment(Align.DOUBLE);
		return ndrByteStreamBuffer.getDouble();
	}
	public float readFloat(){
		ensureAlignment(Align.FLOAT);
		return ndrByteStreamBuffer.getFloat();
	}



	/**
	 *  Conformant and Varying Strings
	 *  A conformant and varying string is a string in which the maximum number of elements is not known beforehand and therefore is included in the representation of the string. 
	 *  NDR represents a conformant and varying string as an ordered sequence of representations of the string elements, preceded by three unsigned long integers.
	 *  The first integer gives the maximum number of elements in the string, including the terminator.
	 *  The second integer gives the offset from the first index of the string to the first index of the actual subset being passed. 
	 *  The third integer gives the actual number of elements being passed, including the terminator. 
	 * @throws IOException 
	 */
	public String readConformantAndVaryingString() throws IOException{
		int maxElementCount = readInt();
		int offset = readInt();
		int actualElementCount = readInt();
		if (actualElementCount > (maxElementCount - offset) || offset > maxElementCount ){
			throw new IOException("Malformed NDR stream. String's actualElementCount must be greater than maxElementCount minus offset. String's maxElementCount must be greater than the offset.");
		}
		//Unicode string so each element is 2 bytes
		//shiftPosition based on the offset
		if (offset > 0) {
			shiftPosition(offset * 2);
		}
		char[] chars = new char[actualElementCount];
		for(int i = 0; i < chars.length; i++) {
			chars[i] = (char)readShort();
		}		
		return new String(chars);
	}




}
