package hazelcast.platform.labs;

import com.hazelcast.config.replacer.spi.ConfigReplacer;

import java.util.Properties;

/**
 * Replaces configuration values using environment variables
 *
 * Put $ENV{MY_ENV_VAR_NAME} in your config.
 */
public class EnvironmentConfigReplacer implements ConfigReplacer {
    @Override
    public void init(Properties properties) {
        
    }

    @Override
    public String getPrefix() {
        return "ENV";
    }

    @Override
    public String getReplacement(String s) {
        String val = System.getenv(s);
        if (val == null){
            val = s + " variable not found in environment";
        }
        return val;
    }
}
