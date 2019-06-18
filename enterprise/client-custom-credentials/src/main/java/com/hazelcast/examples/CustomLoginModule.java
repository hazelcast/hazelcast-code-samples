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

import com.hazelcast.security.ClusterLoginModule;
import com.hazelcast.security.Credentials;
import com.hazelcast.security.CredentialsCallback;
import com.hazelcast.security.TokenCredentials;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CustomLoginModule extends ClusterLoginModule {

    private String name;

    @Override
    protected boolean onLogin() throws LoginException {
        CredentialsCallback cb = new CredentialsCallback();
        try {
            callbackHandler.handle(new Callback[] { cb });
        } catch (IOException | UnsupportedCallbackException e) {
            throw new LoginException("Problem getting credentials");
        }
        Credentials credentials = cb.getCredentials();
        if (!(credentials instanceof TokenCredentials)) {
            throw new FailedLoginException();
        }
        byte[] token = ((TokenCredentials) credentials).getToken();
        byte[] otoken = ((String) options.get("token")).getBytes(StandardCharsets.UTF_8);
        if (Arrays.equals(token, otoken)) {
            name = (String) options.get("name");
            addRole(name);
            return true;
        }
        throw new LoginException("Invalid credentials");
    }

    @Override
    protected String getName() {
        return name;
    }

}
