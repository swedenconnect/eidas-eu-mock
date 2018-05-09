package eu.eidas.SimpleProtocol;

import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorValue;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;


@XmlDiscriminatorValue("addressId")
public class AddressAttribute extends Attribute implements Serializable {

    private static final long serialVersionUID = -2591701187455810317L;

    protected ComplexAddressAttribute value;

    protected String type;

    @XmlElement
    public ComplexAddressAttribute getValue() {
        return value;
    }

    public void setValue(ComplexAddressAttribute value) {
        this.value = value;
    }

    @XmlElement(defaultValue = "addressId", name = "type", required = true)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
