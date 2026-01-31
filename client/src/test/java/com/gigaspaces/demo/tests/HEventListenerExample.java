package com.gigaspaces.demo.tests;


import com.gigaspaces.demo.client.WriteReadExample;
import com.gigaspaces.demo.common.Data;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openspaces.core.GigaSpace;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration test for testing PollingContainer using Data POJO.
 */
@ExtendWith(RemoteProxyExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HEventListenerExample {
    private GigaSpace gigaSpace;
    private static final int NUM_RECORDS = 10;

    // Need the writeObjects method from this class
    private WriteReadExample writeReadExample;

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
    void verifyPollingContainer() {
        Data data = new Data();
        data.setId(1);
        Data dataRead = gigaSpace.read(data);
        assertTrue("Processed should be true in the Data object read", dataRead.getProcessed());
    }

}
