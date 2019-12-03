/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.examples;

import static com.hazelcast.config.LoginModuleConfig.LoginModuleUsage.REQUIRED;
import static com.hazelcast.examples.helper.LicenseUtils.ENTERPRISE_LICENSE_KEY;

import com.hazelcast.config.Config;
import com.hazelcast.config.LoginModuleConfig;
import com.hazelcast.config.PermissionConfig;
import com.hazelcast.config.PermissionConfig.PermissionType;
import com.hazelcast.config.ServerSocketEndpointConfig;
import com.hazelcast.config.security.JaasAuthenticationConfig;
import com.hazelcast.config.security.RealmConfig;
import com.hazelcast.core.Hazelcast;

public class Member {

    public static void main(String[] args) {
        Config config = new Config()
                .setLicenseKey(ENTERPRISE_LICENSE_KEY);
        ServerSocketEndpointConfig clientEndpointConfig = new ServerSocketEndpointConfig().setPort(5701);
        ServerSocketEndpointConfig memberEndpointConfig = new ServerSocketEndpointConfig().setPort(15701);
        config.getAdvancedNetworkConfig().setEnabled(true)
            .setClientEndpointConfig(clientEndpointConfig)
            .setMemberEndpointConfig(memberEndpointConfig);
        JaasAuthenticationConfig jaasConfig = new JaasAuthenticationConfig()
                .addLoginModuleConfig(new LoginModuleConfig(DowTokenLoginModule.class.getName(), REQUIRED));
        config.getSecurityConfig().setEnabled(true)
            .setClientRealmConfig("dayOfWeekAuthentication", new RealmConfig().setJaasAuthenticationConfig(jaasConfig))
            .addClientPermissionConfig(new PermissionConfig(PermissionType.MAP, "*", "admin").addAction("all"))
            .addClientPermissionConfig(new PermissionConfig(PermissionType.MAP, "*", "monitor").addAction("read"));

        Hazelcast.newHazelcastInstance(config)
            .getMap("test").put("updatedBy", "member");
    }
}
