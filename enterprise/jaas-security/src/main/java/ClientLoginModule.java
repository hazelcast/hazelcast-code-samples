import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.security.ClusterPrincipal;
import com.hazelcast.security.Credentials;
import com.hazelcast.security.CredentialsCallback;
import com.hazelcast.security.SecurityConstants;
import com.hazelcast.security.SecurityUtil;
import com.hazelcast.security.UsernamePasswordCredentials;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * An example ClientLoginModule that hard codes authorisation details in a pair of Maps. You could amend this class to
 * perform look up against an LDAP store that would then return a set of Groups for the User.
 * <p>
 * Obviously you would NEVER store passwords in clear text.
 */
public class ClientLoginModule
        implements LoginModule {

    // Username and Password are stored here in clear text, obviously NEVER do this.
    private static final Map<String, String> ALLOWED_USERS_MAP = new HashMap<String, String>();

    // This map represents the userAssignedGroup(s) that a user would belong to, this would be the result from your LDAP store.
    private static final Map<String, String> USER_GROUPS = new HashMap<String, String>();

    private final ILogger logger = Logger.getLogger(getClass().getName());
    private Credentials credentials;
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map options;

    // The Group that the user is authorised for.
    private UserGroupCredentials userGroupCredentials;

    static {

        ALLOWED_USERS_MAP.put("david", "password1");
        ALLOWED_USERS_MAP.put("chris", "password2");

        USER_GROUPS.put("david", "adminGroup");
        USER_GROUPS.put("chris", "readOnlyGroup");

    }

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        // Options are a way of passing extra configuration to the LoginModule for example connection properties
        // for an LDAP server. These are the properties element you can find in the XML configuration for the <login-module>
        this.options = options;
    }

    /**
     * Login is called when this module is executed.
     *
     * @return false if authentication checks could not be executed due to bad or missing parameters
     * @throws LoginException when authentication check has been made but credentials are not accepted or found.
     */
    public boolean login()
            throws LoginException {

        boolean loginOk = true;

        final CredentialsCallback cb = new CredentialsCallback();

        // Get the Credentials upon which to perform the Authentication
        try {
            callbackHandler.handle(new Callback[]{cb});
            credentials = cb.getCredentials();
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getClass().getName() + ":" + e.getMessage());
            return false;
        }

        // If no Credentials were found exit returning false
        if (credentials == null) {
            logger.log(Level.WARNING, "Credentials could not be retrieved!");
            return false;
        }

        logger.log(Level.INFO, "Authenticating " + SecurityUtil.getCredentialsFullName(credentials));

        if (credentials instanceof UsernamePasswordCredentials) {
            doLoginCheck((UsernamePasswordCredentials) credentials);
        } else {
            logger.log(Level.WARNING, "Credentials were not of expected class type of "
                    + UsernamePasswordCredentials.class.getName());
            return false;
        }

        return loginOk;
    }

    private void doLoginCheck(UsernamePasswordCredentials credentials)
            throws FailedLoginException {

        String username = credentials.getUsername();
        String password = ALLOWED_USERS_MAP.get(username);

        if (password != null) {
            if (password.equals(credentials.getPassword())) {
                String userGroup = USER_GROUPS.get(username);
                if (userGroup != null) {
                    userGroupCredentials = new UserGroupCredentials(credentials.getEndpoint(), userGroup);
                    sharedState.put(SecurityConstants.ATTRIBUTE_CREDENTIALS, credentials);
                } else {
                    logger.log(Level.WARNING, "User Group not found for user " + username);
                    throw new FailedLoginException("User Group not found for user " + username);
                }
            }
        } else {
            logger.log(Level.WARNING, "User details not found for " + username);
            throw new FailedLoginException("User details not found for " + username);
        }

    }

    /**
     * Commit is called when all of the modules in the chain have passed.
     *
     * @return
     * @throws LoginException
     */
    public final boolean commit()
            throws LoginException {
        logger.log(Level.FINEST, "Committing authentication of " + SecurityUtil.getCredentialsFullName(credentials));
        final Principal principal = new ClusterPrincipal(userGroupCredentials);
        subject.getPrincipals().add(principal);
        sharedState.put(SecurityConstants.ATTRIBUTE_PRINCIPAL, principal);
        return true;
    }

    /**
     * Abort is called when one of the modules in the chain has failed.
     *
     * @return
     * @throws LoginException
     */
    public final boolean abort()
            throws LoginException {
        logger.log(Level.FINEST, "Aborting authentication of " + SecurityUtil.getCredentialsFullName(credentials));
        clearSubject();
        return true;
    }

    /**
     * Graceful Logout
     *
     * @return
     * @throws LoginException
     */
    public final boolean logout()
            throws LoginException {
        logger.log(Level.FINEST, "Logging out " + SecurityUtil.getCredentialsFullName(credentials));
        clearSubject();
        return true;
    }

    /**
     * Tidy up the Subject
     */
    private void clearSubject() {
        subject.getPrincipals().clear();
        subject.getPrivateCredentials().clear();
        subject.getPublicCredentials().clear();
    }

    public class UserGroupCredentials
            implements Credentials, DataSerializable {

        private String endpoint;
        private String userGroup;

        public UserGroupCredentials() {
        }

        public UserGroupCredentials(String endPoint, String userGroup) {
            this.endpoint = endPoint;
            this.userGroup = userGroup;
        }

        public String getEndpoint() {
            return this.endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getPrincipal() {
            return this.userGroup;
        }

        public void writeData(ObjectDataOutput objectDataOutput)
                throws IOException {
            objectDataOutput.writeUTF(endpoint);
            objectDataOutput.writeUTF(userGroup);
        }

        public void readData(ObjectDataInput objectDataInput)
                throws IOException {
            this.endpoint = objectDataInput.readUTF();
            this.userGroup = objectDataInput.readUTF();
        }

    }
}
