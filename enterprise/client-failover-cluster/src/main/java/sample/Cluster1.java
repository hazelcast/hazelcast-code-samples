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

package sample;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.examples.helper.LicenseUtils;

public class Cluster1 {

    public static void main(String[] args) {
        Config config = new Config();
        config.getGroupConfig().setName("cluster1");

        NetworkConfig networkConfig = config.getNetworkConfig();
        JoinConfig join = networkConfig.getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig().addMember("127.0.0.1").setEnabled(true);
        networkConfig.setPort(5701).setPortCount(2);
        config.setLicenseKey(LicenseUtils.ENTERPRISE_LICENSE_KEY);

        Hazelcast.newHazelcastInstance(config);
        Hazelcast.newHazelcastInstance(config);
    }
}
