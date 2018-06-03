import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.security.UsernamePasswordCredentials;

import java.security.AccessControlException;
import java.util.Map;
import java.util.logging.Level;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;

/**
 * Created by dbrimley on 19/05/2014.
 */
public class Client {

    private final ILogger logger = Logger.getLogger(getClass().getName());

    public static void main(String args[]){

        Client client = new Client();

        client.adminUserCanPutIntoImportantMap();

        client.readOnlyUserCannotPutIntoImportantMap();


    }

    private void readOnlyUserCannotPutIntoImportantMap() {

        HazelcastInstance readOnlyClient = getClientConnection("chris", "password2", "127.0.0.1");

        Map<String,String> readOnlyClientsImportantMap = readOnlyClient.getMap("importantMap");

        // This will pass
        logger.log(Level.INFO,"Chris is performing get on the ImportantMap");
        readOnlyClientsImportantMap.get("1");

        // This will fail as chris is not a member of the admin group
        try{
            logger.log(Level.INFO,"Chris is performing put on the ImportantMap");
            readOnlyClientsImportantMap.put("2","2");
        } catch (AccessControlException e){
            logger.log(Level.SEVERE,"Could not perform put operation, access denied",e);
        }
    }

    private void adminUserCanPutIntoImportantMap() {

        HazelcastInstance adminClient = getClientConnection("david", "password1", "127.0.0.1");

        Map<String,String> adminClientsImportantMap = adminClient.getMap("importantMap");

        // This will pass
        logger.log(Level.INFO,"David is performing put on the ImportantMap");
        adminClientsImportantMap.put("1","1");
    }

    private HazelcastInstance getClientConnection(String username, String password, String thisClientIP) {

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setLicenseKey(ENTERPRISE_LICENSE_KEY);
        clientConfig.setCredentials(new UsernamePasswordCredentials(username, password));
        clientConfig.getCredentials().setEndpoint(thisClientIP);
        return HazelcastClient.newHazelcastClient(clientConfig);
    }
}
