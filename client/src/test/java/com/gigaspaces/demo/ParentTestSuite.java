package com.gigaspaces.demo;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/*
 * Run with: mvn test -Dtest=ParentTestSuite
 * If a test flow needs to make server changes, then it's easier to have separate integration test suites.
 * This is due to problems undeploying the previous space.
 */
@Suite
@SuiteDisplayName("Parent Test Suite Aggregation")
@SelectClasses({
        com.gigaspaces.demo.IntegrationTestSuite.class,
        com.gigaspaces.demo.EventTestSuite.class
        })
public class ParentTestSuite {
}

