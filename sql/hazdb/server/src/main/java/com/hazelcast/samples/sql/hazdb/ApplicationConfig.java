/*
 * Copyright (c) 2008-2022, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.samples.sql.hazdb;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.ClasspathYamlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;

@Configuration
public class ApplicationConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

    /**
     * <p>
     * Configure Hazelcast from "{@code hazelcast.yml}" file. If in Docker,
     * de-activate Kubernetes, activate direct discovery by IP address of host
     * network.
     * </p>
     * <p>
     * This is a {@link com.hazelcast.config.Config Config} object, used to create a
     * server. There is a similar counterpart for clients.
     * </p>
     *
     * @return
     */
    @Bean
    public Config config() {
        Config config = new ClasspathYamlConfig("hazelcast.yml");
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();

        // Host IP is set in Docker scripts only
        String hostIp = System.getProperty("HOST_IP", "");

        if (hostIp.length() > 0) {
            // Assume Docker
            joinConfig.getKubernetesConfig().setEnabled(false);
            joinConfig.getTcpIpConfig().setEnabled(true)
                    .setMembers(List.of(hostIp + ":5701", hostIp + ":5702", hostIp + ":5703"));

            LOGGER.info("Non-Kubernetes configuration: member-list: {}", joinConfig.getTcpIpConfig().getMembers());
        } else {
            // Assume Kubernetes from YAML
            config.getNetworkConfig().getJoin().getKubernetesConfig().getProperties().keySet().forEach(key -> {
                LOGGER.info("Kubernetes configuration: {}: {}", key, joinConfig.getKubernetesConfig().getProperty(key));
            });
        }

        return config;
    }
}
