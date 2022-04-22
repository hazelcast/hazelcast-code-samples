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

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.client.config.YamlClientConfigBuilder;

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
     * This is a {@link com.hazelcast.client.config.ClientConfig ClientConfig}
     * object, used to create a client. There is a similar counterpart for servers.
     * </p>
     *
     * @return
     * @throws IOException
     */
    @Bean
    public ClientConfig clientConfig() throws IOException {
        ClientConfig clientConfig = new YamlClientConfigBuilder("hazelcast-client.yml").build();
        ClientNetworkConfig clientNetworkConfig = clientConfig.getNetworkConfig();

        // Host IP is set in Docker scripts only
        String hostIp = System.getProperty("HOST_IP", "");

        if (hostIp.length() > 0) {
            // Assume Docker
            clientNetworkConfig.getKubernetesConfig().setEnabled(false);
            clientNetworkConfig.setAddresses(List.of(hostIp + ":5701", hostIp + ":5702", hostIp + ":5703"));

            LOGGER.info("Non-Kubernetes configuration: member-list: {}", clientNetworkConfig.getAddresses());
        } else {
            // Assume Kubernetes from YAML
            clientNetworkConfig.getKubernetesConfig().getProperties().keySet().forEach(key -> {
                LOGGER.info("Kubernetes configuration: {}: {}", key,
                        clientNetworkConfig.getKubernetesConfig().getProperty(key));
            });
        }

        for (String s : List.of("spring.datasource.url", "spring.datasource.driver-class-name")) {
            LOGGER.info("{}={}", s, System.getProperty(s));
        }

        return clientConfig;
    }

}

