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

import com.hazelcast.security.Credentials;

public class CustomCredentials implements Credentials {

    private String username;
    private String key1;
    private String key2;
    private String endpoint;

    public CustomCredentials() {

    }

    public CustomCredentials(String username, String key1, String key2) {
        this.username = username;
        this.key1 = key1;
        this.key2 = key2;
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String getPrincipal() {
        return username;
    }

    public String getKey1() {
        return key1;
    }

    public String getKey2() {
        return key2;
    }
}
