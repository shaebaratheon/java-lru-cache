package com.toy.eviction;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Least Frequently Used (LFU) cache operating in O(1) time complexity.
 */
public class LfuCache<K, V> {

    private final int capacity;
    private int minFrequency;
    private final Map<K, V> keyToVal;
    private final Map<K, Integer> keyToCount;
    private final Map<Integer, LinkedHashSet<K>> countToKeys;
    private final ReentrantLock lock = new ReentrantLock();

    public LfuCache(int capacity) {
        this.capacity = capacity;
        this.minFrequency = 0;
        this.keyToVal = new HashMap<>();
        this.keyToCount = new HashMap<>();
        this.countToKeys = new HashMap<>();
    }

    public V get(K key) {
        lock.lock();
        try {
            if (!keyToVal.containsKey(key)) {
                return null;
            }
            incrementFrequency(key);
            return keyToVal.get(key);
        } finally {
            lock.unlock();
        }
    }

    public void put(K key, V value) {
        if (capacity <= 0) {
            return;
        }

        lock.lock();
        try {
            if (keyToVal.containsKey(key)) {
                keyToVal.put(key, value);
                incrementFrequency(key);
                return;
            }

            if (keyToVal.size() >= capacity) {
                evictMinFrequency();
            }

            keyToVal.put(key, value);
            keyToCount.put(key, 1);
            minFrequency = 1;
            countToKeys.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key);
        } finally {
            lock.unlock();
        }
    }

    private void incrementFrequency(K key) {
        int count = keyToCount.get(key);
        keyToCount.put(key, count + 1);

        LinkedHashSet<K> currentKeys = countToKeys.get(count);
        currentKeys.remove(key);
        if (currentKeys.isEmpty()) {
            countToKeys.remove(count);
            if (minFrequency == count) {
                minFrequency++;
            }
        }

        countToKeys.computeIfAbsent(count + 1, k -> new LinkedHashSet<>()).add(key);
    }

    private void evictMinFrequency() {
        LinkedHashSet<K> keys = countToKeys.get(minFrequency);
        if (keys != null && !keys.isEmpty()) {
            K evictKey = keys.iterator().next();
            keys.remove(evictKey);
            if (keys.isEmpty()) {
                countToKeys.remove(minFrequency);
            }
            keyToVal.remove(evictKey);
            keyToCount.remove(evictKey);
        }
    }

    public int size() {
        lock.lock();
        try {
            return keyToVal.size();
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            keyToVal.clear();
            keyToCount.clear();
            countToKeys.clear();
            minFrequency = 0;
        } finally {
            lock.unlock();
        }
    }
}
