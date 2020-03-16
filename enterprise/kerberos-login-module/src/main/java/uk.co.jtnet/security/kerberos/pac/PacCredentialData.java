package uk.co.jtnet.security.kerberos.pac;

import java.io.IOException;

import javax.security.auth.Subject;

import sun.security.krb5.EncryptedData;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.KrbCryptoException;
import sun.security.krb5.internal.KdcErrException;
import sun.security.krb5.internal.KrbApErrException;
import uk.co.jtnet.datatypes.microsoft.windows.SecPkgSupplementalCred;
import uk.co.jtnet.encoding.ndr.MsRpceNdrStream;
import uk.co.jtnet.security.kerberos.Krb5ServerHelper;

public class PacCredentialData {
	
	/*A PAC_CREDENTIAL_INFO structure contains the encrypted user's credentials.
	Supplemental credentials are always encrypted using the client key.
	The PAC_CREDENTIAL_DATA structure is NDR encoded and then encrypted with the key used to encrypt the KDC's reply to the client.
	The Key Usage Number [RFC4120] used in the encryption is KERB_NON_KERB_SALT (16)
	*/
	
	private byte[] pacCredentialEncBytes;
	private int encryptionType;
	
	private long credentialCount;
	private SecPkgSupplementalCred[] secpkgSupplementalCredArray;
	
	public PacCredentialData(byte[] pacCredentialBytes, int encryptionType) {
		this.pacCredentialEncBytes = pacCredentialBytes;
		this.encryptionType = encryptionType;
	}
	
	public byte[] decrypt(EncryptionKey clientPrivateKey) throws KdcErrException, KrbApErrException, KrbCryptoException{
		if (clientPrivateKey.getEType() != this.encryptionType) {
			throw new KrbCryptoException("Encryption type of the provided key (" + clientPrivateKey.getEType() + ") does not match that of the data (" + encryptionType + ").");
		}
		EncryptedData pacCredentialEncData = new EncryptedData(clientPrivateKey.getEType(), clientPrivateKey.getKeyVersionNumber(), pacCredentialEncBytes);
		return pacCredentialEncData.decrypt(clientPrivateKey, PacConstants.KERB_NON_KERB_SALT);
	}
	
	public byte[] decrypt(Subject clientSubject, int encryptionType) throws KdcErrException, KrbApErrException, KrbCryptoException{
		EncryptionKey clientPrivateKey = Krb5ServerHelper.getServerPrivateKey(clientSubject, encryptionType);
		if (clientPrivateKey == null) {
			throw new KrbCryptoException("Could not find key within the provided subject that matches the encryption type of the data (" + encryptionType + ").");
		}
		EncryptedData pacCredentialEncData = new EncryptedData(clientPrivateKey.getEType(), clientPrivateKey.getKeyVersionNumber(), pacCredentialEncBytes);
		return pacCredentialEncData.decrypt(clientPrivateKey, PacConstants.KERB_NON_KERB_SALT);
	}
	
	private static SecPkgSupplementalCred[] process(byte[] pacCredentialClearTextData) throws IOException {
		MsRpceNdrStream dataStream = new MsRpceNdrStream(pacCredentialClearTextData);
		dataStream.initializeStream();
		
		//TODO Need to check if this is the right way to read array from the NDR.
		int credentialCount = (int) dataStream.readUnsignedInt();
		SecPkgSupplementalCred[] secArray = new SecPkgSupplementalCred[(int) credentialCount];
		for (int i = 0; i < credentialCount; i ++) {
			secArray[i] = dataStream.readSecpkgSupplementalCred();
		}
		return secArray;
	}

}
