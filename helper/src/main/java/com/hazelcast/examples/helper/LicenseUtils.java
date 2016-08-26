/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.examples.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.hazelcast.examples.helper.CommonUtils.closeQuietly;

/**
 * Utils class to get the Hazelcast Enterprise license key for code samples.
 *
 * You can provide the license key via
 * <ul>
 * <li>the constant {@link LicenseUtils#ENTERPRISE_LICENSE_KEY}</li>
 * <li>the file {@code .hazelcast-code-samples-license} in your home directory</li>
 * <li>the system property {@code -Dhazelcast.enterprise.license.key=<YOUR_LICENSE_KEY_HERE>}</li>
 * </ul>
 *
 * You can request a trial key at http://hazelcast.com/hazelcast-enterprise-trial
 */
public final class LicenseUtils {

    public static final String ENTERPRISE_LICENSE_KEY = initLicenseKey();

    private static final int READ_BUFFER_SIZE = 8192;

    private LicenseUtils() {
    }

    private static String initLicenseKey() {
        String licenseKey = System.getProperty("hazelcast.enterprise.license.key");
        if (licenseKey != null) {
            System.out.println("Hazelcast Enterprise license key was set via system properties");
            return licenseKey.trim();
        }

        File licenseFile = new File(System.getProperty("user.home"), ".hazelcast-code-samples-license").getAbsoluteFile();
        licenseKey = fileAsText(licenseFile);
        if (licenseKey != null) {
            System.out.println("Hazelcast Enterprise license key was set via license file");
            return licenseKey.trim();
        }

        throw new RuntimeException("Hazelcast Enterprise license key was not configured!"
                + " Please have a look at LicenseUtils for details.");
    }

    private static String fileAsText(File file) {
        FileInputStream stream = null;
        InputStreamReader streamReader = null;
        BufferedReader reader = null;
        try {
            stream = new FileInputStream(file);
            streamReader = new InputStreamReader(stream);
            reader = new BufferedReader(streamReader);

            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[READ_BUFFER_SIZE];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        } catch (IOException e) {
            return null;
        } finally {
            closeQuietly(reader);
            closeQuietly(streamReader);
            closeQuietly(stream);
        }
    }
}
