package com.shaebaratheon.cache.metrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe cache hit-rate and access statistics monitor.
 */
public final class CacheHitStatistics {
  private final AtomicLong hits = new AtomicLong();
  private final AtomicLong misses = new AtomicLong();
  private final AtomicLong evictions = new AtomicLong();

  public void recordHit() {
    hits.incrementAndGet();
  }

  public void recordMiss() {
    misses.incrementAndGet();
  }

  public void recordEviction() {
    evictions.incrementAndGet();
  }

  public long getHitCount() {
    return hits.get();
  }

  public long getMissCount() {
    return misses.get();
  }

  public long getEvictionCount() {
    return evictions.get();
  }

  public double getHitRate() {
    long h = hits.get();
    long total = h + misses.get();
    return total == 0 ? 0.0 : ((double) h) / total;
  }

  public void reset() {
    hits.set(0);
    misses.set(0);
    evictions.set(0);
  }
}
