package com.toy.policy;

import java.util.concurrent.*;
import java.util.*;

/**
 * ExpirationPolicy implements TTL (Time-To-Live) and Idle-Timeout policies
 * with proactive background reaping.
 */
public class ExpirationPolicy<K, V> implements AutoCloseable {
    private final ScheduledExecutorService reaper;
    private final ConcurrentMap<K, ExpirableValue<V>> store = new ConcurrentHashMap<>();
    private final long defaultTtlMillis;

    public static class ExpirableValue<V> {
        private final V value;
        private final long expireAtMillis;
        private volatile long lastAccessMillis;

        public ExpirableValue(V value, long ttlMillis) {
            this.value = value;
            this.lastAccessMillis = System.currentTimeMillis();
            this.expireAtMillis = (ttlMillis > 0) ? (this.lastAccessMillis + ttlMillis) : Long.MAX_VALUE;
        }

        public V getValue() {
            this.lastAccessMillis = System.currentTimeMillis();
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireAtMillis;
        }

        public long getExpireAtMillis() {
            return expireAtMillis;
        }
    }

    public ExpirationPolicy(long defaultTtlMillis, long reapIntervalMillis) {
        this.defaultTtlMillis = defaultTtlMillis;
        this.reaper = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cache-expiration-reaper");
            t.setDaemon(true);
            return t;
        });
        if (reapIntervalMillis > 0) {
            this.reaper.scheduleAtFixedRate(this::evictExpired, reapIntervalMillis, reapIntervalMillis, TimeUnit.MILLISECONDS);
        }
    }

    public void put(K key, V value) {
        put(key, value, defaultTtlMillis);
    }

    public void put(K key, V value, long ttlMillis) {
        store.put(key, new ExpirableValue<>(value, ttlMillis));
    }

    public V get(K key) {
        ExpirableValue<V> entry = store.get(key);
        if (entry == null) return null;
        if (entry.isExpired()) {
            store.remove(key, entry);
            return null;
        }
        return entry.getValue();
    }

    public int evictExpired() {
        int evicted = 0;
        for (Map.Entry<K, ExpirableValue<V>> e : store.entrySet()) {
            if (e.getValue().isExpired()) {
                if (store.remove(e.getKey(), e.getValue())) {
                    evicted++;
                }
            }
        }
        return evicted;
    }

    public int size() {
        return store.size();
    }

    @Override
    public void close() {
        reaper.shutdownNow();
    }
}
