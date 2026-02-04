package com.gigaspaces.demo;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

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
    void undeployPu() throws Exception {
        String baseUrl = DockerTestEnv.getInstance().getManagerBaseUrl();
        String undeployUrl = baseUrl + "/v2/pus/" + DockerTestEnv.PU_NAME;

        HttpResponse response = deleteWithRetry(undeployUrl, 10, 2000);

        assertTrue(response.statusCode == 200 || response.statusCode == 202 || response.statusCode == 404,
                "PU undeploy should succeed or PU already gone (got status: " + response.statusCode + ", body: " + response.body + ")");
    }

    @Test
    @Order(2)
    void stopEnvironment() {
        DockerTestEnv.getInstance().stop();
        assertTrue(!DockerTestEnv.getInstance().isStarted(), "Environment should be stopped");
    }

    private HttpResponse deleteWithRetry(String urlString, int maxRetries, int delayMs) throws Exception {
        Exception lastException = null;
        for (int i = 0; i < maxRetries; i++) {
            try {
                return delete(urlString);
            } catch (Exception e) {
                lastException = e;
                Thread.sleep(delayMs);
            }
        }
        throw new RuntimeException("Failed to DELETE after " + maxRetries + " retries", lastException);
    }

    private HttpResponse delete(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Accept", "text/plain");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(60000);

        int statusCode = connection.getResponseCode();
        String body = readResponseBody(connection);
        connection.disconnect();

        return new HttpResponse(statusCode, body);
    }

    private String readResponseBody(HttpURLConnection connection) {
        try {
            BufferedReader reader;
            if (connection.getResponseCode() >= 400) {
                if (connection.getErrorStream() == null) {
                    return "";
                }
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
            } else {
                if (connection.getInputStream() == null) {
                    return "";
                }
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            }
            String body = reader.lines().collect(Collectors.joining("\n"));
            reader.close();
            return body;
        } catch (Exception e) {
            return "";
        }
    }

    private static class HttpResponse {
        final int statusCode;
        final String body;

        HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }
}
