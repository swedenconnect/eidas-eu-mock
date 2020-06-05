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

import java.util.List;
import java.util.Map;
import java.util.Properties;

import eu.eidas.config.node.EIDASNodeCountry;
import eu.eidas.config.node.EIDASNodeMetaconfigProvider;
import eu.eidas.config.node.EIDASNodeParameter;
import eu.eidas.config.samlmetadata.MetadataRepository;
import eu.eidas.samlengineconfig.SamlEngineConfiguration;

/**
 * eidas.xml mapper
 */
public abstract class EIDASNodeConfiguration {
    protected List<EIDASNodeCountry> countries;
    protected Map<String, EIDASNodeParameter> parameters;
    protected ConfigurationRepository repository;
    protected SamlEngineConfiguration samlEngineConfiguration;
    protected EIDASNodeMetaconfigProvider metadataProvider;
    protected MetadataRepository samlMetadataRepository;
    public abstract void load();
    public abstract void save();

    /**
     *
     * @return name-value pairs
     */
    public abstract Properties getEidasProperties();
    public Map<String, EIDASNodeParameter> getNodeParameters(){
        return parameters;
    }
    public List<EIDASNodeCountry> getEidasCountries(){
        return countries;
    }


    public ConfigurationRepository getRepository() {
        return repository;
    }

    public void setRepository(ConfigurationRepository repository) {
        this.repository = repository;
    }

    public SamlEngineConfiguration getSamlEngineConfiguration() {
        return samlEngineConfiguration;
    }

    public void setSamlEngineConfiguration(SamlEngineConfiguration samlEngineConfiguration) {
        this.samlEngineConfiguration = samlEngineConfiguration;
    }

    public EIDASNodeMetaconfigProvider getMetadataProvider() {
        return metadataProvider;
    }

    public void setMetaconfigProvider(EIDASNodeMetaconfigProvider metadataProvider) {
        this.metadataProvider = metadataProvider;
    }

    public MetadataRepository getSamlMetadataRepository() {
        return samlMetadataRepository;
    }

    public void setSamlMetadataRepository(MetadataRepository samlMetadataRepository) {
        this.samlMetadataRepository = samlMetadataRepository;
    }
}
