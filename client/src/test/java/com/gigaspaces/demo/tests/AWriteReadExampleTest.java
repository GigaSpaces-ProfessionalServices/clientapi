package com.gigaspaces.demo.tests;

import com.gigaspaces.demo.client.WriteReadExample;
import com.gigaspaces.demo.common.Data;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openspaces.core.GigaSpace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for WriteReadExample using Data POJO.
 */
@ExtendWith(RemoteProxyExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AWriteReadExampleTest {

    private GigaSpace gigaSpace;
    private WriteReadExample writeReadExample;
    private static final int NUM_RECORDS = 10;

    @BeforeAll
    void beforeAll() {
        writeReadExample = new WriteReadExample();
        writeReadExample.setGigaSpace(gigaSpace);
    }

    @Test
    @Order(1)
    void writeObjects() {
        writeReadExample.writeObjects(NUM_RECORDS);

        // Verify all records were written
        int count = gigaSpace.count(new Data());
        assertEquals(NUM_RECORDS, count, "Should have written " + NUM_RECORDS + " objects");
    }

    @Test
    @Order(2)
    void queryWithReadMultiple() {
        Data[] results = writeReadExample.queryWithReadMultiple();

        assertNotNull(results, "Results should not be null");
        assertEquals(NUM_RECORDS, results.length, "Should return " + NUM_RECORDS + " objects");

        for (Data result : results) {
            assertNotNull(result.getId(), "ID should not be null");
            assertNotNull(result.getMessage(), "Message should not be null");
            assertEquals(Boolean.FALSE, result.getProcessed(), "Processed should be false");
        }
    }
}
