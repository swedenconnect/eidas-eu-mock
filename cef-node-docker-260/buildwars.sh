#!/usr/bin/env bash

echo ******* building eidas node war dependencies *********

DIR="$( cd "$( dirname "$0" )" && pwd )"

modules=(
  "EIDAS-Light-Commons" \
  "EIDAS-Commons" \
  "EIDAS-Encryption" \
  "EIDAS-Metadata" \
  "EIDAS-JCache-Dev" \
  "EIDAS-JCache-Ignite" \
  "EIDAS-JCache-Ignite-Specific-Communication" \
  "EIDAS-SpecificCommunicationDefinition")

cd ../md-trust
mvn clean install
cd $DIR

for module in "${modules[@]}"; do
  cd ../EIDAS-SOURCES-2.6.0-MDSL/$module
  mvn clean install -Dmaven.test.skip=true
  cd $DIR
done

cd ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-Parent
mvn clean install -Dmaven.test.skip=true
mvn clean install -P NodeOnly,DemoToolsOnly,nodeJcacheIgnite,specificCommunicationJcacheIgnite -Dmaven.test.skip=true



#mvn -f ../md-trust/pom.xml clean install
#mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-Light-Commons clean install -Dmaven.test.skip=true
#mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-Commons clean install -Dmaven.test.skip=true
#mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-Encryption clean install -Dmaven.test.skip=true
#mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-Metadata clean install -Dmaven.test.skip=true
#mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-JCache-Dev clean install -Dmaven.test.skip=true
#mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-JCache-Ignite clean install -Dmaven.test.skip=true
#mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-JCache-Ignite-Specific-Communication clean install -Dmaven.test.skip=true
#mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-SpecificCommunicationDefinition clean install -Dmaven.test.skip=true
#mvn -f ../EIDAS-SOURCES-2.6.0-MDSL/EIDAS-Parent/pom.xml clean install -P NodeOnly,DemoToolsOnly,nodeJcacheIgnite,specificCommunicationJcacheIgnite -Dmaven.test.skip=true

