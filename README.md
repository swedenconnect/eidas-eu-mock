# EU eIDAS node reference implementation mockups
providing Docker images for running eIDAS nodes in a test environment
---
## Scope
This project provide the means to build and deploy the EU reference eIDAS nodes as docker images.
The eIDAS node code is the standard code version 1.4 with only 1 minor modification.

The modification allows configuration of trust via an external PEM certificates file holding one or more trusted certificates.

The docker container can be started with environment variable settings as specified below

### CEF Digital eIDAS code and documentation
This is just a packaging and minor modification of the EU commission reference implementation. The original code and documentation is available here: [https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/eIDAS-Node+-+Current+release](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/eIDAS-Node+-+Current+release).

The License of the code from CEF Digital ([European Union Public License](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/eIDAS-Node+-+Current+release?preview=/46992189/52603177/eupl_v1.2_en%20.pdf)) is linked on the linked page above.

## Modification to original code

The original code from CEF Digital extracts supported certificates from configured key stores.

In a test environment it is desirable to add trusted certificates in a separate PEM file. The project folder **"cef-node-trust"** contains the java code for extending the list of trusted certificates with the content of a PEM certificates file specified using environment variable settings.

This class is used to extend certificates provided in a JKS trust store in the CEF eIDAS sample implementation code by ammending the [eu.eidas.auth.engine.configuration.dom.KeyStoreSignatureConfigurator](https://github.com/elegnamnden/eidas-eu-mock/blob/master/eIDASNodeDev-1.4.0/EIDAS-SAMLEngine/src/main/java/eu/eidas/auth/engine/configuration/dom/KeyStoreSignatureConfigurator.java) class in the EIDAS-SAMLEngine module by the following extensions:

- Adding the field:
  - private static final EidasTrustedPEMCertificates trustedPemCerts = new EidasTrustedPEMCertificates();
- Adding row in method **getSignatureConfiguration**:
  - trustedCertificates = trustedPemCerts.addTrustedCertificates(trustedCertificates);


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

## Running the docker container
The following example runs the docker container under localhost:8080 using external configuration files located at /opt/webapp/configEidas14

```
docker run -d --name cefnode --restart=always \
  -p 8080:8080 \
  -e EIDAS_CONFIG_REPOSITORY=/opt/webapp/configEidas14/tomcat/ \
  -e SPECIFIC_CONFIG_REPOSITORY=/opt/webapp/configEidas14/tomcat/specific/ \
  -e SP_CONFIG_REPOSITORY=/opt/webapp/configEidas14/tomcat/sp/ \
  -e IDP_CONFIG_REPOSITORY=/opt/webapp/configEidas14/tomcat/idp/ \
  -e EIDAS_TRUSTED_CERTS_FILE=/opt/webapp/configEidas14/trust/trustedCerts.pem \
  -e EIDAS_TRUSTED_CERTS_CONSTRAINTS=eidasKeyStore_Connector_CA.jks,eidasKeyStore_Service_CA.jks \
  -v /opt/webapp/configEidas14:/opt/webapp/configEidas14 \
  cef-node-docker
```

Sample docker run scripts are available in the **"cef-node-docker"** module under **src/main/scripts**.
