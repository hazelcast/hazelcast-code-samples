import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.config.PermissionConfig;
import com.hazelcast.config.SecurityConfig;
import com.hazelcast.config.SecurityInterceptorConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.instance.GroupProperties;
import com.hazelcast.security.Credentials;
import com.hazelcast.security.Parameters;
import com.hazelcast.security.SecurityInterceptor;

import java.security.AccessControlException;

/**
 * SecurityInterceptor for filtering individual methods
 */
public class MapSecurityInterceptor {

    static final String ACCEPTED_MAP_NAME = "accepted_map";
    static final String DENIED_MAP_NAME = "denied_map";

    static final String ACCEPTED_KEY = "accepted_key";
    static final String DENIED_KEY = "denied_key";

    static final String ACCEPTED_VALUE = "accepted_value";
    static final String DENIED_VALUE = "denied_value";

    static final String DENIED_METHOD = "replace";

    public static void main(String[] args) {
        //Enter your licenceKey below
        String licenceKey = "---- LICENCE KEY ----";
        final Config config = createConfig(licenceKey);
        final HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);

        final HazelcastInstance client = HazelcastClient.newHazelcastClient();
        final IMap<Object, Object> acceptedMap = client.getMap(ACCEPTED_MAP_NAME);
        final IMap<Object, Object> deniedMap = client.getMap(DENIED_MAP_NAME);

        acceptedMap.put(ACCEPTED_KEY, ACCEPTED_VALUE);

        try {
            deniedMap.put(ACCEPTED_KEY, ACCEPTED_VALUE);
            System.err.println("Should be denied!!!!");
        } catch (Exception expected) {
        }

        try {
            acceptedMap.put(ACCEPTED_KEY, DENIED_VALUE);
            System.err.println("Should be denied!!!!");
        } catch (Exception expected) {
        }

        try {
            acceptedMap.put(DENIED_KEY, ACCEPTED_VALUE);
            System.err.println("Should be denied!!!!");
        } catch (Exception expected) {
        }

        try {
            acceptedMap.replace(ACCEPTED_KEY, ACCEPTED_VALUE);
            System.err.println("Should be denied!!!!");
        } catch (Exception expected) {
        }


    }

    static Config createConfig(String licenceKey) {
        final Config config = new Config();
        config.setLicenseKey(licenceKey);
        config.setProperty(GroupProperties.PROP_WAIT_SECONDS_BEFORE_JOIN, "0");
        final SecurityInterceptorConfig securityInterceptorConfig = new SecurityInterceptorConfig();
        securityInterceptorConfig.setClassName(MySecurityInterceptor.class.getName());
        final SecurityConfig securityConfig = config.getSecurityConfig();
        securityConfig.setEnabled(true).addSecurityInterceptorConfig(securityInterceptorConfig);

        //When you enable security all client requests are denied, so we need to give permission first
        //security-interceptor will be run after checking this permission
        final PermissionConfig permissionConfig = new PermissionConfig(PermissionConfig.PermissionType.ALL, "", null);
        securityConfig.addClientPermissionConfig(permissionConfig);
        return config;
    }

    public static class MySecurityInterceptor implements SecurityInterceptor {

        @Override
        public void before(Credentials credentials, String objectType, String objectName,
                           String methodName, Parameters parameters) throws AccessControlException {
            if (objectName.equals(DENIED_MAP_NAME)) {
                throw new RuntimeException("Denied Map!!!");
            }
            if (methodName.equals(DENIED_METHOD)) {
                throw new RuntimeException("Denied Method!!!");
            }
            final Object firstParam = parameters.get(0);
            final Object secondParam = parameters.get(1);
            if (firstParam.equals(DENIED_KEY)) {
                throw new RuntimeException("Denied Key!!!");
            }
            if (secondParam.equals(DENIED_VALUE)) {
                throw new RuntimeException("Denied Value!!!");
            }
        }

        @Override
        public void after(Credentials credentials, String objectType, String objectName,
                          String methodName, Parameters parameters) {
            System.err.println("qwe c: " + credentials + "\t\tt: " + objectType + "\t\tn: " + objectName
                    + "\t\tm: " + methodName + "\t\tp1: " + parameters.get(0) + "\t\tp2: " + parameters.get(1));
        }
    }

}
