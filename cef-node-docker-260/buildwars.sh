#!/usr/bin/env bash

echo ******* building eidas node war dependencies *********
mvn -f ../md-trust/pom.xml clean install
mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-Light-Commons clean install -Dmaven.test.skip=true
mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-Commons clean install -Dmaven.test.skip=true
mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-Encryption clean install -Dmaven.test.skip=true
mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-Metadata clean install -Dmaven.test.skip=true
mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-JCache-Dev clean install -Dmaven.test.skip=true
mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-JCache-Ignite clean install -Dmaven.test.skip=true
mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-JCache-Ignite-Specific-Communication clean install -Dmaven.test.skip=true
mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-SpecificCommunicationDefinition clean install -Dmaven.test.skip=true
mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-Parent/pom.xml clean install -P NodeOnly,DemoToolsOnly,nodeJcacheIgnite,specificCommunicationJcacheIgnite -Dmaven.test.skip=true

