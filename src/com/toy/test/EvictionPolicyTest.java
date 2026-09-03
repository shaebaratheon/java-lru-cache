package com.toy.test;

import com.toy.eviction.ArcCache;
import com.toy.eviction.LfuCache;
import com.toy.eviction.TwoQueueCache;
import com.toy.policy.ExpirationPolicy;

public class EvictionPolicyTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Running EvictionPolicyTest...");
        testLfu();
        testTwoQueue();
        testArc();
        testExpiration();
        System.out.println("All eviction tests passed successfully!");
    }

    private static void testLfu() {
        LfuCache<String, Integer> cache = new LfuCache<>(3);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3);

        cache.get("a");
        cache.get("a");
        cache.get("b");

        cache.put("d", 4);

        if (cache.get("c") != null) {
            throw new AssertionError("Key 'c' should have been evicted by LFU");
        }
        if (cache.get("a") == null || cache.get("b") == null || cache.get("d") == null) {
            throw new AssertionError("Keys 'a', 'b', and 'd' should still exist");
        }
        System.out.println("  LFU test passed");
    }

    private static void testTwoQueue() {
        TwoQueueCache<String, String> cache = new TwoQueueCache<>(4);
        cache.put("k1", "v1");
        cache.put("k2", "v2");
        cache.put("k3", "v3");

        if (cache.get("k1") != null) {
            throw new AssertionError("Key 'k1' should have been evicted from In-queue");
        }

        cache.get("k2");
        cache.put("k4", "v4");
        cache.put("k5", "v5");

        System.out.println("  2Q test passed");
    }

    private static void testArc() {
        ArcCache<Integer, String> arc = new ArcCache<>(4);
        for (int i = 1; i <= 4; i++) {
            arc.put(i, "val" + i);
        }
        for (int i = 1; i <= 4; i++) {
            if (!("val" + i).equals(arc.get(i))) {
                throw new AssertionError("Missing value for key " + i);
            }
        }
        arc.put(5, "val5");
        if (arc.size() > 4) {
            throw new AssertionError("ArcCache exceeded capacity: " + arc.size());
        }
        System.out.println("  ARC test passed");
    }

    private static void testExpiration() throws Exception {
        try (ExpirationPolicy<String, String> exp = new ExpirationPolicy<>(100, 50)) {
            exp.put("temp", "hello", 100);
            if (!"hello".equals(exp.get("temp"))) {
                throw new AssertionError("Immediate lookup failed");
            }
            Thread.sleep(150);
            if (exp.get("temp") != null) {
                throw new AssertionError("Key did not expire");
            }
        }
        System.out.println("  Expiration test passed");
    }
}
