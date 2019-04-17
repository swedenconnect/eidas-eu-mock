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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.eidas.samlengineconfig.ConfigurationParameter;
import eu.eidas.samlengineconfig.InstanceConfiguration;

/**
 * implements serialization to/from xml of InstanceConfiguration objects
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public class InstanceConfigurationImpl extends InstanceConfiguration {
    public InstanceConfigurationImpl(){
        super();
    }
    public InstanceConfigurationImpl(String name, List<ConfigurationParameter> parameters){
        setName(name);
        if(parameters!=null) {
            setParameters(parameters);
        }else {
            setParameters(new ArrayList<ConfigurationParameter>());
        }
    }
    @Override
    @XmlAttribute(name="name")
    public String getName(){
        return super.getName();
    }
    public void setName(String name){
        super.setName(name);
    }
    @Override
    @XmlJavaTypeAdapter(ConfigurationParameterAdapter.class)
    @XmlElement(name = "parameter", type = JAXBConfigurationParameter.class)
    public List<ConfigurationParameter> getParameters(){
        return super.getParameters();
    }
    public void setParameters(List<ConfigurationParameter> list){
        super.setParameters(list);
    }


}

