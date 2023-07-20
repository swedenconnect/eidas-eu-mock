/*
 * Copyright (c) 2020 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package eu.eidas.SimpleProtocol;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class RequestedAuthenticationContext implements Serializable {

	private static final long serialVersionUID = 4688410114575589607L;

	protected String comparison = "minimum";

	protected List<String> contextClass;

	protected List<String> nonNotifiedContextClass;

	@XmlElement(defaultValue="minimum", required=true, nillable=false)
	public String getComparison() {
		return comparison;
	}

	public void setComparison(String comparison) {
		this.comparison = comparison;
	}

	@XmlElement(name="context_class")
	public List<String> getContextClass() {
		return contextClass;
	}

	public void setContextClass(List<String> contextClass) {
		this.contextClass = contextClass;
	}

	@XmlElement(name="non_notified_context_class")
	public List<String> getNonNotifiedContextClass() {
		return nonNotifiedContextClass;
	}

	public void setNonNotifiedContextClass(List<String> nonNotifiedContextClass) {
		this.nonNotifiedContextClass = nonNotifiedContextClass;
	}

	
}
