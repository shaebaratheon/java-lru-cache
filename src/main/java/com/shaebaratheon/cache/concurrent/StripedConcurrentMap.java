package com.shaebaratheon.cache.concurrent;

import java.util.concurrent.locks.ReentrantLock;

public class StripedConcurrentMap<K, V> {
    private final int stripeCount;
    private final ReentrantLock[] locks;

    public StripedConcurrentMap(int stripeCount) {
        this.stripeCount = stripeCount;
        this.locks = new ReentrantLock[stripeCount];
        for (int i = 0; i < stripeCount; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    public ReentrantLock getLock(K key) {
        int hash = Math.abs(key.hashCode()) % stripeCount;
        return locks[hash];
    }
}
