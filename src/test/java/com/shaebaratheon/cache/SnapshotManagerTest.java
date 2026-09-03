package com.shaebaratheon.cache;

import com.shaebaratheon.cache.snapshot.SnapshotManager;
import org.junit.Assert;
import org.junit.Test;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SnapshotManagerTest {
    @Test
    public void testSnapshotSerializationRoundtrip() throws Exception {
        File dir = new File("/tmp/cache_snap_test");
        SnapshotManager<String, Integer> manager = new SnapshotManager<>(dir);

        Map<String, Integer> data = new HashMap<>();
        data.put("a", 100);
        data.put("b", 200);

        manager.createSnapshot("v1", data);
        Map<String, Integer> loaded = manager.loadSnapshot("v1");

        Assert.assertEquals(2, loaded.size());
        Assert.assertEquals(Integer.valueOf(100), loaded.get("a"));
    }
}
