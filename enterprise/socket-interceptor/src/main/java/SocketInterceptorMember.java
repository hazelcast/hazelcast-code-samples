import com.hazelcast.config.Config;
import com.hazelcast.config.SocketInterceptorConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.nio.MemberSocketInterceptor;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;

/**
 * Socket interceptor used for authentication to newly joining member.
 *
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class SocketInterceptorMember {

    public static void main(String[] args) {
        Config config1 = createConfig();
        Config config2 = createConfig();

        // each member will be given an id via SocketInterceptorConfig property
        config1.getNetworkConfig().getSocketInterceptorConfig().setProperty("member-id", "firstMember");
        config2.getNetworkConfig().getSocketInterceptorConfig().setProperty("member-id", "secondMember");

        Hazelcast.newHazelcastInstance(config1);
        Hazelcast.newHazelcastInstance(config2);
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

    public static class MySocketInterceptor implements MemberSocketInterceptor {

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
