package com.toy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread-safe LRU cache implementation using a Doubly Linked List and HashMap.
 */
public class LruCache<K, V> {

    private final int capacity;
    private final Map<K, Node> map;
    private final Node head;
    private final Node tail;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private class Node {
        K key;
        V value;
        Node prev;
        Node next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public LruCache(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>();
        this.head = new Node(null, null);
        this.tail = new Node(null, null);
        head.next = tail;
        tail.prev = head;
    }

    public V get(K key) {
        lock.readLock().lock();
        try {
            if (!map.containsKey(key)) {
                return null;
            }
        } finally {
            lock.readLock().unlock();
        }

        // We need to write lock to move node to front
        lock.writeLock().lock();
        try {
            Node node = map.get(key);
            if (node != null) { // Double check
                remove(node);
                addFirst(node);
                return node.value;
            }
            return null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            if (map.containsKey(key)) {
                Node node = map.get(key);
                node.value = value;
                remove(node);
                addFirst(node);
            } else {
                if (map.size() >= capacity) {
                    map.remove(tail.prev.key);
                    remove(tail.prev);
                }
                Node newNode = new Node(key, value);
                map.put(key, newNode);
                addFirst(newNode);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

        public int size() {
        lock.readLock().lock();
        try {
            return map.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            map.clear();
            head.next = tail;
            tail.prev = head;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void addFirst(Node node) {
        node.next = head.next;
        node.next.prev = node;
        node.prev = head;
        head.next = node;
    }

    private void remove(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
}
