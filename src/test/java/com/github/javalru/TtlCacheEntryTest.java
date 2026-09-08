package com.github.javalru;

import org.junit.Test;
import static org.junit.Assert.*;

public class TtlCacheEntryTest {
    @Test
    public void testEntryNotExpiredInitially() {
        TtlCacheEntry<String> entry = new TtlCacheEntry<>("value", 5000);
        assertFalse(entry.isExpired());
        assertEquals("value", entry.getValue());
    }

    @Test
    public void testZeroTtlNeverExpires() {
        TtlCacheEntry<String> entry = new TtlCacheEntry<>("persistent", 0);
        assertFalse(entry.isExpired());
        assertEquals(Long.MAX_VALUE, entry.getExpiresAt());
    }
}
