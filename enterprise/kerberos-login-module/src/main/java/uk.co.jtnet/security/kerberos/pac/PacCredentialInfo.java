package uk.co.jtnet.security.kerberos.pac;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacCredentialInfo extends PacInfoBuffer {
	
	/*The KDC may return supplemental credentials in the PAC as well. 
	Supplemental credentials are data associated with a security package that is private to that package.
	They can be used to return an appropriate user key that is specific to that package for the purposes of authentication.
	Supplemental creds are only used in conjunction with PKINIT[2].
	Supplemental credentials are always encrypted using the client key.
	The PAC_CREDENTIAL_DATA structure is NDR encoded and then encrypted with the key used to encrypt the KDC's reply to the client.
	The PAC_CREDENTIAL_INFO structure is included in PAC_INFO_BUFFER of type PAC_CREDENTIAL_TYPE.*/
	
	private int version;
	private int encryptionTypeInt;
	private PacCredentialData pacCredentialData;
	
	public PacCredentialInfo() {}
	
	@Override
	public void process(int pacInfoBufferType, byte[] pacInfoBufferBytes) throws IOException {
		super.process(pacInfoBufferType, pacInfoBufferBytes);
		ByteBuffer pacDataStream = ByteBuffer.wrap(pacInfoBufferBytes);
		pacDataStream.order(ByteOrder.LITTLE_ENDIAN);
		
		this.version = pacDataStream.getInt();
		this.encryptionTypeInt = pacDataStream.getInt();
		byte [] pacCredentialBytes = new byte[pacDataStream.remaining()];
		pacDataStream.get(pacCredentialBytes);
		pacCredentialData = new PacCredentialData(pacCredentialBytes, encryptionTypeInt);		
	}

	public int getVersion() {
		return version;
	}

	public int getEncryptionTypeInt() {
		return encryptionTypeInt;
	}

	public PacCredentialData getPacCredentialData() {
		return pacCredentialData;
	}

}
