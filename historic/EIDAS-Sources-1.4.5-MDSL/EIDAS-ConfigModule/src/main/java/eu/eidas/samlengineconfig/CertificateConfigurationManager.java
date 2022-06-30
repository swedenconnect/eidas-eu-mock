/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.eidas.samlengineconfig;

import java.util.Map;

import eu.eidas.config.EIDASNodeConfiguration;

/**
 * manages a repository of saml engine configuration, including signing and encryption configurations
 */
public interface CertificateConfigurationManager {

    void setLocation(String location);
    String getLocation();
    void setParentConfiguration(EIDASNodeConfiguration parentConfiguration);
    EIDASNodeConfiguration getParentConfiguration();
    void addConfiguration(String name, String type, Map<String,String> props, boolean replaceExisting);
    EngineInstance getInstance(String name);
    Map<String, EngineInstance> getConfiguration();
    boolean isActive();
}
