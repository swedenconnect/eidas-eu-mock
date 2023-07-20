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

@XmlDiscriminatorValue("values")
public class StringListValue implements Serializable {

	private static final long serialVersionUID = -6027743545309215878L;

	protected Boolean latinScript;

	protected String value;

	@XmlElement(name="latin_script")
	public Boolean getLatinScript() {
		return latinScript;
	}

	public void setLatinScript(Boolean latinScript) {
		this.latinScript = latinScript;
	}

	@XmlElement
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
