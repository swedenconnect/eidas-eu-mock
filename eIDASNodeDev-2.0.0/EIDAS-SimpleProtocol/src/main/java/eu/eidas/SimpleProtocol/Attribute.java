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
