package uk.co.jtnet.security.kerberos;

import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class Krb5ClientHelper {

	String clientPrincipal;

	//private static final Logger LOG = LoggerFactory.getLogger(Krb5ClientHelper.class);

	public Krb5ClientHelper(String clientPrincipal) {
		//TODO check the format and make sure the realm is uppercase.
		this.clientPrincipal = clientPrincipal;
	}

	public Krb5ClientHelper(String realm, String username) {
		//Realms should be in uppercase in the krb5.conf so in order to match we need to make this uppercase.
		this.clientPrincipal = username + "@" + realm.toUpperCase();
	}

	public Subject krb5KeytabLogin(String keytab) {
		String loginModuleName = "krb5NonInteractiveClientLogin";

		//LOG.info("Attempting kerberos login of user: " + clientPrincipal + " using keytab: " + keytab);
		//Form jaasOptions map
		Map<String, String> jaasOptions = new HashMap<String, String>();
		jaasOptions.put("useKeyTab", "true");
		jaasOptions.put("keyTab", keytab);
		jaasOptions.put("principal", clientPrincipal);
		jaasOptions.put("storeKey", "true"); //Need this to be true for when the server side logs in.
		jaasOptions.put("doNotPrompt", "true");
		jaasOptions.put("refreshKrb5Config", "false");
		jaasOptions.put("clearPass", "true");
		jaasOptions.put("useTicketCache", "false");
		//LOG.debug("Dynamic jaas configuration used:" + jaasOptions.toString());

		//Create dynamic jaas config
		DynamicJaasConfiguration contextConfig = new DynamicJaasConfiguration();
		contextConfig.addAppConfigEntry(loginModuleName,
				"com.sun.security.auth.module.Krb5LoginModule",
				AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
				jaasOptions);
		try {
			/*
			 * The nonInteractiveCallbackHandler should not be needed as the jaas config sets the client to use keytab file and not prompt the user.
			 * Therefore this is suitable for system authentication. if the callback handler is used the nonInteractiveCallbackHandler just throws exceptions.
			 */
			LoginContext loginCtx = new LoginContext(loginModuleName,null, new NonInteractiveCallbackHandler(), contextConfig);
			loginCtx.login();
			Subject clientSubject = loginCtx.getSubject();
			String loggedInUser = principalNameFromSubject(clientSubject);
			//LOG.info("SUCCESSFUL LOGIN for user: " + loggedInUser + " using keytab: " + keytab);
			return clientSubject;
		}
		catch (LoginException le){
			//LOG.info("LOGIN FAILED for user: " + clientPrincipal + " using keytab: " + keytab + " Reason: " + le.toString());
			le.printStackTrace();
			return null;
		}
	}

	public Subject krb5PasswordLogin(String password) {
		String loginModuleName = "krb5UsernamePasswordLogin";

		//LOG.info("Attempting kerberos authentication of user: " + clientPrincipal + " using username and password mechanism");

		//Set the domain to realm and the kdc
		//System.setProperty("java.security.krb5.realm", "JTLAN.CO.UK");
		//System.setProperty("java.security.krb5.kdc", "jtserver.jtlan.co.uk");
		//System.setProperty("java.security.krb5.conf", "/home/turnerj/git/servlet-security-filter/KerberosSecurityFilter/src/main/resources/krb5.conf");

		//Form jaasOptions map
		Map<String, String> jaasOptions = new HashMap<String, String>();
		jaasOptions.put("useKeyTab", "false");
		jaasOptions.put("storeKey", "false");
		jaasOptions.put("doNotPrompt", "false");
		jaasOptions.put("refreshKrb5Config", "false");
		jaasOptions.put("clearPass", "true");
		jaasOptions.put("useTicketCache", "false");
		//LOG.debug("Dynamic jaas configuration used:" + jaasOptions.toString());

		//Create dynamic jaas config
		DynamicJaasConfiguration contextConfig = new DynamicJaasConfiguration();
		contextConfig.addAppConfigEntry(loginModuleName,
				"com.sun.security.auth.module.Krb5LoginModule",
				AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
				jaasOptions);

		try {
			/*
			 * Create login context using dynamic config
			 * The "krb5UsernamePasswordLogin" needs to correspond to a configuration in the jaas config.
			 */
			LoginContext loginCtx = new LoginContext(loginModuleName,null, new LoginUsernamePasswordHandler(clientPrincipal, password), contextConfig);
			loginCtx.login();
			Subject clientSubject = loginCtx.getSubject();
			String loggedInUser = principalNameFromSubject(clientSubject);
			//LOG.info("SUCCESSFUL LOGIN for user: " + loggedInUser + " using username and password mechanism.");
			return clientSubject;
		}
		catch (LoginException le){
			le.printStackTrace();
			// Failed logins are not an application error so the following line is at info level.
			//LOG.info("LOGIN FAILED for user: " + clientPrincipal + " using username and password mechanism. Reason: " + le.toString());
			return null;
		}
	}

	public String principalNameFromSubject(Subject clientSubject) {
		Set<KerberosPrincipal> principals  = clientSubject.getPrincipals(KerberosPrincipal.class);
		return principals.iterator().next().getName();
	}

	public byte[] requestServiceTicket(Subject clientSubject, String servicePrincipalName) {
		//For some reason SPNs in the format HTTP/servicehost.domain end up as HTTP/servicehost.domain/hostname. SPNs defined as HTTP@servicehost.domain work fine.
		servicePrincipalName = servicePrincipalName.replace('/', '@');
		try {
			Oid krb5Oid = new Oid("1.2.840.113554.1.2.2");
			GSSManager manager = GSSManager.getInstance();
			GSSName serverName = manager.createName(servicePrincipalName,
					GSSName.NT_HOSTBASED_SERVICE, krb5Oid);
			final GSSContext context = manager.createContext( serverName, krb5Oid, null,
					GSSContext.DEFAULT_LIFETIME);
			byte[] serviceTicket = Subject.doAs(clientSubject, new PrivilegedAction<byte[]>() {
				public byte[] run() {
					byte[] token = new byte[0];
					// This is a one pass context initialisation.
					try {
						context.requestMutualAuth(false);
						context.requestCredDeleg(false);
						return context.initSecContext(token, 0, token.length);
					} catch (GSSException e) {
						e.printStackTrace();
						return null;
					}
				}
			});
			return serviceTicket;
		}
		catch ( GSSException e) {
			e.printStackTrace();
			return null;
		}
	}

}
