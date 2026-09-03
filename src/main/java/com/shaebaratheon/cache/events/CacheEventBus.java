package com.shaebaratheon.cache.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CacheEventBus<K, V> {
    public enum EventType { CREATED, UPDATED, EVICTED, REMOVED, EXPIRED }

    public static class CacheEvent<K, V> {
        public final EventType type;
        public final K key;
        public final V value;
        public final long timestamp;

        public CacheEvent(EventType type, K key, V value) {
            this.type = type;
            this.key = key;
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public interface CacheEventListener<K, V> {
        void onEvent(CacheEvent<K, V> event);
    }

    private final List<CacheEventListener<K, V>> listeners = new CopyOnWriteArrayList<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void register(CacheEventListener<K, V> listener) {
        listeners.add(listener);
    }

    public void publish(EventType type, K key, V value) {
        CacheEvent<K, V> event = new CacheEvent<>(type, key, value);
        for (CacheEventListener<K, V> l : listeners) {
            executor.submit(() -> l.onEvent(event));
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
