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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * Hazelcast member initialized by Spring Boot auto configuration
 *
 * @author Viktor Gamov on 12/26/15.
 *         Twitter: @gamussa
 */
@SpringBootApplication(scanBasePackages = "com.hazelcast.springboot.caching.BootifulMember")
@EnableCaching
@EnableAutoConfiguration(exclude = {EmbeddedServletContainerAutoConfiguration.class, WebMvcAutoConfiguration.class})
public class BootifulMember {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootifulMember.class);


    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .profiles("member")
                .sources(BootifulMember.class)
                .run(args);
    }

    @Autowired
    HazelcastInstance hazelcastInstance;

    //A scheduled Job that does something every 10th second
    @Scheduled(cron = "0/10 * * * * *")
    void doSomethingExclusivelyClusterWideOnCronSchedule() {
        ILock lock = hazelcastInstance.getLock("ScheduleJob1000");

            if(lock.tryLock()) {
                try {
                    LOGGER.info(format("ScheduledJob name: {%s} is run at time: {%s} , from instance: {%s} ",
                            lock.getName(), Calendar.getInstance().getTime(), hazelcastInstance.getName()));
                } finally {
                    if(lock != null) {
                        lock.unlock();
                    }
                }
            }
    }


}
