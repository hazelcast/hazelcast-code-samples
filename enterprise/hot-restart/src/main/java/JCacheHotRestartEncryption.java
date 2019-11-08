import com.hazelcast.config.Config;
import com.hazelcast.config.EncryptionAtRestConfig;
import com.hazelcast.config.HotRestartPersistenceConfig;
import com.hazelcast.config.JavaKeyStoreSecureStoreConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.nio.IOUtil;

import javax.cache.Cache;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;

/**
 * You have to set your Hazelcast Enterprise license key to make this code sample work.
 * Please have a look at {@link com.hazelcast.examples.helper.LicenseUtils} for details.
 */
public class JCacheHotRestartEncryption {

    private static final String HOT_RESTART_ROOT_DIR =
            System.getProperty("java.io.tmpdir") + File.separatorChar + "hazelcast-hot-restart";
    private static final String KEYSTORE_FILE =
            System.getProperty("java.io.tmpdir") + File.separatorChar + "hazelcast-hot-restart-keystore.p12";
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEYSTORE_PASSWORD = "password";

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        IOUtil.delete(new File(HOT_RESTART_ROOT_DIR));

        Config config = new Config();
        config.setLicenseKey(ENTERPRISE_LICENSE_KEY);

        config.getNetworkConfig().setPort(5701).setPortAutoIncrement(false);
        JoinConfig join = config.getNetworkConfig().getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig().setEnabled(true).clear().addMember("127.0.0.1");

        int maxKeySize = getAESMaxKeySize();
        JavaKeyStoreSecureStoreConfig keyStoreConfig = createKeyStore(maxKeySize);

        HotRestartPersistenceConfig hotRestartConfig = config.getHotRestartPersistenceConfig();
        hotRestartConfig.setEnabled(true).setBaseDir(new File(HOT_RESTART_ROOT_DIR));
        EncryptionAtRestConfig encryptionAtRestConfig = hotRestartConfig.getEncryptionAtRestConfig();
        encryptionAtRestConfig.setEnabled(true)
                              .setAlgorithm("AES/CBC/PKCS5Padding")
                              .setKeySize(maxKeySize)
                              .setSalt("sugar")
                              .setSecureStoreConfig(keyStoreConfig);

        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);

        Cache<Integer, String> cache = JCacheHotRestart.createCache(instance);
        for (int i = 0; i < 10; i++) {
            cache.put(i, "value" + i);
        }

        instance.shutdown();

        instance = Hazelcast.newHazelcastInstance(config);
        cache = JCacheHotRestart.createCache(instance);

        for (int i = 0; i < 10; i++) {
            System.out.println("cache.get(" + i + ") = " + cache.get(i));
        }

        Hazelcast.shutdownAll();
    }

    private static int getAESMaxKeySize() throws GeneralSecurityException {
        return Math.min(Cipher.getMaxAllowedKeyLength("AES"), 256);
    }

    private static SecretKey generateAESSecretKey(int keySize) throws GeneralSecurityException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(keySize);
        return keyGen.generateKey();
    }

    private static JavaKeyStoreSecureStoreConfig createKeyStore(int maxKeySize) throws IOException, GeneralSecurityException {
        File keyStoreFile = new File(KEYSTORE_FILE);
        IOUtil.delete(keyStoreFile);
        KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
        ks.load(null, null);
        SecretKey masterKey = generateAESSecretKey(maxKeySize);
        KeyStore.SecretKeyEntry secret = new KeyStore.SecretKeyEntry(masterKey);
        KeyStore.ProtectionParameter entryPassword = new KeyStore.PasswordProtection(KEYSTORE_PASSWORD.toCharArray());
        ks.setEntry("entry", secret, entryPassword);
        try (FileOutputStream out = new FileOutputStream(keyStoreFile)) {
            ks.store(out, KEYSTORE_PASSWORD.toCharArray());
        }
        return new JavaKeyStoreSecureStoreConfig(keyStoreFile).setType(KEYSTORE_TYPE).setPassword(KEYSTORE_PASSWORD);
    }
}
