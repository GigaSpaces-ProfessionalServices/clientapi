package com.gigaspaces.demo;


import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Restarts the Docker environment to provide a clean slate between different PU deployments.
 * This is needed because GigaSpaces undeploy can get stuck, making it difficult to redeploy
 * a different PU with the same name.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DockerRestartTest {

    private static final Logger logger = LoggerFactory.getLogger(DockerRestartTest.class);

    @Test
    @Order(1)
    void stopEnvironment() {
        logger.info("Stopping Docker environment for clean restart...");
        DockerTestEnv.getInstance().stop();
        assertTrue(!DockerTestEnv.getInstance().isStarted(), "Environment should be stopped");
        logger.info("Docker environment stopped");
    }

    @Test
    @Order(2)
    void startEnvironment() {
        logger.info("Starting fresh Docker environment...");
        DockerTestEnv.getInstance().start();
        assertTrue(DockerTestEnv.getInstance().isStarted(), "Environment should be started");
        logger.info("Docker environment started");
    }

    @Test
    @Order(3)
    void waitForManagerReady() {
        logger.info("Waiting for XAP Manager to be ready...");
        String url = DockerTestEnv.getInstance().getManagerBaseUrl() + "/v2/info";

        int statusCode = waitForManagerReady(url, Duration.ofMinutes(5));

        assertTrue((statusCode == 200 || statusCode == 400),
                String.format("REST call to %s did not return statusCode of 200 or 400", url));
        /*
        RestClient.get(url)
                .retryUntilStatusIn(200, 401)
                .maxAttempts(60)
                .delayMs(2000)
                .execute()
                .assertStatusIn(200, 401);
        */
        logger.info("XAP Manager is ready");
    }

    private int waitForManagerReady(String url, Duration timeout) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeout.toMillis();

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                int statusCode = connection.getResponseCode();
                connection.disconnect();
                if (statusCode == 200 || statusCode == 401) {
                    return statusCode; // Manager is ready
                }
            } catch (Exception e) {
                // Manager not ready yet, continue waiting
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for manager", e);
            }
        }
        throw new RuntimeException("Timed out waiting for XAP Manager to be ready at " + url);
    }
}
