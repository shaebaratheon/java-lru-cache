package com.shaebaratheon.cache;

import com.shaebaratheon.cache.distributed.RendezvousHashRouter;
import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;

public class RendezvousHashTest {
    @Test
    public void testConsistentRouting() {
        RendezvousHashRouter<String> router = new RendezvousHashRouter<>(Arrays.asList("node-1", "node-2", "node-3"));
        String dest1 = router.route("user:100").orElse(null);
        String dest2 = router.route("user:100").orElse(null);
        Assert.assertEquals(dest1, dest2);
    }
}
