package com.gigaspaces.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;
import java.net.URISyntaxException;
import java.time.Duration;

/**
 * Shared test environment that manages the Docker Compose lifecycle for the GigaSpaces server
 * across multiple test classes.
 */
public class DockerTestEnv {

    private static final Logger logger = LoggerFactory.getLogger(DockerTestEnv.class);

    public static final String XAP_SERVICE = "xap-server";
    public static final String SPACE_NAME = com.gigaspaces.demo.client.Client.SPACE_NAME;
    public static final String PU_NAME = SPACE_NAME + "-pu";
    public static final int XAP_MANAGER_PORT = 8090;
    public static final int XAP_LOOKUP_PORT = 4174;
    private static final String XAP_MANAGER_HOST;

    private DockerComposeContainer<?> environment;
    private boolean started = false;
    private static DockerTestEnv dockerTestEnv = null;

    static {
        XAP_MANAGER_HOST = ClientConfigLoader.getProperty("xapManagerHost");
        Runtime.getRuntime().addShutdownHook(new Thread(DockerTestEnv.getInstance()::stop));
    }

    private DockerTestEnv() {}

    public static synchronized DockerTestEnv getInstance()
    {
        if (dockerTestEnv == null)
            dockerTestEnv = new DockerTestEnv();
        return dockerTestEnv;
    }

    public synchronized void start() {
        if (!started) {
            File dockerComposeFile;
            try {
                dockerComposeFile = new File(DockerTestEnv.class.getClassLoader()
                        .getResource("docker-compose-test.yaml").toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException("Failed to load docker-compose-test.yaml from classpath", e);
            }

            // Mark as started immediately to prevent race conditions
            started = true;

            // Using network_mode: host, so no need for withExposedService
            // Ports are directly available on localhost
            environment = new DockerComposeContainer<>(dockerComposeFile)
                    .withStartupTimeout(Duration.ofMinutes(5));

            environment.start();
            // Manager readiness is validated by ManagerReadyTest which runs first in the test suite
        }
    }

    public synchronized void stop() {
        if (environment != null && started) {
            environment.stop();
            started = false;
        }
    }

    public DockerComposeContainer<?> getEnvironment() {
        return environment;
    }

    public boolean isStarted() {
        return started;
    }

    public String getManagerHost() {
        ensureStarted();
        return XAP_MANAGER_HOST;
    }

    public int getManagerPort() {
        ensureStarted();
        return XAP_MANAGER_PORT;
    }

    public String getManagerBaseUrl() {
        return "http://" + getManagerHost() + ":" + getManagerPort();
    }

    public String getLookupHost() {
        ensureStarted();
        return XAP_MANAGER_HOST;
    }

    public int getLookupPort() {
        ensureStarted();
        return XAP_LOOKUP_PORT;
    }

    public String getLookupLocator() {
        return getLookupHost() + ":" + getLookupPort();
    }

    private void ensureStarted() {
        if (!started) {
            throw new IllegalStateException("Test environment not started. Call start() first.");
        }
    }
}
