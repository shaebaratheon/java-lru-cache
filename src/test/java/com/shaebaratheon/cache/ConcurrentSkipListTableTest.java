package com.shaebaratheon.cache;

import com.shaebaratheon.cache.concurrent.ConcurrentSkipListTable;
import org.junit.Assert;
import org.junit.Test;
import java.util.Map;

public class ConcurrentSkipListTableTest {
    @Test
    public void testRangeQueriesAndAtomicSize() {
        ConcurrentSkipListTable<String, Integer> table = new ConcurrentSkipListTable<>();
        for (int i = 0; i < 50; i++) {
            table.put(String.format("item_%03d", i), i);
        }
        Assert.assertEquals(50, table.size());

        Map<String, Integer> sub = table.range("item_010", "item_020");
        Assert.assertEquals(10, sub.size());
        Assert.assertEquals(Integer.valueOf(10), sub.get("item_010"));
    }
}
