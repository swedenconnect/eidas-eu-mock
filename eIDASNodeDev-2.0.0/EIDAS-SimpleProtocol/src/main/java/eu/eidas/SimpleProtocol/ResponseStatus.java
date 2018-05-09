package eu.eidas.SimpleProtocol;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

public class ResponseStatus implements Serializable {

	private static final long serialVersionUID = -7295082457165791645L;
	
	protected String statusCode;

	protected String subStatusCode;

	protected String statusMessage;

	@XmlElement(name="status_code", required=true, nillable=false)
	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	@XmlElement(name="sub_status_code")
	public String getSubStatusCode() {
		return subStatusCode;
	}

	public void setSubStatusCode(String subStatusCode) {
		this.subStatusCode = subStatusCode;
	}

	@XmlElement(name="status_message")
	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	
}
