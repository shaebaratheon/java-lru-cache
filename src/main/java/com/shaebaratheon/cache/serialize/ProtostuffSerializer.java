package com.shaebaratheon.cache.serialize;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Compact Binary Varint & String Serializer for Cache Entries.
 */
public class ProtostuffSerializer {
    public static void writeVarint(ByteBuffer buffer, long value) {
        while ((value & ~0x7FL) != 0) {
            buffer.put((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        buffer.put((byte) value);
    }

    public static long readVarint(ByteBuffer buffer) {
        long value = 0;
        int shift = 0;
        while (shift < 64) {
            byte b = buffer.get();
            value |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) return value;
            shift += 7;
        }
        throw new IllegalArgumentException("Varint stream corrupted or too long");
    }

    public static void writeString(ByteBuffer buffer, String str) {
        byte[] bytes = str.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        writeVarint(buffer, bytes.length);
        buffer.put(bytes);
    }

    public static String readString(ByteBuffer buffer) {
        int len = (int) readVarint(buffer);
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }
}
