import com.hazelcast.config.Config;
import com.hazelcast.config.SocketInterceptorConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.nio.MemberSocketInterceptor;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

/**
 * Socket interceptor used for authentication to newly joining member
 */
public class SocketInterceptorMember {

    public static void main(String[] args) {
        // enter your licenceKey below
        String licenceKey = "---- LICENCE KEY ----";
        Config config1 = createConfig(licenceKey);
        Config config2 = createConfig(licenceKey);

        // each member will be given an id via SocketInterceptorConfig property
        config1.getNetworkConfig().getSocketInterceptorConfig().setProperty("member-id", "firstMember");
        config2.getNetworkConfig().getSocketInterceptorConfig().setProperty("member-id", "secondMember");

        Hazelcast.newHazelcastInstance(config1);
        Hazelcast.newHazelcastInstance(config2);
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

    private static class MySocketInterceptor implements MemberSocketInterceptor {

        private String memberId;

        public MySocketInterceptor() {
        }

        @Override
        public void onAccept(Socket socket) throws IOException {
            socket.getOutputStream().write(memberId.getBytes());
            byte[] bytes = new byte[1024];
            int len = socket.getInputStream().read(bytes);
            String otherMemberId = new String(bytes, 0, len);
            if (!otherMemberId.equals("secondMember")) {
                throw new RuntimeException("Not a known member!!!");
            }
        }

        @Override
        public void init(Properties properties) {
            memberId = properties.getProperty("member-id");
        }

        @Override
        public void onConnect(Socket socket) throws IOException {
            socket.getOutputStream().write(memberId.getBytes());
            byte[] bytes = new byte[1024];
            int len = socket.getInputStream().read(bytes);
            String otherMemberId = new String(bytes, 0, len);
            if (!otherMemberId.equals("firstMember")) {
                throw new RuntimeException("Not a known member!!!");
            }
        }
    }
}
