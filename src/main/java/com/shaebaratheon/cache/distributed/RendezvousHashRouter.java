package com.shaebaratheon.cache.distributed;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Highest Random Weight (HRW) / Rendezvous Hashing Consistent Router for Distributed Caching.
 */
public class RendezvousHashRouter<N> {
    private final Set<N> clusterNodes = new HashSet<>();

    public RendezvousHashRouter(Collection<N> initialNodes) {
        if (initialNodes != null) {
            clusterNodes.addAll(initialNodes);
        }
    }

    public synchronized void addNode(N node) {
        clusterNodes.add(node);
    }

    public synchronized void removeNode(N node) {
        clusterNodes.remove(node);
    }

    public synchronized Optional<N> route(String key) {
        if (clusterNodes.isEmpty()) {
            return Optional.empty();
        }

        N bestNode = null;
        long maxHash = Long.MIN_VALUE;

        for (N node : clusterNodes) {
            long hash = computeHash(key, node.toString());
            if (hash > maxHash) {
                maxHash = hash;
                bestNode = node;
            }
        }
        return Optional.ofNullable(bestNode);
    }

    private long computeHash(String key, String nodeStr) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest((key + "#" + nodeStr).getBytes(StandardCharsets.UTF_8));
            return ((long) (bytes[0] & 0xFF) << 56) |
                   ((long) (bytes[1] & 0xFF) << 48) |
                   ((long) (bytes[2] & 0xFF) << 40) |
                   ((long) (bytes[3] & 0xFF) << 32) |
                   ((long) (bytes[4] & 0xFF) << 24) |
                   ((long) (bytes[5] & 0xFF) << 16) |
                   ((long) (bytes[6] & 0xFF) << 8)  |
                   ((long) (bytes[7] & 0xFF));
        } catch (NoSuchAlgorithmException e) {
            return (key + nodeStr).hashCode();
        }
    }
}
