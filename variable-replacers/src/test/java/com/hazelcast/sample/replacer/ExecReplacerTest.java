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

package com.hazelcast.sample.replacer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.junit.Assume;
import org.junit.Test;

import com.hazelcast.util.StringUtil;

/**
 * Unit tests for {@link ExecReplacer}.
 */
public class ExecReplacerTest {

    private static final String OS = StringUtil.lowerCaseInternal(System.getProperty("os.name"));

    @Test
    public void testGetPrefix() {
        assertEquals("EXEC", new ExecReplacer().getPrefix());
    }

    @Test
    public void testReplacementDefaultSeparator() {
        Assume.assumeTrue(isUnixFamily());
        ExecReplacer execReplacer = new ExecReplacer();
        execReplacer.init(new Properties());
        assertEquals("Hi", execReplacer.getReplacement("echo -n Hi"));
    }

    @Test
    public void testReplacementCustomSeparator() {
        Assume.assumeTrue(isUnixFamily());
        ExecReplacer execReplacer = new ExecReplacer();
        Properties properties = new Properties();
        properties.setProperty(ExecReplacer.PROPERTY_ARGUMENT_SEPARATOR, "#");
        execReplacer.init(properties);
        assertEquals("Hi, world!", execReplacer.getReplacement("echo#-n#Hi, world!"));
    }

    @Test
    public void testMissingExecutable() {
        ExecReplacer execReplacer = new ExecReplacer();
        execReplacer.init(new Properties());
        assertNull(execReplacer.getReplacement("aCommandWhichIsHardToFind"));
    }

    @Test
    public void testNonZeroExitCode() {
        Assume.assumeTrue(isUnixFamily());
        ExecReplacer execReplacer = new ExecReplacer();
        execReplacer.init(new Properties());
        assertNull(execReplacer.getReplacement("test -f /usr/bin/aCommandWhichIsHardToFind"));
    }

    @Test
    public void testNonZeroExitCodeAllowed() {
        Assume.assumeTrue(isUnixFamily());
        ExecReplacer execReplacer = new ExecReplacer();
        Properties properties = new Properties();
        properties.setProperty(ExecReplacer.PROPERTY_REQUIRES_ZERO_EXIT, "false");
        execReplacer.init(properties);
        assertEquals("", execReplacer.getReplacement("test -f /usr/bin/aCommandWhichIsHardToFind"));
    }

    /**
     * Returns {@code true} if the system is from Unix family.
     *
     * @return {@code true} if the current system is Unix/Linux/AIX.
     */
    private static boolean isUnixFamily() {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    }
}
