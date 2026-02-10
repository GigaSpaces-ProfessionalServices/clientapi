package com.gigaspaces.demo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(ClientConfigLoader.class);
    private static Properties properties = new Properties();

    static {
        try (InputStream input = ClientConfigLoader.class.getClassLoader().getResourceAsStream("test-config.properties")) {
            if (input == null) {
                logger.warn("Unable to open test-config.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test-config.properties from classpath", e);
        }
    }
    public static void setSystemProperties() {
        System.setProperty("com.gs.smart-externalizable.enabled", ClientConfigLoader.getProperty("com.gs.smart-externalizable.enabled"));
        System.setProperty("com.gs.jini_lus.groups", ClientConfigLoader.getProperty("xapLookupGroups"));
        System.setProperty("com.gs.jini_lus.locators", ClientConfigLoader.getProperty("xapManagerHost") + ":" + DockerTestEnv.XAP_LOOKUP_PORT);
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static void clearSystemProperties() {
        System.clearProperty("com.gs.smart-externalizable.enabled");
        System.clearProperty("com.gs.jini_lus.groups");
        System.clearProperty("com.gs.jini_lus.locators");
    }

    public static void printConfigSummary() {
        // Use System.out.println as logger is dependent upon the logger that was bundled with gigaspaces
        // logger dependencies have changed, for example from 16.x - 17.x
        System.out.println("=".repeat(60));
        System.out.println("CLIENT TEST CONFIGURATION SUMMARY");
        System.out.println("=".repeat(60));
        properties.stringPropertyNames().stream()
                .sorted()
                .forEach(key -> System.out.println("  " + key + " = " + properties.getProperty(key)));
        System.out.println("-".repeat(60));
        System.out.println("Derived system properties:");
        System.out.println("  com.gs.jini_lus.locators = " + getProperty("xapManagerHost") + ":" + DockerTestEnv.XAP_LOOKUP_PORT);
        System.out.println("=".repeat(60));
    }
}

