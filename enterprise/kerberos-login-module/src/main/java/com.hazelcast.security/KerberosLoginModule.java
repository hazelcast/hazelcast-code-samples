/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.security;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import com.hazelcast.internal.util.StringUtil;
import com.sun.security.jgss.ExtendedGSSContext;
import com.sun.security.jgss.InquireType;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import sun.security.krb5.Asn1Exception;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import uk.co.jtnet.datatypes.microsoft.windows.RpcSid;

import java.io.IOException;

public class KerberosLoginModule extends ClusterLoginModule {

    public static final String OPTION_RELAX_FLAGS_CHECK = "relaxFlagsCheck";
    private static final int AD_IF_RELEVANT = 1;
    private static final int AD_WIN2K_PAC = 128;

    private String _name;

    private byte[] getToken() throws LoginException {

        // get the credentials
        CredentialsCallback callback = new CredentialsCallback();
        try {
            callbackHandler.handle(new Callback[] { callback });
        }
        catch (IOException | UnsupportedCallbackException e) {
            throw new FailedLoginException("Failed to retrieve certificates. " + e.getMessage());
        }

        Credentials credentials = callback.getCredentials();
        if (credentials == null || !(credentials instanceof TokenCredentials)) {
            throw new FailedLoginException("Failed to get login token credentials.");
        }

        // get the token bytes
        byte[] token = ((TokenCredentials) credentials).getToken();
        if (token == null) {
            throw new FailedLoginException("Failed to retrieve a login token from the token credentials.");
        }

        return token;
    }

    private GSSContext acceptToken(byte[] token) throws LoginException {

        GSSManager gssManager = GSSManager.getInstance();

        try {
            GSSContext gssContext = gssManager.createContext((GSSCredential) null);
            token = gssContext.acceptSecContext(token, 0, token.length);
            if (!gssContext.isEstablished()) {
                throw new FailedLoginException("Multi-step negotiation is not supported by this module.");
            }

            boolean relaxChecks = getBoolOption(OPTION_RELAX_FLAGS_CHECK, false);
            if (!relaxChecks) {
                if (token != null && token.length > 0) {
                    throw new FailedLoginException("Mutual authentication is not supported by this module.");
                }
                if (gssContext.getConfState() || gssContext.getIntegState()) {
                    throw new FailedLoginException("Confidentiality and data integrity are not supported by this module.");
                }
            }

            return gssContext;
        }
        catch (GSSException e) {
            throw new FailedLoginException("Failed to accept the login token. " + e.getMessage());
        }
    }

    private void registerRoles(GSSContext gssContext) throws LoginException {

        // get the extended context - no extended context = no roles, just return
        if (!(gssContext instanceof ExtendedGSSContext)) {
            logger.warning("Failed to get an extended context.");
            return;
        }

        ExtendedGSSContext extendedContext = (ExtendedGSSContext) gssContext;

        try {
            // get the authz data - no data = no roles, just return
            Object authzDataObject = extendedContext.inquireSecContext(InquireType.KRB5_GET_AUTHZ_DATA);
            if (authzDataObject == null) {
                logger.warning("Failed to retrieve authorization data.");
                return;
            }

            com.sun.security.jgss.AuthorizationDataEntry[] authzEntries = (com.sun.security.jgss.AuthorizationDataEntry[]) authzDataObject;
            com.sun.security.jgss.AuthorizationDataEntry authzEntry = null;
            sun.security.krb5.internal.AuthorizationDataEntry pacAuthzEntry = null;
            for (int i = 0; i < authzEntries.length; i++) {
                if (authzEntries[i].getType() != AD_IF_RELEVANT) continue;
                authzEntry = authzEntries[i];

                byte[] adData = authzEntries[i].getData();
                DerInputStream adDataStream = new DerInputStream(adData);
                DerValue[] values = adDataStream.getSet(adData.length, true);
                DerValue value = values[0]; // should be the first one

                try {
                    pacAuthzEntry = new sun.security.krb5.internal.AuthorizationDataEntry(value);
                    if (pacAuthzEntry.adType == AD_WIN2K_PAC) break;
                }
                catch (IOException e) {
                    // swallow
                }

                pacAuthzEntry = null;
            }

            //  no authz entries = no roles, just return
            if (authzEntry == null || pacAuthzEntry == null) {
                logger.warning("Failed to retrieve authorization entries.");
                return;
            }

            // add all groups as roles
            // note: these are SIDs, not group names - we don't have the names in the Kerberos ticket
            try {
                RpcSid[] sids = new Pac(pacAuthzEntry.adData).getGroupMemberships();
                for (int i = 0; i < sids.length; i++) {
                    //logger.info("sid[" + i + "]: " + sids[i].toString());
                    addRole(StringUtil.lowerCaseInternal(sids[i].toString()));
                }
            }
            catch (Exception e) {
                // if things go wrong, no roles, return
                logger.warning("Failed to register roles.", e);
                return;
            }
        }
        catch (GSSException | IOException | Asn1Exception e) {
            // if things go wrong, no roles, return
            logger.warning("Failed to register roles.", e);
            return;
        }
    }

    public boolean onLogin() throws LoginException {

        // get the token
        byte[] token = getToken();

        // accept the token
        GSSContext gssContext = acceptToken(token);

        // register roles
        // does not throw - if anything goes wrong, logs the problem
        // and simply does not register any role
        registerRoles(gssContext);

        // get the principal name
        try {
            _name = gssContext.getSrcName().toString();
        }
        catch (GSSException e) {
            throw new FailedLoginException("Failed to get a name. " + e.getMessage());
        }

        // although this can be convenient, we'd rather not do it
        // and fully rely on group SIDs to control the behavior
        /*
        boolean skipRole = getBoolOption(OPTION_SKIP_ROLE, false);
        if (!skipRole) {
            addRole(_name);
        }
        */

        return true;
    }

    protected String getName() {
        return _name;
    }
}
