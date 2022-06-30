#!/usr/bin/env bash

#
# Start-up script for running the CEF eIDAS node locally using externally configured files
# Before running this script. run: mvn clean install dockerfile:build
#

docker run -d --name cefnode20 --restart=always --net="host" \
  -p 8900:8900 \
  -e EIDAS_CONFIG_REPOSITORY=/opt/webapp/configEidas-XA/XA/ \
  -e SPECIFIC_CONNECTOR_CONFIG_REPOSITORY=/opt/webapp/configEidas-XA/XA/specificConnector/ \
  -e SPECIFIC_PROXY_SERVICE_CONFIG_REPOSITORY=/opt/webapp/configEidas-XA/XA/specificProxyService/ \
  -e SP_CONFIG_REPOSITORY=/opt/webapp/configEidas-XA/XA/sp/ \
  -e IDP_CONFIG_REPOSITORY=/opt/webapp/configEidas-XA/XA/idp/ \
  -e EIDAS_TRUSTED_CERTS_FILE=/opt/webapp/configEidas-XA/trusted/trustedCerts.pem \
  -e EIDAS_TRUSTED_CERTS_CONSTRAINTS=eidasKeyStore_Connector_CA.jks,eidasKeyStore_Service_CA.jks \
  -v /opt/webapp/eidas-local/configEidas20:/opt/webapp/configEidas-XA \
  cef-node-docker-20