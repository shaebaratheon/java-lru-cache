package com.shaebaratheon.cache.metrics;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class CacheHitStatisticsTest {
  private CacheHitStatistics stats;

  @Before
  public void setUp() {
    stats = new CacheHitStatistics();
  }

  @Test
  public void testHitRateCalculation() {
    stats.recordHit();
    stats.recordHit();
    stats.recordHit();
    stats.recordMiss();
    assertEquals(0.75, stats.getHitRate(), 0.001);
    assertEquals(3, stats.getHitCount());
    assertEquals(1, stats.getMissCount());
  }

  @Test
  public void testResetStatistics() {
    stats.recordHit();
    stats.recordEviction();
    stats.reset();
    assertEquals(0, stats.getHitCount());
    assertEquals(0, stats.getEvictionCount());
    assertEquals(0.0, stats.getHitRate(), 0.001);
  }
}
