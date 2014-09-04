import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.SocketInterceptorConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.GroupProperties;
import com.hazelcast.nio.MemberSocketInterceptor;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

/**
 * Socket interceptor used for authentication to clients
 */
public class SocketInterceptorClient {

    public static void main(String[] args) {
        //Enter your licenceKey below
        String licenceKey = "---- LICENCE KEY ----";
        final Config config = createConfig(licenceKey);
        final HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);

        final ClientConfig clientConfig = createClientConfig();
        final HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);

    }

    static Config createConfig(String licenceKey) {
        final Config config = new Config();
        config.setLicenseKey(licenceKey);
        config.setProperty(GroupProperties.PROP_WAIT_SECONDS_BEFORE_JOIN, "0");
        final SocketInterceptorConfig interceptorConfig = new SocketInterceptorConfig();
        interceptorConfig.setEnabled(true).setClassName(MySocketInterceptor.class.getName());
        config.getNetworkConfig().setSocketInterceptorConfig(interceptorConfig);
        return config;
    }

    static ClientConfig createClientConfig() {
        final ClientConfig clientConfig = new ClientConfig();
        final SocketInterceptorConfig interceptorConfig = new SocketInterceptorConfig();
        interceptorConfig.setEnabled(true).setClassName(MySocketInterceptor.class.getName());
        clientConfig.getNetworkConfig().setSocketInterceptorConfig(interceptorConfig);
        return clientConfig;
    }

    public static class MySocketInterceptor implements MemberSocketInterceptor {

        public MySocketInterceptor() {
        }

        @Override
        public void onAccept(final Socket socket) throws IOException {
            socket.getOutputStream().write("a-member".getBytes());
            final byte[] bytes = new byte[1024];
            final int len = socket.getInputStream().read(bytes);
            final String otherMemberId = new String(bytes, 0, len);
            if (!otherMemberId.equals("a-client")) {
                throw new RuntimeException("Not a known client!!!");
            }
        }

        @Override
        public void init(final Properties properties) {
        }

        @Override
        public void onConnect(final Socket socket) throws IOException {
            socket.getOutputStream().write("a-client".getBytes());
            final byte[] bytes = new byte[1024];
            final int len = socket.getInputStream().read(bytes);
            final String otherMemberId = new String(bytes, 0, len);
            if (!otherMemberId.equals("a-member")) {
                throw new RuntimeException("Not a known member!!!");
            }
        }
    }
}
