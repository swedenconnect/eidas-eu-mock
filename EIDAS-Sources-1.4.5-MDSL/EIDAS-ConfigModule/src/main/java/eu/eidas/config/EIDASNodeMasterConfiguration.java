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
package eu.eidas.config;


/**
 * umbrella for different sources of eIDAS Node configuration parameters
 */
public class EIDASNodeMasterConfiguration {
    ConfigurationRepository repository;
    EIDASNodeConfiguration eidasNodeConfiguration;

    public ConfigurationRepository getRepository() {
        return repository;
    }

    public void setRepository(ConfigurationRepository repository) {
        this.repository = repository;
    }

    public EIDASNodeConfiguration getNodeConfiguration() {
        if(eidasNodeConfiguration!=null && eidasNodeConfiguration.getRepository()==null){
            eidasNodeConfiguration.setRepository(repository);
        }
        return eidasNodeConfiguration;
    }

    public void setNodeConfiguration(EIDASNodeConfiguration nodeConfiguration) {
        this.eidasNodeConfiguration = nodeConfiguration;
    }

    public byte[] getRawContent(String url){
        return getRepository().getRawContent(url);
    }
    public void setRawContent(String url, byte[] os){
        getRepository().setRawContent(url, os);
    }
    public void backup() throws ConfigurationException {
        getRepository().backup();
    }
}
