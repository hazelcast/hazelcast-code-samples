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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.lang.System.nanoTime;
import static java.lang.System.out;

/**
 * Hazelcast Client initialized by Spring Boot auto configuration
 *
 * @author Viktor Gamov on 12/26/15.
 *         Twitter: @gamussa
 */
@SpringBootApplication(scanBasePackages = "com.hazelcast.springboot.caching.BootifulClient")
// disable Hazelcast Auto Configuration, and use JCache configuration for the client example
@EnableAutoConfiguration(exclude = {HazelcastAutoConfiguration.class})
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

    @Component
    static class Runner implements CommandLineRunner {

        @Autowired
        IDummyBean dummy;

        @Override
        public void run(String... strings) throws Exception {

            String logFormat = "%s call took %d millis with result: %s";
            long start1 = nanoTime();
            String city = dummy.getCity();
            long end1 = nanoTime();
            out.println(format(logFormat, "First", TimeUnit.NANOSECONDS.toMillis(end1 - start1), city));

            long start2 = nanoTime();
            city = dummy.getCity();
            long end2 = nanoTime();
            out.println(format(logFormat, "Second", TimeUnit.NANOSECONDS.toMillis(end2 - start2), city));
        }
    }
}
