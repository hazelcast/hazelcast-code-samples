package com.hazelcast.samples.querying;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;

/**
 * <P>
 * Create Spring {@code @Bean} objects for Hazelcast IMDG and Hazelcast Jet.
 * </P>
 */
@Configuration
public class ApplicationConfig {

    /**
     * <P>
     * Create a Jet instance, which in turn will create a Hazelcast IMDG instance.
     * Use the supplied IMDG configuration but let the Jet configuration be the
     * default.
     * </P>
     *
     * @return Hazelcast Jet instance
     */
    @Bean
    public JetInstance jetInstance() {
        Config imdgConfig = new ClasspathXmlConfig("hazelcast.xml");
        JetConfig jetConfig = new JetConfig().setHazelcastConfig(imdgConfig);
        return Jet.newJetInstance(jetConfig);
    }

    /**
     * <P>
     * Jet here is bundling Hazelcast IMDG server, which we wish to expose as a
     * Spring {@code @Bean}.
     * </P>
     *
     * @param jetInstance
     *            Hazelcast Jet instance, created above
     * @return Hazelcast iMDG instance, inside that Jet instance
     */
    @Bean
    public HazelcastInstance hazelcastInstance(JetInstance jetInstance) {
        return jetInstance.getHazelcastInstance();
    }

}
