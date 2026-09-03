package com.shaebaratheon.cache.policy;

public interface EvictionPolicy<K> {
    void recordAccess(K key);
    K selectVictim();
    void remove(K key);
}
