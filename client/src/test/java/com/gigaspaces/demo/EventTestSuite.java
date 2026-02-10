package com.gigaspaces.demo;

import com.gigaspaces.demo.tests.*;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite that runs integration tests in the correct order:
 * 1. ManagerReadyTest - starts the XAP server container
 * 2. EventPuDeploymentTest - deploys the event module's PU (with polling container)
 * 3. HEventListenerExample - event listener tests against event PU
 * 4. CleanupTest - stops the environment
 *
 * Run with: mvn test -Dtest=EventTestSuite
 */
@Suite
@SuiteDisplayName("GS Event Integration Test Suite")
@SelectClasses({
    Setup.class,
    ManagerReadyTest.class,
    EventPuDeploymentTest.class,
    HEventListenerExample.class,
    CleanupTest.class
})
public class EventTestSuite {
}
