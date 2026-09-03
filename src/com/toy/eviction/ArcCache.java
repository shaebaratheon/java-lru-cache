package com.toy.eviction;

import java.util.*;

/**
 * ArcCache implements the Adaptive Replacement Cache (ARC) algorithm.
 * ARC dynamically balances between Recency and Frequency depending on workload.
 *
 * It manages four lists:
 * - T1: Recent cache entries
 * - T2: Frequent cache entries
 * - B1: Ghost history for recency (keys evicted from T1)
 * - B2: Ghost history for frequency (keys evicted from T2)
 */
public class ArcCache<K, V> {
    private final int capacity;
    private int p = 0; // Target size for T1

    private final Map<K, V> t1 = new LinkedHashMap<>(16, 0.75f, true);
    private final Map<K, V> t2 = new LinkedHashMap<>(16, 0.75f, true);
    private final Set<K> b1 = Collections.newSetFromMap(new LinkedHashMap<K, Boolean>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, Boolean> eldest) {
            return size() > capacity;
        }
    });
    private final Set<K> b2 = Collections.newSetFromMap(new LinkedHashMap<K, Boolean>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, Boolean> eldest) {
            return size() > 2 * capacity;
        }
    });

    public ArcCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
    }

    public synchronized V get(K key) {
        if (t1.containsKey(key)) {
            V val = t1.remove(key);
            t2.put(key, val);
            return val;
        }
        if (t2.containsKey(key)) {
            return t2.get(key);
        }
        return null;
    }

    public synchronized void put(K key, V value) {
        // Case 1: key in T1 or T2
        if (t1.containsKey(key)) {
            t1.remove(key);
            t2.put(key, value);
            return;
        }
        if (t2.containsKey(key)) {
            t2.put(key, value);
            return;
        }

        // Case 2: key in B1
        if (b1.contains(key)) {
            int delta = Math.max(1, b2.size() / Math.max(1, b1.size()));
            p = Math.min(capacity, p + delta);
            replace(key);
            b1.remove(key);
            t2.put(key, value);
            return;
        }

        // Case 3: key in B2
        if (b2.contains(key)) {
            int delta = Math.max(1, b1.size() / Math.max(1, b2.size()));
            p = Math.max(0, p - delta);
            replace(key);
            b2.remove(key);
            t2.put(key, value);
            return;
        }

        // Case 4: key not in cache or history
        int l1Size = t1.size() + b1.size();
        if (l1Size == capacity) {
            if (t1.size() < capacity) {
                removeOldest(b1);
                replace(key);
            } else {
                K oldestT1 = getOldestKey(t1);
                if (oldestT1 != null) {
                    t1.remove(oldestT1);
                }
            }
        } else if (l1Size < capacity) {
            int total = t1.size() + t2.size() + b1.size() + b2.size();
            if (total >= capacity) {
                if (total == 2 * capacity) {
                    removeOldest(b2);
                }
                replace(key);
            }
        }

        t1.put(key, value);
    }

    private void replace(K key) {
        boolean inB2 = b2.contains(key);
        if (!t1.isEmpty() && ((t1.size() > p) || (inB2 && t1.size() == p))) {
            K victim = getOldestKey(t1);
            if (victim != null) {
                t1.remove(victim);
                b1.add(victim);
            }
        } else if (!t2.isEmpty()) {
            K victim = getOldestKey(t2);
            if (victim != null) {
                t2.remove(victim);
                b2.add(victim);
            }
        }
    }

    private K getOldestKey(Map<K, V> map) {
        Iterator<K> it = map.keySet().iterator();
        return it.hasNext() ? it.next() : null;
    }

    private void removeOldest(Set<K> set) {
        Iterator<K> it = set.iterator();
        if (it.hasNext()) {
            it.next();
            it.remove();
        }
    }

    public synchronized int size() {
        return t1.size() + t2.size();
    }

    public synchronized int getP() {
        return p;
    }

    public synchronized void clear() {
        t1.clear();
        t2.clear();
        b1.clear();
        b2.clear();
        p = 0;
    }
}
