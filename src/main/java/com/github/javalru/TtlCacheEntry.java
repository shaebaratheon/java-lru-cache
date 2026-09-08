package com.github.javalru;

public class TtlCacheEntry<V> {
    private final V value;
    private final long expiresAt;

    public TtlCacheEntry(V value, long ttlMillis) {
        this.value = value;
        this.expiresAt = ttlMillis > 0 ? System.currentTimeMillis() + ttlMillis : Long.MAX_VALUE;
    }

    public V getValue() {
        return value;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiresAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }
}
