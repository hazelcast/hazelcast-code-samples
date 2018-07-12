package com.hazelcast.samples.session.analysis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;

/**
 * <p>Configuration for Hazelcast <a href="https://imdg.hazelcast.org/">IMDG</a>
 * and <a href="https://jet.hazelcast.org/">JET</a>
 * </p>
 */
@Configuration
public class ApplicationConfig {

    /**
     * <p>IMDG is configured from an XML file, or you can
     * do it from Java.
     * </p>
     * <p>Or you can do it from XML then extend from Java,
     * but this is very confusing.
     * </p>
     *
     * @return IMDG configuration
     */
    @Bean
    public Config config() {
        return new ClasspathXmlConfig("hazelcast.xml");
    }

    /**
     * <p>Jet leverages IMDG for clustering, so uses the IMDG
     * configuration.
     * <p>
     * <p>A Jet cluster sits on top of a Hazelcast IMDG cluster,
     * and that's the one it uses for data in this example. It
     * could also connect to other IMDG clusters.
     * </p>
     *
     * @param config Spring {@code @Bean} created above
     * @return A Jet node
     */
    @Bean
    public JetInstance jetInstance(Config config) {
        JetConfig jetConfig = new JetConfig().setHazelcastConfig(config);
        return Jet.newJetInstance(jetConfig);
    }

    /**
     * <p>Make the Hazelcast instance that Jet is using available
     * as a Spring bean.
     * </p>
     *
     * @param jetInstance Spring {@code @Bean} created above
     * @return The Hazelcast server embedded in the Jet instance
     */
    @Bean
    public HazelcastInstance hazelcastInstance(JetInstance jetInstance) {
        return jetInstance.getHazelcastInstance();
    }
}
