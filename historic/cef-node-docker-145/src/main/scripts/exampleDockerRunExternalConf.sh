#!/usr/bin/env bash

#
# Start-up script for running the CEF eIDAS node locally using externally configured files
# Before running this script. run: mvn clean install dockerfile:build
#

docker run -d --name cefnode145 --restart=always --net="host" \
  -p 8900:8900 \
  -e EIDAS_CONFIG_REPOSITORY=/opt/webapp/configEidas/XC/ \
  -e SPECIFIC_CONFIG_REPOSITORY=/opt/webapp/configEidas/XC/specific/ \
  -e SP_CONFIG_REPOSITORY=/opt/webapp/configEidas/XC/sp/ \
  -e IDP_CONFIG_REPOSITORY=/opt/webapp/configEidas/XC/idp/ \
  -e EIDAS_TRUSTED_CERTS_FILE=/opt/webapp/configEidas/trusted/trustedCerts.pem \
  -e EIDAS_TRUSTED_CERTS_CONSTRAINTS=eidasKeyStore_Connector_XC.jks \
  -v /opt/webapp/eidas-local/configEidas145:/opt/webapp/configEidas \
  cef-node-docker-145