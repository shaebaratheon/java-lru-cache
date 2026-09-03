package com.toy.eviction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 2Q (Two-Queue) Simplified Eviction Cache.
 * Protects against full scans by segregating first-time hits into a FIFO probationary queue
 * and multi-hit items into a main LRU queue.
 */
public class TwoQueueCache<K, V> {

    private final int capacity;
    private final int probationCapacity;
    private final Map<K, V> probationMap;
    private final LinkedList<K> probationQueue;
    private final Map<K, V> warmMap;
    private final LinkedList<K> warmQueue;
    private final ReentrantLock lock = new ReentrantLock();

    public TwoQueueCache(int capacity) {
        this.capacity = capacity;
        this.probationCapacity = Math.max(2, capacity / 2);
        this.probationMap = new HashMap<>();
        this.probationQueue = new LinkedList<>();
        this.warmMap = new HashMap<>();
        this.warmQueue = new LinkedList<>();
    }

    public V get(K key) {
        lock.lock();
        try {
            if (warmMap.containsKey(key)) {
                warmQueue.remove(key);
                warmQueue.addFirst(key);
                return warmMap.get(key);
            }

            if (probationMap.containsKey(key)) {
                V val = probationMap.remove(key);
                probationQueue.remove(key);
                promoteToWarm(key, val);
                return val;
            }

            return null;
        } finally {
            lock.unlock();
        }
    }

    public void put(K key, V value) {
        lock.lock();
        try {
            if (warmMap.containsKey(key)) {
                warmMap.put(key, value);
                warmQueue.remove(key);
                warmQueue.addFirst(key);
                return;
            }

            if (probationMap.containsKey(key)) {
                probationMap.remove(key);
                probationQueue.remove(key);
                promoteToWarm(key, value);
                return;
            }

            if (probationQueue.size() >= probationCapacity) {
                K evicted = probationQueue.removeLast();
                probationMap.remove(evicted);
            }

            probationQueue.addFirst(key);
            probationMap.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    private void promoteToWarm(K key, V value) {
        int warmCapacity = capacity - probationCapacity;
        if (warmQueue.size() >= warmCapacity) {
            K evicted = warmQueue.removeLast();
            warmMap.remove(evicted);
        }
        warmQueue.addFirst(key);
        warmMap.put(key, value);
    }

    public int size() {
        lock.lock();
        try {
            return probationMap.size() + warmMap.size();
        } finally {
            lock.unlock();
        }
    }
}
