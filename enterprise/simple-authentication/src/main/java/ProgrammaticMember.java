import java.io.IOException;

import com.hazelcast.config.Config;
import com.hazelcast.config.PermissionConfig;
import com.hazelcast.config.PermissionConfig.PermissionType;
import com.hazelcast.config.security.RealmConfig;
import com.hazelcast.config.security.SimpleAuthenticationConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.examples.helper.LicenseUtils;

public class ProgrammaticMember {

    public static void main(String[] args) throws IOException {
        // Set license key either in ~/.hazelcast-code-samples-license file
        // or as a hazelcast.enterprise.license.key system property
        Config config = new Config().setLicenseKey(LicenseUtils.ENTERPRISE_LICENSE_KEY);
        SimpleAuthenticationConfig simpleAuthenticationConfig = new SimpleAuthenticationConfig()
                .addUser("test", "a1234", "monitor", "hazelcast")
                .addUser("root", "secret", "admin");
        config.getSecurityConfig().setEnabled(true)
            .setClientRealmConfig("simpleRealm",
                    new RealmConfig().setSimpleAuthenticationConfig(simpleAuthenticationConfig))
            .addClientPermissionConfig(new PermissionConfig(PermissionType.ALL, null, "admin"));
        Hazelcast.newHazelcastInstance(config);
    }
}
