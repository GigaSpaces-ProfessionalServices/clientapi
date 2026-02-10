package com.gigaspaces.demo.tests;

import com.gigaspaces.demo.client.BroadcastWriteReadExample;
import com.gigaspaces.demo.client.WriteReadExample;
import com.gigaspaces.demo.common.BroadcastData;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openspaces.core.GigaSpace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for BroadcastWriteReadExample using Data POJO.
 */
@ExtendWith(RemoteProxyExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IBroadcastWriteReadExampleTest {

    private GigaSpace gigaSpace;
    private BroadcastWriteReadExample broadcastWriteReadExample;
    private static final int NUM_RECORDS = 10;

    @BeforeAll
    void beforeAll() {
        broadcastWriteReadExample = new BroadcastWriteReadExample();
        broadcastWriteReadExample.setGigaSpace(gigaSpace);
    }

    @Test
    @Order(1)
    void writeObjects() {
        broadcastWriteReadExample.writeObjects(NUM_RECORDS);

        // Verify all records were written
        int count = gigaSpace.count(new BroadcastData());
        assertEquals(NUM_RECORDS, count, "Should have written " + NUM_RECORDS + " objects");
    }

    @Test
    @Order(2)
    void queryWithReadMultiple() {
        BroadcastData[] results = broadcastWriteReadExample.queryWithReadMultiple();

        assertNotNull(results, "Results should not be null");
        assertEquals(NUM_RECORDS, results.length, "Should return " + NUM_RECORDS + " objects");

        for (BroadcastData result : results) {
            assertNotNull(result.getId(), "ID should not be null");
            assertNotNull(result.getMessage(), "Message should not be null");
            // polling container added
            //assertEquals(Boolean.FALSE, result.getProcessed(), "Processed should be false");
        }
    }
}
