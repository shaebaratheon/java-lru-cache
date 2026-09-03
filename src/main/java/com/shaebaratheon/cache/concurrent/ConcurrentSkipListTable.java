package com.shaebaratheon.cache.concurrent;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Optional;
import java.util.Map;

/**
 * Lock-free Concurrent SkipList Range Index for High-Concurrency Ordered Scans.
 */
public class ConcurrentSkipListTable<K extends Comparable<K>, V> {
    private final ConcurrentSkipListMap<K, V> skipList = new ConcurrentSkipListMap<>();
    private final AtomicLong elementCount = new AtomicLong(0);

    public void put(K key, V value) {
        if (skipList.put(key, value) == null) {
            elementCount.incrementAndGet();
        }
    }

    public Optional<V> get(K key) {
        return Optional.ofNullable(skipList.get(key));
    }

    public boolean remove(K key) {
        if (skipList.remove(key) != null) {
            elementCount.decrementAndGet();
            return true;
        }
        return false;
    }

    public Map<K, V> range(K startInclusive, K endExclusive) {
        return skipList.subMap(startInclusive, true, endExclusive, false);
    }

    public long size() {
        return elementCount.get();
    }
}
