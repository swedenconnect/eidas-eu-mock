#!/usr/bin/env bash

#
# Start-up script for running the CEF eIDAS node locally using externally configured files
# Before running this script. run: mvn clean install dockerfile:build
#

docker run -d --name cefnode --restart=always \
  -p 8080:8080 \
  -e EIDAS_CONFIG_REPOSITORY=/opt/webapp/configEidas14/tomcat/ \
  -e SPECIFIC_CONFIG_REPOSITORY=/opt/webapp/configEidas14/tomcat/specific/ \
  -e SP_CONFIG_REPOSITORY=/opt/webapp/configEidas14/tomcat/sp/ \
  -e IDP_CONFIG_REPOSITORY=/opt/webapp/configEidas14/tomcat/idp/ \
  -e EIDAS_TRUSTED_CERTS_FILE=/opt/webapp/configEidas14/trust/trustedCerts.pem \
  -e EIDAS_TRUSTED_CERTS_CONSTRAINTS=eidasKeyStore_Connector_CA.jks,eidasKeyStore_Service_CA.jks \
  -v /opt/webapp/configEidas14:/opt/webapp/configEidas14 \
  cef-node-docker