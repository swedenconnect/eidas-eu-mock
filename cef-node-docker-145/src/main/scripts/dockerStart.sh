#!/usr/bin/env bash

TOMCAT_HOME=/opt/tomcat

#
# This is the docker run script that is placed in the $CATALINA_HOME/bin folder to be executed inside a docker container
#

: ${EIDAS_CONFIG_REPOSITORY:=/opt/tomcat/configEidas/XA/}
: ${SPECIFIC_CONFIG_REPOSITORY:=/opt/tomcat/configEidas/XA/specific/}
: ${SP_CONFIG_REPOSITORY:=/opt/tomcat/configEidas/XA/sp/}
: ${IDP_CONFIG_REPOSITORY:=/opt/tomcat/configEidas/XA/idp/}
: ${EIDAS_TRUSTED_CERTS_FILE:=}
: ${EIDAS_TRUSTED_CERTS_CONSTRAINTS:=}

export EIDAS_CONFIG_REPOSITORY=$EIDAS_CONFIG_REPOSITORY
export SPECIFIC_CONFIG_REPOSITORY=$SPECIFIC_CONFIG_REPOSITORY
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

${TOMCAT_HOME}/bin/catalina.sh run
