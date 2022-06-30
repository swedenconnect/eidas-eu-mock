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
package eu.eidas.samlengineconfig.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.eidas.samlengineconfig.EngineInstance;
import eu.eidas.samlengineconfig.SamlEngineConfiguration;

@XmlRootElement(name = "instances")
@XmlAccessorType(XmlAccessType.NONE)
public class SamlEngineConfigurationImpl extends SamlEngineConfiguration {

    @Override
    @XmlElement(name = "instance", type=EngineInstanceImpl.class)
    public List<EngineInstance> getInstances(){
        return super.getInstances();
    }
    @Override
    public void setInstances(List<EngineInstance> engineInstances){
        super.setInstances(engineInstances);
    }

    public Map<String, EngineInstance> getInstanceMap(){
        Map<String, EngineInstance> result=new HashMap<String, EngineInstance>();
        for(EngineInstance instance:getInstances()){
            result.put(instance.getName(), instance);
        }
        return result;
    }
}
