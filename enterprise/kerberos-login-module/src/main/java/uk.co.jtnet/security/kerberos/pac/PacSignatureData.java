package uk.co.jtnet.security.kerberos.pac;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.security.auth.Subject;

import sun.security.krb5.Checksum;

public class PacSignatureData extends PacInfoBuffer {

	private int signatureType;
	private byte[] signatureBytes;
	private String signatureString;
	private byte[] zeroedSignaturePacSignatureData;

	@Override
	public void process(int pacInfoBufferType, byte[] pacInfoBufferBytes) throws IOException {
		super.process(pacInfoBufferType, pacInfoBufferBytes);
		ByteBuffer pacDataStream = ByteBuffer.wrap(pacInfoBufferBytes);
		pacDataStream.order(ByteOrder.LITTLE_ENDIAN);

		this.signatureType = pacDataStream.getInt();
		int signatureSize = 0;

		switch (signatureType) {
		case Checksum.CKSUMTYPE_HMAC_MD5_ARCFOUR:
			signatureSize = 16;
			break;
		case Checksum.CKSUMTYPE_HMAC_SHA1_96_AES128:
			signatureSize = 12;
			break;
		case Checksum.CKSUMTYPE_HMAC_SHA1_96_AES256:
			signatureSize = 12;
			break;
		default:
			throw new IOException("PAC signature using unknown encryption algorithm");
		}

		int sigStartPosition = pacDataStream.position();
		byte[] sigBytes = new byte[signatureSize];
		pacDataStream.get(sigBytes);
		this.signatureBytes = sigBytes;;

		for (int i=0; i < signatureSize; i++){
			pacInfoBufferBytes[sigStartPosition + i] = (byte)0;		
		}
		this.zeroedSignaturePacSignatureData = pacInfoBufferBytes;

		//short rodcIdentifier = pacDataStream.getShort();	
	}

	public int getSignatureType() {
		return signatureType;
	}

	public byte[] getSignatureBytes() {
		return signatureBytes;
	}

	public String getSignatureString() {
		if (signatureString != null){
			return signatureString;
		}
		StringBuffer sbuffer = new StringBuffer();
		for (int i = 0; i < signatureBytes.length; i++) {
			String hex = Integer.toHexString(0xFF & signatureBytes[i]);
			if (hex.length() == 1) {
				sbuffer.append('0');
			}
			sbuffer.append(hex);
		}
		return sbuffer.toString();
	}

	public byte[] getZeroedSignaturePacSignatureData() {
		return zeroedSignaturePacSignatureData;
	}


}
