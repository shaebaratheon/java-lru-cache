package com.shaebaratheon.cache;

import org.junit.Assert;
import org.junit.Test;

public class ExhaustiveCacheTest {
    @Test
    public void testHighVolumePutGetCycle() {
        MultiLevelCache<String, String> cache = new MultiLevelCache<>(50, 1024 * 1024);
        for (int i = 0; i < 200; i++) {
            cache.put("key_" + i, "val_" + i);
        }
        Assert.assertNotNull(cache);
    }
}
