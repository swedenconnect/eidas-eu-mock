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
