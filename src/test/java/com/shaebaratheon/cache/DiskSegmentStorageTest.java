package com.shaebaratheon.cache;

import com.shaebaratheon.cache.storage.DiskSegmentStorage;
import org.junit.Assert;
import org.junit.Test;
import java.io.File;

public class DiskSegmentStorageTest {
    @Test
    public void testDiskSegmentPersistence() throws Exception {
        File temp = File.createTempFile("disk_segment", ".dat");
        temp.deleteOnExit();

        DiskSegmentStorage<String, String> diskStorage = new DiskSegmentStorage<>(temp, 1024 * 1024);
        diskStorage.write("doc:1", "Persistent payload");
        Assert.assertEquals("Persistent payload", diskStorage.read("doc:1").orElse(null));
        diskStorage.close();
    }
}
