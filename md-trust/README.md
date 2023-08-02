# Metadata import

This repo provides the source code for addding the capability of CEF eiDAS nodes to import trusted metadata certificates from one or more
MetadataServiceList (MDSL) sources, providing signed and information about trusted metadata sources.

In addition to this, the present patch also provides the capability to manually add trusted certificates in a separate external PEM file
instead of providing these trusted certificates in the private key store of the CEF node.

## Structure
The present source code builds extends the functionality of the existing `eidas-saml-engine` module of an existing eIDAS CEF node.

This is done by integrating the conde from this project in the eu.eidas.auth.engine.configuration.dom.KeyStoreSignatureConfigurator` class
in the SAML-Engine module

## Build information

To build sources:

> \> `mvn clean install`


### Additional instructions for version 2.5.0

For some reason, the 2.5.0 version of the CEF node does not accept BouncyCastle dependencies from the imported updated eidas saml engine dependency. Bouncycastle bcpikix must therefore be added separately in the pom.xml file. For version 2.5.0 update pom.xml from:


        <dependency>
            <groupId>eu.eidas</groupId>
            <artifactId>eidas-saml-engine</artifactId>
        </dependency>


to:

        <dependency>
            <groupId>se.idsec.eidas.cef</groupId>
            <artifactId>eidas-saml-engine-mdsl</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.bouncycastle</groupId>
            <artifactId>bcpkix-jdk15on</artifactId>
            <version>${bouncycastle.version}</version>
        </dependency>


## Deploying the CEF node
The related patch in the SAML-Engine adds two features to provide trusted certificates to the CEF node.

1. Providing trusted certificates in a separate PEM file
2. Configuring zero or more MDSL sources.

These functions initiated using the following environment variables:

Environment variable | value
--- | ---
EIDAS_TRUSTED_CERTS_FILE | The location of a PEM file containing a list of PEM encoded trusted certificates
EIDAS_TRUSTED_CERTS_CONSTRAINTS | A comma separated list of either full KeyStore path, or the ending path of a keystore path. If no constraint is defined, then the list of trusted certificates will always be amended with the PEM list. If the constraint is present, Then the trusted list will only be amended when the keyStorePath properties matches one of the specified constraints.
MDSL_CONFIG_FOLDER | The absolute path to a folder where MDSL configuration data i placed.

The use of PEM file and MDSL source is optional. If none of these environment variables are set, the eIDAS node will operate as a normal unpatched eIDAS node.

The constraints limit the trust in external sources to only the key stores indicated by the constraints for both PEM certificates and MDSL certificates.

The MDSL folder MUST contain a file named `mdsl.properties`. This property file contains the properties for zero or more MDSL sources.
Each property is prefixed mdsl.{index}.{property-name}, where the supported property names are:

Property name | descripton
--- | ---
description | Description of the MDSL source
connector-countries | A space separated list of 2 letter ISO 3166 country codes where the metadata sources providing information about connectors are trusted. The value "all" means all countries.
service-countries | Same as connector-countries but for proxy services
cerificate | The certificate file name holding the certificate used to verify the MDSL
url | the URL location of the MDSL

An example of a valid mdsl.properties file for trusting the NOBID MDSL:

```
mdsl.0.description=NOBID MDSL
mdsl.0.connector-countries=FI IS NL NO SE
mdsl.0.service-countries=SE
mdsl.0.cerificate=nobid-mdsl.crt
mdsl.0.url=https://md.eidas-trust.com/nodeconfig/mdservicelist
```

An example of a valid mdsl.properties file for trusting all countries in the NOBID MDSL

```
mdsl.0.description=NOBID MDSL
mdsl.0.connector-countries=all
mdsl.0.service-countries=all
mdsl.0.cerificate=nobid-mdsl.crt
mdsl.0.url=https://md.eidas-trust.com/nodeconfig/mdservicelist
```

The named certificate files MUST be placed in the MDSL config folder.

Note that the patch only impacts the set of certificates trusted by the eIDAS node. All configuration settings normally applied to an eIDAS node
such as listing supported proxy services in eidas.xml must be provided as usual. All configuration data remains the same except for the fact that
trusted metadata validation certificates are updated automatically.
