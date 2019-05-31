package eu.eidas.idp.actions;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;
import eu.eidas.idp.custom.BasicUserInfo;
import eu.eidas.idp.custom.ScProcessLogin;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class AuthenticateCitizenAction extends ActionSupport {

  private static final long serialVersionUID = -7243683543548722148L;

  private String SAMLRequest;
  private List<BasicUserInfo> userInfoList;

  public String execute() {

    Properties userProp = ScProcessLogin.loadConfigs("user.properties");
    List<String> userIdList = Collections.list(userProp.propertyNames()).stream()
      .map(o -> (String) o)
      .filter(s -> s.split("\\.").length == 1)
      .collect(Collectors.toList());

    userInfoList = userIdList.stream()
      .map(s -> getBasicUserInfo(s, userProp))
      .filter(basicUserInfo -> basicUserInfo != null)
      .sorted((o1, o2) -> o1.getDisplayName().toLowerCase().compareTo(o2.getDisplayName().toLowerCase()))
      .collect(Collectors.toList());

    return Action.SUCCESS;
  }

  private BasicUserInfo getBasicUserInfo(String s, Properties userProp) {

    String givenName = userProp.getProperty(s + ".eidas.europa.eu/attributes/naturalperson/CurrentGivenName");
    String surname = userProp.getProperty(s + ".eidas.europa.eu/attributes/naturalperson/CurrentFamilyName");
    String legalPersonName = userProp.getProperty(s + ".eidas.europa.eu/attributes/legalperson/LegalName");

    String displayName = null;
    if (givenName != null && surname != null) {
      displayName = givenName + " " + surname;
    }
    else {
      if (legalPersonName != null) {
        displayName = legalPersonName;
      }
    }

    if (displayName == null) {
      return null;
    }

    return new BasicUserInfo(s, displayName, userProp.getProperty(s + ".description"));
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

  public List<BasicUserInfo> getUserInfoList() {
    return userInfoList;
  }

}
