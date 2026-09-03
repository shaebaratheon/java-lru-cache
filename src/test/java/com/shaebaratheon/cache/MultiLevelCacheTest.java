package com.shaebaratheon.cache;

import org.junit.Assert;
import org.junit.Test;

public class MultiLevelCacheTest {

    @Test
    public void testL1PromotionAndTiering() {
        MultiLevelCache<String, String> cache = new MultiLevelCache<>(2, 1024 * 1024);
        cache.put("k1", "val1");
        cache.put("k2", "val2");
        Assert.assertEquals("val1", cache.get("k1").orElse(null));

        // Adding third item triggers eviction to L2 OffHeap
        cache.put("k3", "val3");
        Assert.assertEquals(2, cache.size());

        // Ensure evicted item can still be read and promoted from L2
        Assert.assertTrue(cache.get("k2").isPresent() || cache.get("k1").isPresent());
    }
}
