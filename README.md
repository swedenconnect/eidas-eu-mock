# EU eIDAS node reference implementation mockups
providing Docker images for running eIDAS nodes in a test environment
---
## Scope
This project provide the means to build and deploy the EU reference eIDAS nodes as docker images.
The eIDAS node code is the standard code version 1.4 with only 1 minor modification.

The modification allows configuration of trust via an external PEM certificates file holding one or more trusted certificates.

The docker container can be started with environment variable settings as specified below

## Building the docker container
In order to build the docker container, the following maven projects must be built in the specified order:

project | build command
---|---
cef-node-trust | mvn clean install
eIDASNodeDev-1.4.0 | mvn clean install -P tomcat
cef-node-docker | mvn clean install dockerfile:build

This will build a docker image with default internal configuration files.
The user of internal configuration files can be overwritten by the environment variables as specified beolw.

## Environment variables
The following environment variables influence the operation of eIDAS node instances.

Environment variable | Value
---|---
EIDAS_CONFIG_REPOSITORY | The location of eIDAS node config files (Must end with "/").
SPECIFIC_CONFIG_REPOSITORY | The location of eIDAS node config files (Must end with "/").
SP_CONFIG_REPOSITORY | The location of eIDAS node config files (Must end with "/").
IDP_CONFIG_REPOSITORY | The location of eIDAS node config files (Must end with "/").
EIDAS_TRUSTED_CERTS_FILE | Absolute path to a PEM file holding additional trusted certificates
EIDAS_TRUSTED_CERTS_CONSTRAINTS | Constraints for when to add the PEM certificates as trusted certificates. If this variable is absent, then the PEM certificates are always added as trusted. This variable holds a comma separated list of key store path endings that must match for the PEM certificates to be added as trusted. Normally the key store path endings to be specified are the key stores for the connector and the proxy service.

**Examples:**

Environment variable | Value
---|---
EIDAS_CONFIG_REPOSITORY | /opt/webapp/configEidas14/tomcat/
SPECIFIC_CONFIG_REPOSITORY | /opt/webapp/configEidas14/tomcat/specific/
SP_CONFIG_REPOSITORY | /opt/webapp/configEidas14/tomcat/sp/
IDP_CONFIG_REPOSITORY | /opt/webapp/configEidas14/tomcat/idp/
EIDAS_TRUSTED_CERTS_FILE | /opt/webapp/configEidas14/trust/trustedCerts.pem
EIDAS_TRUSTED_CERTS_CONSTRAINTS | eidasKeyStore_Connector_CA.jks,eidasKeyStore_Service_CA.jks


## Docker image
The image has internal configuration files with default values located at:

> /opt/docker/internal/configEidas

## Creating external configuration
In order to connect this eIDAS nodes connector and proxy service services to other services, some configuration settings must be customised. This is easiest done by placing configuration files in an external folder and to specify its use through the environment variables at docker run.

A full set of sample configuration files are provided in the cef-node-docker project under src/main/config.
