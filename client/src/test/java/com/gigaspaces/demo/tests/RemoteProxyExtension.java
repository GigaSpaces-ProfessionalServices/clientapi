package com.gigaspaces.demo.tests;

import com.gigaspaces.demo.ClientConfigLoader;
import com.gigaspaces.demo.DockerTestEnv;
import com.gigaspaces.demo.common.Data;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.openspaces.core.GigaSpace;

import java.lang.reflect.Field;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;

public class RemoteProxyExtension implements BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor {

    private SpaceProxyConfigurer configurer;
    private GigaSpace gigaSpace;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        // System properties moved to postProcessTestInstance (runs before beforeAll)
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) throws Exception {
        if (gigaSpace == null) {
            // Set properties BEFORE creating the space proxy (and before DockerTestEnv.start() which may trigger GigaSpaces class loading)
            ClientConfigLoader.setSystemProperties();

            DockerTestEnv.getInstance().start();

            configurer = new SpaceProxyConfigurer(DockerTestEnv.SPACE_NAME)
                    .lookupTimeout(30000);

            gigaSpace = new GigaSpaceConfigurer(configurer).gigaSpace();
        }

        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if (field.getType().equals(GigaSpace.class)) {
                field.setAccessible(true);
                field.set(testInstance, gigaSpace);
            }
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {

        ClientConfigLoader.clearSystemProperties();
        /*
        System.clearProperty("com.gs.smart-externalizable.enabled");
        System.clearProperty("com.gs.jini_lus.locators");
        System.clearProperty("com.gs.jini_lus.groups");
         */

        if (gigaSpace != null) {
            try {
                gigaSpace.clear(new Data());
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        if (configurer != null) {
            configurer.close();
        }
    }

}
