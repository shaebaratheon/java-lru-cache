package com.shaebaratheon.cache.bloom;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.BitSet;

/**
 * Probabilistic Bloom Filter for Existence Checking to Avoid Disk/L2 Cache Misses.
 */
public class BloomFilter<K> {
    private final BitSet bitSet;
    private final int bitSetSize;
    private final int numHashFunctions;

    public BloomFilter(int expectedElements, double falsePositiveRate) {
        this.bitSetSize = (int) Math.ceil(-1 * expectedElements * Math.log(falsePositiveRate) / Math.pow(Math.log(2), 2));
        this.numHashFunctions = (int) Math.round((bitSetSize / (double) expectedElements) * Math.log(2));
        this.bitSet = new BitSet(bitSetSize);
    }

    public synchronized void add(K element) {
        long[] hashes = computeHashes(element.toString());
        for (long hash : hashes) {
            bitSet.set((int) (Math.abs(hash) % bitSetSize));
        }
    }

    public synchronized boolean mightContain(K element) {
        long[] hashes = computeHashes(element.toString());
        for (long hash : hashes) {
            if (!bitSet.get((int) (Math.abs(hash) % bitSetSize))) {
                return false;
            }
        }
        return true;
    }

    private long[] computeHashes(String val) {
        long[] res = new long[numHashFunctions];
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(val.getBytes(StandardCharsets.UTF_8));
            long h1 = ((long) digest[0] << 56) | ((long) digest[1] & 0xFF) << 48 | ((long) digest[2] & 0xFF) << 40;
            long h2 = ((long) digest[8] << 56) | ((long) digest[9] & 0xFF) << 48 | ((long) digest[10] & 0xFF) << 40;
            for (int i = 0; i < numHashFunctions; i++) {
                res[i] = h1 + i * h2;
            }
        } catch (Exception e) {
            for (int i = 0; i < numHashFunctions; i++) res[i] = val.hashCode() + i * 31;
        }
        return res;
    }
}
