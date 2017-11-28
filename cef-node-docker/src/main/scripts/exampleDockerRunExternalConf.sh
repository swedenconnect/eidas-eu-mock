#!/usr/bin/env bash

#
# Start-up script for running the CEF eIDAS node locally using externally configured files
# Before running this script. run: mvn clean install dockerfile:build
#

docker run -d --name cefnode --restart=always --net="host" \
  -p 8900:8900 \
  -e EIDAS_CONFIG_REPOSITORY=/opt/webapp/configEidas-XA/XA/ \
  -e SPECIFIC_CONFIG_REPOSITORY=/opt/webapp/configEidas-XA/XA/specific/ \
  -e SP_CONFIG_REPOSITORY=/opt/webapp/configEidas-XA/XA/sp/ \
  -e IDP_CONFIG_REPOSITORY=/opt/webapp/configEidas-XA/XA/idp/ \
  -e EIDAS_TRUSTED_CERTS_FILE=/opt/webapp/configEidas-XA/XA/trustedCerts.pem \
  -e EIDAS_TRUSTED_CERTS_CONSTRAINTS=eidasKeyStore_Connector_CA.jks,eidasKeyStore_Service_CA.jks \
  -v /opt/webapp/configEidas-XA:/opt/webapp/configEidas-XA \
  cef-node-docker