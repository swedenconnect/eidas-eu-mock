/*
 * Copyright (c) 2024 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package member_country_specific.sp;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;
import eu.eidas.SimpleProtocol.Attribute;
import eu.eidas.SimpleProtocol.AuthenticationRequest;
import eu.eidas.SimpleProtocol.RequestedAuthenticationContext;
import eu.eidas.SimpleProtocol.utils.SimpleProtocolProcess;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistries;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.SpType;
import eu.eidas.auth.commons.protocol.impl.SamlNameIdFormat;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author iinigo
 * This Action Generates a SAML Request with the data given by the user, then sends it to the selected node
 */
@SuppressWarnings("squid:S1948") //TODO get rid of Struts
public class IndexAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    public static final String ACTION_POPULATE = "populate";
    public static final String COUNTRY = "country";
    public static final String NON_NOTIFIED_LOA = "nonNotifiedLoA";
    private static final long serialVersionUID = 3660074009157921579L;
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexAction.class);
    private static final String ATTRIBUTES_FILENAME = "eidas-attributes.xml";
    private static final String ADDITIONAL_ATTRIBUTES_FILENAME = "additional-attributes.xml";
    private static final AttributeRegistry coreAttributeRegistry = AttributeRegistries.fromFile(ATTRIBUTES_FILENAME, SPUtil.getConfigFilePath());
    private static final AttributeRegistry coreAdditionalAttributeRegistry = AttributeRegistries.fromFile(ADDITIONAL_ATTRIBUTES_FILENAME, SPUtil.getConfigFilePath());
    private static Properties configs;
    private static List<Country> countries;
    private static List<AttributeDefinition> eidasAttributeList;
    private static List<LevelOfAssurance> nonNotifiedLoAs;
    private static List<String> nameIDFormats;
    private static String spId;
    private static String providerName;
    private static String requesterId;
    private HttpServletRequest request;
    private String smsspRequest;
    private String smsspRequestJSON;
    /*Requested parameters*/
    private String nodeMetadataUrl;
    private String postLocationUrl;
    private String redirectLocationUrl;
    private String nodeUrl;
    private String nodeUrl2;
    private String citizen;
    private String returnUrl;
    private String eidasloa;
    private String eidasloaCompareType;
    private List<String> otherloas;
    private String eidasNameIdentifier;
    private String eidasSPType;

    private static void loadGlobalConfig() {
        configs = SPUtil.loadSPConfigs();
        spId = configs.getProperty(Constants.PROVIDER_NAME);
        providerName = configs.getProperty(Constants.PROVIDER_NAME);
        requesterId = configs.getProperty(Constants.REQUESTER_ID);
        countries = new ArrayList<>();
        eidasAttributeList = new ArrayList<>();
        nonNotifiedLoAs = new ArrayList<>();
        nameIDFormats = Arrays.stream(SamlNameIdFormat.values())
                .map((s) -> s.getNameIdFormat().split(":"))
                .map((array) -> array[array.length-1])
                .collect(Collectors.toList());
    }

    /**
     * Fill the data in the JSP that is shown to the user in order to fill the requested data to generate a smssp request
     *
     * @return ACTION_REDIRECT
     */
    public String populate() {

        IndexAction.loadGlobalConfig();

        returnUrl = configs.getProperty(Constants.SP_RETURN);

        LOGGER.debug("populate.returnUrl=" + returnUrl);

        countries.addAll(getConfiguredCountries());
        nonNotifiedLoAs.addAll(getConfiguredNonNotifiedLoAs());

        SortedSet<AttributeDefinition<?>> eidasAttributeDefinitions = coreAttributeRegistry.getAttributes();
        SortedSet<AttributeDefinition<?>> eidasAddtionalAttributeDefinitions = coreAdditionalAttributeRegistry.getAttributes();
        eidasAttributeList.addAll(eidasAttributeDefinitions);
        eidasAttributeList.addAll(eidasAddtionalAttributeDefinitions);

        return ACTION_POPULATE;
    }

    private List<Country> getConfiguredCountries() {
        List<Country> configuredCountries = new ArrayList<>();
        int numCountries = Integer.parseInt(configs.getProperty(Constants.COUNTRY_NUMBER));
        for (int i = 1; i <= numCountries; i++) {
            final String url = getUrl(i);
            Country country = new Country(i, configs.getProperty(COUNTRY + Integer.toString(i) + ".name"), url,
                    configs.getProperty(COUNTRY + Integer.toString(i) + ".countrySelector"));
            configuredCountries.add(country);
            LOGGER.info(country.toString());
        }
        return configuredCountries;
    }

    private String getUrl(int i) {
        final boolean isSpecificConnectorJar = (Boolean) ApplicationContextProvider.getApplicationContext().getBean(Constants.SPECIFIC_CONNECTOR_JAR);
        final String url = configs.getProperty(COUNTRY + Integer.toString(i) + ".url");
        if (isSpecificConnectorJar) {
            return url;
        } else {
            return url.replace(Constants.EIDAS_NODE_CONNECTOR, Constants.SPECIFIC_CONNECTOR);
        }
    }

    private List<LevelOfAssurance> getConfiguredNonNotifiedLoAs() {
        List<LevelOfAssurance> configuredNonNotifiedLoAs = new ArrayList<>();
        int i = 1;
        LevelOfAssurance nonNotifiedLoA;
        do {
            nonNotifiedLoA = null;
            String name = configs.getProperty(NON_NOTIFIED_LOA + i + ".name");
            String value = configs.getProperty(NON_NOTIFIED_LOA + i++ + ".value");
            if (value != null && !value.isEmpty()) {
                nonNotifiedLoA = new LevelOfAssurance(name, value);
                configuredNonNotifiedLoAs.add(nonNotifiedLoA);
            }
        } while (nonNotifiedLoA != null);
        return configuredNonNotifiedLoAs;
    }

    /**
     * @return Action.SUCCESS
     */
    @Override
    public String execute() throws JAXBException {

        SortedSet<AttributeDefinition<?>> allSupportedAttributesSet =
                coreAttributeRegistry.getAttributes();
        SortedSet<AttributeDefinition<?>> eidasAdditionalAttributeDefinitions =
                coreAdditionalAttributeRegistry.getAttributes();

        List<AttributeDefinition<?>> reqAttrList = new ArrayList<>(allSupportedAttributesSet);
        reqAttrList.addAll(eidasAdditionalAttributeDefinitions);

        //remove attributes the SP decides not to send (for testing purpose)
        for (AttributeDefinition<?> attributeDefinition : allSupportedAttributesSet) {
            String attributeName = request.getParameter(attributeDefinition.getFriendlyName());
            if (attributeName == null || "none".equals(request.getParameter(attributeName + "Type"))) {
                reqAttrList.remove(attributeDefinition);
            }
        }
        for (AttributeDefinition<?> attributeDefinition : eidasAdditionalAttributeDefinitions) {
            String attributeName = request.getParameter(attributeDefinition.getFriendlyName());
            if (attributeName == null || "none".equals(request.getParameter(attributeName + "Type"))) {
                reqAttrList.remove(attributeDefinition);
            }
        }

        //generate Json:
        final AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setId(UUID.randomUUID().toString());
        authenticationRequest.setServiceUrl(returnUrl);
        authenticationRequest.setProviderName(providerName);
        if (requesterId != null && !requesterId.trim().isEmpty())
            authenticationRequest.setRequesterId(requesterId);
        authenticationRequest.setCitizenCountry(citizen);
        if (nameIDFormats.contains(eidasNameIdentifier))
            authenticationRequest.setNameIdPolicy(eidasNameIdentifier);

        if (eidasSPType.equalsIgnoreCase(SpType.PRIVATE.toString()) || eidasSPType.equalsIgnoreCase(SpType.PUBLIC.toString()))
            authenticationRequest.setSpType(eidasSPType);

        authenticationRequest.setAuthContext(getRequestedAuthenticationContext());

        //attributes
        List<Attribute> simpleAttributes = new ArrayList<>();
        for (AttributeDefinition<?> attributeDefinition : reqAttrList) {
            final String friendlyName = attributeDefinition.getFriendlyName();
            final Attribute simpleAttribute = new Attribute();
            simpleAttribute.setName(friendlyName);
            boolean isRequired = false;
            if ("true".equals(request.getParameter(friendlyName + "Type")))
                isRequired = true;
            simpleAttribute.setRequired(isRequired);
            simpleAttributes.add(simpleAttribute);
        }

        authenticationRequest.setAttributes(simpleAttributes);

        smsspRequestJSON = new SimpleProtocolProcess().convert2Json(authenticationRequest);
        smsspRequest = EidasStringUtil.encodeToBase64(smsspRequestJSON);
        return Action.SUCCESS;
    }

    private RequestedAuthenticationContext getRequestedAuthenticationContext() {
        if ((eidasloa == null || eidasloa.isEmpty()) && (otherloas == null || otherloas.isEmpty())) {
            return null;
        }
        final RequestedAuthenticationContext requestedAuthenticationContext = new RequestedAuthenticationContext();
        LevelOfAssuranceComparison comparisonType = LevelOfAssuranceComparison.fromString(eidasloaCompareType);
        requestedAuthenticationContext.setComparison(comparisonType.stringValue());
        if (eidasloa != null && !eidasloa.isEmpty()) {
            requestedAuthenticationContext.setContextClass(Arrays.asList(eidasloa));
        }
        if (otherloas != null && !otherloas.isEmpty()) {
            requestedAuthenticationContext.setNonNotifiedContextClass(otherloas);
        }
        return requestedAuthenticationContext;
    }

    private void setDefaultURLsFromMetadata(final String metadataURL) {
        final String postSSOSLocationURL = getSSOSLocation(metadataURL);
        LOGGER.debug("setDefaultURLsFromMetadata.metadataURL=" + metadataURL);
        setPostLocationUrl(postSSOSLocationURL);
        setNodeUrl(postSSOSLocationURL);

        final String redirectSSOSLocation = getSSOSLocation(metadataURL);
        setRedirectLocationUrl(redirectSSOSLocation);
    }

    private String getSSOSLocation(String metadataUrl) {
        LOGGER.debug("getSSOSLocation.metadataUrl=" + metadataUrl);
        return metadataUrl;
    }

    public List<AttributeDefinition> getEidasAttributeList() {
        return eidasAttributeList;
    }

    public String getSmsspRequest() {
        return smsspRequest;
    }

    public void setSmsspRequest(String smsspToken) {
        this.smsspRequest = smsspToken;
    }

    public String getSpId() {
        return spId;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getRequesterId() {
        return requesterId;
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

    public String getSmsspRequestJSON() {
        return smsspRequestJSON;
    }

    public void setSmsspRequestJSON(String smsspRequestJSON) {
        this.smsspRequestJSON = smsspRequestJSON;
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

    public void setNodeMetadataUrl(final String nodeMetadataUrl) {
        this.nodeMetadataUrl = nodeMetadataUrl;
        LOGGER.debug("setNodeMetadataUrl.nodeMetadataUrl=" + nodeMetadataUrl);

        setDefaultURLsFromMetadata(nodeMetadataUrl);
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

    /**
     * Method to be used to return the correct URL based on
     * metadata ssos location. If metadata has been read in EIDAS mode returns
     * the post location ssos contained in the metadata or
     * returns nodeUrl otherwise. This allows to be able to function.
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

    public List<LevelOfAssurance> getNonNotifiedLoAs() {
        return nonNotifiedLoAs;
    }

    public List<String> getNameIDFormats() {
        return nameIDFormats;
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

    public List<String> getOtherloas() {
        return otherloas;
    }

    public void setOtherloas(List<String> otherloas) {
        this.otherloas = otherloas;
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

}
