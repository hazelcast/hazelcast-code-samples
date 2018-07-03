package com.hazelcast.sample.replacer;

import java.util.Properties;

import com.hazelcast.config.replacer.spi.ConfigReplacer;

/**
 * Variable replacer, which just returns provided value (i.e. identity).
 */
public class IdReplacer implements ConfigReplacer {

    @Override
    public void init(Properties properties) {
        // Nothing to do here. This replacer has no configurable properties.
    }

    @Override
    public String getPrefix() {
        return "ID";
    }

    /**
     * Returns the variable value without any change.
     */
    @Override
    public String getReplacement(String value) {
        return value;
    }
}
