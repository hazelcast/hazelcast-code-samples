import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.SocketInterceptorConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.nio.MemberSocketInterceptor;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;

/**
 * Socket interceptor used for authentication to clients
 *
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class SocketInterceptorClient {

    public static void main(String[] args) {
        Config config = createConfig();
        Hazelcast.newHazelcastInstance(config);

        ClientConfig clientConfig = createClientConfig();
        HazelcastClient.newHazelcastClient(clientConfig);
    }

    private static Config createConfig() {
        Config config = new Config();
        config.setLicenseKey(ENTERPRISE_LICENSE_KEY);
        config.setProperty("hazelcast.wait.seconds.before.join", "0");

        SocketInterceptorConfig interceptorConfig = new SocketInterceptorConfig();
        interceptorConfig.setEnabled(true).setClassName(MySocketInterceptor.class.getName());
        config.getNetworkConfig().setSocketInterceptorConfig(interceptorConfig);

        return config;
    }

    private static ClientConfig createClientConfig() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setLicenseKey(ENTERPRISE_LICENSE_KEY);
        SocketInterceptorConfig interceptorConfig = new SocketInterceptorConfig();
        interceptorConfig.setEnabled(true).setClassName(MySocketInterceptor.class.getName());
        clientConfig.getNetworkConfig().setSocketInterceptorConfig(interceptorConfig);
        return clientConfig;
    }

    /**
     * This class needs to be public, so it can be accessed via Hazelcast.
     */
    public static class MySocketInterceptor implements MemberSocketInterceptor {

        public MySocketInterceptor() {
        }

        @Override
        public void onAccept(Socket socket) throws IOException {
            socket.getOutputStream().write("a-member".getBytes());
            byte[] bytes = new byte[1024];
            int len = socket.getInputStream().read(bytes);
            String otherMemberId = new String(bytes, 0, len);
            if (!otherMemberId.equals("a-client")) {
                throw new RuntimeException("Not a known client!!!");
            }
        }

        @Override
        public void init(Properties properties) {
        }

        @Override
        public void onConnect(Socket socket) throws IOException {
            socket.getOutputStream().write("a-client".getBytes());
            byte[] bytes = new byte[1024];
            int len = socket.getInputStream().read(bytes);
            String otherMemberId = new String(bytes, 0, len);
            if (!otherMemberId.equals("a-member")) {
                throw new RuntimeException("Not a known member!!!");
            }
        }
    }
}
