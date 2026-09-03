package com.shaebaratheon.cache.storage;

import java.util.Optional;

public interface CacheStorage<K, V> {
    void write(K key, V value);
    Optional<V> read(K key);
    boolean delete(K key);
    void close();
}
