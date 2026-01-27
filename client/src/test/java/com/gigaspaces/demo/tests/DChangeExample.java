package com.gigaspaces.demo.tests;

import com.gigaspaces.demo.client.ChangeExample;
import com.gigaspaces.demo.common.Data;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openspaces.core.GigaSpace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for ChangeExample.
 * Tests aggregation operations: max, min on space data.
 */
@ExtendWith(RemoteProxyExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DChangeExample {

    private static final int NUM_RECORDS = 10;

    private GigaSpace gigaSpace;
    private ChangeExample changeExample;

    @BeforeAll
    void beforeAll() {
        changeExample = new ChangeExample();
        changeExample.setGigaSpace(gigaSpace);
    }

    @Test
    @Order(1)
    void writeObjects() {
        changeExample.writeObjects(NUM_RECORDS);

        int count = gigaSpace.count(new Data());
        assertEquals(NUM_RECORDS, count, "Should have written " + NUM_RECORDS + " objects");
    }

    @Test
    @Order(2)
    void doChange() {
        Integer numberOfChangedEntries = changeExample.doChange();

        assertNotNull(numberOfChangedEntries, "Number of changed entries should not be null");
        assertEquals(NUM_RECORDS , numberOfChangedEntries, "Number of changed entries should be " + (NUM_RECORDS));
    }

}
