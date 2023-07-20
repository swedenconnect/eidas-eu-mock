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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.eidas.SimpleProtocol.adapter.DateAdapter;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorNode;
import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorValue;

import java.io.Serializable;
import java.util.Date;

@XmlDiscriminatorNode("@type")
@XmlDiscriminatorValue("requested_attribute")
@XmlSeeAlso({StringAttribute.class, StringListAttribute.class, DateAttribute.class, AddressAttribute.class})
public class Attribute implements Serializable {

	private static final long serialVersionUID = 2710254527099932149L;

	protected String name;

	protected Boolean required;

	@XmlElement(required=true, nillable=false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(required=false)
	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

}
