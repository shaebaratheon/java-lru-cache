package com.github.javalru;

import org.junit.Test;
import static org.junit.Assert.*;

public class WeigherTest {
    @Test
    public void testByteArrayWeigher() {
        Weigher<String, byte[]> byteWeigher = (k, bytes) -> bytes.length;
        byte[] payload = new byte[1024];
        assertEquals(1024, byteWeigher.weigh("blob", payload));
    }
}
