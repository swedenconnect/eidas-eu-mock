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
package eu.eidas.config.impl;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.eidas.config.node.EIDASNodeParameterMeta;

@XmlAccessorType(XmlAccessType.NONE)
public class EIDASParameterMetaImpl extends EIDASNodeParameterMeta{

    @XmlElement(name="info")
    public String getInfoMessageID() {
        return super.getInfoMessageID();
    }
    public void setInfoMessageID(String infoMessageID) {
        super.setInfoMessageID(infoMessageID);
    }

    @XmlElement(name="type")
    public String getTypeAsString(){
        return super.getType().toString();
    }
    public void setTypeAsString(String typeAsString){
        super.setType(Type.fromString(typeAsString));
    }

    @XmlElement(name="label")
    public String getLabel() {
        return super.getLabel();
    }
    public void setLabel(String label) {
        super.setLabel(label);
    }

    @XmlElement(name="default")
    public String getDefaultValue() {
        return super.getDefaultValue();
    }
    public void setDefaultValue(String defaultValue) {
        super.setDefaultValue(defaultValue);
    }

    @XmlElement(name="category")
    public Set<String> getCategories(){
        return super.getCategories();
    }
    public void setCategories(Set<String> categories){
        if(categories!=null){
            //super.setCategories(new HashSet<String>());
            for(String category:categories){
                category=category.trim();
                if(!category.isEmpty()) {
                    super.addCategory(category);
                }
            }
        }
    }
    @XmlAttribute(required = true, namespace="")
    @Override
    public String getName() {
        return super.getName();
    }
    public void setName(String name) {
        super.setName(name);
    }

    private String status;
    @XmlElement(name="status")
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status=status;
    }

    EIDASNodeConfFile sourceFile;
    @XmlTransient
    public EIDASNodeConfFile getSourceFile(){
        return sourceFile;
    }
    public void setSourceFile(EIDASNodeConfFile confFile){
        sourceFile=confFile;
    }


}
