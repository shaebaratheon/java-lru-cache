package com.github.javalru;

import org.junit.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class LoadingCacheTest {
    @Test
    public void testComputeIfAbsentLoadsOnce() throws Exception {
        AtomicInteger computeCount = new AtomicInteger(0);
        LoadingCache<String, Integer> cache = new LoadingCache<>(k -> {
            computeCount.incrementAndGet();
            return k.length();
        });

        assertEquals(Integer.valueOf(5), cache.get("apple"));
        assertEquals(Integer.valueOf(5), cache.get("apple"));
        assertEquals(1, computeCount.get());
        assertEquals(1, cache.size());
    }
}
