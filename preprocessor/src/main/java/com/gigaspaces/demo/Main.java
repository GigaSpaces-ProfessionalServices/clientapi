package com.gigaspaces.demo;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static final String SMART_EXTERNALIZABLE = "com.gs.smart-externalizable.enabled";
    private Path projectHome;
    private Path configFilePath;
    private Properties properties = new Properties();


    private void processSmartExternalizable(String smartExternalizableReplaceValue) {
        // 1. docker-compose-test.yaml
        generateFile("client/src/test/resources/docker-compose-test.yaml.templ",
                "client/src/test/resources/docker-compose-test.yaml",
                SMART_EXTERNALIZABLE, smartExternalizableReplaceValue);
        // 2. test-config.properties
        generateFile("client/src/test/resources/test-config.properties.templ",
                "client/src/test/resources/test-config.properties",
                SMART_EXTERNALIZABLE, smartExternalizableReplaceValue);

    }
    private void generateFile(String templateFilePath, String outputFilePath, String searchStr, String replaceValue) {

        Path template = projectHome.resolve(templateFilePath);
        Path outputFilename = projectHome.resolve(outputFilePath);

        String content = null;

        // Read the file)
        try (Stream<String> lines = Files.lines(Paths.get(template.toString() ))) {
            String sTemplate = lines.collect(Collectors.joining(System.lineSeparator()));
            System.out.println(sTemplate);

            // Search and replace in string
            content = sTemplate.replace(String.format("${%s}", searchStr), replaceValue);

            // Files.write()
        } catch (IOException e) {
            // Handle I/O exceptions, such as the file not being found
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
        try (FileWriter fw = new FileWriter(outputFilename.toString());
             BufferedWriter bw = new BufferedWriter(fw)) {

            bw.write(content);

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void preprocess() {
        System.out.println("The properties file contains: ");
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            System.out.println(String.format("  <K,V>: %s, %s", key, value));
            if (SMART_EXTERNALIZABLE.equals(key)) {
                processSmartExternalizable(value);
            }
        }
    }
    public void displayArgs() {
        System.out.println("projectHome is: " + projectHome);
        System.out.println("config.properties is located at: " + configFilePath);
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

    }
}
