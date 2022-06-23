/*
 * Copyright (c) 2022.  Agency for Digital Government (DIGG)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.swedenconnect.eidas.cef.confbuilder.handler.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;
import se.swedenconnect.eidas.cef.confbuilder.configuration.*;
import se.swedenconnect.eidas.cef.confbuilder.handler.ConfigFileProcessor;
import se.swedenconnect.eidas.cef.confbuilder.handler.EIDASNodeConfigBuilder;
import se.swedenconnect.eidas.cef.confbuilder.utils.X509Utils;

import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Description
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
public class CEF26ConfigBuilder implements EIDASNodeConfigBuilder {

  @Override public void buildConfiguration(
    File configFile, File templateDirectory, File targetDirectory,
    BaseProperties baseProperties, MetadataProperties metadataProperties,
    ServicesProperties servicesProperties, KeystoreProperties keystoreProperties,
    IdpProperties idpProperties, SpProperties spProperties
  ) throws IOException {
    System.out.println("Building configuration data for CEF node version 2.6.0");

    System.out.println("Copying template files to target directory");
    FileUtils.copyDirectory(templateDirectory, targetDirectory);
    //FileUtils.copyDirectory(new File(configDirectory, "keystores"), new File(targetDirectory, "keystores"));

    // Get key and cert info
    File keyStoreDir = new File(targetDirectory, "keystores");
    keyStoreDir.mkdirs();
    File serviceKeyStoreSource = getKeyStoreFile(configFile, keystoreProperties.getService().getLocation());
    File serviceKeyStoreFile = new File(keyStoreDir, serviceKeyStoreSource.getName());
    FileUtils.copyFile(serviceKeyStoreSource, serviceKeyStoreFile);
    File connectorKeyStoreSource = getKeyStoreFile(configFile, keystoreProperties.getConnector().getLocation());
    File connectorKeyStoreFile = new File(keyStoreDir, connectorKeyStoreSource.getName());
    FileUtils.copyFile(connectorKeyStoreSource, connectorKeyStoreFile);
    EidasKeyConfigData serviceKeyData;
    EidasKeyConfigData connectorKeyData;
    try {
      serviceKeyData = getEidasKeyConfigData(serviceKeyStoreFile, keystoreProperties.getService());
      connectorKeyData = getEidasKeyConfigData(connectorKeyStoreFile, keystoreProperties.getConnector());
    }
    catch (Exception e) {
      throw new IOException(e);
    }

    // Build the eidas.xml file
    System.out.println("Patching eidas.xml config file");
    ConfigFileProcessor eidasXml = new ConfigFileProcessor("eidas.xml", templateDirectory, targetDirectory);
    eidasXml.update("config.base-url", baseProperties.getBaseUrl());
    eidasXml.update("config.country", baseProperties.getCountry());
    // contact
    eidasXml.update("metadata.contact.support.email", metadataProperties.getContact().getSupport().getEmail());
    eidasXml.update("metadata.contact.support.company", metadataProperties.getContact().getSupport().getCompany());
    eidasXml.update("metadata.contact.support.givenname", metadataProperties.getContact().getSupport().getGivenname());
    eidasXml.update("metadata.contact.support.surname", metadataProperties.getContact().getSupport().getSurname());
    eidasXml.update("metadata.contact.support.phone", metadataProperties.getContact().getSupport().getPhone());
    eidasXml.update("metadata.contact.technical.email", metadataProperties.getContact().getTechnical().getEmail());
    eidasXml.update("metadata.contact.technical.company", metadataProperties.getContact().getTechnical().getCompany());
    eidasXml.update("metadata.contact.technical.givenname", metadataProperties.getContact().getTechnical().getGivenname());
    eidasXml.update("metadata.contact.technical.surname", metadataProperties.getContact().getTechnical().getSurname());
    eidasXml.update("metadata.contact.technical.phone", metadataProperties.getContact().getTechnical().getPhone());
    // Organization
    eidasXml.update("metadata.organization.name", metadataProperties.getOrganization().getName());
    eidasXml.update("metadata.organization.displayname", metadataProperties.getOrganization().getDisplayname());
    eidasXml.update("metadata.organization.url", metadataProperties.getOrganization().getUrl());
    // Services
    eidasXml.update("config.service-count", servicesProperties.getService().size());
    // Build services config data
    List<String> counties = new ArrayList<>(servicesProperties.getService().keySet());
    StringBuilder b = new StringBuilder();
    for (int i = 0; i<counties.size() ; i++) {
      String country = counties.get(i);
      ServicesProperties.ServiceData serviceData = servicesProperties.getService().get(country);
      b.append(getServiceEntry(i, "id", country));
      b.append(getServiceEntry(i, "name", serviceData.getName()));
      b.append(getServiceEntry(i, "skew.notbefore", "0"));
      b.append(getServiceEntry(i, "skew.notonorafter", "0"));
      b.append(getServiceEntry(i, "metadata.url", serviceData.getMetadata()));
    }
    eidasXml.update("service.information", b.toString());


    // Build encryption config
    System.out.println("Patching encryptionConf.xml config file");
    ConfigFileProcessor encryptionConf = new ConfigFileProcessor("encryptionConf.xml", templateDirectory, targetDirectory);
    b = new StringBuilder();
    for (String country : counties) {
      b.append("  <entry key=\"EncryptTo.").append(country).append("\">true</entry>\n");
    }
    encryptionConf.update("service.encrypt.policy", b.toString());

    System.out.println("Patching EncryptModule_Connector.xml config file");
    ConfigFileProcessor encryptModuleConnector = new ConfigFileProcessor("EncryptModule_Connector.xml", templateDirectory, targetDirectory);
    encryptModuleConnector.update("keyStorePath", connectorKeyData.getKeyStorePath());
    encryptModuleConnector.update("keyStorePassword", connectorKeyData.getPassword());
    encryptModuleConnector.update("keyStoreType", connectorKeyData.getKeyStoreType());
    encryptModuleConnector.update("subjectName", connectorKeyData.getSubjectDnStr());
    encryptModuleConnector.update("certSerial", connectorKeyData.getSerialNumber());

    System.out.println("Patching SignModule_Connector.xml config file");
    ConfigFileProcessor signModuleConnector = new ConfigFileProcessor("SignModule_Connector.xml", templateDirectory, targetDirectory);
    signModuleConnector.update("connector.sig-algo", keystoreProperties.getConnector().getSignatureAlgorithm());
    signModuleConnector.update("keyStorePath", connectorKeyData.getKeyStorePath());
    signModuleConnector.update("keyStorePassword", connectorKeyData.getPassword());
    signModuleConnector.update("issuerName", connectorKeyData.getIssuerDnStr());
    signModuleConnector.update("certSerial", connectorKeyData.getSerialNumber());
    signModuleConnector.update("keyStoreType", connectorKeyData.getKeyStoreType());

    System.out.println("Patching EncryptModule_Service.xml config file");
    ConfigFileProcessor encryptModuleService = new ConfigFileProcessor("EncryptModule_Service.xml", templateDirectory, targetDirectory);
    encryptModuleService.update("keyStorePath", serviceKeyData.getKeyStorePath());
    encryptModuleService.update("keyStorePassword", serviceKeyData.getPassword());
    encryptModuleService.update("keyStoreType", serviceKeyData.getKeyStoreType());
    encryptModuleService.update("subjectName", serviceKeyData.getSubjectDnStr());
    encryptModuleService.update("certSerial", serviceKeyData.getSerialNumber());

    System.out.println("Patching SignModule_Service.xml config file");
    ConfigFileProcessor signModuleService = new ConfigFileProcessor("SignModule_Service.xml", templateDirectory, targetDirectory);
    signModuleService.update("service.sig-algo", keystoreProperties.getService().getSignatureAlgorithm());
    signModuleService.update("keyStorePath", serviceKeyData.getKeyStorePath());
    signModuleService.update("keyStorePassword", serviceKeyData.getPassword());
    signModuleService.update("issuerName", serviceKeyData.getIssuerDnStr());
    signModuleService.update("certSerial", serviceKeyData.getSerialNumber());
    signModuleService.update("keyStoreType", serviceKeyData.getKeyStoreType());



    System.out.println("Patching specificConnector.xml config file");
    ConfigFileProcessor specificConnector = new ConfigFileProcessor("specificConnector/specificConnector.xml", templateDirectory, targetDirectory);
    specificConnector.update("config.base-url", baseProperties.getBaseUrl());

    System.out.println("Patching specificProxyService.xml config file");
    ConfigFileProcessor specificProxyService = new ConfigFileProcessor("specificProxyService/specificProxyService.xml", templateDirectory, targetDirectory);
    specificProxyService.update("idp.url", idpProperties.getUrl());
    specificProxyService.update("config.base-url", baseProperties.getBaseUrl());
    specificProxyService.update("idp.request-consent", idpProperties.isRequestConsent());
    specificProxyService.update("idp.response-consent", idpProperties.isResponseConsent());

    System.out.println("Patching idp.properties config file");
    ConfigFileProcessor idpIdpProperties = new ConfigFileProcessor("idp/idp.properties", templateDirectory, targetDirectory);
    idpIdpProperties.update("config.country", baseProperties.getCountry());

    System.out.println("Patching sp.properties config file");
    ConfigFileProcessor spSpProperties = new ConfigFileProcessor("sp/sp.properties", templateDirectory, targetDirectory);
    spSpProperties.update("sp.name", spProperties.getName());
    spSpProperties.update("requester.id", spProperties.getRequesterId());
    spSpProperties.update("config.base-url", baseProperties.getBaseUrl());
    spSpProperties.update("config.country", baseProperties.getCountry());


    System.out.println("CEF eIDAS node configuration complete");
  }

  private EidasKeyConfigData getEidasKeyConfigData(File keyStoreFile, KeystoreProperties.KeyStoreData keyStoreData)
    throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

    EidasKeyConfigData.EidasKeyConfigDataBuilder builder = EidasKeyConfigData.builder();
    String type = null;
    String keyStoreFileName = keyStoreFile.getName();
    if (keyStoreFileName.toLowerCase().endsWith(".jks")){
      type = "JKS";
    }
    if (keyStoreFileName.toLowerCase().endsWith(".p12")){
      type = "PKCS12";
    }
    Optional.ofNullable(type).orElseThrow(() -> new IllegalArgumentException("Unrecognized key store file extension. Must be .jks or .p12"));
    KeyStore ks = KeyStore.getInstance(type);
    try(InputStream ksIn = new FileInputStream(keyStoreFile)){
      ks.load(ksIn, keyStoreData.getPassword().toCharArray());
    }
    X509Certificate ksCert = X509Utils.decodeCertificate(ks.getCertificate(keyStoreData.getAlias()).getEncoded());

    return builder
      .keyStoreType(type)
      .password(keyStoreData.getPassword())
      .keyStorePath("keystores/" + keyStoreFileName)
      .issuerDnStr(ksCert.getIssuerDN().toString())
      .subjectDnStr(ksCert.getSubjectDN().toString())
      .serialNumber(ksCert.getSerialNumber().toString(16).toUpperCase())
      .build();
  }

  private File getKeyStoreFile(File configFile, String location) {
    if (location.startsWith("/")){
      return new File(location);
    }
    return new File(configFile.getParentFile(), location);
  }

  private String getServiceEntry(int idx, String name, String val) {
    StringBuilder b = new StringBuilder();
    b.append("  <entry key=\"service")
      .append(idx + 1).append(".")
      .append(name).append("\">").append(val).append("</entry>\n");
    return b.toString();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class EidasKeyConfigData {
    private String keyStorePath;
    private String password;
    private String issuerDnStr;
    private String subjectDnStr;
    private String serialNumber;
    private String keyStoreType;
  }
}
