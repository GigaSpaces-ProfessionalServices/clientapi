package com.gigaspaces.demo;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Deploys the event module's PU (with polling container) for event listener tests.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventPuDeploymentTest extends PuDeploymentTest {

    @Override
    protected String getStatefulModuleName() {
        return "event";
    }
}
