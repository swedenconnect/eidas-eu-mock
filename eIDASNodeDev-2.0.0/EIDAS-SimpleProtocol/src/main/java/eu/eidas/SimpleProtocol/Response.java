package eu.eidas.SimpleProtocol;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "response")
public class Response implements Serializable {

    private static final long serialVersionUID = -2174784578493805682L;

    protected String version;

    protected String id;

    protected String nameId;

    protected String subject;

    protected String destination;

    protected String inResponseTo;

    protected String clientIpAddress;

    protected String createdOn;

    protected String authContextClass;

    protected String issuer;

    protected ResponseStatus status;

    protected List<Attribute> attributes;

    @XmlElement(required = true, nillable = false)
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @XmlElement(name = "id", required = true, nillable = false)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name = "subject", required = true, nillable = false)
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @XmlElement(name = "destination")
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.subject = destination;
    }

    @XmlElement(name = "name_id", required = true, nillable = false)
    public String getNameId() {
        return nameId;
    }

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    @XmlElement(name = "inresponse_to", required = true, nillable = false)
    public String getInResponseTo() {
        return inResponseTo;
    }

    public void setInResponseTo(String inResponseTo) {
        this.inResponseTo = inResponseTo;
    }

    @XmlElement(name = "created_on", required = true, nillable = false)
    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    @XmlElement(name = "authentication_context_class")
    public String getAuthContextClass() {
        return authContextClass;
    }

    public void setAuthContextClass(String authContextClass) {
        this.authContextClass = authContextClass;
    }

    @XmlElement(required = true, nillable = false)
    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    @XmlElement(required = true)
    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    @XmlElement(name = "attribute_list", required = true)
    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    @XmlElement(name = "client_Ip_Address", required = true)
    public String getClientIpAddress() {
        return clientIpAddress;
    }

    public void setClientIpAddress(String clientIpAddress) {
        this.clientIpAddress = clientIpAddress;
    }
}
