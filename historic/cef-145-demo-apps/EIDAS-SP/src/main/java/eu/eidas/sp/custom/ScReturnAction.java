package eu.eidas.sp.custom;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.sp.*;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static eu.eidas.sp.Constants.SP_CONF;

/**
 * This Action recives a SAML Response, shows it to the user and then validates it getting the attributes values
 *
 * @author iinigo
 */
@SuppressWarnings("squid:S1948") //TODO get rid of Struts
public class ScReturnAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

  private static final long serialVersionUID = 3660074009157921579L;

  private static final String SAML_VALIDATION_ERROR = "Could not validate token for Saml Response";

  static final Logger logger = LoggerFactory.getLogger(ScReturnAction.class.getName());
  private String spId;
  private AuthnContext authnContext;

  @SuppressWarnings("squid:S00116") //parameter as-is
  private String SAMLResponse;

  private String samlResponseXML;

  private String samlUnencryptedResponseXML;

  @SuppressWarnings("squid:S00116") //parameter as-is
  private String RelayState;

  private ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> attrMap;

  private HttpServletRequest request;

  private Properties configs;

  private String providerName;

  private List<ResultAttribute> resultAttributeList;

  /**
   * Translates the samlResponse to XML format in order to be shown in the JSP
   *
   * @return
   */
  @Override
  public String execute() {

    configs = SPUtil.loadSPConfigs();

    providerName = configs.getProperty(Constants.PROVIDER_NAME);
    String metadataUrl = configs.getProperty(Constants.SP_METADATA_URL);

    byte[] decSamlToken = EidasStringUtil.decodeBytesFromBase64(SAMLResponse);
    samlResponseXML = EidasStringUtil.toString(decSamlToken);
    try {
      SpProtocolEngineI engine = SpProtocolEngineFactory.getSpProtocolEngine(SP_CONF);
      //validate SAML Token
      engine.unmarshallResponseAndValidate(decSamlToken, request.getRemoteHost(), 0, 0, metadataUrl, null, false);

      boolean encryptedResponse = SPUtil.isEncryptedSamlResponse(decSamlToken);
      if (encryptedResponse) {
        byte[] eidasTokenSAML = engine.checkAndDecryptResponse(decSamlToken);
        samlUnencryptedResponseXML = SPUtil.extractAssertionAsString(EidasStringUtil.toString(eidasTokenSAML));
      }

    }
    catch (UnmarshallException e) {
      logger.error(e.getMessage(), e);
      throw new ApplicationSpecificServiceException(SAML_VALIDATION_ERROR, e.getMessage());
    }
    catch (EIDASSAMLEngineException e) {
      logger.error(e.getMessage(), e);
      if (StringUtils.isEmpty(e.getErrorDetail())) {
        throw new ApplicationSpecificServiceException(SAML_VALIDATION_ERROR, e.getErrorMessage());
      }
      else {
        throw new ApplicationSpecificServiceException(SAML_VALIDATION_ERROR, e.getErrorDetail());
      }
    }

    request.getSession().setAttribute("assertion", samlUnencryptedResponseXML);

    return Action.SUCCESS;
  }

  /**
   * Validates the request and displays the value of the requested attributes
   *
   * @return
   */
  public String populate() {

    IAuthenticationResponse authnResponse;

    //Decodes SAML Response
    byte[] decSamlToken = EidasStringUtil.decodeBytesFromBase64(SAMLResponse);
    samlResponseXML = getStyledXml(EidasStringUtil.toString(decSamlToken));
    samlUnencryptedResponseXML = getStyledXml((String) request.getSession().getAttribute("assertion"));


    configs = SPUtil.loadSPConfigs();
    String metadataUrl = configs.getProperty(Constants.SP_METADATA_URL);
    spId = configs.getProperty(Constants.PROVIDER_NAME);

    //Get SAMLEngine instance
    try {
      ProtocolEngineI engine = SpProtocolEngineFactory.getSpProtocolEngine(SP_CONF);
      //validate SAML Token
      authnResponse = engine.unmarshallResponseAndValidate(decSamlToken, request.getRemoteHost(), 0, 0, metadataUrl, null, false);

    }
    catch (EIDASSAMLEngineException e) {
      logger.error(e.getMessage());
      logger.error("", e);
      if (StringUtils.isEmpty(e.getErrorDetail())) {
        throw new ApplicationSpecificServiceException(SAML_VALIDATION_ERROR, e.getErrorMessage());
      }
      else {
        throw new ApplicationSpecificServiceException(SAML_VALIDATION_ERROR, e.getErrorDetail());
      }
    }

    if (authnResponse.isFailure()) {
      throw new ApplicationSpecificServiceException("Saml Response is fail", authnResponse.getStatusMessage());
    }
    else {
      attrMap = authnResponse.getAttributes().getAttributeMap();
      setResultAttributeList(extractAttributeValues());
      authnContext = new AuthnContext();
      authnContext.setIdp(authnResponse.getIssuer());
      authnContext.setNotBefore(authnResponse.getNotBefore().toString());
      authnContext.setLoa(authnResponse.getLevelOfAssurance());

      return "populate";
    }
  }

  private String getStyledXml(String rawXml) {
    try {
      XmlObject xmlObject = XmlObject.Factory.parse(new ByteArrayInputStream(rawXml.getBytes(StandardCharsets.UTF_8)));
      return new String(XmlBeansUtil.getStyledBytes(xmlObject), StandardCharsets.UTF_8);
    } catch (Exception ex){
      return "Bad or null xml: " +ex.getMessage();
    }
  }

  private List<ResultAttribute> extractAttributeValues() {
    List<ResultAttribute> attrList = attrMap.keySet().stream()
      .map(attributeDefinition -> {
        ResultAttribute resultAttribute = new ResultAttribute();
        resultAttribute.setNaturalPerson(attributeDefinition.getPersonType().equals(PersonType.NATURAL_PERSON));
        resultAttribute.setRequired(attributeDefinition.isRequired());
        resultAttribute.setValue(extractAttributeValue(attrMap.get(attributeDefinition)));
        resultAttribute.setName(attributeDefinition.getNameUri().toString());
        resultAttribute.setFriendlyName(attributeDefinition.getFriendlyName());
        return resultAttribute;
      })
      .filter(resultAttribute -> resultAttribute.getValue() != null)
      .collect(Collectors.toList());

    Collections.sort(attrList, new Comparator<ResultAttribute>() {
      @Override public int compare(ResultAttribute o1, ResultAttribute o2) {
        if (o1.isNaturalPerson() != o2.isNaturalPerson()) {
          return o1.isNaturalPerson() ? -1 : 1;
        }
        if (o1.isRequired() != o2.isRequired()) {
          return o1.isRequired() ? -1 : 1;
        }
        return o1.getFriendlyName().compareTo(o2.getFriendlyName());
      }
    });
    return attrList;
  }

  private String extractAttributeValue(ImmutableSet<? extends AttributeValue<?>> attributeValues) {
    ImmutableList<? extends AttributeValue<?>> attributeValuesList = attributeValues.asList();
    if (attributeValuesList == null) {
      return null;
    }

    Optional<? extends AttributeValue<?>> optionalNonLatinscriptAlt = attributeValues.stream()
      .filter(o -> !((AttributeValue) o).isNonLatinScriptAlternateVersion())
      .findFirst();

    if (optionalNonLatinscriptAlt.isPresent()) {
      return htmlPrint(optionalNonLatinscriptAlt.get().toString());
    }

    if (attributeValuesList.size() > 0) {
      return htmlPrint(attributeValuesList.get(0).toString());
    }

    return null;
  }

  private String htmlPrint(String inpString) {
    if (inpString == null) {
      return null;
    }
    List<String> stringList = Arrays.asList(inpString.split("\\r?\\n"));
    if (stringList.size() == 1) {
      return "<b>" + inpString + "</b>";
    }

    stringList = stringList.stream()
      .map(s -> {
        String[] split = s.split(":");
        if (split.length == 1) {
          return s;
        }
        return "<b>" + split[0] + "</b>: " + split[1].trim();
      }).collect(Collectors.toList());

    return String.join("<br/>", stringList);
  }

  @Override
  public void setServletRequest(HttpServletRequest request) {
    this.request = request;
  }

  @Override
  @SuppressWarnings("squid:S1186")
  public void setServletResponse(HttpServletResponse response) {
  }

  public void setAttrMap(ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> attrMap) {
    this.attrMap = attrMap;
  }

  public ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> getAttrMap() {
    return attrMap;
  }

  public String getProviderName() {
    return providerName;
  }

  public void setProviderName(String providerName) {
    this.providerName = providerName;
  }

  public String getSAMLResponse() {
    return SAMLResponse;
  }

  public void setSAMLResponse(String samlResponse) {
    this.SAMLResponse = samlResponse;
  }

  public String getSamlResponseXML() {
    return samlResponseXML;
  }

  public void setSamlResponseXML(String samlResponseXML) {
    this.samlResponseXML = samlResponseXML;
  }

  public String getSamlUnencryptedResponseXML() {
    return samlUnencryptedResponseXML;
  }

  public void setSamlUnencryptedResponseXML(String samlUnencryptedResponseXML) {
    this.samlUnencryptedResponseXML = samlUnencryptedResponseXML;
  }

  public String getRelayState() {
    return RelayState;
  }

  public void setRelayState(String relayState) {
    RelayState = relayState;
  }

  public List<ResultAttribute> getResultAttributeList() {
    return resultAttributeList;
  }

  public void setResultAttributeList(List<ResultAttribute> resultAttributeList) {
    this.resultAttributeList = resultAttributeList;
  }

  public String getSpId() {
    return spId;
  }

  public AuthnContext getAuthnContext() {
    return authnContext;
  }

}