# clientapi
A test framework for integration testing with GigaSpaces API.


##### GigaSpaces server version
1. Set the GigasSpaces server version used by docker-compose.
   [docker-compose-test.yaml.templ](client/src/test/resources/docker-compose-test.yaml.templ)

##### GigaSpaces client version
1. Set the `gigaspaces.client.version` in the main `pom.xml`.

##### client Java version
1. The pom.xml contains the `maven.compiler.source` and `maven.compiler.target`. 
2. The Java version of the client is determined by the JVM used to run the tests in scripts/runTests.sh.

##### Execution flow
1. The [preprocessor](preprocessor/src/main/java/com/gigaspaces/demo/Main.java) is responsible for replacing placeholders and generating configuration files.  
   The placeholders are defined in [config.properties](config.properties)
   This module is compiled before getting run.
2. The build of the tests is run. A second build is done because step 1 will modify source configuration files. It also needs to create the pu.jar and copy the dependencies needed to run the JUnit test.
3. The tests are run.  
   See:  
   [scripts/runTests.sh](scripts/runTests.sh)


##### Limitations / Design consideration
1. Type introduction by GigaSpaces client doesn't seem to work when the GigaSpaces server is deployed on Docker. The workaround is to package the types with the processing unit.
2. `network_mode: host` is used in the [docker-compose file](client/src/test/resources/docker-compose-test.yaml) because GigaSpaces uses tcp communication and will communicate on un-advertised ports.
3. Each of the tests has a corresponding example. For example, BDistributedTaskExample uses DistributedTaskExample. The reason is JUnit tests introduce a lot of boilerplate code. It is convenient to have the basic code intact, so you can easily read, modify, run from the IDE, and share the code.

#####
TODO - containerize JDK for client or make setting client Java versions easier.
