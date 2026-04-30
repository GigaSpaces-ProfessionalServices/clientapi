package com.gigaspaces.demo;

import com.gigaspaces.demo.rest.RestManagerClient;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cleanup test that runs last to tear down resources.
 * This test class should run after all other tests to clean up:
 * 1. Undeploy the PU created in PuDeploymentTest
 * 2. Stop the Docker environment started in ManagerReadyTest
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CleanupTest {

    private static final Logger logger = LoggerFactory.getLogger(CleanupTest.class);

    @Test
    @Order(1)
    void clearProperties() {
        ClientConfigLoader.clearSystemProperties();
    }
    @Test
    @Order(2)
    void undeployPu() throws Exception {
        String baseUrl = DockerTestEnv.getInstance().getManagerBaseUrl();
        String undeployUrl = baseUrl + "/v2/pus/" + DockerTestEnv.PU_NAME;

        RestManagerClient.delete(undeployUrl)
                .retryOnFailure()
                .maxAttempts(10)
                .delayMs(2000)
                .execute()
                .assertStatusIn("PU undeploy should succeed or PU already gone", 200, 202, 404);
/*
        HttpResponse response = deleteWithRetry(undeployUrl, 10, 2000);

        assertTrue(response.statusCode == 200 || response.statusCode == 202 || response.statusCode == 404,
                "PU undeploy should succeed or PU already gone (got status: " + response.statusCode + ", body: " + response.body + ")");

 */
    }

    @Test
    @Order(3)
    void stopEnvironment() {
        DockerTestEnv.getInstance().stop();
        assertTrue(!DockerTestEnv.getInstance().isStarted(), "Environment should be stopped");
    }
}
