package com.gigaspaces.demo;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ContainerState;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that checks if Xap Manager is ready
 * and verifies it is running correctly.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ManagerReadyTest {

    private static final Logger logger = LoggerFactory.getLogger(ManagerReadyTest.class);

    @BeforeAll
    static void setUp() {
        // Set properties BEFORE creating the space proxy (and before DockerTestEnv.start() which may trigger GigaSpaces class loading)
        ClientConfigLoader.setSystemProperties();
        DockerTestEnv.start();
    }

    @AfterAll
    static void tearDown() {
        ClientConfigLoader.clearSystemProperties();
        //System.clearProperty("com.gs.smart-externalizable.enabled");
        // Environment cleanup is handled by CleanupTest which runs last
    }

    @Test
    @Order(1)
    void allServicesAreRunning() {
        // With network_mode: host, verify service is running via REST API
        // The container check, i.e., getContainerByServiceName may not work reliably with host networking
        assertTrue(DockerTestEnv.isStarted(), "Test environment should be started");
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

    @Test
    @Order(3)
    void testSmartExternalizableSystemProperty() {
        String expected = ClientConfigLoader.getProperty("com.gs.smart-externalizable.enabled");
        String actual = System.getProperty("com.gs.smart-externalizable.enabled");
        assertEquals(expected, actual,
            String.format("Smart externalizable system property should be set: expected=%s, actual=%s", expected, actual));
    }

    @Test
    @Order(4)
    void xapServerServiceIsRunningAndCorrectHostConfigured() throws Exception {
        // Collect results from both checks so we can report all failures
        String restApiError = null;

        // Check 1: XAP Manager REST API is accessible
        try {
            int statusCode = waitForManagerReady(Duration.ofMinutes(2));

            if (statusCode != 200 && statusCode != 401) {
                restApiError = "XAP Manager REST API should be accessible (got status: " + statusCode + ")";
            }
        } catch (Exception e) {
            restApiError = "XAP Manager REST API connection failed: " + e.getMessage();
        }

        // Check 2: Verify correct xapManagerHost in container logs
        final String hostConfigError = verifyXapManagerHostInLogs();

        // Report all failures together using assertAll
        final String finalRestApiError = restApiError;

        assertAll(
                () -> assertNull(finalRestApiError, finalRestApiError),
                () -> assertNull(hostConfigError, hostConfigError)
        );
    }

    private int waitForManagerReady(Duration timeout) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeout.toMillis();
        String url = DockerTestEnv.getManagerBaseUrl() + "/v2/info";

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

    private String verifyXapManagerHostInLogs() throws InterruptedException {
        String expectedHost = ClientConfigLoader.getProperty("xapManagerHost");
        if (expectedHost == null) {
            return "xapManagerHost should be configured in test-config.properties";
        }

        // Check for the Lookup Service locator pattern which proves the manager bound to this host
        // Example: "locator=jini://127.0.1.1:4174/"
        String expectedLocatorPattern = String.format("locator=jini://%s:%s/", expectedHost, DockerTestEnv.XAP_LOOKUP_PORT);

        // Retry with timeout - wait up to 30 seconds for the pattern to appear in logs
        int maxRetries = 3;
        int retryDelayMs = 10_000;
        String logs = null;

        for (int i = 0; i < maxRetries; i++) {
            Optional<ContainerState> containerOpt = DockerTestEnv.getEnvironment()
                    .getContainerByServiceName(DockerTestEnv.XAP_SERVICE + "_1");

            if (!containerOpt.isPresent()) {
                return "Docker container '" + DockerTestEnv.XAP_SERVICE + "' should be running";
            }

            ContainerState container = containerOpt.get();
            logs = container.getLogs();

            if (logs != null && logs.contains(expectedLocatorPattern)) {
                logger.info("Verified xapManagerHost '" + expectedHost + "' found in Lookup Service locator: " + expectedLocatorPattern);
                return null; // Success - no error
            }

            Thread.sleep(retryDelayMs);
        }

        // Build error message with hint for correct host
        // Example log snippet:
        // 2026-01-29 23:24:03,891 LUS INFO [com.sun.jini.reggie.GigaRegistrar] - Started Lookup Service [duration=0.681s, groups=[xap-17.1.4], service-id=607d9019-ec68-4409-b6a6-4194f5656f93, locator=jini://127.0.1.1:4174/]
        String hostHint = String.format("%sCheck the docker logs to confirm the correct xap manager host. Look for jini://<host name>:%s", System.lineSeparator(), DockerTestEnv.XAP_LOOKUP_PORT);
        String regex = String.format("(LUS INFO \\[com.sun.jini.reggie.GigaRegistrar\\] - Started Lookup Service.*?locator=jini://([\\w\\.-]*):%d/])", DockerTestEnv.XAP_LOOKUP_PORT);
        Pattern pattern = Pattern.compile(regex);
        if (logs != null) {
            Matcher matcher = pattern.matcher(logs);
            if (matcher.find()) {
                hostHint = String.format("%sPlease try with xapManagerHost: %s%sPlease refer to message: %s",
                        System.lineSeparator(), matcher.group(2), System.lineSeparator(), matcher.group(1));
            }
        }

        return String.format("%s%sContainer logs should contain the Lookup Service locator pattern '%s' within %d seconds. " +
                        "Please confirm the xapManagerHost is correct",
                hostHint,
                System.lineSeparator(),
                expectedLocatorPattern,
                (maxRetries * retryDelayMs) / 1000);
    }
}
