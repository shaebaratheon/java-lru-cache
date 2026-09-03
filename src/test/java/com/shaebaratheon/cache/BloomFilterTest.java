package com.shaebaratheon.cache;

import com.shaebaratheon.cache.bloom.BloomFilter;
import org.junit.Assert;
import org.junit.Test;

public class BloomFilterTest {
    @Test
    public void testBloomFilterContainment() {
        BloomFilter<String> filter = new BloomFilter<>(100, 0.01);
        filter.add("user_100");
        filter.add("user_200");

        Assert.assertTrue(filter.mightContain("user_100"));
        Assert.assertTrue(filter.mightContain("user_200"));
        Assert.assertFalse(filter.mightContain("user_9999"));
    }
}
