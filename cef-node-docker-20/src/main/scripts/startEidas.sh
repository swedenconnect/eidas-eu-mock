#!/usr/bin/env bash

#
# This script is used to start the eIDAS applications of this tomcat instance.
#

usage() {
    echo "Usage: $0 [options...]" >&2
    echo
    echo "   -d, --confdir          Configuration directory (default is \$CATALINA_HOME/configEidas)"
    echo "   -f, --folder           Configuration folder (default is XA)"
    echo "   -h, --help             Prints this help"
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

while :
do
    case "$1" in
	-h | --help)
	    usage
	    exit 0
	    ;;
	-d | --confdir)
	    BASE_CONFIG_PATH="$2"
	    shift 2
	    ;;
	-f | --folder)
	    CONFIG_FOLDER="$2"
	    shift 2
	    ;;
	--)
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
export SPECIFIC_CONFIG_REPOSITORY=${BASE_CONFIG_PATH}/${CONFIG_FOLDER}/specific/
export SP_CONFIG_REPOSITORY=${BASE_CONFIG_PATH}/${CONFIG_FOLDER}/sp/
export IDP_CONFIG_REPOSITORY=${BASE_CONFIG_PATH}/${CONFIG_FOLDER}/idp/
export EIDAS_TRUSTED_CERTS_FILE=${BASE_CONFIG_PATH}/trust/trustedCerts.pem
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



