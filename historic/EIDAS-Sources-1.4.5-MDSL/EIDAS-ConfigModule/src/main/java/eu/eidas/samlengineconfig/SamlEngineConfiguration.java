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
 * represent a saml engine master config. it is made of one or more EngineInstance object
 * (corresponds to SamlEngine.xml file)
 */
public class SamlEngineConfiguration {
    private List<EngineInstance> instances=new ArrayList<EngineInstance>();
    private Set<String> instanceNames=new HashSet<String>();
    public List<EngineInstance> getInstances() {
        return instances;
    }

    public void setInstances(List<EngineInstance> instances) {
        if(instances==null) {
            return;
        }
        this.instances=new ArrayList<EngineInstance>();
        instanceNames=new HashSet<String>();
        for (EngineInstance instance : instances) {
            addInstance(instance);
        }
    }
    public void addInstance(EngineInstance instance){
        if(instance.getName()!=null && !instance.getName().isEmpty() && !instanceNames.contains(instance.getName())) {
            this.instances.add(instance);
            instanceNames.add(instance.getName());
        }
    }


}
