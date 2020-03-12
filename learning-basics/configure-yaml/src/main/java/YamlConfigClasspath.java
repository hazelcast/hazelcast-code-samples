/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
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

import com.hazelcast.config.ClasspathYamlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Properties;

public class YamlConfigClasspath {
    public static void main(String[] args) {
        // taking the member port from system properties
        System.setProperty("hazelcast.member.port", "5555");
        Config config = new ClasspathYamlConfig("hazelcast-sample.yaml");
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        instance.shutdown();

        // taking the member port from the provided properties instance
        Properties configProperties = new Properties();
        configProperties.setProperty("hazelcast.member.port", "5999");
        config = new ClasspathYamlConfig("hazelcast-sample.yaml", configProperties);
        instance = Hazelcast.newHazelcastInstance(config);
        instance.shutdown();
    }
}
