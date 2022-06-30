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

import eu.eidas.config.EIDASNodeConfiguration;

/**
 * base CertificateConfigurationManager implementation
 */
public abstract class AbstractCertificateConfigurationManager implements CertificateConfigurationManager {

    private String location;
    private EIDASNodeConfiguration parentConfiguration;

    @Override
    public void setLocation(String location) {
        this.location=location;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public boolean isActive(){
        return false;
    }

    public EIDASNodeConfiguration getParentConfiguration() {
        return parentConfiguration;
    }

    public void setParentConfiguration(EIDASNodeConfiguration parentConfiguration) {
        this.parentConfiguration = parentConfiguration;
    }
}
