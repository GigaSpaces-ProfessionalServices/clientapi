package com.gigaspaces.demo;

import org.junit.jupiter.api.*;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that deploys a "demo" space onto the XAP server.
 * This test should run after ManagerReadyTest has started the environment.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PuDeploymentTest {

    private static final Logger logger = LoggerFactory.getLogger(PuDeploymentTest.class);


    /**
     * Override this method in subclasses to change the module name.
     * Default is "space".
     */
    protected String getStatefulModuleName() {
        return "space";
    }

    /**
     * Override this method in subclasses to change the PU jar name.
     * Default is "demo-pu.jar".
     */
    protected String getPuJarName() {
        return "demo-pu.jar";
    }

    @BeforeAll
    void setUp() {
        // Set properties BEFORE creating the space proxy (and before DockerTestEnv.start() which may trigger GigaSpaces class loading)
        ClientConfigLoader.setSystemProperties();
        // Ensure the shared environment is started
        DockerTestEnv.getInstance().start();
    }

    @AfterAll
    void tearDown() {
        ClientConfigLoader.clearSystemProperties();

    }

    /* depending upon which method was used the pu jar will be in a slightly different relative location
       1. mvn test -pl client; working directory is client module
       2. java org.junit.platform.console.ConsoleLauncher; working directory is project root
     */
    private File findPuJar() {
        String[] searchPaths = {
            getStatefulModuleName() + "/target",    // from project root
            "../" + getStatefulModuleName() + "/target" // from client module
        };
        for (String dir : searchPaths) {
            File jar = new File(dir, getPuJarName());
            if (jar.exists()) {
                return jar;
            }
        }
        throw new IllegalStateException(getPuJarName() + " not found in any of the expected locations");
    }


    @Test
    @Order(1)
    void createOrReplacePu() throws Exception {
        String baseUrl = DockerTestEnv.getInstance().getManagerBaseUrl();

        // create or replace processing unit
        // curl -X PUT --header 'Content-Type: multipart/form-data' --header 'Accept: text/plain' {"type":"formData"} 'http://localhost:8090/v2/pus/resources'
        String resourceUrl = baseUrl + "/v2/pus/resources";

        File puJar = findPuJar();

        String resourceName = DockerTestEnv.PU_NAME + ".jar";
        HttpResponse response = putMultipartFile(resourceUrl, puJar, resourceName);

        assertTrue(response.statusCode == 200 || response.statusCode == 201,
                "PU resource upload should succeed (got status: " + response.statusCode + ", body: " + response.body + ")");
    }

    @Test
    @Order(2)
    void verifyPuExists() throws Exception {
        String baseUrl = DockerTestEnv.getInstance().getManagerBaseUrl();
        String resourcesUrl = baseUrl + "/v2/pus/resources";

        // Wait for PU resource to be available and verify
        HttpResponse response = getWithRetry(resourcesUrl, 60, 2000);

        assertEquals(200, response.statusCode,
                "PU resources endpoint should be accessible (got status: " + response.statusCode + ", body: " + response.body + ")");
        assertTrue(response.body.contains(DockerTestEnv.PU_NAME),
                String.format("Response should contain PU resource name '%s'", DockerTestEnv.PU_NAME));
    }

    @Test
    @Order(3)
    void deployPu() throws Exception {
        String baseUrl = DockerTestEnv.getInstance().getManagerBaseUrl();

        // Deploy the PU using JSON body
        String deployUrl = baseUrl + "/v2/pus";
        String jsonBody = new String(
                getClass().getClassLoader().getResourceAsStream("deploy-pu-request.json").readAllBytes(),
                StandardCharsets.UTF_8);

        HttpResponse response = postJsonWithRetry(deployUrl, jsonBody, 30, 2000);

        assertTrue(response.statusCode == 200 || response.statusCode == 201 || response.statusCode == 202,
                "PU deployment should succeed (got status: " + response.statusCode + ", body: " + response.body + ")");
    }


    @Test
    @Order(4)
    void verifyPuIsDeployed() throws Exception {
        String baseUrl = DockerTestEnv.getInstance().getManagerBaseUrl();
        String puUrl = baseUrl + "/v2/pus/" + DockerTestEnv.PU_NAME;

        // Wait for PU to be deployed and verify
        HttpResponse response = getWithRetry(puUrl, 60, 2000);

        assertEquals(200, response.statusCode,
                String.format("%s PU should be accessible (got status: %d, body: %s)", DockerTestEnv.PU_NAME, response.statusCode, response.body));
        assertTrue(response.body.contains(DockerTestEnv.PU_NAME),
                String.format("Response should contain PU name '%s'", DockerTestEnv.PU_NAME));
    }

    @Test
    @Order(5)
    void verifySpaceIsDeployed() throws Exception {
        String baseUrl = DockerTestEnv.getInstance().getManagerBaseUrl();
        String spaceUrl = baseUrl + "/v2/spaces/" + DockerTestEnv.SPACE_NAME;

        // Wait for space to be deployed and verify
        HttpResponse response = getWithRetry(spaceUrl, 60, 2000);

        assertEquals(200, response.statusCode,
                String.format("%s space should be accessible (got status: %d, body: %s)", DockerTestEnv.SPACE_NAME, response.statusCode, response.body));
        assertTrue(response.body.contains(DockerTestEnv.SPACE_NAME),
                String.format("Response should contain space name '%s'", DockerTestEnv.SPACE_NAME));
    }

    @Test
    @Order(6)
    void verifySpaceIsDiscoverableViaJini() throws Exception {
        String lookupLocator = DockerTestEnv.getInstance().getLookupLocator();

        // Retry logic to wait for the space to be discoverable via JINI lookup.
        // The REST API may report the space as deployed before JINI registration completes.
        int maxRetries = 4;
        int retryDelayMs = 5000;
        int lookupTimeoutMs = 10000;
        Exception lastException = null;
        GigaSpace gigaSpace = null;
        SpaceProxyConfigurer configurer = null;

        for (int i = 0; i < maxRetries; i++) {
            try {
                configurer = new SpaceProxyConfigurer(DockerTestEnv.SPACE_NAME)
                        .lookupLocators(lookupLocator)
                        .lookupTimeout(lookupTimeoutMs);

                gigaSpace = new GigaSpaceConfigurer(configurer).gigaSpace();
                break; // Success
            } catch (Exception e) {
                lastException = e;
                if (configurer != null) {
                    try {
                        configurer.close();
                    } catch (Exception ignore) {
                        // Ignore cleanup errors
                    }
                    configurer = null;
                }
                if (i < maxRetries - 1) {
                    Thread.sleep(retryDelayMs);
                }
            }
        }

        assertNotNull(gigaSpace, "Space should be discoverable via JINI lookup at " + lookupLocator +
                (lastException != null ? ". Last error: " + lastException.getMessage() : ""));

        // Clean up
        if (configurer != null) {
            configurer.close();
        }
    }

    private HttpResponse postJsonWithRetry(String urlString, String jsonBody, int maxRetries, int delayMs) throws Exception {
        Exception lastException = null;
        for (int i = 0; i < maxRetries; i++) {
            try {
                return postJson(urlString, jsonBody);
            } catch (Exception e) {
                lastException = e;
                Thread.sleep(delayMs);
            }
        }
        throw new RuntimeException("Failed to POST after " + maxRetries + " retries", lastException);
    }

    private HttpResponse postJson(String urlString, String jsonBody) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "text/plain");
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(60000);

        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
            outputStream.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }

        int statusCode = connection.getResponseCode();
        String body = readResponseBody(connection);
        connection.disconnect();

        return new HttpResponse(statusCode, body);
    }

    private HttpResponse getWithRetry(String urlString, int maxRetries, int delayMs) throws Exception {
        Exception lastException = null;
        HttpResponse lastResponse = null;

        for (int i = 0; i < maxRetries; i++) {
            try {
                lastResponse = get(urlString);
                if (lastResponse.statusCode == 200) {
                    return lastResponse;
                }
            } catch (Exception e) {
                lastException = e;
            }
            Thread.sleep(delayMs);
        }

        if (lastResponse != null) {
            return lastResponse;
        }
        throw new RuntimeException("Failed to GET after " + maxRetries + " retries", lastException);
    }

    private HttpResponse get(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "*/*");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);

        int statusCode = connection.getResponseCode();
        String body = readResponseBody(connection);
        connection.disconnect();

        return new HttpResponse(statusCode, body);
    }

    private String readResponseBody(HttpURLConnection connection) {
        try {
            BufferedReader reader;
            if (connection.getResponseCode() >= 400) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            }
            String body = reader.lines().collect(Collectors.joining("\n"));
            reader.close();
            return body;
        } catch (Exception e) {
            return "";
        }
    }

    private HttpResponse putMultipartFile(String urlString, File file, String fileName) throws Exception {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        String lineEnd = "\r\n";

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("Accept", "text/plain");
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(120000);

        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
            // Write file part
            outputStream.writeBytes("--" + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + lineEnd);
            outputStream.writeBytes("Content-Type: application/octet-stream" + lineEnd);
            outputStream.writeBytes(lineEnd);

            // Write file content
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            outputStream.write(fileBytes);
            outputStream.writeBytes(lineEnd);

            // Write closing boundary
            outputStream.writeBytes("--" + boundary + "--" + lineEnd);
            outputStream.flush();
        }

        int statusCode = connection.getResponseCode();
        String body = readResponseBody(connection);
        connection.disconnect();

        return new HttpResponse(statusCode, body);
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
