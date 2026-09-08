package com.github.javalru;

@FunctionalInterface
public interface Weigher<K, V> {
    int weigh(K key, V value);
}
