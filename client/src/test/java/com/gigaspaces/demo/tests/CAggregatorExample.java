package com.gigaspaces.demo.tests;

import com.gigaspaces.demo.client.AggregatorExample;
import com.gigaspaces.demo.common.MyData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openspaces.core.GigaSpace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for AggregatorExample.
 * Tests aggregation operations: max, min on space data.
 */
@ExtendWith(RemoteProxyExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CAggregatorExample {

    private static final int NUM_RECORDS = 10;

    private GigaSpace gigaSpace;
    private AggregatorExample aggregatorExample;

    @BeforeAll
    void beforeAll() {
        aggregatorExample = new AggregatorExample();
        aggregatorExample.setGigaSpace(gigaSpace);
    }

    @Test
    @Order(1)
    void writeObjects() {
        aggregatorExample.writeObjects(NUM_RECORDS);
        int count = gigaSpace.count(new MyData());
        assertEquals(NUM_RECORDS, count, "Should have written " + NUM_RECORDS + " objects");
    }

    @Test
    @Order(2)
    void maxIntProperty() {
        Integer maxId = aggregatorExample.maxIntProperty();

        assertNotNull(maxId, "Max ID should not be null");
        assertEquals(NUM_RECORDS - 1, maxId, "Max ID should be " + (NUM_RECORDS - 1));
    }

    @Test
    @Order(3)
    void maxLongProperty() {
        Long maxValue = aggregatorExample.maxLongProperty();

        assertNotNull(maxValue, "Max value should not be null");
        assertEquals(NUM_RECORDS - 1, maxValue, "Max value should be " + (NUM_RECORDS - 1));
    }

    @Test
    @Order(4)
    void minValue() {
        Integer minId = aggregatorExample.minValue();

        assertNotNull(minId, "Min ID should not be null");
        assertEquals(0, minId, "Min ID should be 0");
    }
}
