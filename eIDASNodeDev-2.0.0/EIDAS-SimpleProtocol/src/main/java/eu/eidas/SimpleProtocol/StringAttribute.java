package eu.eidas.SimpleProtocol;

import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorValue;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

@XmlDiscriminatorValue("string")
public class StringAttribute extends Attribute implements Serializable {

    private static final long serialVersionUID = 4082836582313904617L;

    protected String type;

    protected String value;

    protected Boolean latinScript;

    @XmlElement(defaultValue = "string", name = "type", required = true)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlElement
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @XmlElement(name = "latin_script")
    public Boolean getLatinScript() {
        return latinScript;
    }

    public void setLatinScript(Boolean latinScript) {
        this.latinScript = latinScript;
    }


}
