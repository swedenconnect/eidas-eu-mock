#!/usr/bin/env bash

echo ******* building eidas node war dependencies *********
mvn -f ../md-trust/pom.xml clean install
mvn -f ../EIDAS-SOURCES-2.5.0-MDSL/EIDAS-Parent/pom.xml clean install -P NodeOnly,DemoToolsOnly,nodeJcacheIgnite,specificCommunicationJcacheIgnite -Dmaven.test.skip=true

