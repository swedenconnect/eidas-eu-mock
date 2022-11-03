#!/usr/bin/env bash

echo ******* building eidas node war dependencies *********

DIR="$( cd "$( dirname "$0" )" && pwd )"

modules=("EIDAS-Light-Commons" "EIDAS-Commons" "EIDAS-Encryption" "EIDAS-Metadata" "EIDAS-JCache-Dev" "EIDAS-JCache-Ignite" "EIDAS-JCache-Ignite-Specific-Communication" "EIDAS-SpecificCommunicationDefinition")

cd ../md-trust
mvn clean install
cd ../cef-node-docker-260

for module in "${modules[@]}"; do
  echo "Building cef module $module"
  cd ../EIDAS-Sources-2.6.0-MDSL/$module
  mvn clean install -Dmaven.test.skip=true
  cd ../../cef-node-docker-260
done

cd ../EIDAS-Sources-2.6.0-MDSL/EIDAS-Parent
mvn clean install -Dmaven.test.skip=true
mvn clean install -P NodeOnly,DemoToolsOnly,nodeJcacheIgnite,specificCommunicationJcacheIgnite -Dmaven.test.skip=true
cd ../../cef-node-docker-260

