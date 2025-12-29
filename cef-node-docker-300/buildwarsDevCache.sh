#!/bin/bash

echo ******* building eidas node war dependencies *********

cd ../md-trust || exit
mvn clean install

cd ../EIDAS-Sources-2.8.0-MDSL-pre || exit
mvn clean install -f EIDAS-Parent/pom.xml -P NodeOnly,DemoToolsOnly,nodeJcacheDev,specificCommunicationJcacheDev -Dmaven.test.skip=true

cd ../cef-node-docker-280 || exit

