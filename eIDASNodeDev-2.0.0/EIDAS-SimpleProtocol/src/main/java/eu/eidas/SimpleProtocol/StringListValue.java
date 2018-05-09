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
