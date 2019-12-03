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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.DayOfWeek.MONDAY;
import static java.time.format.TextStyle.FULL;
import static java.util.Locale.US;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;

/**
 * Token based login module which checks if the incoming token is the current day-of-week name. All successfully authenticated
 * clients gets "monitor" role. If the client authenticates on Monday, they get also the "admin" role assigned.
 */
public class DowTokenLoginModule extends ClusterLoginModule {

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
        byte[] actualToken = ((TokenCredentials) credentials).getToken();
        DayOfWeek dow = LocalDate.now().getDayOfWeek();
        byte[] expectedToken = dow.name().getBytes(UTF_8);
        if (Arrays.equals(actualToken, expectedToken)) {
            // Token doesn't need to hold a name, we can choose an arbitrary one.
            // The name will be wrapped by parent class (ClusterLoginModue) into a ClusterIdentityPrincipal
            // instance and added to the JAAS Subject.
            name = dow.getDisplayName(FULL, US);

            // We can assign more roles to the JAAS Subject. Just call addRole() method for every role.
            // The role names are represented by ClusterRolePrincipal instances in the JAAS Subject.
            addRole("monitor");

            // if it's Monday today, lets give the client full access
            if (dow == MONDAY) {
                addRole("admin");
            }

            return true;
        }
        throw new LoginException("Invalid token");
    }

    @Override
    protected String getName() {
        return name;
    }

}
