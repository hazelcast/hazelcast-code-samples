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
 * Socket interceptor used for authentication to newly joining member
 */
public class SocketInterceptorMember {


    public static void main(String[] args) {
        //Enter your licenceKey below
        String licenceKey = "---- LICENCE KEY ----";
        final Config config1 = createConfig(licenceKey);
        final Config config2 = createConfig(licenceKey);

        //Each member will be given an id via SocketInterceptorConfig property
        config1.getNetworkConfig().getSocketInterceptorConfig().setProperty("member-id", "firstMember");
        config2.getNetworkConfig().getSocketInterceptorConfig().setProperty("member-id", "secondMember");

        final HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(config1);
        final HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(config2);

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

    public static class MySocketInterceptor implements MemberSocketInterceptor {

        String memberId = null;

        public MySocketInterceptor() {
        }

        @Override
        public void onAccept(final Socket socket) throws IOException {
            socket.getOutputStream().write(memberId.getBytes());
            final byte[] bytes = new byte[1024];
            final int len = socket.getInputStream().read(bytes);
            final String otherMemberId = new String(bytes, 0, len);
            if (!otherMemberId.equals("secondMember")) {
                throw new RuntimeException("Not a known member!!!");
            }
        }

        @Override
        public void init(final Properties properties) {
            memberId = properties.getProperty("member-id");
        }

        @Override
        public void onConnect(final Socket socket) throws IOException {
            socket.getOutputStream().write(memberId.getBytes());
            final byte[] bytes = new byte[1024];
            final int len = socket.getInputStream().read(bytes);
            final String otherMemberId = new String(bytes, 0, len);
            if (!otherMemberId.equals("firstMember")) {
                throw new RuntimeException("Not a known member!!!");
            }
        }
    }
}
