package uk.co.jtnet.datatypes.microsoft.windows;

import java.io.IOException;

import uk.co.jtnet.encoding.ndr.MsRpceNdrStream;

public class SecPkgSupplementalCred {
	
	/*The SECPKG_SUPPLEMENTAL_CRED structure defines the name of the security package that
	requires supplemental credentials and the credential buffer for that package. The
	SECPKG_SUPPLEMENTAL_CRED structure is marshaled by RPC [MS-RPCE].
	
	PackageName: A RPC_UNICODE_STRING structure that MUST store the name of the
		security protocol for which the supplemental credentials are being presented. 
		The only package name that Microsoft KDCs use is "NTLM". If any other
		package name is provided, Windows discards the supplemental credential.
	CredentialSize: A 32-bit unsigned integer that MUST specify the length, in bytes, of the data in
		the Credentials member.
	Credentials: A pointer that MUST reference the serialized credentials being presented to the
		security protocol named in PackageName.
	
	*/
	
	private byte[] credentialBytes;
	
	private RpcUnicodeString packageName;
	private long credentialsSize;
	private long credentialsPointer; //PUCHAR??? Not sure where we get the actual credentials. Perhaps it is straight after this but not sure without test data
	
	public SecPkgSupplementalCred(RpcUnicodeString packageName, long credentialsSize, long credentialsPointer) throws IOException {
		this.packageName = packageName;
		this.credentialsSize = credentialsSize;
		this.credentialsPointer = credentialsPointer;
	}

}
