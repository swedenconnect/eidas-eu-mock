# EU eIDAS node reference implementation mockups
providing Docker images for running eIDAS nodes in a test environment
---


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
