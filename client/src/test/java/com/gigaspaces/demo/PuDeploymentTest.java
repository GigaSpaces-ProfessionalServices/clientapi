package com.gigaspaces.demo;

import com.gigaspaces.demo.rest.RestManagerClient;
import org.junit.jupiter.api.*;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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

        logger.info("Uploading PU jar: {}", puJar.getAbsolutePath());

        RestManagerClient.put(resourceUrl)
                .multipartFile("file", puJar, resourceName)
                .timeout(30000, 120000)
                .execute()
                .assertStatusIn(200, 201);

    }

    @Test
    @Order(2)
    void verifyPuExists() throws Exception {
        String baseUrl = DockerTestEnv.getInstance().getManagerBaseUrl();
        String resourcesUrl = baseUrl + "/v2/pus/resources";

        // Wait for PU resource to be available and verify
        RestManagerClient.get(resourcesUrl)
                .retryUntilStatus(200)
                .maxAttempts(60)
                .delayMs(2000)
                .execute()
                .assertStatus(200)
                .assertBodyContains(DockerTestEnv.PU_NAME);
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
        logger.info("Deploying PU: {}", getPuJarName());

        RestManagerClient.post(deployUrl)
                .jsonBody(jsonBody)
                .timeout(10000, 60000)
                .retryOnFailure()
                .maxAttempts(30)
                .delayMs(2000)
                .execute()
                .assertStatusIn(200, 201, 202);
    }


    @Test
    @Order(4)
    void verifyPuIsDeployed() throws Exception {
        String baseUrl = DockerTestEnv.getInstance().getManagerBaseUrl();
        String puUrl = baseUrl + "/v2/pus/" + DockerTestEnv.PU_NAME;

        // Wait for PU to be deployed and verify
        RestManagerClient.get(puUrl)
                .retryUntilStatus(200)
                .maxAttempts(60)
                .delayMs(2000)
                .execute()
                .assertStatus(200)
                .assertBodyContains(DockerTestEnv.PU_NAME);
    }

    @Test
    @Order(5)
    void verifySpaceIsDeployed() throws Exception {
        String baseUrl = DockerTestEnv.getInstance().getManagerBaseUrl();
        String spaceUrl = baseUrl + "/v2/spaces/" + DockerTestEnv.SPACE_NAME;

        // Wait for space to be deployed and verify
        RestManagerClient.get(spaceUrl)
                .retryUntilStatus(200)
                .maxAttempts(60)
                .delayMs(2000)
                .execute()
                .assertStatus(200)
                .assertBodyContains(DockerTestEnv.SPACE_NAME);
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
}
