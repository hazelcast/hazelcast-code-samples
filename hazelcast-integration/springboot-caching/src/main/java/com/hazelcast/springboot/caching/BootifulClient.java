/*
 *
 *  Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.hazelcast.springboot.caching;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.lang.System.nanoTime;

/**
 * Hazelcast Client initialized by Spring Boot auto configuration
 *
 * @author Viktor Gamov on 12/26/15.
 *         Twitter: @gamussa
 */
@SpringBootApplication(scanBasePackages = "com.hazelcast.springboot.caching.BootifulClient")
@EnableCaching
@SuppressWarnings("unused")
public class BootifulClient {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(BootifulClient.class)
                .profiles("client")
                .run(args);
    }

    @Bean
    public IDummyBean dummyBean() {
        return new DummyBean();
    }

    @Bean
    CacheManager cacheManager() {
        return new HazelcastCacheManager(hazelcastInstance());
    }

    @Bean
    KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Bean
    @Profile("client")
    HazelcastInstance hazelcastInstance() {
        // for client HazelcastInstance LocalMapStatistics will not available
        return HazelcastClient.newHazelcastClient();
        // return Hazelcast.newHazelcastInstance();
    }

    @RestController
    static class CityController {

        private static final Logger LOGGER = LoggerFactory.getLogger(CityController.class);

        @Autowired
        IDummyBean dummy;

        @Autowired
        HazelcastInstance hazelcastInstance;

        @RequestMapping("/city")
        public String getCity() {
            String logFormat = "%s call took %d millis with result: %s";
            long start1 = nanoTime();
            String city = dummy.getCity();
            long end1 = nanoTime();
            LOGGER.info(format(logFormat, "Rest", TimeUnit.NANOSECONDS.toMillis(end1 - start1), city));
            return city;
        }

        @RequestMapping(value = "city/{city}", method = RequestMethod.GET)
        public String setCity(@PathVariable String city) {
            return dummy.setCity(city);
        }
    }
}
