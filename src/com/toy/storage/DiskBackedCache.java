package com.toy.storage;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * DiskBackedCache provides a persistent key-value store with an in-memory index
 * and append-only log file on disk.
 */
public class DiskBackedCache implements Closeable {
    private final Path dataDir;
    private final Path dataFile;
    private final Path indexFile;
    private final Map<String, EntryMeta> index = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private RandomAccessFile raf;
    private FileChannel channel;
    private volatile boolean closed = false;

    public static class EntryMeta {
        final long offset;
        final int length;
        final long timestamp;

        public EntryMeta(long offset, int length, long timestamp) {
            this.offset = offset;
            this.length = length;
            this.timestamp = timestamp;
        }
    }

    public DiskBackedCache(String directory) throws IOException {
        this.dataDir = Paths.get(directory);
        Files.createDirectories(dataDir);
        this.dataFile = dataDir.resolve("cache.data");
        this.indexFile = dataDir.resolve("cache.idx");
        this.raf = new RandomAccessFile(dataFile.toFile(), "rw");
        this.channel = raf.getChannel();
        loadIndex();
    }

    private void loadIndex() throws IOException {
        if (!Files.exists(indexFile)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(indexFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(":");
                if (parts.length == 4) {
                    String key = parts[0];
                    long offset = Long.parseLong(parts[1]);
                    int len = Integer.parseInt(parts[2]);
                    long ts = Long.parseLong(parts[3]);
                    index.put(key, new EntryMeta(offset, len, ts));
                }
            }
        }
    }

    private void persistIndex() throws IOException {
        Path tempFile = dataDir.resolve("cache.idx.tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Map.Entry<String, EntryMeta> entry : index.entrySet()) {
                EntryMeta meta = entry.getValue();
                writer.write(String.format("%s:%d:%d:%d%n", entry.getKey(), meta.offset, meta.length, meta.timestamp));
            }
        }
        Files.move(tempFile, indexFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public void put(String key, byte[] data) throws IOException {
        ensureOpen();
        rwLock.writeLock().lock();
        try {
            long offset = channel.size();
            channel.position(offset);
            ByteBuffer buf = ByteBuffer.wrap(data);
            while (buf.hasRemaining()) {
                channel.write(buf);
            }
            index.put(key, new EntryMeta(offset, data.length, System.currentTimeMillis()));
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public byte[] get(String key) throws IOException {
        ensureOpen();
        rwLock.readLock().lock();
        try {
            EntryMeta meta = index.get(key);
            if (meta == null) {
                return null;
            }
            ByteBuffer buf = ByteBuffer.allocate(meta.length);
            channel.position(meta.offset);
            while (buf.hasRemaining()) {
                int read = channel.read(buf);
                if (read < 0) break;
            }
            return buf.array();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public boolean contains(String key) {
        return index.containsKey(key);
    }

    public void sync() throws IOException {
        ensureOpen();
        rwLock.writeLock().lock();
        try {
            channel.force(true);
            persistIndex();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public int size() {
        return index.size();
    }

    public Set<String> keySet() {
        return Collections.unmodifiableSet(index.keySet());
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("DiskBackedCache has already been closed");
        }
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        rwLock.writeLock().lock();
        try {
            closed = true;
            sync();
            channel.close();
            raf.close();
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
