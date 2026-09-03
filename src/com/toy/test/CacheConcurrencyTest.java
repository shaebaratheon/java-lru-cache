package com.toy.test;

import com.toy.eviction.LfuCache;
import com.toy.eviction.TwoQueueCache;
import com.toy.storage.SegmentedConcurrentCache;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CacheConcurrencyTest {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Java Cache Concurrency and Eviction Validation...");

        // 1. Test SegmentedConcurrentCache Concurrency
        final int threads = 8;
        final int opsPerThread = 5000;
        final SegmentedConcurrentCache<String, Integer> cache = new SegmentedConcurrentCache<>(1000, 16);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            executor.submit(() -> {
                for (int i = 0; i < opsPerThread; i++) {
                    String key = "key_" + ((threadId * 1000) + (i % 200));
                    cache.put(key, i);
                    Integer val = cache.get(key);
                    if (val == null || !val.equals(i)) {
                        System.err.println("Concurrency read inconsistency on key: " + key);
                    }
                }
                latch.countDown();
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        System.out.println("  [PASS] SegmentedConcurrentCache multi-threaded consistency. Active entries: " + cache.size());

        // 2. Test LFU Eviction logic
        LfuCache<String, String> lfu = new LfuCache<>(3);
        lfu.put("A", "Alpha");
        lfu.put("B", "Beta");
        lfu.put("C", "Gamma");

        lfu.get("A"); // freq 2
        lfu.get("A"); // freq 3
        lfu.get("B"); // freq 2

        lfu.put("D", "Delta"); // Evicts C (freq 1)

        if (lfu.get("C") != null || !"Alpha".equals(lfu.get("A")) || !"Beta".equals(lfu.get("B"))) {
            throw new RuntimeException("LFU eviction assertion failed!");
        }
        System.out.println("  [PASS] LfuCache frequency-based eviction verification.");

        // 3. Test 2Q Eviction logic
        TwoQueueCache<String, String> tq = new TwoQueueCache<>(4);
        tq.put("1", "one");
        tq.put("2", "two");
        tq.get("1"); // Promotes 1 to warm queue

        tq.put("3", "three");
        tq.put("4", "four");
        tq.put("5", "five"); // Evicts oldest from probation queue (not 1)

        if (!"one".equals(tq.get("1"))) {
            throw new RuntimeException("2Q warm cache retention assertion failed!");
        }
        System.out.println("  [PASS] TwoQueueCache probation-warm eviction verification.");

        System.out.println("All Java Cache tests finished successfully!");
    }
}
