package com.gigaspaces.demo;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

import com.gigaspaces.demo.tests.*;

/**
 * Test suite that runs integration tests in the correct order:
 * 1. ManagerReadyTest - starts the XAP server container
 * 2. PuDeploymentTest - deploys and verifies the "demo" space
 * 3. AWriteReadExampleTest - connects to space and performs basic operations
 * 4. BDistributedTaskExample - executes a distributed task
 * 5. CleanupTest - undeploys PU and stops the environment
 *
 * Run with: mvn test -Dtest=IntegrationTestSuite
 */
@Suite
@SuiteDisplayName("XAP Integration Test Suite")
@SelectClasses({
    ManagerReadyTest.class,
    PuDeploymentTest.class,
    AWriteReadExampleTest.class,
    BDistributedTaskExample.class,
    CAggregatorExample.class,
    DChangeExample.class,
    ELocalViewExample.class,
    CleanupTest.class
})
class IntegrationTestSuite {
}
