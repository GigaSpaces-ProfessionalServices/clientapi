package com.gigaspaces.demo.tests;

import com.gigaspaces.demo.client.LocalViewExample;
import com.gigaspaces.demo.common.Data;
import com.j_spaces.core.client.SQLQuery;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.cache.LocalViewSpaceConfigurer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for ChangeExample.
 * Tests aggregation operations: max, min on space data.
 */
@ExtendWith(RemoteProxyExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ELocalViewExample {

    private static final int NUM_RECORDS = 10;

    private GigaSpace gigaSpace;
    private LocalViewExample localViewExample;

    @BeforeAll
    void beforeAll() {
        localViewExample = new LocalViewExample();
        localViewExample.setGigaSpace(gigaSpace);
    }

    @Test
    @Order(1)
    void populate() {
        localViewExample.populate(NUM_RECORDS);

        int count = gigaSpace.count(new Data());
        assertEquals(NUM_RECORDS, count, "Should have written " + NUM_RECORDS + " objects");
    }

    @Test
    @Order(2)
    void doReadFromLocalView() {

        // seems to be a timing issue when space proxy writes objects after localView is initialized
        // so do it here
        LocalViewSpaceConfigurer localViewConfigurer =
                new LocalViewSpaceConfigurer(gigaSpace.getSpace())
                        .addViewQuery(new SQLQuery<>(Data.class, ""));
        GigaSpace localView = new GigaSpaceConfigurer(localViewConfigurer).gigaSpace();

        localViewExample.setLocalView(localView);

        Object object = localViewExample.readFromLocalView();

        assertNotNull(object, "Object read from local view should not be null");
    }

}
