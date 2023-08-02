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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.swedenconnect.eidas.cef.confbuilder.configuration.BaseProperties;
import se.swedenconnect.eidas.cef.confbuilder.configuration.IdpProperties;
import se.swedenconnect.eidas.cef.confbuilder.configuration.KeystoreProperties;
import se.swedenconnect.eidas.cef.confbuilder.configuration.MetadataProperties;
import se.swedenconnect.eidas.cef.confbuilder.configuration.ServicesProperties;
import se.swedenconnect.eidas.cef.confbuilder.configuration.SpProperties;
import se.swedenconnect.eidas.cef.confbuilder.handler.ConfigFileProcessor;
import se.swedenconnect.eidas.cef.confbuilder.handler.EIDASNodeConfigBuilder;
import se.swedenconnect.eidas.cef.confbuilder.utils.X509Utils;

/**
 * Description
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
public class CEF27ConfigBuilder implements EIDASNodeConfigBuilder {

  @Override public void buildConfiguration(
    File configFile, File templateDirectory, File targetDirectory,
    BaseProperties baseProperties, MetadataProperties metadataProperties,
    ServicesProperties servicesProperties, KeystoreProperties keystoreProperties,
    IdpProperties idpProperties, SpProperties spProperties
  ) throws IOException {
    System.out.println("Building configuration data for CEF node version 2.7.0");

    // Copy config file directory
    System.out.println("Copying template files to target directory");
    FileUtils.copyDirectory(templateDirectory, targetDirectory);

    // Define config directories
    File dirTargetConnector = new File(targetDirectory, "connector");
    File dirTemplateConnector = new File(templateDirectory, "connector");
    File dirTargetService = new File(targetDirectory, "proxy");
    File dirTemplateService = new File(templateDirectory, "proxy");
    File dirTargetSpecificConnector = new File(targetDirectory, "specificConnector");
    File dirTemplateSpecificConnector = new File(templateDirectory, "specificConnector");
    File dirTargetSpecificService = new File(targetDirectory, "specificProxyService");
    File dirTemplateSpecificService = new File(templateDirectory, "specificProxyService");


    // Save key stores and get key data
    File dirTargetConnectorKeyStore = new File(dirTargetConnector, "keystore");
    EidasKeyConfigData connectorKeyData = storeKey(configFile, keystoreProperties.getConnector(), dirTargetConnectorKeyStore);
    File dirTargetServiceKeyStore = new File(dirTargetService, "keystore");
    EidasKeyConfigData serviceKeyData = storeKey(configFile, keystoreProperties.getService(), dirTargetServiceKeyStore);

    // Build the Connector config
    System.out.println("Patching Connector eidas.xml config file");
    ConfigFileProcessor connectorEidasXml = new ConfigFileProcessor("eidas.xml", dirTemplateConnector, dirTargetConnector);
    connectorEidasXml.update("config.base-url", baseProperties.getBaseUrl());
    connectorEidasXml.update("config.country", baseProperties.getCountry());
    // contact
    connectorEidasXml.update("metadata.contact.support.email", metadataProperties.getContact().getSupport().getEmail());
    connectorEidasXml.update("metadata.contact.support.company", metadataProperties.getContact().getSupport().getCompany());
    connectorEidasXml.update("metadata.contact.support.givenname", metadataProperties.getContact().getSupport().getGivenname());
    connectorEidasXml.update("metadata.contact.support.surname", metadataProperties.getContact().getSupport().getSurname());
    connectorEidasXml.update("metadata.contact.support.phone", metadataProperties.getContact().getSupport().getPhone());
    connectorEidasXml.update("metadata.contact.technical.email", metadataProperties.getContact().getTechnical().getEmail());
    connectorEidasXml.update("metadata.contact.technical.company", metadataProperties.getContact().getTechnical().getCompany());
    connectorEidasXml.update("metadata.contact.technical.givenname", metadataProperties.getContact().getTechnical().getGivenname());
    connectorEidasXml.update("metadata.contact.technical.surname", metadataProperties.getContact().getTechnical().getSurname());
    connectorEidasXml.update("metadata.contact.technical.phone", metadataProperties.getContact().getTechnical().getPhone());
    // Organization
    connectorEidasXml.update("metadata.organization.name", metadataProperties.getOrganization().getName());
    connectorEidasXml.update("metadata.organization.displayname", metadataProperties.getOrganization().getDisplayname());
    connectorEidasXml.update("metadata.organization.url", metadataProperties.getOrganization().getUrl());
    // Services
    connectorEidasXml.update("config.service-count", servicesProperties.getService().size());
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
    connectorEidasXml.update("service.information", b.toString());
    connectorEidasXml.update("metadata.sector", Optional.ofNullable(metadataProperties.getSpType())
      .map(spType -> "  <entry key=\"metadata.sector\">" + spType + "</entry>")
      .orElse(""));


    // Build the Proxy Service config
    System.out.println("Patching Proxy Service eidas.xml config file");
    ConfigFileProcessor serviceEidasXml = new ConfigFileProcessor("eidas.xml", dirTemplateService, dirTargetService);
    serviceEidasXml.update("config.base-url", baseProperties.getBaseUrl());
    serviceEidasXml.update("config.country", baseProperties.getCountry());
    // contact
    serviceEidasXml.update("metadata.contact.support.email", metadataProperties.getContact().getSupport().getEmail());
    serviceEidasXml.update("metadata.contact.support.company", metadataProperties.getContact().getSupport().getCompany());
    serviceEidasXml.update("metadata.contact.support.givenname", metadataProperties.getContact().getSupport().getGivenname());
    serviceEidasXml.update("metadata.contact.support.surname", metadataProperties.getContact().getSupport().getSurname());
    serviceEidasXml.update("metadata.contact.support.phone", metadataProperties.getContact().getSupport().getPhone());
    serviceEidasXml.update("metadata.contact.technical.email", metadataProperties.getContact().getTechnical().getEmail());
    serviceEidasXml.update("metadata.contact.technical.company", metadataProperties.getContact().getTechnical().getCompany());
    serviceEidasXml.update("metadata.contact.technical.givenname", metadataProperties.getContact().getTechnical().getGivenname());
    serviceEidasXml.update("metadata.contact.technical.surname", metadataProperties.getContact().getTechnical().getSurname());
    serviceEidasXml.update("metadata.contact.technical.phone", metadataProperties.getContact().getTechnical().getPhone());
    // Organization
    serviceEidasXml.update("metadata.organization.name", metadataProperties.getOrganization().getName());
    serviceEidasXml.update("metadata.organization.displayname", metadataProperties.getOrganization().getDisplayname());
    serviceEidasXml.update("metadata.organization.url", metadataProperties.getOrganization().getUrl());

    // Service LoA
    serviceEidasXml.update("service.loa", getServiceLoa(metadataProperties.getServiceLoa()));



    // Build encryption config
    System.out.println("Patching Proxy Service encryptionConf.xml config file");
    ConfigFileProcessor encryptionConf = new ConfigFileProcessor("encryptionConf.xml", dirTemplateService, dirTargetService);
    b = new StringBuilder();
    for (String country : counties) {
      b.append("  <entry key=\"EncryptTo.").append(country).append("\">true</entry>\n");
    }
    encryptionConf.update("service.encrypt.policy", b.toString());

    System.out.println("Patching EncryptModule_Connector.xml config file");
    ConfigFileProcessor encryptModuleConnector = new ConfigFileProcessor("EncryptModule_Connector.xml", dirTemplateConnector, dirTargetConnector);
    encryptModuleConnector.update("keyStorePath", connectorKeyData.getKeyStorePath());
    encryptModuleConnector.update("keyStorePassword", connectorKeyData.getPassword());
    encryptModuleConnector.update("keyStoreType", connectorKeyData.getKeyStoreType());
    encryptModuleConnector.update("subjectName", connectorKeyData.getSubjectDnStr());
    encryptModuleConnector.update("certSerial", connectorKeyData.getSerialNumber());

    System.out.println("Patching SignModule_Connector.xml config file");
    ConfigFileProcessor signModuleConnector = new ConfigFileProcessor("SignModule_Connector.xml", dirTemplateConnector, dirTargetConnector);
    signModuleConnector.update("connector.sig-algo", keystoreProperties.getConnector().getSignatureAlgorithm());
    signModuleConnector.update("keyStorePath", connectorKeyData.getKeyStorePath());
    signModuleConnector.update("keyStorePassword", connectorKeyData.getPassword());
    signModuleConnector.update("issuerName", connectorKeyData.getIssuerDnStr());
    signModuleConnector.update("certSerial", connectorKeyData.getSerialNumber());
    signModuleConnector.update("keyStoreType", connectorKeyData.getKeyStoreType());

    System.out.println("Patching SignModule_Service.xml config file");
    ConfigFileProcessor signModuleService = new ConfigFileProcessor("SignModule_Service.xml", dirTemplateService, dirTargetService);
    signModuleService.update("service.sig-algo", keystoreProperties.getService().getSignatureAlgorithm());
    signModuleService.update("keyStorePath", serviceKeyData.getKeyStorePath());
    signModuleService.update("keyStorePassword", serviceKeyData.getPassword());
    signModuleService.update("issuerName", serviceKeyData.getIssuerDnStr());
    signModuleService.update("certSerial", serviceKeyData.getSerialNumber());
    signModuleService.update("keyStoreType", serviceKeyData.getKeyStoreType());


    System.out.println("Patching specificConnector.xml config file");
    ConfigFileProcessor specificConnector = new ConfigFileProcessor("specificConnector.xml", dirTemplateSpecificConnector, dirTargetSpecificConnector);
    specificConnector.update("config.base-url", baseProperties.getBaseUrl());

    System.out.println("Patching specificProxyService.xml config file");
    ConfigFileProcessor specificProxyService = new ConfigFileProcessor("specificProxyService.xml", dirTemplateSpecificService, dirTargetSpecificService);
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


    System.out.println("CEF 2.7 eIDAS node configuration complete");
  }

  private EidasKeyConfigData storeKey(File configFile, KeystoreProperties.KeyStoreData keyStoreData, File targetKeyStoreDir)
    throws IOException {
    // Get key and cert info
    // TODO Fix key store saving for each service below
    File keyStoreSourceFile = getKeyStoreFile(configFile, keyStoreData.getLocation());
    File keyStoreTargetFile = new File(targetKeyStoreDir, keyStoreSourceFile.getName());
    FileUtils.copyFile(keyStoreSourceFile, keyStoreTargetFile);
    try {
      return getEidasKeyConfigData(keyStoreTargetFile, keyStoreData);
    }
    catch (Exception e) {
      throw new IOException(e);
    }
  }

  private String getSPTypeEntry(String spType) {
    return Optional.ofNullable(spType)
      .map(s -> "<entry key=\"metadata.sector\">" + s + "</entry>")
      .orElse("");
  }

  private String getServiceLoa(List<String> serviceLoaList) {
    if (serviceLoaList == null || serviceLoaList.isEmpty()){
      return "";
    }
    StringBuilder b = new StringBuilder();
    b.append("  <entry key=\"service.LoA\">\n         ");
    b.append(String.join(",\n         ", serviceLoaList));
    b.append("\n  </entry>");
    return b.toString();
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
      .keyStorePath("./keystore/" + keyStoreFileName)
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
