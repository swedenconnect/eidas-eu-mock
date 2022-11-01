<img height="100" src="img/nobid-full.svg"></img>

# NOBID Metadata import

This repo provides the source code for addding the capability of CEF eiDAS nodes to import trusted metadata certificates from one or more
MetadataServiceList (MDSL) sources, providing signed and information about trusted metadata sources.

The NOBID metadata project provides a metadata service that can be used to import trusted certificates here [https://md.eidas-trust.com](https://md.eidas-trust.com).

In addition to this, the present patch also provides the capability to manually add trusted certificates in a separate external PEM file
instead of providing these trusted certificates in the private key store of the CEF node.

## Structure
The present source code builds amends the functionality of the existing `eidas-saml-engine` module of an existing eIDAS CEF node.

This is done by providing an altered version of the eu.eidas.auth.engine.configuration.dom.KeyStoreSignatureConfigurator` class that
replaces the original version of the class. This is achieved through the maven shade plugin.

One project module exist for every version of the CEF code where the module `md-trust-cef-240-path` is the module to patch
the 2.4.0 version of the CEF eidas node.

## Build information
To build this project it is first necessary to build the original CEF eIDAS node sources of the desired version.
This is necessary to install the .jar of the original `eidas-saml-engine` module in the local maven repository.

To build all modules and versions of the patch simply build the maven project in the root directory:

> \> `mvn clean install`

To build only one particular version of the patch then build individually first following projects in the following order:

- md-trust-xml
- md-trust-core

and finally the target version of the patch (such as md-trust-cef-240-patches)

All modules are build by executing the `mvn clean install` command in the root directory of each module.

## Building the updated CEF node
Once this is built, the `EIDAS-Node` module `pom.xml` file must be modified to use the new `eidas-saml-engine` module.

This is done by altering the following lines of code in the pom.xml file from:

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

Once this is done, then the CEF node can be rebuild using the normal maven command for building the CEF node. Please refer to the CEF manual
to determine the appropriate maven command used to build the CEF node for your particular environment.

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
The provided patch adds two features to provide trusted certificates to the CEF node.

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

The current certificate for validating signatures on the NOBID MDSL is:

```
-----BEGIN CERTIFICATE-----
MIIEODCCAqCgAwIBAgIEXoUPrDANBgkqhkiG9w0BAQ0FADBQMQswCQYDVQQGEwJTRTEbMBkGA1UE
CgwSSURzZWMgU29sdXRpb25zIEFCMSQwIgYDVQQDDBtOT0JJRCBUZXN0IE1ldGFkYXRhIFNlcnZp
Y2UwHhcNMjAwNDAxMjIwMzI0WhcNMjEwNDAxMjIwMzI0WjBQMQswCQYDVQQGEwJTRTEbMBkGA1UE
CgwSSURzZWMgU29sdXRpb25zIEFCMSQwIgYDVQQDDBtOT0JJRCBUZXN0IE1ldGFkYXRhIFNlcnZp
Y2UwggGiMA0GCSqGSIb3DQEBAQUAA4IBjwAwggGKAoIBgQC/I7Y/2dDKJx18qllyKAcyEdAP7IWh
DQNhS1YZPrA/3GYuffnJAofKpaJl46iCHQrsepqn3+j+WGZTHLn+wJc/gXoIEoK3pK5d95+99Ax3
Bxx2r7CImMQMW8kuE6W/TXyPB11J6B1/6CJ1kOfsvF7VtnmuMgzuTb9bmYaDesLBaTUyd4wAIlNC
WeWQ9J1umTw0eFJpw5QoshUQmA9Pl54761AiSsuKwi6rrYsuBe7TDL98WCxckoGnvKQLCFxq0rZ3
K2zHoTGz13Op2xRZj2sVM5pPILayntRAWxjZaa4hjMKAUyk7lInt13rkSBNI8D9BBj4CcR/DUHM/
hsQXS0kS24T/9MoAVnWSrxjXve6B9ZnGGZ1aJBg0Pu/QbmD2Inr2yA7ZllgvqEnRJqun5976Es5H
TKMqoZtnJN6iTgPYd1dPxHF6pK3ixK+TCKMx0pCg0VeLgos5aTDiPE3+vs4N7c2ODf1AlrLWfgFq
0NtxdkY0Mrgrfk7He4iheL352YcCAwEAAaMaMBgwCQYDVR0TBAIwADALBgNVHQ8EBAMCBaAwDQYJ
KoZIhvcNAQENBQADggGBABmRSn2CfuQHeMxOzzZhZYmXW9+c3VakXs0cGGdPTaypZ/JDybDayUbo
rTWz02NG5WmUjFZxcKZxCVtKvN9chI4hPeJFaIz9rbOZNwzFzMcJW/JA1LQzYSHefqIWD8+u2Lni
C/4gFrxFL0cbLET5+UVt0VS6nmhf5u9ENPR0k5h/f13NatnE4OBSgoCVYaYUcH2BLqupjq90fH5t
1Wk5H0vJ1X6ti3nGJv0WEBjpTVDDZ3MyTqaTAv0c4lY24J/YdeQvvzeynRaoCB7SmElFpPCOIdHn
Fior4opGZn8uk0AxGYOO6ZnkMND/5ePS+novBgqF3rLr5NmTAC+FfQlaTpMl4b+IwXQaA/yGcaPZ
jt9wKXzo4oCcp2LayUM8a6IjheQp+IGa58GJAhdZyo75t9X7/go2CCMr/RdeDkmh2chqEojI5TfP
NJMJlIcoBoH1GO681m+09YjDySIuO7v9bR8CQQckHsuyqHVRsd8Uj6NPr9jGcnlrpkXk5jqInQAj
4Q==
-----END CERTIFICATE-----
```

Note that the patch only impacts the set of certificates trusted by the eIDAS node. All configuration settings normally applied to an eIDAS node
such as listing supported proxy services in eidas.xml must be provided as usual. All configuration data remains the same except for the fact that
trusted metadata validation certificates are updated automatically.
