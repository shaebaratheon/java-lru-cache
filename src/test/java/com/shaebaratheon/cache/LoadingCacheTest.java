package com.shaebaratheon.cache;

import com.shaebaratheon.cache.loader.LoadingCache;
import org.junit.Assert;
import org.junit.Test;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadingCacheTest {
    @Test
    public void testThunderingHerdSingleFlight() throws Exception {
        MultiLevelCache<String, String> cache = new MultiLevelCache<>(10, 1024);
        AtomicInteger computeCount = new AtomicInteger(0);

        LoadingCache<String, String> loadingCache = new LoadingCache<>(cache, key -> {
            computeCount.incrementAndGet();
            return "computed_" + key;
        });

        String v1 = loadingCache.get("item1");
        String v2 = loadingCache.get("item1");

        Assert.assertEquals("computed_item1", v1);
        Assert.assertEquals("computed_item1", v2);
        Assert.assertEquals(1, computeCount.get());
    }
}
