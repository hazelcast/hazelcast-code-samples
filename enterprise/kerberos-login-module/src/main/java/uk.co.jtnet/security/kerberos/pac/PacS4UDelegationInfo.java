package uk.co.jtnet.security.kerberos.pac;

import java.io.IOException;

import uk.co.jtnet.datatypes.microsoft.windows.RpcUnicodeString;
import uk.co.jtnet.datatypes.microsoft.windows.SecPkgSupplementalCred;
import uk.co.jtnet.encoding.ndr.MsRpceNdrStream;

public class PacS4UDelegationInfo extends PacInfoBuffer {
	
	private RpcUnicodeString s4u2ProxyTarget;
	private int transitedListSize;
	private RpcUnicodeString[] s4uTransitedServices;
	
	
	@Override
	public void process(int pacInfoBufferType, byte[] pacInfoBufferBytes) throws IOException {
		super.process(pacInfoBufferType, pacInfoBufferBytes);
		MsRpceNdrStream pacDataStream = new MsRpceNdrStream(pacInfoBufferBytes);
		pacDataStream.initializeStream();
		
		this.s4u2ProxyTarget = pacDataStream.readRpcUnicodeString();
		this.transitedListSize = (int) pacDataStream.readUnsignedInt(); //Not sure if this is the right datatype to read.
		RpcUnicodeString[] s4uTransServs = new RpcUnicodeString[this.transitedListSize];
		
		//TODO Need to check if this is the right way to read array from the NDR.
		for (int i = 0; i < transitedListSize; i ++) {
			s4uTransServs[i] = pacDataStream.readRpcUnicodeString();
		}
		this.s4uTransitedServices = s4uTransServs;
		
	}


}
