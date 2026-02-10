package com.gigaspaces.demo;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

import com.gigaspaces.demo.tests.*;

/**
 * Test suite that runs integration tests in the correct order:
 * 1. ManagerReadyTest - starts the XAP server container
 * 2. PuDeploymentTest - deploys the space module's PU
 * 3. A-G tests - client API examples against the demo space
 * 4. DockerRestartTest - restarts Docker for clean slate (undeploy can get stuck)
 * 5. EventPuDeploymentTest - deploys the event module's PU (with polling container)
 * 6. HEventListenerExample - event listener tests against event PU
 * 7. CleanupTest - stops the environment
 *
 * Run with: mvn test -Dtest=IntegrationTestSuite
 */
@Suite
@SuiteDisplayName("GS Client API Integration Test Suite")
@SelectClasses({
    Setup.class,
    ManagerReadyTest.class,
    PuDeploymentTest.class,
    AWriteReadExampleTest.class,
    BDistributedTaskExample.class,
    CAggregatorExample.class,
    DChangeExample.class,
    ELocalViewExample.class,
    FJdbcV3Example.class,
    GCustomAggregatorExample.class,
    //DockerRestartTest.class,
    //EventPuDeploymentTest.class,
    //HEventListenerExample.class,
    CleanupTest.class
})
public class IntegrationTestSuite {
}
