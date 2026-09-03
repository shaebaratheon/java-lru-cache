package com.shaebaratheon.cache.snapshot;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Background Asynchronous Snapshot Checkpointer for Cache Cold Recovery.
 */
public class SnapshotManager<K, V> {
    private final File snapshotDir;

    public SnapshotManager(File snapshotDir) {
        this.snapshotDir = snapshotDir;
        if (!snapshotDir.exists()) snapshotDir.mkdirs();
    }

    public synchronized void createSnapshot(String snapshotId, Map<K, V> dataset) throws IOException {
        File target = new File(snapshotDir, "snapshot_" + snapshotId + ".bin");
        File temp = new File(snapshotDir, "snapshot_" + snapshotId + ".tmp");

        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(temp)))) {
            oos.writeInt(dataset.size());
            for (Map.Entry<K, V> entry : dataset.entrySet()) {
                oos.writeObject(entry.getKey());
                oos.writeObject(entry.getValue());
            }
            oos.flush();
        }
        if (!temp.renameTo(target)) {
            throw new IOException("Failed to finalize snapshot rename: " + target);
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized Map<K, V> loadSnapshot(String snapshotId) throws IOException, ClassNotFoundException {
        File target = new File(snapshotDir, "snapshot_" + snapshotId + ".bin");
        if (!target.exists()) return new ConcurrentHashMap<>();

        Map<K, V> result = new ConcurrentHashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(target)))) {
            int count = ois.readInt();
            for (int i = 0; i < count; i++) {
                K k = (K) ois.readObject();
                V v = (V) ois.readObject();
                result.put(k, v);
            }
        }
        return result;
    }
}
