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

import com.hazelcast.config.GroupConfig;
import com.hazelcast.security.Credentials;
import com.hazelcast.security.ICredentialsFactory;

import java.util.Properties;

public  class CustomCredentialsFactory implements ICredentialsFactory {
    private String username;
    private String key1;
    private String key2;
    @Override
    public void configure(GroupConfig groupConfig, Properties properties) {
        username = properties.getProperty("username");
        key1 = properties.getProperty("key1");
        key2 = properties.getProperty("key2");
    }
    @Override
    public Credentials newCredentials() {
        return new CustomCredentials(username, key1, key2);
    }
    @Override
    public void destroy() {
    }
}
