package com.gigaspaces.demo;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Setup {
    @BeforeAll
    static void setUp() {
        ClientConfigLoader.printConfigSummary();

        // Set properties BEFORE creating the space proxy (and before DockerTestEnv.start() which may trigger GigaSpaces class loading)
        ClientConfigLoader.setSystemProperties();
        DockerTestEnv.getInstance().start();
    }

    @Test
    @Order(1)
    void testSmartExternalizableSystemProperty() {
        String expected = ClientConfigLoader.getProperty("com.gs.smart-externalizable.enabled");
        String actual = System.getProperty("com.gs.smart-externalizable.enabled");
        assertEquals(expected, actual,
                String.format("Smart externalizable system property should be set: expected=%s, actual=%s", expected, actual));
    }

    @Test
    @Order(2)
    void testLookupLocatorsSystemProperty() {
        String expected = ClientConfigLoader.getProperty("xapManagerHost") + ":" + DockerTestEnv.XAP_LOOKUP_PORT;
        String actual = System.getProperty("com.gs.jini_lus.locators");
        assertEquals(expected, actual,
                String.format("Lookup locators system property should be set: expected=%s, actual=%s", expected, actual));
    }
    @Test
    @Order(2)
    void testLookupGroupsSystemProperty() {
        String expectedLookupGroups = ClientConfigLoader.getProperty("xapLookupGroups");
        String actual = System.getProperty("com.gs.jini_lus.groups");
        assertEquals(expectedLookupGroups, actual, String.format("XapLookupGroups %s and system property com.gs.jini_lus.groups %s should be the same", actual, expectedLookupGroups));
    }
}
