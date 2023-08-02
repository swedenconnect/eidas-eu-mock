#!/bin/bash

echo ******* building eidas node war dependencies *********

cd ../md-trust || exit
mvn clean install

cd ../EIDAS-Sources-2.7.0-MDSL || exit
mvn clean install -f EIDAS-Parent/pom.xml -P NodeOnly,DemoToolsOnly,nodeJcacheDev,specificCommunicationJcacheDev -DspecificJar -Dmaven.test.skip=true

cd ../cef-node-docker-270 || exit

