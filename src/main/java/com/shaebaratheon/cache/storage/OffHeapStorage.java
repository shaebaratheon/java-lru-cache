package com.shaebaratheon.cache.storage;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Direct ByteBuffer off-heap storage avoiding JVM garbage collection pauses.
 */
public class OffHeapStorage<K, V> implements CacheStorage<K, V> {
    private final ByteBuffer memoryArena;
    private final Map<K, Integer> offsetMap = new ConcurrentHashMap<>();
    private final Map<K, Integer> sizeMap = new ConcurrentHashMap<>();
    private int currentOffset = 0;

    public OffHeapStorage(long capacityBytes) {
        this.memoryArena = ByteBuffer.allocateDirect((int) capacityBytes);
    }

    @Override
    public synchronized void write(K key, V value) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.flush();
            byte[] bytes = baos.toByteArray();

            if (currentOffset + bytes.length > memoryArena.capacity()) {
                currentOffset = 0; // Circular wrap eviction
            }

            int offset = currentOffset;
            memoryArena.position(offset);
            memoryArena.put(bytes);

            offsetMap.put(key, offset);
            sizeMap.put(key, bytes.length);
            currentOffset += bytes.length;
        } catch (IOException e) {
            throw new RuntimeException("Serialization failure", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized Optional<V> read(K key) {
        Integer offset = offsetMap.get(key);
        Integer size = sizeMap.get(key);
        if (offset == null || size == null) {
            return Optional.empty();
        }

        try {
            byte[] bytes = new byte[size];
            memoryArena.position(offset);
            memoryArena.get(bytes);

            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return Optional.of((V) ois.readObject());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean delete(K key) {
        sizeMap.remove(key);
        return offsetMap.remove(key) != null;
    }

    @Override
    public void close() {
        offsetMap.clear();
        sizeMap.clear();
    }
}
