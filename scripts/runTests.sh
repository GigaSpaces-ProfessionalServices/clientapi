#!/usr/bin/env bash

JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"

SCRIPTS_DIR="`dirname \"$0\"`"
SCRIPTS_DIR="`( cd \"$SCRIPTS_DIR\" && pwd )`"
echo "SCRIPTS_DIR is: $SCRIPTS_DIR"

PROJ_HOME="`(cd \"$SCRIPTS_DIR\" && cd .. && pwd )`"
echo "PROJ_HOME is: $PROJ_HOME"

CWD=$(pwd)
echo "Current working directory is: ${CWD}"

cd "$PROJ_HOME"

function pre_process() {
  local PROJ_HOME="$1"
  mvn clean
  mvn compile -pl preprocessor
  $JAVA_HOME/bin/java -cp "preprocessor/target/classes" com.gigaspaces.demo.Main --projectHome="$PROJ_HOME"
}

function build() {
  # mvn test -DIntegrationSuite
  # This will build, compile tests, but skip running them
  # -Dgigaspaces.client.version=17.1.4
  mvn clean
  mvn test-compile
  # -DskipTests
  # The following is needed to make sure pu.jar is created
  mvn package -Dmaven.test.skip=true

  # make third party jars available for when we run the test
  mvn dependency:copy-dependencies -pl client -DincludeScope=test -DoutputDirectory=target/test-libs

}

function runTest() {

  $JAVA_HOME/bin/java -cp "client/target/test-classes:client/target/classes:common/target/classes:client/target/test-libs/*" \
      org.junit.platform.console.ConsoleLauncher execute \
      --select-class=com.gigaspaces.demo.ParentTestSuite

}

echo "About to pre-process configurations..."
pre_process "$PROJ_HOME"

echo "About to build project..."
build

echo "About to run integration test..."
runTest

cd "$CWD"
