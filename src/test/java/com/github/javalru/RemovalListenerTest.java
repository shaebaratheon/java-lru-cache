package com.github.javalru;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class RemovalListenerTest {
    @Test
    public void testRemovalNotification() {
        List<String> logs = new ArrayList<>();
        RemovalListener<String, String> listener = (k, v, cause) -> logs.add(k + ":" + cause);

        listener.onRemoval("user_1", "Alice", RemovalCause.SIZE);
        assertEquals(1, logs.size());
        assertEquals("user_1:SIZE", logs.get(0));
    }
}
