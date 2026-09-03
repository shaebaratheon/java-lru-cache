package com.shaebaratheon.cache;

import org.junit.Assert;
import org.junit.Test;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiLevelConcurrencyTest {
    @Test
    public void testConcurrentMultiThreadedAccess() throws Exception {
        MultiLevelCache<String, String> cache = new MultiLevelCache<>(100, 10 * 1024 * 1024);
        int threads = 8;
        int operations = 250;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int t = 0; t < threads; t++) {
            final int tid = t;
            pool.submit(() -> {
                try {
                    for (int i = 0; i < operations; i++) {
                        cache.put("key_" + tid + "_" + i, "val_" + i);
                        if (cache.get("key_" + tid + "_" + i).isPresent()) {
                            successCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
        Assert.assertEquals(threads * operations, successCount.get());
        pool.shutdown();
    }
}
