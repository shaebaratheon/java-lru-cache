package com.toy.storage;

import com.toy.LruCache;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Segmented concurrent cache inspired by ConcurrentHashMap stripes.
 * Splits keyspace across 16 independent segments to minimize lock contention.
 */
public class SegmentedConcurrentCache<K, V> {

    private static final int DEFAULT_SEGMENTS = 16;
    private final LruCache<K, V>[] segments;
    private final int mask;

    @SuppressWarnings("unchecked")
    public SegmentedConcurrentCache(int totalCapacity, int numSegments) {
        int segmentCount = 1;
        while (segmentCount < numSegments) {
            segmentCount <<= 1;
        }
        this.mask = segmentCount - 1;
        this.segments = new LruCache[segmentCount];
        int perSegmentCapacity = Math.max(1, totalCapacity / segmentCount);

        for (int i = 0; i < segmentCount; i++) {
            this.segments[i] = new LruCache<>(perSegmentCapacity);
        }
    }

    public SegmentedConcurrentCache(int totalCapacity) {
        this(totalCapacity, DEFAULT_SEGMENTS);
    }

    private int getSegmentIndex(K key) {
        int h = key == null ? 0 : key.hashCode();
        // Spread bits
        h ^= (h >>> 20) ^ (h >>> 12);
        h ^= (h >>> 7) ^ (h >>> 4);
        return h & mask;
    }

    public V get(K key) {
        int idx = getSegmentIndex(key);
        return segments[idx].get(key);
    }

    public void put(K key, V value) {
        int idx = getSegmentIndex(key);
        segments[idx].put(key, value);
    }

    public boolean containsKey(K key) {
        int idx = getSegmentIndex(key);
        return segments[idx].get(key) != null;
    }

    public void clear() {
        for (LruCache<K, V> seg : segments) {
            seg.clear();
        }
    }

    public int size() {
        int total = 0;
        for (LruCache<K, V> seg : segments) {
            total += seg.size();
        }
        return total;
    }
}
