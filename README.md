


##### GigaSpaces server version
1. Set the GigasSpaces server version used by docker-compose.
    * In [config.properties](config.properties) set `gsServerVersion`
    * This sets the version `gigaspaces/smart-cache-enterprise:<version>` in [docker-compose.yaml](client/src/test/resources/docker-compose-test.yaml) by way of [template](client/src/test/resources/docker-compose-test.yaml.templ)
    * Set the appropriate lookup group `xapLookupGroups`, also in the `config.properties`.

##### GigaSpaces client version
1. Set the GigaSpaces client version
    * In [config.properties](config.properties) set `gsClientVersion`.
    * This sets `gigaspaces.client.version` in the main [pom.xml](pom.xml) through the [pom.xml template](pom.xml.tepmpl).

##### Client Java version
1. In [config.properties](config.properties) set the `mavenCompilerJdkVersion` to control the `maven.compiler.source` and `maven.compiler.target` in the pom.xml.

Note: The client JVM version is determined by the JVM chosen in [scripts/runTess.sh](scripts/runTests.sh).
Note: The Java server version is determined by what version of Java the GigaSpaces docker image `gigaspaces/smart-cache-enterprise` is built on top of.

##### Execution flow
1. Run the test at [scripts/runTests.sh](scripts/runTests.sh)
2. The [preprocessor](preprocessor/src/main/java/com/gigaspaces/demo/Main.java) is responsible for replacing placeholders and generating configuration files.  
   The placeholders are defined in [config.properties](config.properties)
   This module is compiled before getting run.
3. The build of the tests is run. A second build is done because step 1 will modify source configuration files. It also needs to create the pu.jar and copy the dependencies needed to run the JUnit test.
4. The tests are run.


##### Limitations / Design consideration
1. Type introduction by GigaSpaces client doesn't seem to work when the GigaSpaces server is deployed on Docker. The workaround is to package the types with the processing unit.
2. `network_mode: host` is used in the [docker-compose file](client/src/test/resources/docker-compose-test.yaml) because GigaSpaces uses tcp communication and will communicate on un-advertised ports.
3. Each of the tests has a corresponding example. For example, BDistributedTaskExample uses DistributedTaskExample. The reason is JUnit tests introduce a lot of boilerplate code. It is convenient to have the basic code intact, so you can easily read, modify, run from the IDE, and share the code.

#####
IDEAS - containerize JDK for client or make setting client Java versions easier.
