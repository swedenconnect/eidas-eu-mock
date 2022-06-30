package eu.eidas.sp.custom;

public class AuthnContext {
  private String loa;
  private String idp;
  private String notBefore;

  public String getLoa() {
    return loa;
  }

  public void setLoa(String loa) {
    this.loa = loa;
  }

  public String getIdp() {
    return idp;
  }

  public void setIdp(String idp) {
    this.idp = idp;
  }

  public String getNotBefore() {
    return notBefore;
  }

  public void setNotBefore(String notBefore) {
    this.notBefore = notBefore;
  }
}
