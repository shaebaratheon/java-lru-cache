package com.shaebaratheon.cache.loader;

import com.shaebaratheon.cache.MultiLevelCache;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Self-Populating Loading Cache with Single-Flight Thundering Herd Prevention.
 */
public class LoadingCache<K, V> {
    private final MultiLevelCache<K, V> underlyingCache;
    private final Function<K, V> loader;
    private final ConcurrentHashMap<K, CompletableFuture<V>> inFlightCalls = new ConcurrentHashMap<>();

    public LoadingCache(MultiLevelCache<K, V> underlyingCache, Function<K, V> loader) {
        this.underlyingCache = underlyingCache;
        this.loader = loader;
    }

    public V get(K key) throws ExecutionException, InterruptedException {
        return underlyingCache.get(key).orElseGet(() -> {
            CompletableFuture<V> future = inFlightCalls.computeIfAbsent(key, k -> CompletableFuture.supplyAsync(() -> {
                V loaded = loader.apply(k);
                underlyingCache.put(k, loaded);
                return loaded;
            }));

            try {
                return future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                inFlightCalls.remove(key);
            }
        });
    }
}
