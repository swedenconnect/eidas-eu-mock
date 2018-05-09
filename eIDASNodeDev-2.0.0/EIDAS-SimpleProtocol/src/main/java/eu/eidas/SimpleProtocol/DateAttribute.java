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
