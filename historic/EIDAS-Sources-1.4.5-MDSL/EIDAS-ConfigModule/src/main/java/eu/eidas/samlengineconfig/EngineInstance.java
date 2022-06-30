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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * represent a saml engine instance, used either for signing or for encrypting
 */
public class EngineInstance {
    private String name;
    private List<InstanceConfiguration> configurations=new ArrayList<InstanceConfiguration>();
    private Set<String> configurationNames=new HashSet<String>();
    public List<InstanceConfiguration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<InstanceConfiguration> configurations) {
        if(configurations==null) {
            return;
        }
        this.configurations=new ArrayList<InstanceConfiguration>();
        configurationNames=new HashSet<String>();
        for(InstanceConfiguration config:configurations){
            addConfiguration(config);
        }
    }
    public void addConfiguration(InstanceConfiguration config){
        if(config.getName()!=null && !config.getName().isEmpty() && !configurationNames.contains(config.getName())) {
            this.configurations.add(config);
            configurationNames.add(config.getName());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
