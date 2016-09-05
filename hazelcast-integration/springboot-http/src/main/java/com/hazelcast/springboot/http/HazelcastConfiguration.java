package com.hazelcast.springboot.http;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.web.WebFilter;

import java.util.Arrays;
import java.util.Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <P>
 * A conditional configuration that potentially adds the bean definitions in
 * this class to the Spring application context, depending on whether the
 * {@code @ConditionalOnExpression} is true or not.
 * </P>
 * <P>
 * When true, beans are added that create a Hazelcast instance, and bind this
 * instance to Tomcat for storage of HTTP sessions, instead of Tomcat's default
 * implementation.
 * </P>
 */
@Configuration
@ConditionalOnExpression(Application.USE_HAZELCAST)
public class HazelcastConfiguration {

	/**
	 * <P>
	 * Create a Hazelcast {@code Config} object as a bean. Spring Boot will use
	 * the presence of this to determine that a {@code HazelcastInstance} should
	 * be created with this configuration.
	 * </P>
	 * <P>As a simple side-step to possible networking issues, turn off multicast
	 * in favour of TCP connection to the local host.
	 * </P>
	 *
	 * @return Configuration for the Hazelcast instance
	 */
	@Bean
	public Config config() {

		Config config = new Config();

		JoinConfig joinConfig = config.getNetworkConfig().getJoin();
		
		joinConfig.getMulticastConfig().setEnabled(false);
		joinConfig.getTcpIpConfig().setEnabled(true).setMembers(Arrays.asList("127.0.0.1"));

		return config;
	}
	

	/**
	 * <P>
	 * Create a web filter. Parameterise this with two properties,
	 * <OL>
	 * <LI><I>instance-name</I>
	 * <P>
	 * Direct the web filter to use the existing Hazelcast instance rather than
	 * to create a new one.
	 * </P>
	 * </LI>
	 * <LI><I>sticky-session</I>
	 * <P>
	 * As the HTTP session will be accessed from multiple processes, deactivate
	 * the optimization that assumes each user's traffic is routed to the same
	 * process for that user.
	 * </P>
	 * </LI>
	 * </OL>
	 * </P>
	 * <P>
	 * Spring will assume dispatcher types of {@code FORWARD}, {@code INCLUDE}
	 * and {@code REQUEST}, and a context pattern of "{@code /*}".
	 * </P>
	 * 
	 * @param hazelcastInstance
	 *            Created by Spring
	 * @return The web filter for Tomcat
	 */
	@Bean
	public WebFilter webFilter(HazelcastInstance hazelcastInstance) {

		Properties properties = new Properties();
		properties.put("instance-name", hazelcastInstance.getName());
		properties.put("sticky-session", "false");

		WebFilter webFilter = new WebFilter(properties);

		return webFilter;
	}

}
