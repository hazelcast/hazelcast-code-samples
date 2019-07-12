package com.hazelcast.sample.replacer;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit test for {@link IdReplacer}.
 */
public class IdReplacerTest {

    @Test
    public void testPrefix() {
        assertEquals("ID", new IdReplacer().getPrefix());
    }

    @Test
    public void testReplacements() {
        IdReplacer idReplacer = new IdReplacer();
        idReplacer.init(new Properties());
        assertEquals("test", idReplacer.getReplacement("test"));
        assertNull(idReplacer.getReplacement(null));
    }
}
