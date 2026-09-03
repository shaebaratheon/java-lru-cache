package com.shaebaratheon.cache.storage;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

/**
 * High-performance L3 Persistent Disk Segment Storage via Memory-Mapped Files (mmap).
 */
public class DiskSegmentStorage<K, V> implements CacheStorage<K, V> {
    private final File segmentFile;
    private final RandomAccessFile raf;
    private final FileChannel channel;
    private final MappedByteBuffer mmapBuffer;
    private final Map<K, Long> indexTable = new ConcurrentHashMap<>();
    private final Map<K, Integer> lengthTable = new ConcurrentHashMap<>();
    private long writePointer = 0;

    public DiskSegmentStorage(File segmentFile, long segmentSizeBytes) throws IOException {
        this.segmentFile = segmentFile;
        this.raf = new RandomAccessFile(segmentFile, "rw");
        this.channel = raf.getChannel();
        this.mmapBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, segmentSizeBytes);
    }

    @Override
    public synchronized void write(K key, V value) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.flush();
            byte[] payload = baos.toByteArray();

            CRC32 crc = new CRC32();
            crc.update(payload);
            long checksum = crc.getValue();

            int totalSize = 8 + 4 + payload.length; // CRC (8B) + len (4B) + payload
            if (writePointer + totalSize > mmapBuffer.capacity()) {
                writePointer = 0; // Wrap around
            }

            long offset = writePointer;
            mmapBuffer.position((int) offset);
            mmapBuffer.putLong(checksum);
            mmapBuffer.putInt(payload.length);
            mmapBuffer.put(payload);

            indexTable.put(key, offset);
            lengthTable.put(key, payload.length);
            writePointer += totalSize;
        } catch (IOException e) {
            throw new RuntimeException("Disk segment write error", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized Optional<V> read(K key) {
        Long offset = indexTable.get(key);
        Integer length = lengthTable.get(key);
        if (offset == null || length == null) {
            return Optional.empty();
        }

        mmapBuffer.position((int) offset.longValue());
        long checksum = mmapBuffer.getLong();
        int payloadLen = mmapBuffer.getInt();
        byte[] payload = new byte[payloadLen];
        mmapBuffer.get(payload);

        CRC32 crc = new CRC32();
        crc.update(payload);
        if (crc.getValue() != checksum) {
            return Optional.empty(); // Corrupted segment
        }

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(payload);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return Optional.of((V) ois.readObject());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean delete(K key) {
        lengthTable.remove(key);
        return indexTable.remove(key) != null;
    }

    @Override
    public void close() {
        try {
            channel.close();
            raf.close();
        } catch (IOException ignored) {}
    }
}
