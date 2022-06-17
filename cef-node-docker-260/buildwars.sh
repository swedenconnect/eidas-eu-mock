#!/usr/bin/env bash

echo ******* building eidas node war dependencies *********
mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-Parent/pom.xml clean install -P NodeOnly,DemoToolsOnly,nodeJcacheIgnite,specificCommunicationJcacheIgnite

