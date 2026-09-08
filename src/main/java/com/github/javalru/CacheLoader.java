package com.github.javalru;

@FunctionalInterface
public interface CacheLoader<K, V> {
    V load(K key) throws Exception;
}
