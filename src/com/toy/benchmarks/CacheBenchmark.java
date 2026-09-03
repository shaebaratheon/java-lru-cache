package com.toy.benchmarks;

import com.toy.LruCache;
import com.toy.eviction.ArcCache;
import com.toy.eviction.LfuCache;
import com.toy.storage.SegmentedConcurrentCache;

import java.util.Random;
import java.util.Arrays;

/**
 * CacheBenchmark measures throughput and hit rate across different cache implementations
 * under uniform and Zipfian-like access distributions.
 */
public class CacheBenchmark {
    private static final int NUM_OPS = 200_000;
    private static final int KEY_UNIVERSE = 10_000;
    private static final int CACHE_CAPACITY = 1_000;

    public static void main(String[] args) {
        System.out.println("Running CacheBenchmark with " + NUM_OPS + " operations...");
        int[] requests = generateZipfianWorkload(NUM_OPS, KEY_UNIVERSE, 0.85);

        benchLru(requests);
        benchLfu(requests);
        benchArc(requests);
        benchSegmented(requests);
        System.out.println("Benchmark completed!");
    }

    private static int[] generateZipfianWorkload(int n, int universe, double skew) {
        int[] work = new int[n];
        Random rnd = new Random(42);
        double[] cdf = new double[universe];
        double sum = 0;
        for (int i = 1; i <= universe; i++) {
            sum += 1.0 / Math.pow(i, skew);
            cdf[i - 1] = sum;
        }
        for (int i = 0; i < universe; i++) {
            cdf[i] /= sum;
        }

        for (int i = 0; i < n; i++) {
            double r = rnd.nextDouble();
            int idx = Arrays.binarySearch(cdf, r);
            if (idx < 0) idx = -idx - 1;
            if (idx >= universe) idx = universe - 1;
            work[i] = idx;
        }
        return work;
    }

    private static void benchLru(int[] requests) {
        LruCache<Integer, String> cache = new LruCache<>(CACHE_CAPACITY);
        int hits = 0;
        long start = System.nanoTime();
        for (int key : requests) {
            String val = cache.get(key);
            if (val != null) {
                hits++;
            } else {
                cache.put(key, "data_" + key);
            }
        }
        long durationMs = (System.nanoTime() - start) / 1_000_000;
        printResult("LruCache", hits, requests.length, durationMs);
    }

    private static void benchLfu(int[] requests) {
        LfuCache<Integer, String> cache = new LfuCache<>(CACHE_CAPACITY);
        int hits = 0;
        long start = System.nanoTime();
        for (int key : requests) {
            String val = cache.get(key);
            if (val != null) {
                hits++;
            } else {
                cache.put(key, "data_" + key);
            }
        }
        long durationMs = (System.nanoTime() - start) / 1_000_000;
        printResult("LfuCache", hits, requests.length, durationMs);
    }

    private static void benchArc(int[] requests) {
        ArcCache<Integer, String> cache = new ArcCache<>(CACHE_CAPACITY);
        int hits = 0;
        long start = System.nanoTime();
        for (int key : requests) {
            String val = cache.get(key);
            if (val != null) {
                hits++;
            } else {
                cache.put(key, "data_" + key);
            }
        }
        long durationMs = (System.nanoTime() - start) / 1_000_000;
        printResult("ArcCache", hits, requests.length, durationMs);
    }

    private static void benchSegmented(int[] requests) {
        SegmentedConcurrentCache<Integer, String> cache = new SegmentedConcurrentCache<>(16, CACHE_CAPACITY);
        int hits = 0;
        long start = System.nanoTime();
        for (int key : requests) {
            String val = cache.get(key);
            if (val != null) {
                hits++;
            } else {
                cache.put(key, "data_" + key);
            }
        }
        long durationMs = (System.nanoTime() - start) / 1_000_000;
        printResult("SegmentedCache", hits, requests.length, durationMs);
    }

    private static void printResult(String name, int hits, int total, long ms) {
        double hitRatio = 100.0 * hits / total;
        double opsPerSec = (1000.0 * total) / Math.max(1, ms);
        System.out.printf("  [%-16s] Hits: %6d (%.2f%%) | Time: %4d ms | Throughput: %,10.0f ops/s%n",
                name, hits, hitRatio, ms, opsPerSec);
    }
}
