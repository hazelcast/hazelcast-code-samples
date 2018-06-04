import com.hazelcast.client.ClientEndpoint;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Client;
import com.hazelcast.core.ClientListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.io.InputStream;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;

/**
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class Member {

    public static void main(String[] args) {

        InputStream inputSteam = Member.class.getResourceAsStream("hazelcast.xml");
        XmlConfigBuilder configBuilder = new XmlConfigBuilder(inputSteam);
        Config config = configBuilder.build();

        // Now set the license key.
        config.setLicenseKey(ENTERPRISE_LICENSE_KEY);

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        System.out.println("Hazelcast Member instance is running!");

        hz.getClientService().addClientListener(new ClientListener() {

            public void clientConnected(Client client) {
                ClientEndpoint clientEndpoint = (ClientEndpoint) client;
                System.out.println("Connected");
            }

            public void clientDisconnected(Client client) {

            }
        });
    }

}
