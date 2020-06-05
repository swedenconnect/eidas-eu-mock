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
package eu.eidas.config.node;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

/**
 * eIDAS Node parameter metadata information
 */
@XmlTransient
public class EIDASNodeParameterMeta {
    public enum Type{
        INT,
        STRING,
        BOOLEAN;
        //TODO is it worth having other types like URL, double, list of strings?


        public static Type fromString(String stringValue){
            if("i".equalsIgnoreCase(stringValue) || "int".equalsIgnoreCase(stringValue)) {
                return INT;
            }else if("b".equalsIgnoreCase(stringValue) || "boolean".equalsIgnoreCase(stringValue)){
                return BOOLEAN;
            }
            return STRING;
        }
    }
    String infoMessageID;
    String label;
    Type type;
    String defaultValue;
    String name;
    Set<String> categories=new HashSet<String>();

    public String getInfoMessageID() {
        return infoMessageID;
    }

    public void setInfoMessageID(String infoMessageID) {
        this.infoMessageID = infoMessageID;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    public void setCategories(Set<String> categories){
        this.categories=categories;
    }
    public Set<String> getCategories(){
        return categories;
    }
    public void addCategory(String category){
        categories.add(category);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
