package com.shaebaratheon.cache;

import com.shaebaratheon.cache.events.CacheEventBus;
import org.junit.Assert;
import org.junit.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CacheEventBusTest {
    @Test
    public void testEventDispatch() throws Exception {
        CacheEventBus<String, String> bus = new CacheEventBus<>();
        CountDownLatch latch = new CountDownLatch(1);
        bus.register(event -> {
            if (event.type == CacheEventBus.EventType.CREATED) {
                latch.countDown();
            }
        });
        bus.publish(CacheEventBus.EventType.CREATED, "k1", "v1");
        Assert.assertTrue(latch.await(2, TimeUnit.SECONDS));
        bus.shutdown();
    }
}
