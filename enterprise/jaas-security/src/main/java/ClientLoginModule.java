import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.security.*;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * An example ClientLoginModule that hard codes authorisation details in a pair of Maps.  You could amend this class to
 * perform look up against an LDAP store that would then return a set of Groups for the User.
 * <p>
 * Obviously you would NEVER store passwords in clear text.
 *
 */
public class ClientLoginModule implements LoginModule  {

    private final ILogger logger = Logger.getLogger(getClass().getName());
    private Credentials credentials;
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map options;

    // The Group that the user is authorised for.
    private UserGroupCredentials userGroupCredentials;

    // Usernames and Password are stored here in clear text, obviously NEVER do this.
    private static Map<String,String> allowedUsersMap = new HashMap<String,String>();

    // This map represents the userAssignedGroup(s) that a user would belong to, this would be the result from your LDAP store.
    private static Map<String,String> userGroups = new HashMap<String,String>();

    static{

        allowedUsersMap.put("david","password1");
        allowedUsersMap.put("chris","password2");

        userGroups.put("david","adminGroup");
        userGroups.put("chris","readOnlyGroup");

    }

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
    }

    /**
     * Login is called when this module is executed.
     *
     * @return is login successful
     * @throws LoginException
     */
    public boolean login() throws LoginException {
        boolean loginOk = false;

        final CredentialsCallback cb = new CredentialsCallback();
        try {
            callbackHandler.handle(new Callback[]{cb});
            credentials = cb.getCredentials();
        } catch (Exception e) {
            throw new LoginException(e.getClass().getName() + ":" + e.getMessage());
        }

        if(credentials == null) {
            logger.log(Level.WARNING, "Credentials could not be retrieved!");
            return false;
        }
        logger.log(Level.INFO, "Authenticating " + SecurityUtil.getCredentialsFullName(credentials));

        if (credentials instanceof UsernamePasswordCredentials){
            loginOk = doLoginCheck((UsernamePasswordCredentials) credentials);
        }

        return loginOk;
    }

    private boolean doLoginCheck(UsernamePasswordCredentials credentials) {

        String username = credentials.getUsername();
        String password = allowedUsersMap.get(username);
        boolean loginCheckOk = false;

        if (password != null){
            if(password.equals(credentials.getPassword())){
                String userGroup = userGroups.get(username);
                if (userGroup != null){
                    userGroupCredentials = new UserGroupCredentials(credentials.getEndpoint(),userGroup);
                    sharedState.put(SecurityConstants.ATTRIBUTE_CREDENTIALS, credentials);
                    loginCheckOk = true;
                } else {
                    logger.log(Level.WARNING, "User Group not found for user " + username);
                    loginCheckOk = false;
                }
            }
        } else {
            logger.log(Level.WARNING, "User details not found for " + username);
            loginCheckOk = false;
        }

        return loginCheckOk;

    }

    /**
     * Commit is called when all of the modules in the chain have passed.
     * @return
     * @throws LoginException
     */
    public final boolean commit() throws LoginException {
        logger.log(Level.FINEST, "Committing authentication of " + SecurityUtil.getCredentialsFullName(credentials));
        final Principal principal = new ClusterPrincipal(userGroupCredentials);
        subject.getPrincipals().add(principal);
        sharedState.put(SecurityConstants.ATTRIBUTE_PRINCIPAL, principal);
        return true;
    }

    /**
     * Abort is called when one of the modules in the chain has failed.
     * @return
     * @throws LoginException
     */
    public final boolean abort() throws LoginException {
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
    public final boolean logout() throws LoginException {
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

    public class UserGroupCredentials implements Credentials, DataSerializable {

        private String endpoint;
        private String userGroup;

        public UserGroupCredentials(){}

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

        public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
            objectDataOutput.writeUTF(endpoint);
            objectDataOutput.writeUTF(userGroup);
        }

        public void readData(ObjectDataInput objectDataInput) throws IOException {
            this.endpoint = objectDataInput.readUTF();
            this.userGroup = objectDataInput.readUTF();
        }

    }
}
