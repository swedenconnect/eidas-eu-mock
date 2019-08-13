#!/usr/bin/env bash

TOMCAT_HOME=/opt/tomcat

#
# This is the docker run script that is placed in the $CATALINA_HOME/bin folder to be executed inside a docker container
#

: ${EIDAS_CONFIG_REPOSITORY:=/opt/tomcat/configEidas/XA/}
: ${SPECIFIC_CONNECTOR_CONFIG_REPOSITORY:=/opt/tomcat/configEidas/XA/specificConnector/}
: ${SPECIFIC_PROXY_SERVICE_CONFIG_REPOSITORY:=/opt/tomcat/configEidas/XA/specificProxyService/}
: ${SP_CONFIG_REPOSITORY:=/opt/tomcat/configEidas/XA/sp/}
: ${IDP_CONFIG_REPOSITORY:=/opt/tomcat/configEidas/XA/idp/}
: ${EIDAS_TRUSTED_CERTS_FILE:=}
: ${EIDAS_TRUSTED_CERTS_CONSTRAINTS:=}

export EIDAS_CONFIG_REPOSITORY=$EIDAS_CONFIG_REPOSITORY
export SPECIFIC_CONNECTOR_CONFIG_REPOSITORY=$SPECIFIC_CONNECTOR_CONFIG_REPOSITORY
export SPECIFIC_PROXY_SERVICE_CONFIG_REPOSITORY=$SPECIFIC_PROXY_SERVICE_CONFIG_REPOSITORY
export SP_CONFIG_REPOSITORY=$SP_CONFIG_REPOSITORY
export IDP_CONFIG_REPOSITORY=$IDP_CONFIG_REPOSITORY
export EIDAS_TRUSTED_CERTS_FILE=$EIDAS_TRUSTED_CERTS_FILE
export EIDAS_TRUSTED_CERTS_CONSTRAINTS=$EIDAS_TRUSTED_CERTS_CONSTRAINTS

#
# System settings
#
: ${JVM_MAX_HEAP:=1536m}
: ${JVM_START_HEAP:=512m}

export JAVA_OPTS="-XX:MaxPermSize=512m"
export CATALINA_OPTS="\
          -Xmx${JVM_MAX_HEAP}\
          -Xms${JVM_START_HEAP}\
"

#
# Debug
#
export JPDA_ADDRESS=8000
export JPDA_TRANSPORT=dt_socket

if [ $DEBUG_MODE == true ]; then
    echo "Running in debug"
    ${TOMCAT_HOME}/bin/catalina.sh jpda run
else
    echo "Running in normal mode"
    ${TOMCAT_HOME}/bin/catalina.sh run
fi
