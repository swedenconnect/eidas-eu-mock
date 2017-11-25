#!/bin/bash
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Replace /cygdrive/c with c:/ (if running on Windows)
SCRIPT_DIR_WIN=`echo $SCRIPT_DIR | sed 's/\/cygdrive\/c/c:/g'`

# Remove /src/main/scripts
BASE_DIR_WIN=`echo $SCRIPT_DIR_WIN | sed 's/\/src\/main\/scripts//g'`

# Tomcat
TOMCAT_HOME=$BASE_DIR_WIN/target/dependency/apache-tomcat-8.5.23
CATALINA_HOME=$TOMCAT_HOME

# ENV variables
export EIDAS_CONFIG_REPOSITORY=/opt/webapp/configEidas14/tomcat/
export SPECIFIC_CONFIG_REPOSITORY=/opt/webapp/configEidas14/tomcat/specific/
export SP_CONFIG_REPOSITORY=/opt/webapp/configEidas14/tomcat/sp/
export IDP_CONFIG_REPOSITORY=/opt/webapp/configEidas14/tomcat/idp/
export EIDAS_TRUSTED_CERTS_FILE=/opt/webapp/configEidas14/trust/trustedCerts.pem
export EIDAS_TRUSTED_CERTS_CONSTRAINTS=eidasKeyStore_Connector_CA.jks,eidasKeyStore_Service_CA.jks

#
# System settings
#
export JAVA_OPTS="-XX:MaxPermSize=512m"
export CATALINA_OPTS="-Xms512m -Xmx1536m"

#
# Debug
#
export JPDA_ADDRESS=8000
export JPDA_TRANSPORT=dt_socket

if [ "$1" == "-d" ]; then
    echo "Running in debug"    
    $CATALINA_HOME/bin/catalina.sh jpda run
else
    $CATALINA_HOME/bin/catalina.sh run
fi



