package com.shaebaratheon.cache.policy;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Frequency sketch TinyLFU policy combined with LRU eviction window.
 */
public class TinyLFUPolicy<K> implements EvictionPolicy<K> {
    private final LinkedHashMap<K, Integer> accessFrequency;
    private final int capacity;

    public TinyLFUPolicy(int capacity) {
        this.capacity = capacity;
        this.accessFrequency = new LinkedHashMap<>(capacity, 0.75f, true);
    }

    @Override
    public synchronized void recordAccess(K key) {
        int count = accessFrequency.getOrDefault(key, 0);
        accessFrequency.put(key, Math.min(15, count + 1));
    }

    @Override
    public synchronized K selectVictim() {
        K victim = null;
        int minFreq = Integer.MAX_VALUE;
        for (Map.Entry<K, Integer> entry : accessFrequency.entrySet()) {
            if (entry.getValue() < minFreq) {
                minFreq = entry.getValue();
                victim = entry.getKey();
            }
        }
        if (victim != null) {
            accessFrequency.remove(victim);
        }
        return victim;
    }

    @Override
    public synchronized void remove(K key) {
        accessFrequency.remove(key);
    }
}
