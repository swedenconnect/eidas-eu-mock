package eu.eidas.SimpleProtocol;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

//https://stackoverflow.com/questions/10795793/i-cant-understand-why-this-jaxb-illegalannotationexception-is-thrown

@XmlRootElement(name = "authentication_request")
public class AuthenticationRequest implements Serializable {

    private static final long serialVersionUID = -6264588477993174860L;

    protected String version = "1";

    protected String id;

    protected String destination;

    protected String createdOn = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")).format(Calendar.getInstance().getTime());


    protected boolean forceAuthentication = true;

    protected String nameIdPolicy;

    protected String providerName;

    protected String spType;

    protected RequestedAuthenticationContext authContext;

    protected List<Attribute> attributes;

    protected String serviceUrl;

    protected String citizenCountry;

    @XmlElement(name = "serviceUrl")
    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    @XmlElement(defaultValue = "1", required = true, nillable = false)
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @XmlElement(name = "name_id_policy")
    public String getNameIdPolicy() {
        return nameIdPolicy;
    }

    public void setNameIdPolicy(String nameIdPolicy) {
        this.nameIdPolicy = nameIdPolicy;
    }

    @XmlElement(required = true, nillable = false)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(required = true, nillable = false)
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @XmlElement(name = "created_on", required = true, nillable = false)
    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    @XmlElement(name = "force_authentication", defaultValue = "true", required = true, nillable = false)
    public boolean isForceAuthentication() {
        return forceAuthentication;
    }

    public void setForceAuthentication(boolean forceAuthentication) {
        this.forceAuthentication = forceAuthentication;
    }

    @XmlElement(name = "provider_name", required = true, nillable = false)
    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    @XmlElement(name = "sp_type")
    public String getSpType() {
        return spType;
    }

    public void setSpType(String spType) {
        this.spType = spType;
    }

    @XmlElement(name = "requested_authentication_context", required = true)
    public RequestedAuthenticationContext getAuthContext() {
        return authContext;
    }

    public void setAuthContext(RequestedAuthenticationContext authContext) {
        this.authContext = authContext;
    }

    @XmlElement(name = "attribute_list", required = true)
    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    @XmlElement(name = "citizen_country")
    public String getCitizenCountry() { return citizenCountry; }

    public void setCitizenCountry(String citizen_country) { this.citizenCountry = citizen_country; }
}
