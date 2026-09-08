package com.github.javalru;

import java.util.concurrent.ConcurrentHashMap;

public class LoadingCache<K, V> {
    private final ConcurrentHashMap<K, V> store = new ConcurrentHashMap<>();
    private final CacheLoader<K, V> loader;

    public LoadingCache(CacheLoader<K, V> loader) {
        if (loader == null) throw new IllegalArgumentException("Loader cannot be null");
        this.loader = loader;
    }

    public V get(K key) throws Exception {
        V existing = store.get(key);
        if (existing != null) return existing;
        synchronized (store) {
            if (store.containsKey(key)) return store.get(key);
            V loaded = loader.load(key);
            if (loaded != null) store.put(key, loaded);
            return loaded;
        }
    }

    public int size() {
        return store.size();
    }
}
