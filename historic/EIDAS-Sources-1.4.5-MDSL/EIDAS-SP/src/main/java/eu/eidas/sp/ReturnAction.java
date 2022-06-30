package eu.eidas.sp;

import static eu.eidas.sp.Constants.SP_CONF;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * This Action recives a SAML Response, shows it to the user and then validates it getting the attributes values
 *
 * @author iinigo
 */
@SuppressWarnings("squid:S1948") //TODO get rid of Struts
public class ReturnAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    private static final long serialVersionUID = 3660074009157921579L;

    private static final String SAML_VALIDATION_ERROR = "Could not validate token for Saml Response";

    static final Logger logger = LoggerFactory.getLogger(IndexAction.class.getName());

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
            engine.unmarshallResponseAndValidate(decSamlToken, request.getRemoteHost(), 0, 0, metadataUrl, null,false);

            boolean encryptedResponse = SPUtil.isEncryptedSamlResponse(decSamlToken);
            if (encryptedResponse) {
                byte[] eidasTokenSAML = engine.checkAndDecryptResponse(decSamlToken);
                samlUnencryptedResponseXML = SPUtil.extractAssertionAsString(EidasStringUtil.toString(eidasTokenSAML));
            }

        } catch (UnmarshallException e) {
            logger.error(e.getMessage(), e);
            throw new ApplicationSpecificServiceException(SAML_VALIDATION_ERROR, e.getMessage());
        } catch (EIDASSAMLEngineException e) {
            logger.error(e.getMessage(), e);
            if (StringUtils.isEmpty(e.getErrorDetail())) {
                throw new ApplicationSpecificServiceException(SAML_VALIDATION_ERROR, e.getErrorMessage());
            } else {
                throw new ApplicationSpecificServiceException(SAML_VALIDATION_ERROR, e.getErrorDetail());
            }
        }

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

        configs = SPUtil.loadSPConfigs();
        String metadataUrl = configs.getProperty(Constants.SP_METADATA_URL);

        //Get SAMLEngine instance
        try {
            ProtocolEngineI engine = SpProtocolEngineFactory.getSpProtocolEngine(SP_CONF);
            //validate SAML Token
            authnResponse = engine.unmarshallResponseAndValidate(decSamlToken, request.getRemoteHost(), 0, 0, metadataUrl,null,false);

        } catch (EIDASSAMLEngineException e) {
            logger.error(e.getMessage());
            logger.error("", e);
            if (StringUtils.isEmpty(e.getErrorDetail())) {
                throw new ApplicationSpecificServiceException(SAML_VALIDATION_ERROR, e.getErrorMessage());
            } else {
                throw new ApplicationSpecificServiceException(SAML_VALIDATION_ERROR, e.getErrorDetail());
            }
        }

        if (authnResponse.isFailure()) {
            throw new ApplicationSpecificServiceException("Saml Response is fail", authnResponse.getStatusMessage());
        } else {
            attrMap = authnResponse.getAttributes().getAttributeMap();
            return "populate";
        }
    }

    @Override
    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    @SuppressWarnings("squid:S1186")
    public void setServletResponse(HttpServletResponse response) {
    }

    public void setAttrMap( ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> attrMap) {
        this.attrMap = attrMap;
    }

    public  ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> getAttrMap() {
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

}