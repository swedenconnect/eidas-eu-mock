package eu.eidas.idp.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import eu.eidas.idp.ProcessLogin;

import java.io.IOException;

public class LoginAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

	private static final long serialVersionUID = -7243683543548722148L;
	private transient HttpServletRequest request;
	private transient HttpServletResponse response;

	private String samlToken;
	private String username;
	private String callback;

	private String ipAddress;

	public String execute() throws IOException {
		ProcessLogin pl = new ProcessLogin();
		pl.processAuthentication(request, response);
		this.samlToken = pl.getSamlToken();
		this.username = pl.getUsername();
		this.callback = pl.getCallback();
		return Action.SUCCESS;
	}

	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setServletResponse(HttpServletResponse response) {
		this.response = response;
	}

	/**
	 * @param samlToken the samlToken to set
	 */
	public void setSamlToken(String samlToken) {
		this.samlToken = samlToken;
	}

	/**
	 * @return the samlToken
	 */
	public String getSamlToken() {
		return samlToken;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param callback the callback to set
	 */
	public void setCallback(String callback) {
		this.callback = callback;
	}

	/**
	 * @return the callback
	 */
	public String getCallback() {
		return callback;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String subjectLocality) {
		this.ipAddress = ipAddress;
	}


}
