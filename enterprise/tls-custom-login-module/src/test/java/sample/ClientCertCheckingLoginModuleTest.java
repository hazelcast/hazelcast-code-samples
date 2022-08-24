package sample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sample.ClientCertCheckingLoginModule.OPTION_ALLOWED_ATTRIBUTE_VALUES;
import static sample.ClientCertCheckingLoginModule.OPTION_ALLOWED_SAN_VALUES;
import static sample.ClientCertCheckingLoginModule.OPTION_CHECKED_ATTRIBUTE;
import static sample.ClientCertCheckingLoginModule.OPTION_ROLE_ATTRIBUTE;

import java.security.AccessControlException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientConfigXmlGenerator;
import com.hazelcast.config.Config;
import com.hazelcast.config.ConfigXmlGenerator;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.LoginModuleConfig;
import com.hazelcast.config.LoginModuleConfig.LoginModuleUsage;
import com.hazelcast.config.PermissionConfig;
import com.hazelcast.config.PermissionConfig.PermissionType;
import com.hazelcast.config.SSLConfig;
import com.hazelcast.config.SecurityConfig;
import com.hazelcast.config.security.JaasAuthenticationConfig;
import com.hazelcast.config.security.RealmConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.examples.helper.LicenseUtils;

/**
 * Sample test scenario for using the {@link ClientCertCheckingLoginModule}.
 */
class ClientCertCheckingLoginModuleTest {

    @AfterEach
    public void afterEach() {
        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }

    @Test
    void testDnsSan() throws Exception {
        String allowedSans = "server.my-company.com";
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(createMemberConfig("server", allowedSans));
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(createMemberConfig("server", allowedSans));
        hz1.getMap("map").put("key", "value");
        assertThrows(IllegalStateException.class,
                () -> Hazelcast.newHazelcastInstance(createMemberConfig("dev-client", allowedSans)),
                "Authentication should fail as the given certificate doesn't have SAN entries");
        assertEquals(2, hz1.getCluster().getMembers().size());
        assertEquals(2, hz2.getCluster().getMembers().size());

        HazelcastInstance adminClient = HazelcastClient.newHazelcastClient(createClientConfig("admin-client"));
        assertEquals("value", adminClient.getMap("map").get("key"));
        HazelcastInstance devClient = HazelcastClient.newHazelcastClient(createClientConfig("dev-client"));
        assertThrows(AccessControlException.class, () -> devClient.getMap("map").get("key"));
    }

    @Test
    void testEmailSan_correct() throws Exception {
        String allowedSans = "server@hazelcast.com";
        HazelcastInstance hz1 = Hazelcast.newHazelcastInstance(createMemberConfig("server", allowedSans));
        HazelcastInstance hz2 = Hazelcast.newHazelcastInstance(createMemberConfig("server", allowedSans));
        assertThrows(IllegalStateException.class,
                () -> Hazelcast.newHazelcastInstance(createMemberConfig("dev-client", allowedSans)),
                "Authentication should fail as the given certificate doesn't have SAN entries");
        assertEquals(2, hz1.getCluster().getMembers().size());
        assertEquals(2, hz2.getCluster().getMembers().size());
    }

    @Test
    void testEmailSan_incorrect() throws Exception {
        String allowedSans = "client@hazelcast.com";
        Hazelcast.newHazelcastInstance(createMemberConfig("server", allowedSans));

        assertThrows(IllegalStateException.class,
                () -> Hazelcast.newHazelcastInstance(createMemberConfig("server", allowedSans)),
                "Authentication should fail as the given certificate doesn't have expected SAN entry");
    }

    private ClientConfig createClientConfig(String storeName) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("127.0.0.1").setSSLConfig(new SSLConfig()).getSSLConfig().setEnabled(true)
                .setProperty("keyStore", "src/" + storeName + ".p12") //
                .setProperty("keyStorePassword", "123456") //
                .setProperty("trustStore", "src/ca.p12") //
                .setProperty("trustStorePassword", "123456");
        System.out.println("Client configuration:\n" + ClientConfigXmlGenerator.generate(clientConfig, 2));
        return clientConfig;
    }

    private Config createMemberConfig(String storeName, String memberRealmAllowedSan) {
        Config config = new Config().setLicenseKey(LicenseUtils.ENTERPRISE_LICENSE_KEY)
                .setProperty("hazelcast.phone.home.enabled", "false");
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getTcpIpConfig().setEnabled(true).addMember("127.0.0.1");
        config.getNetworkConfig().setSSLConfig(new SSLConfig()).getSSLConfig().setEnabled(true)
                .setProperty("mutualAuthentication", "REQUIRED") //
                .setProperty("keyStore", "src/" + storeName + ".p12") //
                .setProperty("keyStorePassword", "123456") //
                .setProperty("trustStore", "src/ca.p12") //
                .setProperty("trustStorePassword", "123456");
        SecurityConfig securityConfig = config.getSecurityConfig().setEnabled(true);
        securityConfig.setMemberRealmConfig("memberRealm",
                new RealmConfig().setJaasAuthenticationConfig(new JaasAuthenticationConfig().addLoginModuleConfig(
                        new LoginModuleConfig(ClientCertCheckingLoginModule.class.getName(), LoginModuleUsage.REQUIRED)
                                .setProperty(OPTION_ALLOWED_SAN_VALUES, memberRealmAllowedSan))));
        securityConfig.setClientRealmConfig("clientRealm",
                new RealmConfig().setJaasAuthenticationConfig(new JaasAuthenticationConfig().addLoginModuleConfig(
                        new LoginModuleConfig(ClientCertCheckingLoginModule.class.getName(), LoginModuleUsage.REQUIRED)
                                .setProperty(OPTION_CHECKED_ATTRIBUTE, "cn")
                                .setProperty(OPTION_ALLOWED_ATTRIBUTE_VALUES, "client,client35,client87")
                                .setProperty(OPTION_ROLE_ATTRIBUTE, "ou"))));
        securityConfig.addClientPermissionConfig(new PermissionConfig(PermissionType.ALL, null, "admin"));
        showXmlConfig(config);
        return config;
    }

    private void showXmlConfig(Config config) {
        String xml = new ConfigXmlGenerator(true, false).generate(config);
        System.out.println("Member configuration:\n" + xml);
    }

}
