/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.SimpleProtocol;

import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorValue;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

@XmlDiscriminatorValue("string_list")
public class StringListAttribute extends Attribute implements Serializable {

    private static final long serialVersionUID = 2069605350045042628L;

    protected List<StringListValue> values;

    protected String type;

    @XmlElement(defaultValue = "string_list", name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlElement(name = "values")
    public List<StringListValue> getValues() {
        return values;
    }

    public void setValues(List<StringListValue> values) {
        this.values = values;
    }
}
