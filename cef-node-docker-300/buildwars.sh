#!/bin/bash

echo ******* building eidas node war dependencies *********

cd ../md-trust || exit
mvn clean install

cd ../EIDAS-Sources-3.0.0-MDSL || exit
mvn clean install -f EIDAS-Parent/pom.xml -P NodeOnly,DemoToolsOnly,nodeJcacheHazelcast,specificCommunicationJcacheHazelcast -Dmaven.test.skip=true

cd ../cef-node-docker-300 || exit

