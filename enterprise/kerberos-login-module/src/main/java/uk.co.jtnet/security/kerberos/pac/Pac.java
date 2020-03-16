package uk.co.jtnet.security.kerberos.pac;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import sun.security.krb5.Checksum;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.KrbCryptoException;
import sun.security.krb5.internal.KdcErrException;
import sun.security.krb5.internal.KrbApErrException;
import uk.co.jtnet.datatypes.microsoft.windows.RpcSid;

public class Pac {

	/* The pac takes the following format
    cBuffers (4 bytes): A 32-bit unsigned integer in little-endian format that defines the number of entries in the Buffers array.
    Version (4 bytes): A 32-bit unsigned integer in little-endian format that defines the PAC version; MUST be 0x00000000.
    Buffers (variable): An array of PAC_INFO_BUFFER structures.
    Ref: https://msdn.microsoft.com/en-us/library/cc237950.aspx

    Therefore...
     - first 4 bytes is the pacInfoBufferCount
     - second 4 bytes is the pacVersion
     - remaining is an array of PAC_INFO_BUFFER

     The little endian format means that we need to use a little endian byte buffer for reading the pacInfoBufferCount and the pacVersion.
	 */

	static final int PAC_VERSION = 0;

	private int pacVersion;
	private int pacInfoBufferCount;
	private HashMap<Integer, PacInfoBuffer> pacInfoBufferMap = new HashMap<Integer, PacInfoBuffer>();

	public Pac(byte[] PacBytes, EncryptionKey serverPrivateKey) throws Exception{
		if (PacBytes.length <= (8)){
			//Pac must be more than 8 bytes as it must contain the buffer count (4bytes), version (4bytes) and some data.
			throw new Exception("PAC contains no information");			
		}
		ByteBuffer bbLittle = ByteBuffer.wrap(PacBytes);
		bbLittle.order(ByteOrder.LITTLE_ENDIAN);
		//The order of calling the following 3 is important.
		this.pacInfoBufferCount = bbLittle.getInt();
		this.pacVersion = bbLittle.getInt();

		//Now go through the PAC_INFO_BUFFERS (Ref: https://msdn.microsoft.com/en-us/library/cc237954.aspx)
		for(int pacInfoBufferIndex = 0; pacInfoBufferIndex < pacInfoBufferCount; pacInfoBufferIndex++) {
			int pacInfoBufferType = bbLittle.getInt();
			//TODO should only be one at most for each type. Ignore duplicates and don't add to array.
			int pacInfoBufferSize = bbLittle.getInt();
			long pacInfoBufferOffest = bbLittle.getLong();
			byte[] pacInfoBufferData = new byte[pacInfoBufferSize];
			System.arraycopy(PacBytes, (int)pacInfoBufferOffest, pacInfoBufferData, 0, pacInfoBufferSize);
			PacInfoBuffer pacInfo = pacInfoBufferParser.parse(pacInfoBufferType, pacInfoBufferData);
			if (pacInfoBufferType ==  pacInfoBufferParser.SERVER_CHECKSUM.getPacInfoBufferTypeInt() || 
					pacInfoBufferType ==  pacInfoBufferParser.KDC_PRIVSERVER_CHECKSUM.getPacInfoBufferTypeInt()){
				PacSignatureData pacSignatureData = (PacSignatureData)pacInfo;
				System.arraycopy(pacSignatureData.getZeroedSignaturePacSignatureData(), 0, PacBytes, (int)pacInfoBufferOffest, pacInfoBufferSize);
			}
			if (! pacInfoBufferMap.containsKey(pacInfoBufferType)) {
				pacInfoBufferMap.put(pacInfoBufferType, pacInfo);
			}
		}	
		validatePacContents(pacInfoBufferMap);
		if (serverPrivateKey != null && !(validatePacSignature(serverPrivateKey, PacBytes, (PacSignatureData) pacInfoBufferMap.get(pacInfoBufferParser.SERVER_CHECKSUM.getPacInfoBufferTypeInt())))){
			throw new IOException("PAC Server signature check failed. PAC integrity cannot be trusted");
		}
	}
	
	public void validatePacContents(HashMap<Integer, PacInfoBuffer> pacInfoBufferMap) throws IOException{
		int[] mustContainBufferTypes = { pacInfoBufferParser.KERB_VALIDATION_INFO.getPacInfoBufferTypeInt(),
				pacInfoBufferParser.PAC_CLIENT_INFO.getPacInfoBufferTypeInt(),
				pacInfoBufferParser.SERVER_CHECKSUM.getPacInfoBufferTypeInt(),
				pacInfoBufferParser.KDC_PRIVSERVER_CHECKSUM.getPacInfoBufferTypeInt()};
		for (int i = 0; i < mustContainBufferTypes.length; i++){
			if ( ! pacInfoBufferMap.containsKey(mustContainBufferTypes[i])){
				throw new IOException("PAC is missing a PAC Info Buffer of type: " + pacInfoBufferParser.getPacInfoBufferTypeString(mustContainBufferTypes[i]) + " (" + mustContainBufferTypes[i] + ").");
			}
		}
	}

	public boolean validatePacSignature(EncryptionKey serverPrivateKey, byte[] PacBytes, PacSignatureData pacServerChecksum) throws KdcErrException, KrbApErrException, KrbCryptoException {
		Checksum checksum = new Checksum(pacServerChecksum.getSignatureType(), PacBytes, serverPrivateKey, PacConstants.KERB_NON_KERB_CKSUM_SALT);
		byte[] validationSignature = checksum.getBytes(); 
		if (Arrays.equals(validationSignature, pacServerChecksum.getSignatureBytes())) {
			return true;
		}
		return false;
	}

	public enum pacInfoBufferParser {

		KERB_VALIDATION_INFO(1, new PacKerbValidationInfo()),
		PAC_CREDENTIALS(2, new PacCredentialInfo()),
		SERVER_CHECKSUM(6, new PacSignatureData()),
		KDC_PRIVSERVER_CHECKSUM(7, new PacSignatureData()),
		PAC_CLIENT_INFO(10, new PacClientInfo()),
		CONSTRAINED_DELEGATION_INFO(11, new PacS4UDelegationInfo()),
		UPN_DNS_INFO(12, new PacUpnDnsInfo()),
		PAC_CLIENT_CLAIMS_INFO(13, new PacClientClaimsInfo()),
		PAC_DEVICE_INFO(14, new PacDeviceInfo()),
		PAC_DEVICE_CLAIMS_INFO(15, new PacDeviceClaimsInfo());

		private final int pacInfoBufferTypeInt;
		private final PacInfoBuffer pacInfoBuffer;

		pacInfoBufferParser(int pacInfoBufferTypeInt, PacInfoBuffer pacInfoBuffer) {
			this.pacInfoBufferTypeInt = pacInfoBufferTypeInt;
			this.pacInfoBuffer = pacInfoBuffer;
		}
		public int getPacInfoBufferTypeInt() {
			return pacInfoBufferTypeInt;
		}
		public static PacInfoBuffer parse(int pacInfoBufferTypeInt, byte[] pacInfoBufferData) throws IOException {
			PacInfoBuffer pacInfoBuffer = findByPacInfoBufferTypeInt(pacInfoBufferTypeInt).pacInfoBuffer;
			pacInfoBuffer.process(pacInfoBufferTypeInt, pacInfoBufferData);
			return pacInfoBuffer;
		}
		
		public static pacInfoBufferParser findByPacInfoBufferTypeInt(int pacInfoBufferTypeInt) throws IOException {
			for	(pacInfoBufferParser value : values()) {
				if (value.pacInfoBufferTypeInt == pacInfoBufferTypeInt) {            
					return value;           
				}
			}	 
			throw new IOException ("Malformed PAC. Unknown PAC Info Buffer.");
		}
		public static String getPacInfoBufferTypeString(int pacInfoBufferTypeInt) throws IOException {
			return findByPacInfoBufferTypeInt(pacInfoBufferTypeInt).toString();
		}

	}

	public int getPacVersion() {
		return pacVersion;
	}

	public int getPacInfoBufferCount() {
		return pacInfoBufferCount;
	}	

	public HashMap<Integer, PacInfoBuffer> getPacInfoBufferMap() {
		return pacInfoBufferMap;
	}
	
	public String getFullName(){
		return ((PacKerbValidationInfo) pacInfoBufferMap.get(pacInfoBufferParser.KERB_VALIDATION_INFO.getPacInfoBufferTypeInt())).getFullName();
	}
	
	public String getHomeDirectory(){
		return ((PacKerbValidationInfo) pacInfoBufferMap.get(pacInfoBufferParser.KERB_VALIDATION_INFO.getPacInfoBufferTypeInt())).getHomeDirectory();
	}
	
	public long getiGroupCount(){
		return ((PacKerbValidationInfo) pacInfoBufferMap.get(pacInfoBufferParser.KERB_VALIDATION_INFO.getPacInfoBufferTypeInt())).getGroupCount();
	}
	
	public RpcSid[] getGroupMemberships(){
		return ((PacKerbValidationInfo) pacInfoBufferMap.get(pacInfoBufferParser.KERB_VALIDATION_INFO.getPacInfoBufferTypeInt())).getGroupMembershipSids();
	}
	
	public String getLogonDomainName(){
		return ((PacKerbValidationInfo) pacInfoBufferMap.get(pacInfoBufferParser.KERB_VALIDATION_INFO.getPacInfoBufferTypeInt())).getLogonDomainName();
	}
	
	public String getKdc(){
		return ((PacKerbValidationInfo) pacInfoBufferMap.get(pacInfoBufferParser.KERB_VALIDATION_INFO.getPacInfoBufferTypeInt())).getLogonServer();
	}
	
	public Date getPasswordSetDateTime(){
		return ((PacKerbValidationInfo) pacInfoBufferMap.get(pacInfoBufferParser.KERB_VALIDATION_INFO.getPacInfoBufferTypeInt())).getPasswordLastSetTime();
	}
	
	public Date getPasswordExpiresDateTime(){
		return ((PacKerbValidationInfo) pacInfoBufferMap.get(pacInfoBufferParser.KERB_VALIDATION_INFO.getPacInfoBufferTypeInt())).getPasswordMustChangeTime();
	}
	
	public long getPrimaryGroup(){
		return ((PacKerbValidationInfo) pacInfoBufferMap.get(pacInfoBufferParser.KERB_VALIDATION_INFO.getPacInfoBufferTypeInt())).getPrimaryGroupId();
	}
	
	public long getLogonCount(){
		return ((PacKerbValidationInfo) pacInfoBufferMap.get(pacInfoBufferParser.KERB_VALIDATION_INFO.getPacInfoBufferTypeInt())).getLogonCount();
	}
	
	public String getProfilePath(){
		return ((PacKerbValidationInfo) pacInfoBufferMap.get(pacInfoBufferParser.KERB_VALIDATION_INFO.getPacInfoBufferTypeInt())).getProfilePath();
	}


}
