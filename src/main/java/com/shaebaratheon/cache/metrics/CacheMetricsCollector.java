package com.shaebaratheon.cache.metrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * High-throughput latency and hit-ratio metrics collector.
 */
public class CacheMetricsCollector {
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong evictionCount = new AtomicLong(0);
    private final AtomicLong totalReadLatencyNanos = new AtomicLong(0);

    public void recordHit(long latencyNanos) {
        hitCount.incrementAndGet();
        totalReadLatencyNanos.addAndGet(latencyNanos);
    }

    public void recordMiss(long latencyNanos) {
        missCount.incrementAndGet();
        totalReadLatencyNanos.addAndGet(latencyNanos);
    }

    public void recordEviction() {
        evictionCount.incrementAndGet();
    }

    public double getHitRatio() {
        long hits = hitCount.get();
        long misses = missCount.get();
        long total = hits + misses;
        return total == 0 ? 1.0 : (double) hits / total;
    }

    public double getAverageLatencyMs() {
        long total = hitCount.get() + missCount.get();
        if (total == 0) return 0.0;
        return (totalReadLatencyNanos.get() / (double) total) / 1_000_000.0;
    }
}
