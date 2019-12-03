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

import java.security.AccessControlException;
import java.time.LocalDate;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.security.TokenEncoding;
import com.hazelcast.config.security.TokenIdentityConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class Client {

    public static void main(String[] args) {

        // Use a day-of-week token for authentication
        String token = LocalDate.now().getDayOfWeek().name();

        System.out.println("Authenticating with token: " + token);
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getSecurityConfig().setTokenIdentityConfig(new TokenIdentityConfig(TokenEncoding.NONE, token));

        HazelcastInstance hz = HazelcastClient.newHazelcastClient(clientConfig);

        try {
            // every authenticated client gets the "monitor" role which has "read" access to all IMap instances
            IMap<String, String> map = hz.getMap("test");
            System.out.println("Map was last updated by: " + map.get("updatedBy"));

            // If the authentication takes place on Monday, then the client is assigned also the "admin" role which has full
            // access to all IMap instances
            System.out.println("Trying to update the Map.");
            map.put("updatedBy", "client");
            System.out.println("Map was successfully updated.");
        } catch (AccessControlException e) {
            System.out.println("Access to the Map was not allowed. Try it on Monday.");
            e.printStackTrace();
        } finally {
            System.out.println("Shutting down the client");
            hz.shutdown();
        }
    }
}
