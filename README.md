# EU eIDAS node reference implementation mockups
providing Docker images for running eIDAS nodes in a test environment
---
## Scope
This project provide the means to build and deploy the EU reference eIDAS nodes as docker images.
The eIDAS node code is currently the standard code version 1.4.5  or version 2.2.0 with only a few minor modifications.

Modification | Description
---|---
Trust config | Affects 1.4.5 and 2.2.0. The modification allows configuration of trust via an external PEM certificates file holding one or more trusted certificates. (https://github.com/swedenconnect/eidas-eu-mock/commit/714b5b13f86f23f1b1f6629a6d28806a1fa42d4f).
Required attr | Affects 1.4.5. The attribute definition is locked whether the attribute is required or not. The SP UI can't change whether an attribute is required or not, even if the demo UI suggests that this is possible. The `required` boolean of eIDAS-Light-Commons/src/main/java/eu/eidas/auth/commons/attribute/AttributeDefinition.java is not made final (https://github.com/swedenconnect/eidas-eu-mock/commit/10499d9b14b2fb86e8b6678b3289a69426ce77d6#diff-60d0edf58c3e4768ec41ea87e5804c2e).
Struts version | Affects 1.4.5. Struts version is upgraded from 2.3.32 to 2.3.37 to avoid known vulnerabilities
Build error | Affects 1.4.5. Code can't be built caused by an expired metadata sample in the project test data. The metadata signature check test is turned off as a temporary fix. (https://github.com/swedenconnect/eidas-eu-mock/commit/40ec61833d6ca8f2a324b832d6ddce42f2f15a76)




The docker container or raw services can be deployed as specified below

### CEF Digital eIDAS code and documentation
This project just provides packaging and minor modification of the EU commission reference implementation. The original code and documentation is available here: [https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/eIDAS-Node+-+Current+release](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/eIDAS-Node+-+Current+release).

The License of the code from CEF Digital ([European Union Public License](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/eIDAS-Node+-+Current+release?preview=/46992189/52603177/eupl_v1.2_en%20.pdf)) is linked on the linked page above.

## Externalised trust store

The original code from CEF Digital use certificates from configured key stores, holding private keys of the service, as source for all trusted certificates. This means that the sensible key stores holding the service private key must be updated and changed on a frequent basis when new eIDAS nodes are added to the list of trusted services.

In a test environments and, in particular, in production it is desirable to add trusted certificates in a separate store or PEM file. In this project, the project folder **"cef-node-trust"** contains the java code for extending the list of trusted certificates with the content of a PEM certificates file specified using environment variable settings.

This class is used to extend certificates provided in a JKS trust store in the CEF eIDAS sample implementation code by ammending the [eu.eidas.auth.engine.configuration.dom.KeyStoreSignatureConfigurator](https://github.com/elegnamnden/eidas-eu-mock/blob/master/eIDASNodeDev-1.4.0/EIDAS-SAMLEngine/src/main/java/eu/eidas/auth/engine/configuration/dom/KeyStoreSignatureConfigurator.java) class in the EIDAS-SAMLEngine module by the following extensions:

- Adding the field:
  - private static final EidasTrustedPEMCertificates trustedPemCerts = new EidasTrustedPEMCertificates();
- Adding row in method **getSignatureConfiguration**:
  - trustedCertificates = trustedPemCerts.addTrustedCertificates(trustedCertificates);

# Building and deploying CEF node version 1.4.5
This version of the CEF code creates a Tomcat instance holding the following services:

 - CEF eIDAS node version 1.4.5
 - Sweden Connect Demo SP
 - Sweden Connect Demo IdP

 This tomcat can be built into a Docker image or can alternatively run in any suitable host directly.

## Building the tomcat and the docker container
In order to build the packaged tomcat server and docker container, the following maven projects must be built in the specified order:

Module | Function | Build command
---|---|---
cef-node-trust | Builds PEM certificate trust extension| mvn clean install
eIDASNodeDev-1.4.5 |Builds the CEF Digital eIDAS code with the trust extension| mvn clean install -P tomcat
cef-145-demo-apps |Builds the Sweden Connect demo SP and IdP services| mvn clean install -P tomcat
cef-node-docker |Builds a configured tomcat instance with relevant services | mvn clean install -P sedemo

The docker image can be built and pushed to its default location by executing the following maven command:

> mvn dockerfile:build dockerfile:push

An alternative is to build the docker image as a local image by using the `dockerbuild.sh` as template and specifying any suitable image name.

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
EIDAS_CONFIG_REPOSITORY | /opt/configEidas145/
SPECIFIC_CONFIG_REPOSITORY | /opt/configEidas145/specific/
SP_CONFIG_REPOSITORY | /opt/webapp/configEidas14/sp/
IDP_CONFIG_REPOSITORY | /opt/webapp/configEidas14/idp/
EIDAS_TRUSTED_CERTS_FILE | /opt/configEidas145/trust/trustedCerts.pem
EIDAS_TRUSTED_CERTS_CONSTRAINTS | eidasKeyStore_Connector_XC.jks,eidasKeyStore_Service_XC.jks


## Internal configuration
The image has internal configuration files for a local test country XC with default values located at:

> /opt/tomcat/configEidas

These configuration files are used by default when the docker container is started without specifying any of the environment variables above. The default node with default configuration can be built and run with the following simple command.

> docker build -t cefnode145 . && docker run -p 8900:8900 --name cefnode145 cefnode145

The Demo SP is then accessible from [http://localhost:8900/SP](http://localhost:8900/SP)

## Creating external configuration
In order to connect this eIDAS nodes connector and proxy service services to other services, some configuration settings must be customised. This is easiest done by placing configuration files in an external folder and to specify its use through the environment variables at docker run and mount the volumes from the host where configuration files are located.

A full set of sample configuration files are provided in the cef-node-docker-145 project under src/main/config.

## Running the docker container
The following example runs the docker container under localhost:8080 using external configuration files located at /opt/webapp/configEidas14

```
docker run -d --name cefnode145 --restart=always \
  -p 8080:8080 \
  -e EIDAS_CONFIG_REPOSITORY=/opt/configEidas/ \
  -e SPECIFIC_CONFIG_REPOSITORY=/opt/configEidas/specific/ \
  -e SP_CONFIG_REPOSITORY=/opt/configEidas/sp/ \
  -e IDP_CONFIG_REPOSITORY=/optconfigEidas/idp/ \
  -e EIDAS_TRUSTED_CERTS_FILE=/optconfigEidas/trust/trustedCerts.pem \
  -e EIDAS_TRUSTED_CERTS_CONSTRAINTS=eidasKeyStore_Connector_CA.jks,eidasKeyStore_Service_CA.jks \
  -v /opt/configEidas145:/opt/configEidas \
  cefnode145
```

Sample docker run scripts are available in the **"cef-node-docker"** module under **src/main/scripts**.
