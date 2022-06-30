package eu.eidas.idp.actions;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

public class AuthenticateCitizenAction extends ActionSupport{

	private static final long serialVersionUID = -7243683543548722148L;

	private String SAMLRequest;

	public String execute(){
		return Action.SUCCESS;
	}

	/**
	 * @param SAMLRequest the sAMLRequest to set
	 */
	public void setSAMLRequest(String SAMLRequest) {
		this.SAMLRequest = SAMLRequest;
	}

	/**
	 * @return the SAMLRequest
	 */
	public String getSAMLRequest() {
		return SAMLRequest;
	}

}
