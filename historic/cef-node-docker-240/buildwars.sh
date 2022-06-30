#!/usr/bin/env bash

echo ******* building eidas node war dependencies *********
mvn -f ../eIDASNodeDev-2.4.0/EIDAS-Parent/pom.xml clean install -P NodeOnly,DemoToolsOnly,nodeJcacheIgnite,specificCommunicationJcacheIgnite

