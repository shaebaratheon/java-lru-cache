package com.shaebaratheon.cache;

import com.shaebaratheon.cache.policy.EvictionPolicy;
import com.shaebaratheon.cache.policy.TinyLFUPolicy;
import com.shaebaratheon.cache.storage.CacheStorage;
import com.shaebaratheon.cache.storage.OffHeapStorage;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Multi-Tiered Cache architecture: L1 Heap, L2 Direct Off-Heap, L3 Disk.
 */
public class MultiLevelCache<K, V> {
    private final int heapCapacity;
    private final ConcurrentHashMap<K, V> l1Heap;
    private final EvictionPolicy<K> evictionPolicy;
    private final CacheStorage<K, V> l2OffHeap;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public MultiLevelCache(int heapCapacity, long offHeapBytes) {
        this.heapCapacity = heapCapacity;
        this.l1Heap = new ConcurrentHashMap<>(heapCapacity);
        this.evictionPolicy = new TinyLFUPolicy<>(heapCapacity);
        this.l2OffHeap = new OffHeapStorage<>(offHeapBytes);
    }

    public Optional<V> get(K key) {
        lock.readLock().lock();
        try {
            V val = l1Heap.get(key);
            if (val != null) {
                evictionPolicy.recordAccess(key);
                return Optional.of(val);
            }
            Optional<V> offHeapVal = l2OffHeap.read(key);
            if (offHeapVal.isPresent()) {
                // Promote back to L1
                promoteToL1(key, offHeapVal.get());
                return offHeapVal;
            }
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            if (l1Heap.size() >= heapCapacity) {
                K victim = evictionPolicy.selectVictim();
                if (victim != null) {
                    V evictedVal = l1Heap.remove(victim);
                    if (evictedVal != null) {
                        l2OffHeap.write(victim, evictedVal);
                    }
                }
            }
            l1Heap.put(key, value);
            evictionPolicy.recordAccess(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void promoteToL1(K key, V value) {
        lock.writeLock().lock();
        try {
            l2OffHeap.delete(key);
            put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int size() {
        return l1Heap.size();
    }
}
