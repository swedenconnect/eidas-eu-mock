#!/bin/bash

#
# Example run script for running the CEF eIDAS node locally using externally configured files
# Before running this script the docker image must be built and tagged as "cef-node-docker"
#

docker run -d --name cefnode --restart=always \
  -p 8900:8900 \
  -e "EIDAS_CONNECTOR_CONFIG_REPOSITORY=/opt/webapp/configEidas/connector" \
  -e "EIDAS_PROXY_CONFIG_REPOSITORY=/opt/webapp/configEidas/proxy" \
  -e "SPECIFIC_CONNECTOR_CONFIG_REPOSITORY=/opt/webapp/configEidas/specificConnector" \
  -e "SPECIFIC_PROXY_SERVICE_CONFIG_REPOSITORY=/opt/webapp/configEidas/specificProxyService" \
  -e "EIDAS_TRUSTED_CERTS_CONSTRAINTS=" \
  -e "MDSL_CONFIG_FOLDER=/opt/webapp/trust/mdsl" \
  -e "DEBUG_MODE=true" \
  -v /etc/localtime:/etc/localtime:ro \
  -v /opt/docker/configEidas20/XA-270:/opt/webapp/configEidas \
  -v /opt/docker/configEidas20/trust:/opt/webapp/trust \
  cef-node-docker