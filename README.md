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
Build error | Affects 1.4.5 and 2.2.0. Code can't be built caused by an expired metadata sample in the project test data. The metadata signature check test is turned off as a temporary fix. (https://github.com/swedenconnect/eidas-eu-mock/commit/40ec61833d6ca8f2a324b832d6ddce42f2f15a76)

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
cef-node-docker-145 |Builds a configured tomcat instance with relevant services | mvn clean install -P sedemo

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
SP_CONFIG_REPOSITORY | /opt/webapp/configEidas145/sp/
IDP_CONFIG_REPOSITORY | /opt/webapp/configEidas145/idp/
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

Sample docker run scripts are available in the **"cef-node-docker-145"** module under **src/main/scripts**.



# Building and deploying CEF node version 2.2.0
This version of the CEF code creates one Tomcat instance holding the CEF eIDAS node version 2.2.0 service and one Spring Boot application with SP and IdP services for one or more CEF nodes:

## Building the tomcat and the docker container for the CEF node
In order to build the packaged tomcat server and docker container, the following maven projects must be built in the specified order:

Module | Function | Build command
---|---|---
cef-node-trust | Builds PEM certificate trust extension| mvn clean install
eIDASNodeDev-2.2.0 |Builds the CEF Digital eIDAS code with the trust extension| mvn clean install
cef-node-docker-220 |Builds a configured tomcat instance with relevant services | mvn clean install

The docker image can be built and pushed to its default location by executing the following maven command:

> mvn dockerfile:build dockerfile:push

This will build a docker image with default internal configuration files.
The user of internal configuration files can be overwritten by the environment variables as specified beolw.

## Environment variables
The following environment variables influence the operation of eIDAS node instances.

Environment variable | Value
---|---
EIDAS_CONFIG_REPOSITORY | The location of eIDAS node config files (Must end with "/").
SPECIFIC_CONNECTOR_CONFIG_REPOSITORY | The location of eIDAS node config files (Must end with "/").
SPECIFIC_PROXY_SERVICE_CONFIG_REPOSITORY | The location of eIDAS node config files (Must end with "/").
SP_CONFIG_REPOSITORY | The location of eIDAS node config files (Must end with "/").
IDP_CONFIG_REPOSITORY | The location of eIDAS node config files (Must end with "/").
EIDAS_TRUSTED_CERTS_FILE | Absolute path to a PEM file holding additional trusted certificates
EIDAS_TRUSTED_CERTS_CONSTRAINTS | Constraints for when to add the PEM certificates as trusted certificates. If this variable is absent, then the PEM certificates are always added as trusted. This variable holds a comma separated list of key store path endings that must match for the PEM certificates to be added as trusted. Normally the key store path endings to be specified are the key stores for the connector and the proxy service.

**Examples:**

Environment variable | Value
---|---
EIDAS_CONFIG_REPOSITORY | /opt/configEidas220/
SPECIFIC_CONNECTOR_CONFIG_REPOSITORY | /opt/configEidas220/specificConnector/
SPECIFIC_PROXY_SERVICE_CONFIG_REPOSITORY | /opt/configEidas220/specificProxyService/
SP_CONFIG_REPOSITORY | /opt/webapp/configEidas220/sp/
IDP_CONFIG_REPOSITORY | /opt/webapp/configEidas220/idp/
EIDAS_TRUSTED_CERTS_FILE | /opt/configEidas220/trust/trustedCerts.pem
EIDAS_TRUSTED_CERTS_CONSTRAINTS | eidasKeyStore_Connector_XC.jks,eidasKeyStore_Service_XC.jks


## Internal configuration
The image has internal configuration files for a local test country XC with default values located at:

> /opt/tomcat/configEidas

These configuration files are used by default when the docker container is started without specifying any of the environment variables above. The default node with default configuration can be built and run with the following simple command.

> docker build -t cefnode220 . && docker run -p 8900:8900 --name cefnode220 cefnode220

The Demo SP is not available in the docker image as it only holds the eIDAS node. The SP application is deployed in the demo application Spring Boot application as described below.

The default configuration expects a demo application hub to be deployed at port 8080.

## Creating external configuration
In order to connect this eIDAS nodes connector and proxy service services to other services, some configuration settings must be customised. This is easiest done by placing configuration files in an external folder and to specify its use through the environment variables at docker run and mount the volumes from the host where configuration files are located.

A full set of sample configuration files are provided in the cef-node-docker-220 project under src/main/config.

## Running the docker container
The following example runs the docker container under localhost:8080 using external configuration files located at /opt/webapp/configEidas14

```
docker run -d --name cefnode220 --restart=always \
  -p 8080:8080 \
  -e "EIDAS_CONFIG_REPOSITORY=/opt/webapp/configEidas/XA/" \
  -e "SP_CONFIG_REPOSITORY=/opt/webapp/configEidas/XA/sp/" \
  -e "IDP_CONFIG_REPOSITORY=/opt/webapp/configEidas/XA/idp" \
  -e "EIDAS_TRUSTED_CERTS_FILE=/opt/webapp/configEidas/trust/trustedCerts.pem" \
  -e "EIDAS_TRUSTED_CERTS_CONSTRAINTS=" \
  -e "SPECIFIC_CONNECTOR_CONFIG_REPOSITORY=/opt/webapp/configEidas/XA/specificConnector" \
  -e "SPECIFIC_PROXY_SERVICE_CONFIG_REPOSITORY=/opt/webapp/configEidas/XA/specificProxyService" \
  -v /etc/localtime:/etc/localtime:ro \
  -v /opt/docker/cef20/configEidas20:/opt/webapp/configEidas \
docker.eidastest.se:5000/cef-node-docker-220
```

Sample docker run scripts are available in the **"cef-node-docker-220"** module under **src/main/scripts**.

## Building and deploying the demo application hub

The version 2.2.0 demo application hub is a spring boot application that can provide SP and IDP services for any number of CEF node instances. All demo application instances are setup using properties configuration values.

The source code of the demo application hub is located in the project module cef-20-demo-hub. The source is build by the maven command:

> mvn clean install

### Configuration and setup

The demo application hub has no key store and key management as it implements the simplified specific API that is designed as a local API between the specific national implementations and the eIDAS node, which is one of the major differences in version 2.x compared to 1.x.

This application sets up demo SP and demo IdP services for any number of countries based on the spconfig.properties file.

The following example defines two SP and IdP instances for countries XA and XB, which communicates with 5 countries:

- XA (Being the first CEF eIDAS node version 2.2.0)
- XB (Being the second CEF eIDAS node version 2.2.0)
- XC (Being a separate CEF eIDAS node running version 1.4.5)
- SE (Being the eIDAS proxy and connector for country SE)
- XE (Being the eIDAS proxy and connector nodes for country XE).


```
baseReturnUrl=http://localhost:8080/cef20-apps/return/

extSp.XC.cefVersion=1.4.5
extSp.XC.description=Test country based on CEF node version ${extSp.XC.cefVersion}.<br/>This node accepts requests from private SP.
extSp.XC.flagUrl=img/EU-flag.png
extSp.XC.spUrl=https://xc.testnode.eidastest.se/SP/populateIndexPage

# XA Country SP and IdP
sp.XA.cefVersion=2.2.0
sp.XA.name=Test Country XA - CEF ${sp.XA.cefVersion}
sp.XA.description=Test country based on CEF node version ${sp.XA.cefVersion}.<br/>This node accepts requests from private SP.
sp.XA.idpName=Test Country XA Identity Provider (CEF 2.2.0)
sp.XA.requestUrl=http://localhost:8900/SpecificConnector/ServiceProvider
sp.XA.flagUrl=img/EU-flag.png
sp.XA.country.XA.name=Test Country XA - CEF node V${sp.XA.cefVersion}
sp.XA.country.XA.flag=EU.png
sp.XA.country.SE.name=Sweden
sp.XA.country.SE.flag=SE.png
sp.XA.country.XB.name=Country XB - CEF node V${sp.XB.cefVersion}
sp.XA.country.XB.flag=EU.png

# XB Country SP and IdP
sp.XB.cefVersion=2.2.0
sp.XB.name=Test Country XB - CEF ${sp.XA.cefVersion}
sp.XB.description=Test country based on CEF node version ${sp.XB.cefVersion}.<br/>This node only accepts requests from public SP.
sp.XB.idpName=Test Country XB Identity Provider (CEF 2.2.0)
sp.XB.requestUrl=http://localhost:8900/SpecificConnector/ServiceProvider
sp.XB.flagUrl=img/EU-flag.png
sp.XB.country.XA.name=Country XA - CEF node V${sp.XA.cefVersion}
sp.XB.country.XA.flag=EU.png
sp.XB.country.XY.name=Sweden (XY Dev)
sp.XB.country.XY.flag=SE.png
sp.XB.country.XB.name=Country XB - CEF node V${sp.XB.cefVersion}
sp.XB.country.XB.flag=EU.png
sp.XB.country.XC.name=Country XC - CEF node V${extSp.XC.cefVersion}
sp.XB.country.XC.flag=EU.png
```

The basic configuration file for the Spring Boot application (application.properties) must be modified to provide the actual service URL.

The location of these configuration files can be modified by setting the environment variables for external config locations such as:

> SPRING_CONFIG_LOCATION (For complete new config files)
> SPRING_CONFIG_ADDITIONAL_LOCATION (For delta config files specifying only modified parameters)

### Building and deploying docker container

A docker image for the Spring Boot application is built and pushed to the repository by the following maven command:

> mvn dockerfile:build dockerfile:push

The following example command starts a docker container:

```
docker run -d --name cef20hub --restart=always \
  -p 8080:8080 -p 8009:8009 \
  -e "SPRING_CONFIG_LOCATION=/opt/cef20hub/" \
  -v /etc/localtime:/etc/localtime:ro \
  -v /opt/docker/cef20/hub:/opt/cef20hub \
  docker.eidastest.se:5000/cef20demohub
```
