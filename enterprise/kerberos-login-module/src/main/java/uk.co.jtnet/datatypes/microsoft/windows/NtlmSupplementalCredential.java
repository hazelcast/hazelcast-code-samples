package uk.co.jtnet.datatypes.microsoft.windows;

import java.io.IOException;
import java.util.Arrays;

import uk.co.jtnet.encoding.ndr.MsRpceNdrStream;

public class NtlmSupplementalCredential {
	
	/*The NTLM_SUPPLEMENTAL_CREDENTIAL structure is used to encode the credentials that the
	NTLM security protocol uses, specifically the LAN Manager hash (LM OWF) and the NT hash (NT
	OWF). Generating the hashes encoded in this structure is not addressed in the PAC Data Structure
	specification. Details on how the hashes are created are as specified in [MS-NLMP]. The PAC buffer
	type is included only when PKINIT [MS-PKCA] is used to authenticate the user. The
	NTLM_SUPPLEMENTAL_CREDENTIAL structure is marshaled by RPC [MS-RPCE].
	
	Byte size should be 40
	 - version - 32bit unsigned int = 4 bytes
	 - flags - 32 bit unsigned int = 4 bytes
	 - lmPassword - 16 byte array
	 - ntPassword - 16 byte array
	 
	Version: A 32-bit unsigned integer that defines the credential version. This field MUST be 0x00000000.
	Flags: A 32-bit unsigned integer containing flags that define the credential options. Flags MUST
		contain a non zero bit at at least one of the last two positions.
	
	*/
	
	private byte[] credentialBytes = new byte[40];
	
	private long version;
	private long flags;
	private byte[] lmPassword = new byte[16];
	private byte[] ntPassword = new byte[16];
	private boolean lmOwf;
	private boolean ntOwf;
	
	public NtlmSupplementalCredential(byte[] credentialBytes) throws IOException {
		if (credentialBytes.length != 40){
			throw new IOException("NTLM Supplemental Credential data is not the right size.");
		}
		this.credentialBytes = credentialBytes;
		process();
	}
	
	private void process() throws IOException{
		MsRpceNdrStream dataStream = new MsRpceNdrStream(credentialBytes);
		dataStream.initializeStream();
		
		this.version = dataStream.readUnsignedInt();
		if (version != 0) {
			throw new IOException("NTLM Supplemental Credntial does not have the correct version of zero.");
		}
		this.flags = dataStream.readUnsignedInt();
		processFlags();
		dataStream.readIntoArray(this.lmPassword);
		dataStream.readIntoArray(this.ntPassword);
		if (!lmOwf){
			//LmPassword member MUST be ignored if the LM OWF flag is not set in the Flags member
			Arrays.fill(this.lmPassword, (byte)0);
		}
		if (!ntOwf){
			//NtPassword member MUST be ignored if the NT OWF flag is not set in the Flags member
			Arrays.fill(this.ntPassword, (byte)0);
		}
		if (!lmOwf && ! ntOwf) {
			throw new IOException("NTLM Supplemental Credential does not have either the LM OWF or NT OWF flag set.");
		}
	}
	;
	
	private void processFlags() {
		this.lmOwf = (getBitAtPosition(flags, 63) != 0);
		this.ntOwf = (getBitAtPosition(flags, 62) != 0);
	}
	
	private int getBitAtPosition(long x, int bitPosition){
		return (int) ((x >>> (63 - bitPosition)) & 1);
	}

}
