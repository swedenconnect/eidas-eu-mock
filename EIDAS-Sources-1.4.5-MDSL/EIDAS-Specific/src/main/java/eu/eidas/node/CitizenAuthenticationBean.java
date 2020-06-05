package eu.eidas.node;

import eu.eidas.auth.specific.IAUService;

public class CitizenAuthenticationBean {
  /**
   * Specific Node service.
   */
  private transient IAUService specAuthenticationNode;

  /**
   * Is IdP external?
   */
  private boolean externalAuth;

  /**
   * URL of IdP.
   */
  private String idpUrl;

  public boolean isExternalAuth() {
    return externalAuth;
  }

  public void setExternalAuth(boolean externalAuth) {
    this.externalAuth = externalAuth;
  }

  public String getIdpUrl() {
    return idpUrl;
  }

  public void setIdpUrl(String idpUrl) {
    this.idpUrl = idpUrl;
  }

  public IAUService getSpecAuthenticationNode() {

    return specAuthenticationNode;
  }

  public void setSpecAuthenticationNode(IAUService specAuthenticationNode) {
    this.specAuthenticationNode = specAuthenticationNode;
  }
}
