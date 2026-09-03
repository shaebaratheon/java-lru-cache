package com.toy.metrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Real-time operational metrics for cache hit-ratios and latency.
 */
public class CacheMetrics {

    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final AtomicLong evictions = new AtomicLong(0);
    private final AtomicLong totalDurationNanos = new AtomicLong(0);

    public void recordHit(long durationNanos) {
        hits.incrementAndGet();
        totalDurationNanos.addAndGet(durationNanos);
    }

    public void recordMiss(long durationNanos) {
        misses.incrementAndGet();
        totalDurationNanos.addAndGet(durationNanos);
    }

    public void recordEviction() {
        evictions.incrementAndGet();
    }

    public double getHitRatio() {
        long h = hits.get();
        long m = misses.get();
        long total = h + m;
        return total == 0 ? 0.0 : (double) h / total;
    }

    public double getAverageLatencyMicros() {
        long totalOps = hits.get() + misses.get();
        if (totalOps == 0) return 0.0;
        return (double) totalDurationNanos.get() / (totalOps * 1000.0);
    }

    public long getEvictionsCount() {
        return evictions.get();
    }

    public void reset() {
        hits.set(0);
        misses.set(0);
        evictions.set(0);
        totalDurationNanos.set(0);
    }

    @Override
    public String toString() {
        return String.format(
            "CacheMetrics{hits=%d, misses=%d, hitRatio=%.2f%%, avgLatency=%.2fus, evictions=%d}",
            hits.get(), misses.get(), getHitRatio() * 100.0, getAverageLatencyMicros(), evictions.get()
        );
    }
}
