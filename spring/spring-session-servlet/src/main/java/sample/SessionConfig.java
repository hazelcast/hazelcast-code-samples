/*
 * Copyright 2014-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample;

import com.hazelcast.config.Config;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.IndexType;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import com.hazelcast.spring.session.config.annotation.web.http.EnableHazelcastHttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.hazelcast.spring.session.HazelcastIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME;
import static com.hazelcast.spring.session.HazelcastIndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE;

// tag::class[]
@EnableHazelcastHttpSession(maxInactiveIntervalInSeconds = 300)
@Configuration
public class SessionConfig {

    @Bean(destroyMethod = "shutdown")
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(0);
        networkConfig.getJoin().getAutoDetectionConfig().setEnabled(false);
        config.getMapConfig(DEFAULT_SESSION_MAP_NAME)
              .addIndexConfig(
                      new IndexConfig(IndexType.HASH, PRINCIPAL_NAME_ATTRIBUTE));
        return Hazelcast.newHazelcastInstance(config);
    }

}
// end::class[]
