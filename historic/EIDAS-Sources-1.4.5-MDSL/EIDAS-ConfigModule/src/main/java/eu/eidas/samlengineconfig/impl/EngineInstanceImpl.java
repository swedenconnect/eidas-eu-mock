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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.eidas.samlengineconfig.EngineInstance;
import eu.eidas.samlengineconfig.InstanceConfiguration;

/**
 * used for jaxb marshalling/unmarshalling of EngineInstances
 */
@XmlRootElement(name="EngineInstance")
@XmlAccessorType(XmlAccessType.NONE)
public class EngineInstanceImpl extends EngineInstance {
    public EngineInstanceImpl(){
        super();
    }
    @XmlElement(name = "configuration", type = InstanceConfigurationImpl.class)
    public List<InstanceConfiguration> getConfigurations() {
        return super.getConfigurations();
    }

    @XmlAttribute(required = true, namespace="")
    @Override
    public String getName(){
        return super.getName();
    }
    @Override
    public void setName(String name){
        super.setName(name);
    }
    public void setConfigurations(List<InstanceConfiguration> configurations) {
        super.setConfigurations(configurations);
    }

}
