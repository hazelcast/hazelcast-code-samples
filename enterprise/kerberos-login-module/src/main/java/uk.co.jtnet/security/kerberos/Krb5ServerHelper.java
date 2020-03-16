package uk.co.jtnet.security.kerberos;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.kerberos.KeyTab;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import sun.security.jgss.krb5.Krb5Util;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.Realm;
import sun.security.krb5.RealmException;
import sun.security.krb5.internal.AuthorizationData;
import sun.security.krb5.internal.AuthorizationDataEntry;
import sun.security.krb5.internal.EncTicketPart;
import sun.security.krb5.internal.Ticket;
import sun.security.krb5.internal.crypto.KeyUsage;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import uk.co.jtnet.security.Identity;
import uk.co.jtnet.security.kerberos.pac.Pac;

public class Krb5ServerHelper {

	private String servicePrincipalName;
	private String serviceAccount;
	private Subject serverSubject;
	private EncryptionKey serverPrivateKey;
	//GSSManager gssManager;
	//GSSContext gssContext;

	//private static final Logger LOG = LoggerFactory.getLogger(Krb5ServerHelper.class);

	public Krb5ServerHelper(String servicePrincipalName, String serviceAccount, String keytab) {
		this.servicePrincipalName = servicePrincipalName.replace("/", "@").replace("\\","@");
		this.serviceAccount = serviceAccount;
		login(keytab);
		//LOG.debug("Server side logged in.");
	}

	public Krb5ServerHelper(String servicePrincipalName, Subject serverSubject) {
		this.servicePrincipalName = servicePrincipalName;
		this.serverSubject = serverSubject;
		//LOG.debug("Using provided subject for server side");
	}

	private void login(String keytab) {
		//The server side has to get a subject to decrypt service tickets. We can use the client login to do this.
		Krb5ClientHelper krbServiceClient = new Krb5ClientHelper(serviceAccount);
		this.serverSubject = krbServiceClient.krb5KeytabLogin(keytab);
	}

	/*    private void initGSSContext() {
    	Subject.doAs(serverSubject, new PrivilegedAction<Void>() {
			public Void run() {
				try {
					gssManager = GSSManager.getInstance();
					gssContext = gssManager.createContext((GSSCredential) null);
					gssContext.requestReplayDet(true);
				} catch (GSSException e) {
					LOG.error("Could not establish service side GSS context");
					e.printStackTrace();
				}
				return null;
			}
		});
    }*/

	public String authenticateClient(byte[] serviceTicket){
		try {
			Identity identity = getClientIdentity(serviceTicket);
			return (String) identity.getUsername();
		} catch (Exception e) {
			//LOG.info("Authentication of the client failed.");
			e.printStackTrace();
			return null;
		}
	}


	public Identity getClientIdentity(byte[] serviceTicket){
		try {
			DerValue APREQ = extractAPREQ(serviceTicket);
			Ticket authenticationTicket = extractAuthenticationTicketFromAPREQ(APREQ.toDerInputStream(), APREQ.length());
			Identity identity = decryptAuthenticationTicket(authenticationTicket);
			return identity;
		} catch (Exception e) {
			return null;
		}
	}


	//Step 1 - decode the service ticket and extract the AP-REQ
	private DerValue extractAPREQ(byte[] serviceTicket) throws Exception {
		//Decode the service ticket into a set of DER values.
		DerInputStream ticketStream = new DerInputStream(serviceTicket);
		DerValue[] values = ticketStream.getSet(serviceTicket.length, true);
		//Now extract the AP-REQ part from RFC 1510 this is found at...
		//AP-REQ ::= [APPLICATION 14] SEQUENCE
		for (int i=0; i<values.length; i++) {
			DerValue value = values[i];
			if (value.isConstructed((byte)14)) {
				value.resetTag( DerValue.tag_Set);
				return value;
			}
		}
		throw new Exception( "AP-REQ not found in service ticket.");
	}

	//Step 2 - Extract the encrypted authentication ticket part of the AP-REQ (see RFC 1510 section 5.5.1 for AP-REQ defintion)
	private Ticket extractAuthenticationTicketFromAPREQ(DerInputStream APREQStream, int APREQLength) throws Exception {
		//Structure of AP-REQ from RFC 1510
		//  AP-REQ ::=
		//         pvno[0]                       INTEGER,
		//         msg-type[1]                   INTEGER,
		//         ap-options[2]                 APOptions,
		//         ticket[3]                     Ticket,
		//         authenticator[4]              EncryptedData
		DerValue authenticationTicket = null;
		DerValue[] values = APREQStream.getSet(APREQLength, true);
		for (int i=0; i<values.length; i++) {
			DerValue value = values[i];
			if (value.isContextSpecific((byte)3)) {
				authenticationTicket = value.getData().getDerValue();
			}
		}

		if ( authenticationTicket == null) {
			throw new Exception("No Ticket found in AP-REQ PDU");
		}
		return new Ticket(authenticationTicket);
	}

	//Step 3 - Decrypt the authentication ticket
	private Identity decryptAuthenticationTicket(Ticket authenticationTicket) throws Exception{
		//Get the servers private key from its subject for the encryption type used for the authentication ticket
		this.serverPrivateKey = getServerPrivateKey(serverSubject, authenticationTicket.encPart.getEType());
		if (this.serverPrivateKey == null){
			throw new Exception("Unable to retrieve server's private key.");
		}
		//Decrypt the authentication ticket into cleartext bytes
		byte[] clearAuthenticationTicketBytes = authenticationTicket.encPart.decrypt(serverPrivateKey, KeyUsage.KU_TICKET);
		if (clearAuthenticationTicketBytes.length <= 0){
			throw new Exception("Authentication ticket is zero in size");
		}
		EncTicketPart readableEncPart = new EncTicketPart(authenticationTicket.encPart.reset(clearAuthenticationTicketBytes));

		//We can not access attributes that are decrypted
		//  EncTicketPart ::=     [APPLICATION 3] SEQUENCE {
		//        flags[0]             TicketFlags,
		//        key[1]               EncryptionKey,
		//        crealm[2]            Realm,
		//        cname[3]             PrincipalName,
		//        transited[4]         TransitedEncoding,
		//        authtime[5]          KerberosTime,
		//        starttime[6]         KerberosTime OPTIONAL,
		//        endtime[7]           KerberosTime,
		//        renew-till[8]        KerberosTime OPTIONAL,
		//        caddr[9]             HostAddresses OPTIONAL,
		//        authorization-data[10]   AuthorizationData OPTIONAL

		Identity clientIdentity = new Identity(readableEncPart.cname.toString());
		//Have to use reflection as for some reason crealm has been removed in Java 8 and the cname includes the @<DOMAIN>
		try {
			Realm crealm = (Realm) readableEncPart.getClass().getDeclaredField("crealm").get(readableEncPart);
			clientIdentity.setRealm(crealm.toString());
		} catch (Exception e) {
			if (clientIdentity.getUsername().contains("@")){
				String[] fqdnUsernameSplit = clientIdentity.getUsername().split("@");
				clientIdentity.setUsername(fqdnUsernameSplit[0]);
				clientIdentity.setRealm(fqdnUsernameSplit[1]);
			} else {
				clientIdentity.setRealm("unknown");
				//LOG.debug("In later versions of Java the crealm attribute is not present in the EncTicketPart class so we cannot get the client realm.");
			}
		}

		// clientDetails.put("clientRealm", readableEncPart.crealm.toString());  crealm no longer available as of Java 8.
		clientIdentity.setAuthenticationDateTime(new Date(readableEncPart.authtime.getTime()));
		clientIdentity.addAttribute("endTime", new Date(readableEncPart.endtime.getTime()));
		if (readableEncPart.caddr != null){
			clientIdentity.addAttribute("clientAddress", readableEncPart.caddr.toString());
		} else {
			//LOG.debug("clientAddress is null in the service ticket. This is defined as optional in RFC 1510.");
			clientIdentity.addAttribute("clientAddress", "unknown");
		}
		if (readableEncPart.authorizationData != null){
			AuthorizationData authorizationData = (AuthorizationData) readableEncPart.authorizationData;
			Map<String, Object> clientDetailsFromPac = decodeAuthorizationData(authorizationData);
			clientIdentity.addAllAttributes(clientDetailsFromPac);
			if(clientDetailsFromPac.containsKey("fullName") && clientIdentity.getDisplayName() == null){
				clientIdentity.setDisplayName((String)clientDetailsFromPac.get("fullName"));
			}
		} else {
			//LOG.debug("AuthorizationData is null.");
		}
		return clientIdentity;
	}

	private Map<String, Object> decodeAuthorizationData(AuthorizationData authorizationData) throws Exception{
		Map<String, Object> clientDetailsFromPac = new HashMap<String, Object>();
		//Iterate through the authorizationData and find adData with adType = AD-IF-RELEVANT (1) - see RFC 4210 section 7.5.4
		for( int i = 0; i < authorizationData.count(); i++) {
			if (authorizationData.item(i).adType == 1){
				DerInputStream adDataStream = new DerInputStream(authorizationData.item(i).adData);
				DerValue[] values = adDataStream.getSet(authorizationData.item(i).adData.length, true);
				//values[0] contains authorizationData entry with adType = AD-WIN2k-PAC (128) - see RFC 4210 section 7.5.4
				DerValue pacDerValue = values[0];
				AuthorizationDataEntry pacAuthorizationDataEntry = new AuthorizationDataEntry(pacDerValue);
				if (pacAuthorizationDataEntry.adType != 128){
					throw new IOException("PAC not found within authorization data as expected. Was expecting adType=128 (AD-WIN2K-PAC) within AD-IF-RELEVANT");
				}
				Pac pac = new Pac(pacAuthorizationDataEntry.adData, this.serverPrivateKey);
				clientDetailsFromPac.put("pac", pac);
				clientDetailsFromPac.put("fullName", pac.getFullName());
				clientDetailsFromPac.put("groupMemberships", pac.getGroupMemberships());
				clientDetailsFromPac.put("homeDirectory", pac.getHomeDirectory());
				clientDetailsFromPac.put("groupCount", pac.getiGroupCount());
				clientDetailsFromPac.put("kdc", pac.getKdc());
				clientDetailsFromPac.put("logonCount", pac.getLogonCount());
				clientDetailsFromPac.put("logonDomainName", pac.getLogonDomainName());
				clientDetailsFromPac.put("passwordExpiryDateTime", pac.getPasswordExpiresDateTime());
				clientDetailsFromPac.put("passwordSetDateTime", pac.getPasswordSetDateTime());
				clientDetailsFromPac.put("primaryGroup", pac.getPrimaryGroup());
				clientDetailsFromPac.put("profilePath", pac.getProfilePath());
			}
		}
		return clientDetailsFromPac;
	}


	//Gets the private key from the server's subject for the encryption type used in the ticket received from the client
	//This method has got more complicated as behviour seems to have changed in Java 8.
	public static EncryptionKey getServerPrivateKey(Subject subject, int keyType){
		Set<Object> serverCredentials = subject.getPrivateCredentials(Object.class);
		KerberosPrincipal serverPrincipal = null;
		KeyTab serverKeyTab = null;
		for ( Iterator<Object> i = serverCredentials.iterator(); i.hasNext();) {
			Object cred = i.next();
			if (cred instanceof KerberosKey) {
				KerberosKey key = (KerberosKey) cred;
				if ( key.getKeyType() == keyType) {
					KerberosKey krbKey = (KerberosKey) cred;
					return new EncryptionKey (krbKey.getEncoded(), krbKey.getKeyType(), keyType);
				}
			}
			if (cred instanceof KeyTab) {
				serverKeyTab = (KeyTab) cred;
				continue;
			}
			if (cred instanceof KerberosTicket) {
				KerberosTicket serverKerbTicket = (KerberosTicket) cred;
				serverPrincipal = serverKerbTicket.getClient();
				continue;
			}
		}
		try {
			PrincipalName princName = new PrincipalName(serverPrincipal.getName(), serverPrincipal.getRealm());
			EncryptionKey[] encKeyArray = Krb5Util.keysFromJavaxKeyTab(serverKeyTab, princName);
			for (EncryptionKey encKey : encKeyArray) {
			    if (encKey.getEType() == keyType) {
			    	return encKey;
			    }
			}
		} catch (RealmException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

}
