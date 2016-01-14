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

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Hazelcast member initialized by Spring Boot auto configuration
 *
 * @author Viktor Gamov on 12/26/15.
 *         Twitter: @gamussa
 */
@SpringBootApplication(scanBasePackages = "com.hazelcast.springboot.caching.BootifulMember")
@EnableAutoConfiguration(exclude = {
        // disable Hazelcast Auto Configuration, and use JCache configuration for the member example
        HazelcastAutoConfiguration.class
})
@EnableCaching
public class BootifulMember {
    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .profiles("member")
                .sources(BootifulMember.class)
                .run(args);
    }
}
