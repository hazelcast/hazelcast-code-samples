import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.examples.helper.LicenseUtils;

import auditlog.JsonAuditlogFactory;

/**
 * This programm shows programmatic auditlog configuration. It writes auditable events to standard output stream.
 */
public class MainProgrammaticConfig {

    public static void main(String[] args) {
        String licenseKey = LicenseUtils.ENTERPRISE_LICENSE_KEY;
        Config config1 = new Config().setLicenseKey(licenseKey);
        config1.getAuditlogConfig().setEnabled(true).setFactoryClassName(JsonAuditlogFactory.class.getName());
        Hazelcast.newHazelcastInstance(config1);

        Config config2 = new Config().setLicenseKey(licenseKey).setLiteMember(true);
        config2.getAuditlogConfig().setEnabled(true).setFactoryClassName(JsonAuditlogFactory.class.getName());
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(config2);
        hz2.getCluster().promoteLocalLiteMember();

        Hazelcast.shutdownAll();
    }
}
