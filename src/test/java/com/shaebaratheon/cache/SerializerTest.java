package com.shaebaratheon.cache;

import com.shaebaratheon.cache.serialize.ProtostuffSerializer;
import org.junit.Assert;
import org.junit.Test;
import java.nio.ByteBuffer;

public class SerializerTest {
    @Test
    public void testVarintAndStringSerialization() {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        ProtostuffSerializer.writeVarint(buf, 1048576L);
        ProtostuffSerializer.writeString(buf, "High Performance Cache Entry");

        buf.flip();
        Assert.assertEquals(1048576L, ProtostuffSerializer.readVarint(buf));
        Assert.assertEquals("High Performance Cache Entry", ProtostuffSerializer.readString(buf));
    }
}
