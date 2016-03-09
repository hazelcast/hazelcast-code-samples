import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.SocketInterceptorConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.nio.MemberSocketInterceptor;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

/**
 * Socket interceptor used for authentication to clients
 */
public class SocketInterceptorClient {

    public static void main(String[] args) {
        // enter your licenceKey below
        String licenceKey = "---- LICENCE KEY ----";
        Config config = createConfig(licenceKey);
        Hazelcast.newHazelcastInstance(config);

        ClientConfig clientConfig = createClientConfig();
        HazelcastClient.newHazelcastClient(clientConfig);
    }

    private static Config createConfig(String licenceKey) {
        Config config = new Config();
        config.setLicenseKey(licenceKey);
        config.setProperty("hazelcast.wait.seconds.before.join", "0");

        SocketInterceptorConfig interceptorConfig = new SocketInterceptorConfig();
        interceptorConfig.setEnabled(true).setClassName(MySocketInterceptor.class.getName());
        config.getNetworkConfig().setSocketInterceptorConfig(interceptorConfig);

        return config;
    }

    private static ClientConfig createClientConfig() {
        ClientConfig clientConfig = new ClientConfig();
        SocketInterceptorConfig interceptorConfig = new SocketInterceptorConfig();
        interceptorConfig.setEnabled(true).setClassName(MySocketInterceptor.class.getName());
        clientConfig.getNetworkConfig().setSocketInterceptorConfig(interceptorConfig);
        return clientConfig;
    }

    private static class MySocketInterceptor implements MemberSocketInterceptor {

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
