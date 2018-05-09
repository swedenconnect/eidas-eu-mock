package eu.eidas.SimpleProtocol;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class RequestedAuthenticationContext implements Serializable {

	private static final long serialVersionUID = 4688410114575589607L;

	protected String comparison = "minimum";

	protected List<String> contextClass;

	@XmlElement(defaultValue="minimum", required=true, nillable=false)
	public String getComparison() {
		return comparison;
	}

	public void setComparison(String comparison) {
		this.comparison = comparison;
	}

	@XmlElement(name="context_class", required=true, nillable=false)
	public List<String> getContextClass() {
		return contextClass;
	}

	public void setContextClass(List<String> contextClass) {
		this.contextClass = contextClass;
	}

	
}
