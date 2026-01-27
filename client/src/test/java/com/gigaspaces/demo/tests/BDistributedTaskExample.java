package com.gigaspaces.demo.tests;

import com.gigaspaces.demo.client.DistributedTaskExample;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openspaces.core.GigaSpace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for DistributedTaskExample.
 * Executes a distributed task that returns the number of primary partitions.
 */
@ExtendWith(RemoteProxyExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BDistributedTaskExample {

    private GigaSpace gigaSpace;
    private DistributedTaskExample distributedTaskExample;

    @BeforeAll
    void beforeAll() {
        distributedTaskExample = new DistributedTaskExample();
        distributedTaskExample.setGigaSpace(gigaSpace);
    }

    @Test
    void runDistributedTask() throws Exception {
        long result = distributedTaskExample.runDistributedTask();

        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result, "Result should equal number of primary partitions");
    }
}
