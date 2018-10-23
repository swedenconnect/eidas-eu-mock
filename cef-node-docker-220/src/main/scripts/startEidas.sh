#!/usr/bin/env bash

#
# This script is used to start the eIDAS applications of this tomcat instance.
#

usage() {
    echo "Usage: $0 [options...]" >&2
    echo
    echo "   -c, --confdir          Configuration directory (default is \$CATALINA_HOME/configEidas)"
    echo "   -d                     Debug mode"
    echo "   -f, --folder           Configuration folder (default is XA)"
    echo "   -h, --help             Prints this help"
    echo "   -t, --contraints       Set the trusted certs constraints (Comma separated line of key store path endings)"
    echo
}


SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Replace /cygdrive/c with c:/ (if running on Windows)
SCRIPT_DIR_WIN=`echo $SCRIPT_DIR | sed 's/\/cygdrive\/c/c:/g'`

# Remove /src/main/scripts
BASE_DIR_WIN=`echo $SCRIPT_DIR_WIN | sed 's/\/bin//g'`

# Tomcat
TOMCAT_HOME=$BASE_DIR_WIN
CATALINA_HOME=$TOMCAT_HOME

BASE_CONFIG_PATH=$CATALINA_HOME/configEidas
CONFIG_FOLDER=XA
TRUSTED_CERTS_CONSTRAINTS=
DEBUG_MODE=false

while :
do
    case "$1" in
	-h | --help)
	    usage
	    exit 0
	    ;;
	-c | --confdir)
	    BASE_CONFIG_PATH="$2"
	    shift 2
	    ;;
	-f | --folder)
	    CONFIG_FOLDER="$2"
	    shift 2
	    ;;
	-t | --constraints)
	    TRUSTED_CERTS_CONSTRAINTS="$2"
	    shift 2
	    ;;
	--)
	    shift
	    break;
	    ;;
	-d)
        DEBUG_MODE=true
	    shift
	    break;
	    ;;
	-*)
	    echo "Error: Unknown option: $1" >&2
	    usage
	    exit 0
	    ;;
	*)
	    break
	    ;;
    esac
done


# ENV variables
export EIDAS_CONFIG_REPOSITORY=${BASE_CONFIG_PATH}/${CONFIG_FOLDER}/
export SPECIFIC_CONNECTOR_CONFIG_REPOSITORY=${BASE_CONFIG_PATH}/${CONFIG_FOLDER}/specificConnector/
export SPECIFIC_PROXY_SERVICE_CONFIG_REPOSITORY=${BASE_CONFIG_PATH}/${CONFIG_FOLDER}/specificProxyService/
export SP_CONFIG_REPOSITORY=${BASE_CONFIG_PATH}/${CONFIG_FOLDER}/sp/
export IDP_CONFIG_REPOSITORY=${BASE_CONFIG_PATH}/${CONFIG_FOLDER}/idp/
export EIDAS_TRUSTED_CERTS_FILE=${BASE_CONFIG_PATH}/trust/trustedCerts.pem
export EIDAS_TRUSTED_CERTS_CONSTRAINTS=${TRUSTED_CERTS_CONSTRAINTS}

#
# System settings
#
export JAVA_OPTS="-XX:MaxPermSize=512m"
export CATALINA_OPTS="-Xms512m -Xmx1536m"

#
# Debug
#
export JPDA_ADDRESS=8920
export JPDA_TRANSPORT=dt_socket

if [ $DEBUG_MODE == true ]; then
    echo "Running in debug"    
    $CATALINA_HOME/bin/catalina.sh jpda run
else
    echo "Running in normal mode"
    $CATALINA_HOME/bin/catalina.sh run
fi



