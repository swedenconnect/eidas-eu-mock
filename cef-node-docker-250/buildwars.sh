#!/usr/bin/env bash

echo ******* building eidas node war dependencies *********
mvn -f ../EIDAS-SOURCES-2.5.0-SNAPSHOT-MDSL/EIDAS-Parent/pom.xml clean install -P NodeOnly,DemoToolsOnly,nodeJcacheIgnite,specificCommunicationJcacheIgnite

