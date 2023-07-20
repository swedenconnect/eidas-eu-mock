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

import eu.eidas.SimpleProtocol.adapter.DateAdapter;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorValue;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Date;

@XmlDiscriminatorValue("date")
public class DateAttribute extends Attribute implements Serializable {

    private static final long serialVersionUID = -5919455827688107519L;

    protected Date dateValue;

    protected String type;

    @XmlElement
    @XmlJavaTypeAdapter(DateAdapter.class)
    public Date getValue() {
        return dateValue;
    }

    public void setValue(Date value) {
        this.dateValue = value;
    }

	@XmlElement(defaultValue="date",name="type", required=true)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
