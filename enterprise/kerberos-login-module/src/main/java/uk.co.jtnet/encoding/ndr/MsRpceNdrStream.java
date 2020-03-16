package uk.co.jtnet.encoding.ndr;

import java.io.IOException;
import java.util.Date;

import uk.co.jtnet.datatypes.microsoft.windows.FileTime;
import uk.co.jtnet.datatypes.microsoft.windows.KerbSidAndAttributes;
import uk.co.jtnet.datatypes.microsoft.windows.RpcSid;
import uk.co.jtnet.datatypes.microsoft.windows.RpcSidIdentifierAuthority;
import uk.co.jtnet.datatypes.microsoft.windows.RpcUnicodeString;
import uk.co.jtnet.datatypes.microsoft.windows.SecPkgSupplementalCred;

public class MsRpceNdrStream extends NdrStream {

	public MsRpceNdrStream(byte[] rawBytesArray) throws IOException{
		super(rawBytesArray);
	}

	@Override
	public void parseRPCCommonHeader() throws IOException{
		/* The first 8 bytes comprise the Common RPC Header for type marshalling.
		 * The next 8 bytes comprise the RPC type marshalling private header for constructed types. 
		 */	
		if (rawBytesArray.length < 16){
			throw new IOException("Malformed NDR data stream. Not big enough to contain RPC headers and data");
		}
		/* RPC Common Header (https://msdn.microsoft.com/en-us/library/cc243890.aspx):
		 * - First Byte[0] - the protocol version
		 * - RPC Common Header Must be a multiple of 8 bytes in length
		 */
		rpcTypeSerializationVerion = rawBytesArray[0];
		switch (rpcTypeSerializationVerion) {
		case 1:
			parseRPCCommonHeaderVersionOne();
			break;
		case 2:
			throw new IOException("RPC Common Type Header parsing for serialization version 2 not yet implemented");
		default:
			throw new IOException("Malformed Common RPC Header. Unkown serialization version.");
		}
	}

	private void parseRPCCommonHeaderVersionOne() throws IOException{
		/* Within the RPC Common Header is the NDR Format Label:
		 *   - Second Byte[1]:
		 *     - Big or Little Endian (first 4 bits)
		 *     - ASCII or EBCDIC (next 4 bits)
		 *   - Next 2 bytes (short) common header length
		 * - Remaining content of the RPC Common Header is filler and can be ignored
		 */
		byte secondByte = rawBytesArray[1];
		Endian = secondByte >> 4 & 0xF;
		charEncoding = secondByte & 0xF;  // For MS-RPCE this will always be zero, ie ASCII
		floatingPointRepresentation = IEEE;
		initializeStreamBuffer();
		//Shift over the first two bytes processed in the Common Type header
		shiftPosition(2);
		headerLength = ndrByteStreamBuffer.getShort();
		//The header length must be a multiple of 8
		if (headerLength % 8 != 0){
			throw new IOException("Malformed Common RPC Header. Header length must be a multiple of 8 octets");
		}
		//Set the position according to the header length
		ndrByteStreamBuffer.position(headerLength);

		// We are now at the end of the Common RPC Header and at the beginning of the Private RPC Header (Ref: MS-RPCE section 2.2.6.2)
		parseRPCPrivateHeaderVersionOne();
	}

	private void parseRPCPrivateHeaderVersionOne(){
		/* Common RPC Header and at the beginning of the Private RPC Header (Ref: MS-RPCE section 2.2.6.2)
		 * Private RPC Header (Total 8 bytes)
		 * - First 4 bytes - data length
		 * - Second 4 bytes - filler and can be ignored
		 */
		dataLength = ndrByteStreamBuffer.getInt();
		//Shift over the remaining 4 bytes of filler of the Private RPC Header
		shiftPosition(4);
		//We are now at the start of the data
	}

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
	public Date readFileTime() throws IOException {
		int lowOrder = readInt();
		int highOrder = readInt();

		FileTime fileTime = new FileTime(lowOrder, highOrder);
		return fileTime.getDate();

	}
	
	/**
	 * RPC_UNICODE_STRING
	 * Length: The length, in bytes, of the string pointed to by the Buffer member, not including the terminating null character if any. The length MUST be a multiple of 2. The length SHOULD equal the entire size of the Buffer, in which case there is no terminating null character. Any method that accesses this structure MUST use the Length specified instead of relying on the presence or absence of a null character.
	 * MaximumLength: The maximum size, in bytes, of the string pointed to by Buffer. The size MUST be a multiple of 2. If not, the size MUST be decremented by 1 prior to use. This value MUST not be less than Length.
	 * Buffer: A pointer to a string buffer. If MaximumLength is greater than zero, the buffer MUST contain a non-null value. 
	 */
	public RpcUnicodeString readRpcUnicodeString() throws IOException{
		short length = readShort();
		short maxLength = readShort();
		int pointer = readInt();

		if(maxLength < length || (length % 2 != 0) || (maxLength % 2 != 0)) {
			throw new IOException("Malformed RPC_UNICODE_STRING in the Pac");
		}

		return new RpcUnicodeString(length, maxLength, pointer);	
	}
	

	public SecPkgSupplementalCred readSecpkgSupplementalCred () throws IOException{
		RpcUnicodeString packageName = readRpcUnicodeString();
		long credentialsSize = readUnsignedInt();
		long credentialsPointer = readUnsignedInt();

		return new SecPkgSupplementalCred(packageName, credentialsSize, credentialsPointer);	
	}

	public KerbSidAndAttributes readKerbSidAndAttributes(){
		long rid = readUnsignedInt();
		int attributes = readInt();
		return new KerbSidAndAttributes(rid, attributes);
	}

	public RpcSid readRpcSid() {	
		int unknown = readInt();
		byte revision = readByte();
		byte subAuthorityCount = readByte();
		RpcSid rpcSid = new RpcSid(revision);
		byte[] identifierAuthority = new byte[6];
		readIntoArray(identifierAuthority);
		rpcSid.setIdentifierAuthority(new RpcSidIdentifierAuthority(identifierAuthority));
		for (int i = 0; i < subAuthorityCount; i++) {
			rpcSid.addSubAuthority(readUnsignedInt());
		}
		return rpcSid;
	}

}
