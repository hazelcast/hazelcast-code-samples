package com.hazelcast.sample.replacer;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

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
