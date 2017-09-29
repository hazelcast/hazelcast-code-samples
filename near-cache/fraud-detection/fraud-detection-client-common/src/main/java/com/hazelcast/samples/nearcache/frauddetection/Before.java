package com.hazelcast.samples.nearcache.frauddetection;

import java.time.Instant;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import lombok.Getter;

/**
 * <P>Capture the timestamp before testing begins.
 * </P>
 * <P>The {@code @Order} annotation ensures this method
 * runs before the {@link FraudService} has been tested.
 * </P>
 */
@Component
@Order(Integer.MIN_VALUE)
@Getter
public class Before implements CommandLineRunner {

	private Instant before;

	@Override
	public void run(String... arg0) throws Exception {
		this.before = Instant.now();
	}

}
