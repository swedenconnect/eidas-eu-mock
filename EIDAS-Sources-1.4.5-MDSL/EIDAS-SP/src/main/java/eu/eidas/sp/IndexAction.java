/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.sp;

import com.google.common.collect.ImmutableSortedSet;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.SpType;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.EidasSamlBinding;
import eu.eidas.auth.commons.protocol.impl.SamlBindingUri;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.sp.metadata.SPCachingMetadataFetcher;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @author iinigo
 *         This Action Generates a SAML Request with the data given by the user, then sends it to the selected node
 */
@SuppressWarnings("squid:S1948") //TODO get rid of Struts
public class IndexAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    private static final long serialVersionUID = 3660074009157921579L;

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexAction.class);
    public static final String ACTION_POPULATE = "populate";

    private final ProtocolEngineI protocolEngine = SpProtocolEngineFactory.getSpProtocolEngine(Constants.SP_CONF);

    private HttpServletRequest request;
    private String samlRequest;
    private String samlRequestXML;

    private static Properties configs;
    private static List<Country> countries;
    private static List<AttributeDefinition> storkAttributeList;
    private static List<AttributeDefinition> eidasAttributeList;

    private static String spId;
    private static String providerName;
    private static ResourceBundle resourceBundle;

    /*Requested parameters*/
    private String nodeMetadataUrl;
    private String postLocationUrl;
    private String redirectLocationUrl;

    private String nodeUrl;
    private String nodeUrl2;
    private String qaa;
    private String citizen;
    private String returnUrl;
    private String eidasloa;
    private String eidasloaCompareType;
    private String eidasNameIdentifier;
    private String eidasSPType;

    private static boolean eidasNodeOnly = true;

    private final static SPCachingMetadataFetcher metadataFetcher = new SPCachingMetadataFetcher();

    private static void loadGlobalConfig() {
        configs = SPUtil.loadSPConfigs();
        spId = configs.getProperty(Constants.PROVIDER_NAME);
        providerName = configs.getProperty(Constants.PROVIDER_NAME);
        countries = new ArrayList<Country>();
        eidasAttributeList = new ArrayList<AttributeDefinition>();
        eidasNodeOnly = !(Boolean.FALSE.toString().equalsIgnoreCase(configs.getProperty(Constants.SP_EIDAS_ONLY)));
        resourceBundle = ResourceBundle.getBundle(Constants.SYSADMIN_RESOURCE_BUNDLE_BASE_NAME);
    }

    /**
     * Fill the data in the JSP that is shown to the user in order to fill the requested data to generate a saml request
     *
     * @return ACTION_REDIRECT
     */
    public String populate() throws EIDASSAMLEngineException {

        IndexAction.loadGlobalConfig();

        returnUrl = configs.getProperty(Constants.SP_RETURN);
        qaa = configs.getProperty(Constants.SP_QAALEVEL);

        int numCountries = Integer.parseInt(configs.getProperty(Constants.COUNTRY_NUMBER));
        for (int i = 1; i <= numCountries; i++) {
            Country country = new Country(i, configs.getProperty("country" + Integer.toString(i) + ".name"), configs.getProperty("country" + Integer.toString(i) + ".url"),
                    configs.getProperty("country" + Integer.toString(i) + ".countrySelector"), configs.getProperty("country" + Integer.toString(i) + ".metadata.url"));
            countries.add(country);
            LOGGER.info(country.toString());
        }

        ImmutableSortedSet<AttributeDefinition<?>> eidasAttributeDefinitions = protocolEngine.getProtocolProcessor().getAllSupportedAttributes();
        eidasAttributeList.addAll(eidasAttributeDefinitions);

        return ACTION_POPULATE;
    }

    /**
     * Generates de Saml Request with the data given by the user
     *
     * @return Action.SUCCESS
     */

    @Override
    public String execute() {

        final ImmutableSortedSet<AttributeDefinition<?>> allSupportedAttributesSet =
                protocolEngine.getProtocolProcessor().getAllSupportedAttributes();
        List<AttributeDefinition<?>> reqAttrList = new ArrayList<AttributeDefinition<?>>(allSupportedAttributesSet);
        byte[] token;

        //remove attributes the SP decides not to send (for testing purpose)
        for (AttributeDefinition<?> attributeDefinition : allSupportedAttributesSet) {
            String attributeName = request.getParameter(attributeDefinition.getNameUri().toASCIIString());
            if (attributeName == null || "none".equals(request.getParameter(attributeName + "Type"))) {
                reqAttrList.remove(attributeDefinition);
            }
        }

        ImmutableAttributeMap reqAttrMap = new ImmutableAttributeMap.Builder().putAll(reqAttrList).build();

        // build the request
        EidasAuthenticationRequest.Builder reqBuilder = new EidasAuthenticationRequest.Builder();
        reqBuilder.destination(nodeUrl);
        reqBuilder.providerName(providerName);
        if (qaa != null) {
            //TODO quick fix for having a flow working end-to-end check if this is correct: removed setting of qaa
//            reqBuilder.qaa(Integer.parseInt(qaa));
        }
        reqBuilder.requestedAttributes(reqAttrMap);
        if (LevelOfAssurance.getLevel(eidasloa) == null) {
            reqBuilder.levelOfAssurance(LevelOfAssurance.LOW.stringValue());
        } else {
            reqBuilder.levelOfAssurance(eidasloa);
        }
        if (eidasSPType.equalsIgnoreCase(SpType.PRIVATE.toString()) || eidasSPType.equalsIgnoreCase(SpType.PUBLIC.toString())) {
            reqBuilder.spType(eidasSPType);
        }
        reqBuilder.levelOfAssuranceComparison(LevelOfAssuranceComparison.fromString(eidasloaCompareType).stringValue());
        reqBuilder.nameIdFormat(eidasNameIdentifier);
        reqBuilder.binding(EidasSamlBinding.EMPTY.getName());

        String metadataUrl = configs.getProperty(Constants.SP_METADATA_URL);
        if (metadataUrl != null && !metadataUrl.isEmpty() && SPUtil.isMetadataEnabled()) {
            reqBuilder.issuer(metadataUrl);
        }

        //TODO quick fix for having a flow working end-to-end check if this is correct: removed setting of spSector and spApplication
//        reqBuilder.spSector(spSector);
//        reqBuilder.spApplication(spApplication);
        reqBuilder.serviceProviderCountryCode(request.getParameter("connector_ms_input"));
        //V-IDP parameters
        reqBuilder.citizenCountryCode(citizen);
        //TODO quick fix for having a flow working end-to-end check if this is correct: removed setting of spId
//        reqBuilder.spId(spId);

        IRequestMessage binaryRequestMessage;

        try {
            //TODO quick fix for having a flow working end-to-end check if this is correct: generated missing id
            reqBuilder.id(SAMLEngineUtils.generateNCName());
            binaryRequestMessage = protocolEngine.generateRequestMessage(reqBuilder.build(), nodeMetadataUrl);
        } catch (EIDASSAMLEngineException e) {
            LOGGER.error(e.getMessage());
            LOGGER.error("", e);
            final String errorMessage = getErrorMessageFromBundle(e.getErrorCode(), e.getErrorMessage());
            throw new ApplicationSpecificServiceException("Could not generate token for Saml Request", errorMessage);
        }

        token = binaryRequestMessage.getMessageBytes();

        samlRequestXML = EidasStringUtil.toString(token);
        samlRequest = EidasStringUtil.encodeToBase64(token);

        return Action.SUCCESS;
    }

    private String getErrorMessageFromBundle(final String errorCode, final String errorMessage){
        return MessageFormat.format(resourceBundle.getString(errorMessage), errorCode);
    }

    private void setDefaultURLsFromMetadata(final String metadataURL) throws EIDASSAMLEngineException {
        final String postSSOSLocationURL = getSSOSLocation(metadataURL, SamlBindingUri.SAML2_POST);
        setPostLocationUrl(postSSOSLocationURL);
        setNodeUrl(postSSOSLocationURL);

        final String redirectSSOSLocation = getSSOSLocation(metadataURL, SamlBindingUri.SAML2_REDIRECT);
        setRedirectLocationUrl(redirectSSOSLocation);
    }

    private String getSSOSLocation(String metadataUrl, SamlBindingUri binding) throws EIDASSAMLEngineException {
        MetadataSignerI metadataSigner = (MetadataSignerI) protocolEngine.getSigner();
        EntityDescriptor entityDescriptor =
                metadataFetcher.getEntityDescriptor(metadataUrl, metadataSigner);
        IDPSSODescriptor idPSSODescriptor = MetadataUtil.getIDPSSODescriptor(entityDescriptor);
        return MetadataUtil.getSingleSignOnUrl(idPSSODescriptor, binding);
    }


    public List<AttributeDefinition> getStorkAttributeList() {
        return storkAttributeList;
    }

    public List<AttributeDefinition> getEidasAttributeList() {
        return eidasAttributeList;
    }

    public void setSamlRequest(String samlToken) {
        this.samlRequest = samlToken;
    }

    public String getSamlRequest() {
        return samlRequest;
    }

    public String getQaa() {
        return qaa;
    }

    public void setQaa(String qaa) {
        this.qaa = qaa;
    }

    public String getSpId() {
        return spId;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getCitizen() {
        return citizen;
    }

    public void setCitizen(String citizen) {
        this.citizen = citizen;
    }

    public void setCitizenEidas(String citizen) {
        setCitizen(citizen);
    }

    public String getSamlRequestXML() {
        return samlRequestXML;
    }

    public void setSamlRequestXML(String samlRequestXML) {
        this.samlRequestXML = samlRequestXML;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }

    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    public String getNodeMetadataUrl() {
        return nodeMetadataUrl;
    }

    public String getPostLocationUrl() {
        return postLocationUrl;
    }

    private void setPostLocationUrl(String postLocationUrl) {
        this.postLocationUrl = postLocationUrl;
    }

    public String getRedirectLocationUrl() {
        return redirectLocationUrl;
    }

    private void setRedirectLocationUrl(String redirectLocationUrl) {
        this.redirectLocationUrl = redirectLocationUrl;
    }

    public void setNodeMetadataUrl(final String nodeMetadataUrl) throws EIDASSAMLEngineException {
        this.nodeMetadataUrl = nodeMetadataUrl;

        setDefaultURLsFromMetadata(nodeMetadataUrl);
    }


    /**
     * Method to be used to return the correct URL based on
     * metadata ssos location. If metadata has been read in EIDAS mode returns
     * the post location ssos contained in the metadata or
     * returns nodeUrl otherwise. This allows to be able to function eihter in
     * EIDAS or STORK mode respectively.
     *
     * @return a default URL string
     */
    public String getDefaultActionUrl() {
        return getPostActionUrl();
    }

    public String getPostActionUrl() {
        return postLocationUrl != null ? postLocationUrl : nodeUrl;
    }

    public String getRedirectActionUrl() {
        return redirectLocationUrl != null ? redirectLocationUrl : nodeUrl;
    }

    public List<Country> getCountries() {
        return countries;
    }

    @Override
    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    @SuppressWarnings("squid:S1186")
    public void setServletResponse(HttpServletResponse response) {
    }

    public String getNodeUrl2() {
        return nodeUrl2;
    }

    public void setNodeUrl2(String nodeUrl2) {
        this.nodeUrl2 = nodeUrl2;
    }

    public String getEidasloa() {
        return eidasloa;
    }

    public void setEidasloa(String eidasloa) {
        this.eidasloa = eidasloa;
    }

    public String getEidasloaCompareType() {
        return eidasloaCompareType;
    }

    public void setEidasloaCompareType(String eidasloaCompareType) {
        this.eidasloaCompareType = eidasloaCompareType;
    }

    public String getEidasNameIdentifier() {
        return eidasNameIdentifier;
    }

    public void setEidasNameIdentifier(String eidasNameIdentifier) {
        this.eidasNameIdentifier = eidasNameIdentifier;
    }

    public String getEidasSPType() {
        return eidasSPType;
    }

    public void setEidasSPType(String eidasSPType) {
        this.eidasSPType = eidasSPType;
    }

    public boolean isEidasNodeOnly() {
        return eidasNodeOnly;
    }

    public void setEidasNodeOnly(boolean eidasNodeOnly) {
        IndexAction.setGlobalEidasNodeOnly(eidasNodeOnly);
    }

    public static void setGlobalEidasNodeOnly(boolean eidasNodeOnly) {
        IndexAction.eidasNodeOnly = eidasNodeOnly;
    }
}
