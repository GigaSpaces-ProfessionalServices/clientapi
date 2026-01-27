#!/usr/bin/env bash

set -x
pwd

JAVA_HOME="/usr/lib/jvm/java-1.8.0-openjdk-amd64"

GIGA="/home/dixson/work/gs/smart/gigaspaces-smart-cache-enterprise-16.4.3"


BIN_DIR="`dirname \"$0\"`"
BIN_DIR="`( cd \"$BIN_DIR\" && pwd )`"

BASE_NAME=$(basename $BIN_DIR)

if [ "resources" == "$BASE_NAME" ]; then
# clientapi/client/src/main/resources
  PROJ_DIR="$( cd ../../../.. && pwd )"
elif [ "target" == "$BASE_NAME" ]; then
# clientapi/client/target/classes
  PROJ_DIR="$( cd ../../.. && pwd )"
fi

export MODEL_DIR="$PROJ_DIR/common/target/classes"

export CLASSES_DIR="$PROJ_DIR/client/target/classes"

export CLASSPATH=$GIGA/lib/required/*

export CLASSPATH=$CLASSPATH:$GIGA/lib/optional/near-cache/*

export CLASSPATH=$CLASSPATH:$MODEL_DIR

export CLASSPATH=$CLASSPATH:$CLASSES_DIR

export GS_LOOKUP_LOCATORS="localhost"
export GS_LOOKUP_GROUPS="xap-17.1.0"

$JAVA_HOME/bin/java -Xms1g -Xmx1g -classpath "$CLASSPATH" com.gigaspaces.demo.client.LocalViewExample
