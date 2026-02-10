package com.gigaspaces.demo;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
   This class is used to manage configurations that are related and may require configuration in multiple files.
 */
public class Main {
    public static final String BIND_MNT_HOST_DIR = "bindMountHostDir";
    public static final String SMART_EXTERNALIZABLE = "com.gs.smart-externalizable.enabled";
    public static final String GS_CLIENT_VERSION = "gsClientVersion";
    public static final String GS_SERVER_VERSION = "gsServerVersion";
    public static final String MAVEN_COMPILER_JDK_VERSION = "mavenCompilerJdkVersion";
    public static final String NUM_PARTITIONS = "numberOfPartitions";
    public static final String XAP_LOOKUP_GROUPS = "xapLookupGroups";
    private Path projectHome;
    private Path configFilePath;
    private Properties properties = new Properties();
    String dockerComposeTest = "";
    String testConfig = "";
    String projectPom = "";

    private void processBindMountHostDir(String bindMountHostDirReplaceValue) {
        // 1. docker-compose-test.yaml
        dockerComposeTest = generateFile("client/src/test/resources/docker-compose-test.yaml.templ",
                "client/src/test/resources/docker-compose-test.yaml", null,
                BIND_MNT_HOST_DIR, bindMountHostDirReplaceValue);

    }

    private void processSmartExternalizable(String smartExternalizableReplaceValue) {
        // 1. docker-compose-test.yaml
        dockerComposeTest = generateFile(null,
                "client/src/test/resources/docker-compose-test.yaml", dockerComposeTest,
                SMART_EXTERNALIZABLE, smartExternalizableReplaceValue);
        // 2. test-config.properties
        testConfig = generateFile("client/src/test/resources/test-config.properties.templ",
                "client/src/test/resources/test-config.properties", null,
                SMART_EXTERNALIZABLE, smartExternalizableReplaceValue);

    }
    private void processGsClientVersion(String gsClientVersionReplaceValue) {
        // 1. pom.xml
        projectPom = generateFile("pom.xml.templ",
                "pom.xml", null,
                GS_CLIENT_VERSION, gsClientVersionReplaceValue);
    }
    private void processGsServerVersion(String gsServerVersionValue) {
        // 1. docker-compose-test.yaml
        dockerComposeTest = generateFile(null,
                "client/src/test/resources/docker-compose-test.yaml", dockerComposeTest,
                GS_SERVER_VERSION, gsServerVersionValue);
    }
    private void processMavenCompilerJdkVersion(String mavenCompilerJdkVersionValue) {
        // 1. pom.xml
        projectPom = generateFile(null,
                "pom.xml", projectPom,
                MAVEN_COMPILER_JDK_VERSION, mavenCompilerJdkVersionValue);
    }
    private void processNumberOfPartitions(String numberOfPartitionsReplaceValue) {
        // 1. docker-compose-test.yaml
        generateFile(null,
                "client/src/test/resources/docker-compose-test.yaml", dockerComposeTest,
                NUM_PARTITIONS, numberOfPartitionsReplaceValue);

        // 2. test-config.properties
        testConfig = generateFile(null,
                "client/src/test/resources/test-config.properties", testConfig,
                NUM_PARTITIONS, numberOfPartitionsReplaceValue);

        // 3. deploy-pu-request.json
        generateFile("client/src/test/resources/deploy-pu-request.json.templ",
                "client/src/test/resources/deploy-pu-request.json", null,
                NUM_PARTITIONS, numberOfPartitionsReplaceValue);
    }

    private void processXapLookupGroups(String lookupGroupsReplaceValue) {
        // 1. test-config.properties
        testConfig = generateFile(null,
                "client/src/test/resources/test-config.properties", testConfig,
                XAP_LOOKUP_GROUPS, lookupGroupsReplaceValue);
    }
    private String openTemplateFile(String templateFilePath) {
        Path template = projectHome.resolve(templateFilePath);
        String sTemplate = null;

        // Read the file
        try (Stream<String> lines = Files.lines(Paths.get(template.toString() ))) {
            sTemplate = lines.collect(Collectors.joining(System.lineSeparator()));
            //System.out.println(sTemplate);
            return sTemplate;
        } catch (IOException e) {
            // Handle I/O exceptions, such as the file not being found
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
        return sTemplate;
    }
    private String generateFile(String templateFilePath, String outputFilePath, String previousOutput, String searchStr, String replaceValue) {

        String sTemplate = null;
        if (previousOutput != null && ! previousOutput.equals("")) {
            sTemplate = previousOutput;
        }
        else {
            sTemplate = openTemplateFile(templateFilePath);
        }
        // Search and replace in string
        String content = sTemplate.replace(String.format("${%s}", searchStr), replaceValue);

        Path outputFilename = projectHome.resolve(outputFilePath);

        try (FileWriter fw = new FileWriter(outputFilename.toString());
             BufferedWriter bw = new BufferedWriter(fw)) {

            bw.write(content);
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        return content;
    }

    private void preprocess() {
        //System.out.println("The properties file contains: ");
        Set<String> keys = properties.stringPropertyNames();
        List<String> sortedKeys = new ArrayList<>(keys);
        // Sort the list of keys
        Collections.sort(sortedKeys);

        // Since keys are sorted, they always get processed in the same order
        for (String key : sortedKeys) {

            String value = (String) properties.getProperty(key);
            //System.out.println(String.format("  <K,V>: %s, %s", key, value));
            if (BIND_MNT_HOST_DIR.equals(key)) {
                processBindMountHostDir(value);
            } else if (SMART_EXTERNALIZABLE.equals(key)) {
                processSmartExternalizable(value);
            } else if (GS_CLIENT_VERSION.equals(key)) {
                processGsClientVersion(value);
            } else if (GS_SERVER_VERSION.equals(key)) {
                processGsServerVersion(value);
            } else if (MAVEN_COMPILER_JDK_VERSION.equals(key)) {
                processMavenCompilerJdkVersion(value);
            } else if (NUM_PARTITIONS.equals(key)) {
                processNumberOfPartitions(value);
            } else if (XAP_LOOKUP_GROUPS.equals(key)) {
                processXapLookupGroups(value);
            }
        }
    }
    public void displayArgs() {
        System.out.println("projectHome is: " + projectHome);
        System.out.println("config.properties is located at: " + configFilePath);
    }

    public void printConfigSummary() {
        System.out.println("=".repeat(60));
        System.out.println("GLOBAL TEST CONFIGURATION SUMMARY");
        System.out.println("=".repeat(60));
        properties.stringPropertyNames().stream()
                .sorted()
                .forEach(key -> System.out.println("  " + key + " = " + properties.getProperty(key)));
        System.out.println("-".repeat(60));
    }
    public void printUsage() {
        System.out.println("This program is used to set configurations.");
        System.out.println("The following arguments are used:");
        System.out.println("  --projectHome=</path/to/project/directory>");
        System.out.println("    The location of the maven project.");
        System.out.println("  --help");
        System.out.println("    Display this help message.");
    }
    private void loadProperties(Path configFilePath) throws Exception {
        try (FileInputStream stream = new FileInputStream(configFilePath.toString())) {
            properties.load(stream);
        } catch (Exception e ) {
            System.out.println("Unable to open properties file.");
            e.printStackTrace();
            throw(e);
        }
    }
    public void processArgs(String[] args) {

        try {
            String sProjectHome = null;

            int i = 0;
            while (i < args.length) {
                String s = args[i];
                String sUpper = s.toUpperCase();

                if (sUpper.startsWith("--help".toUpperCase())) {
                    printUsage();
                    System.exit(0);
                } else if (sUpper.startsWith("--projectHome".toUpperCase())) {
                    String[] sArray = s.split("=", 2);
                    sProjectHome = sArray[1];
                } else {
                    System.out.println("Please enter valid arguments.");
                    printUsage();
                    System.exit(0);
                }
                i++;
            }
            projectHome = Paths.get(sProjectHome);
            configFilePath = projectHome.resolve("config.properties");
            loadProperties(configFilePath);

        } catch (Exception e) {
            e.printStackTrace();
            printUsage();
            System.exit(-1);
        }

    }
    public static void main(String[] args) {
        Main main = new Main();
        main.processArgs(args);
        main.displayArgs();
        main.preprocess();
        main.printConfigSummary();
    }
}
