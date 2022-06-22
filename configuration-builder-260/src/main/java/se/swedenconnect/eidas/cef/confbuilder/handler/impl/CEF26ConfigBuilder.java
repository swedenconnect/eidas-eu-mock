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

import se.swedenconnect.eidas.cef.confbuilder.configuration.*;
import se.swedenconnect.eidas.cef.confbuilder.handler.ConfigFileProcessor;
import se.swedenconnect.eidas.cef.confbuilder.handler.EIDASNodeConfigBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Description
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
public class CEF26ConfigBuilder implements EIDASNodeConfigBuilder {

  @Override public void buildConfiguration(
    File configDirectory, File templateDirectory, File targetDirectory,
    BaseProperties baseProperties, MetadataProperties metadataProperties,
    ServicesProperties servicesProperties, KeystoreProperties keystoreProperties,
    IdpProperties idpProperties, SpProperties spProperties
  ) throws IOException {
    System.out.println("Building configuration data for CEF node version 2.6.0");

    // Build the eidas.xml file
    ConfigFileProcessor eidasXml = new ConfigFileProcessor("eidas.xml", templateDirectory, targetDirectory);
    eidasXml.update("config.base-url", baseProperties.getBaseUrl());
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

    int sdf=0;

  }

  private String getServiceEntry(int idx, String name, String val) {
    StringBuilder b = new StringBuilder();
    b.append("  <entry key=\"service")
      .append(idx + 1).append(".")
      .append(name).append("\">").append(val).append("</entry>\n");
    return b.toString();
  }
}
