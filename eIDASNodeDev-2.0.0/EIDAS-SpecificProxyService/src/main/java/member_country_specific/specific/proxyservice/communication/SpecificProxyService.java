/*
 * Copyright (c) 2017 by European Commission
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

package member_country_specific.specific.proxyservice.communication;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import eu.eidas.SimpleProtocol.*;
import eu.eidas.SimpleProtocol.utils.ContextClassTranslator;
import eu.eidas.SimpleProtocol.utils.NameIdPolicyTranslator;
import eu.eidas.SimpleProtocol.utils.SimpleProtocolProcess;
import eu.eidas.SimpleProtocol.utils.StatusCodeTranslator;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.*;
import eu.eidas.auth.commons.attribute.impl.DateTimeAttributeValue;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.LightResponse;
import eu.eidas.auth.commons.protocol.eidas.impl.Gender;
import eu.eidas.auth.commons.protocol.eidas.impl.PostalAddress;
import eu.eidas.auth.commons.protocol.eidas.impl.PostalAddressAttributeValue;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.specificcommunication.BinaryLightTokenHelper;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import member_country_specific.specific.proxyservice.SpecificProxyServiceParameterNames;
import member_country_specific.specific.proxyservice.SpecificProxyServiceViewNames;
import member_country_specific.specific.proxyservice.utils.CorrelatedRequestsHolder;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * SpecificProxyService: provides a sample implementation for interacting with the IdP.
 * For the request: it creates the simple protocol request to be send to IdP for authentication
 * For the response: it processes the received IdP specific response and builds the LightResponse
 *
 * @since 2.0
 */
public class SpecificProxyService {

    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(SpecificProxyService.class.getName());

    private String eidasAttributesFile;

    private String additionalAttributesFile;

    private AttributeRegistry eidasAttributeRegistry;

    private AttributeRegistry additionalAttributeRegistry;

    private String specificIdpResponseServiceUrl;

    private String defaultSpecificIdpResponseServiceUrl;

    private String idpUrl;

    private String specificProxyserviceResponseUrl;

    private boolean askConsentRequest;

    private boolean askConsentResponse;

    private boolean askConsentResponseShowOnlyEidasAttributes;

    private boolean askConsentResponseShowAttributeValues;

    private String issuerName;

    /**
     * Correlation Map between the simple protocol request Id to be send to the IdP and the holder
     * of the light request and correlated simple protocol request sent by the Proxy-service.
     */
    private CorrelationMap<CorrelatedRequestsHolder> specificMSIdpRequestCorrelationMap;

    //Correlation map that holds the request to be consented
    private CorrelationMap<ILightRequest> tokenRequestCorrelationMap;

    //Correlation map that holds the response to be consented
    private CorrelationMap<ILightResponse> tokenResponseCorrelationMap;

    //secret for the consent of the request
    private String consentRequestLightTokenSecret;

    //algorithm for the consent of the request
    private String consentRequestLightTokenAlgorithm;

    //secret for the consent of the response
    private String consentResponseLightTokenSecret;

    //algorithm for the consent of the response
    private String consentResponseLightTokenAlgorithm;

    private Boolean specificProxyServiceDeployedJar;


    /**
     * Method that translates from the Node Request to the MS Specific Request.
     *
     * @param originalIlightRequest  the initial light request received
     * @param consentedIlightRequest the resulting light request that only contains the consent attributes
     * @return MS Specific Request translated from the Node Request Base64 encoded
     * @throws JAXBException if the MS Specific Request could not be marshalled
     */
    public String translateNodeRequest(ILightRequest originalIlightRequest, @Nonnull ILightRequest consentedIlightRequest) throws JAXBException {
        final AuthenticationRequest authenticationRequest = createSpecificRequest(consentedIlightRequest);
        final String specificRequest = convertAuthenticationRequestToJson(authenticationRequest);

        final CorrelatedRequestsHolder correlatedRequestsHolder = new CorrelatedRequestsHolder(originalIlightRequest, authenticationRequest);
        specificMSIdpRequestCorrelationMap.put(authenticationRequest.getId(), correlatedRequestsHolder);

        return specificRequest;
    }

    private String convertAuthenticationRequestToJson(@Nonnull final AuthenticationRequest specificRequest) throws JAXBException {
        final String specificRequestJson = new SimpleProtocolProcess().convert2Json(specificRequest);
        return EidasStringUtil.encodeToBase64(specificRequestJson);
    }

    private AuthenticationRequest createSpecificRequest(ILightRequest lightRequest) {

        final AuthenticationRequest authenticationRequest = new AuthenticationRequest();

        authenticationRequest.setId(UUID.randomUUID().toString());

        authenticationRequest.setServiceUrl(getSpecificIdpResponseServiceUrl());

        if ((lightRequest.getProviderName() != null) && (!"".equals(lightRequest.getProviderName())))
            authenticationRequest.setProviderName(lightRequest.getProviderName());

        if ((lightRequest.getSpType() != null) && (!"".equals(lightRequest.getSpType())))
            authenticationRequest.setSpType(lightRequest.getSpType());

        if ((lightRequest.getCitizenCountryCode() != null) && (!"".equals(lightRequest.getCitizenCountryCode())))
            authenticationRequest.setCitizenCountry(lightRequest.getCitizenCountryCode());

        if ((lightRequest.getLevelOfAssurance() != null) && (!"".equals(lightRequest.getLevelOfAssurance()))) {
            final RequestedAuthenticationContext requestedAuthenticationContext = new RequestedAuthenticationContext();
            final ArrayList<String> levelOfAssurances = new ArrayList<>();

            final String levelOfAssurance = lightRequest.getLevelOfAssurance();
            final String contextClass = ContextClassTranslator.getContextClass(levelOfAssurance);

            levelOfAssurances.add(contextClass);
            requestedAuthenticationContext.setContextClass(levelOfAssurances);
            authenticationRequest.setAuthContext(requestedAuthenticationContext);
        }
        if ((lightRequest.getNameIdFormat() != null) && (!"".equals(lightRequest.getNameIdFormat()))) {
            final NameIdPolicyTranslator nameIdPolicyTranslator = NameIdPolicyTranslator.fromEidasNameIdPolicyString(lightRequest.getNameIdFormat());
            authenticationRequest.setNameIdPolicy(nameIdPolicyTranslator.stringSmsspNameIdPolicy());
        }

        //attributes
        ImmutableAttributeMap requestImmutableAttributeMap = lightRequest.getRequestedAttributes();
        ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> requestImmutableMap =
                requestImmutableAttributeMap.getAttributeMap();

        List<Attribute> simpleAttributes = new ArrayList<>();
        for (AttributeDefinition attrDef : requestImmutableMap.keySet()) {

            AttributeValue<?> attributeValue = null;
            ImmutableSet<? extends AttributeValue<?>> attributeValues = requestImmutableMap.get(attrDef);

            if (!requestImmutableMap.get(attrDef).isEmpty()) {
                attributeValue = attributeValues.iterator().next();
                final Class parameterizedType = attrDef.getParameterizedType();
                if ((DateTime.class).equals(parameterizedType)) {
                    simpleAttributes.add(translateDateAttribute(attrDef, attributeValue));
                } else if ((Gender.class).equals(parameterizedType)) {
                    simpleAttributes.add(translateStringAttribute(attrDef, attributeValue));
                } else if ((PostalAddress.class).equals(parameterizedType)) {
                    simpleAttributes.add(translateAddressAttribute(attrDef, attributeValue));
                } else if ((String.class).equals(parameterizedType)) {
                    if (attributeValues.size() > 1) {
                        simpleAttributes.add(translateStringListAttribute(attrDef, attributeValues.asList()));
                    } else {
                        simpleAttributes.add(translateStringAttribute(attrDef, attributeValue));
                    }
                } else {
                    simpleAttributes.add(translateOtherAttribute(attrDef));
                }
            } else {
                simpleAttributes.add(translateOtherAttribute(attrDef));
            }
        }

        authenticationRequest.setAttributes(simpleAttributes);
        return authenticationRequest;
    }

    private Attribute translateOtherAttribute(AttributeDefinition attrDef) {
        final Attribute simpleAttribute = new Attribute();
        simpleAttribute.setName(attrDef.getFriendlyName());
        simpleAttribute.setRequired(attrDef.isRequired());

        return simpleAttribute;
    }

    private StringAttribute translateStringAttribute(AttributeDefinition attrDef, AttributeValue<?> attributeValue) {
        final StringAttribute stringAttribute = new StringAttribute();
        stringAttribute.setName(attrDef.getFriendlyName());
        stringAttribute.setRequired(attrDef.isRequired());

        if (attributeValue != null) {
            try {
                AttributeValueMarshaller<?> attributeValueMarshaller = attrDef.getAttributeValueMarshaller();
                String valueString = attributeValueMarshaller.marshal((AttributeValue) attributeValue);
                stringAttribute.setValue(valueString);
                if (AttributeValueTransliterator.needsTransliteration(valueString))
                    stringAttribute.setLatinScript(false);
            } catch (AttributeValueMarshallingException e) {
                throw new IllegalStateException(e);
            }
        }

        return stringAttribute;
    }

    private DateAttribute translateDateAttribute(AttributeDefinition attrDef, AttributeValue<?> attributeValue) {
        final DateAttribute attribute = new DateAttribute();
        attribute.setName(attrDef.getFriendlyName());
        attribute.setRequired(attrDef.isRequired());
        if (attributeValue != null)
            attribute.setValue(((DateTime) attributeValue.getValue()).toDate());
        return attribute;
    }

    private Attribute translateStringListAttribute(AttributeDefinition<?> attributeDefinition, ImmutableList<? extends AttributeValue<?>> attributeValues) {
        final StringListAttribute stringListAttribute = new StringListAttribute();
        stringListAttribute.setName(attributeDefinition.getFriendlyName());
        stringListAttribute.setRequired(attributeDefinition.isRequired());
        final ArrayList<StringListValue> stringListValues = new ArrayList<>();
        if (attributeValues != null) {
            AttributeValueMarshaller<?> attributeValueMarshaller = attributeDefinition.getAttributeValueMarshaller();

            for (AttributeValue<?> attributeValue : attributeValues) {
                String valueString = null;
                try {
                    valueString = attributeValueMarshaller.marshal((AttributeValue) attributeValue);
                } catch (AttributeValueMarshallingException e) {
                    throw new IllegalStateException(e);
                }

                final StringListValue stringListValue = new StringListValue();
                stringListValue.setValue(valueString);
                setStringListValueAttributeLatinScript(valueString, stringListValue);
                stringListValues.add(stringListValue);
            }
            stringListAttribute.setValues(stringListValues);
        }

        return stringListAttribute;
    }

    private void setStringListValueAttributeLatinScript(String valueString, StringListValue stringListValue) {
        final boolean notLatinScript = AttributeValueTransliterator.needsTransliteration(valueString);
        if (notLatinScript)
            stringListValue.setLatinScript(false);
    }

    private AddressAttribute translateAddressAttribute(AttributeDefinition attrDef, AttributeValue<?> attributeValue) {
        final AddressAttribute addressAttribute = new AddressAttribute();
        if (attributeValue != null) {
            PostalAddress postalAddress = (PostalAddress) attributeValue.getValue();

            ComplexAddressAttribute complexAddressAttribute = new ComplexAddressAttribute();
            complexAddressAttribute.setAdminUnitFirstLine(postalAddress.getAdminUnitFirstLine());
            complexAddressAttribute.setAdminUnitSecondLine(postalAddress.getAdminUnitSecondLine());
            complexAddressAttribute.setPostName(postalAddress.getPostName());
            complexAddressAttribute.setFullCVAddress(postalAddress.getFullCvaddress());
            complexAddressAttribute.setLocatorDesignator(postalAddress.getLocatorDesignator());
            complexAddressAttribute.setLocatorName(postalAddress.getLocatorName());
            complexAddressAttribute.setPoBox(postalAddress.getPoBox());
            complexAddressAttribute.setPostCode(postalAddress.getPostCode());
            complexAddressAttribute.setThoroughFare(postalAddress.getThoroughfare());
            complexAddressAttribute.setAddressId(postalAddress.getAddressId());
            complexAddressAttribute.setAddressArea(postalAddress.getCvAddressArea());
            addressAttribute.setValue(complexAddressAttribute);
        }
        addressAttribute.setName(attrDef.getFriendlyName());
        addressAttribute.setRequired(attrDef.isRequired());
        return addressAttribute;
    }

    private void validateMandatoryFields(Response specificResponse) {
        if ((specificResponse.getInResponseTo() == null) || ("".equals(specificResponse.getInResponseTo())))
            throw new InvalidParameterEIDASException("0000", "Invalid InResponseTo");
        if ((specificResponse.getId() == null) || ("".equals(specificResponse.getId())))
            throw new InvalidParameterEIDASException("0000", "Invalid Id");
        if ((specificResponse.getVersion() == null) || ("".equals(specificResponse.getVersion())))
            throw new InvalidParameterEIDASException("0000", "Invalid Version");
        if ((specificResponse.getCreatedOn() == null) || ("".equals(specificResponse.getCreatedOn())))
            throw new InvalidParameterEIDASException("0000", "Invalid CreatedOn");
        if ((specificResponse.getStatus() == null) || (specificResponse.getStatus().getStatusCode() == null) || (StatusCodeTranslator.fromSmsspStatusCodeString(specificResponse.getStatus().getStatusCode()) == null)) {
            logger.error("Invalid Response Status");
            throw new InvalidParameterEIDASException("0000", "Invalid Response");
        }
    }

    private ILightResponse createLightResponse(@Nonnull final Response specificResponse, final String inResponseToId, final String relayState) throws ServletException {

        final LightResponse.Builder lightResponseBuilder = LightResponse.builder()
                .id(specificResponse.getId())
                .inResponseToId(inResponseToId)
                .relayState(getRelayState(relayState));

        if ((specificResponse.getIssuer() != null) && (!"".equals(specificResponse.getIssuer())))
            lightResponseBuilder.issuer(specificResponse.getIssuer());

        if ((specificResponse.getClientIpAddress() != null) && (!"".equals(specificResponse.getClientIpAddress())))
            lightResponseBuilder
                    .ipAddress(specificResponse.getClientIpAddress());

        if ((specificResponse.getStatus() != null) && (specificResponse.getStatus().getStatusCode() != null) && (StatusCodeTranslator.fromSmsspStatusCodeString(specificResponse.getStatus().getStatusCode()) != null)) {
            final ResponseStatus specificResponseStatus = specificResponse.getStatus();
            final eu.eidas.auth.commons.light.impl.ResponseStatus responseStatus = buildLightResponseStatus(specificResponseStatus, StatusCodeTranslator.fromSmsspStatusCodeString(specificResponseStatus.getStatusCode()));
            lightResponseBuilder.status(responseStatus);
        }

        if ((specificResponse.getAuthContextClass() != null) && (!"".equals(specificResponse.getAuthContextClass()))) {
            final String contextClass = specificResponse.getAuthContextClass();
            final String levelOfAssurance = ContextClassTranslator.getLevelOfAssurance(contextClass);
            if (levelOfAssurance != null)
                lightResponseBuilder.levelOfAssurance(levelOfAssurance);
            else
                lightResponseBuilder.levelOfAssurance(contextClass);
        }
        if ((specificResponse.getSubject() != null) && (!"".equals(specificResponse.getSubject())))
            lightResponseBuilder.subject(specificResponse.getSubject());

        if ((specificResponse.getNameId() != null) && (!"".equals(specificResponse.getNameId())))
            lightResponseBuilder.subjectNameIdFormat(NameIdPolicyTranslator.fromSmsspNameIdPolicyString(specificResponse.getNameId()).stringEidasNameIdPolicy());

        final ImmutableAttributeMap convertedAttributes = translateToEidasAttributesValues(specificResponse);
        lightResponseBuilder.attributes(convertedAttributes);

        return lightResponseBuilder.build();
    }

    private String getRelayState(final String relayState) {
        return (StringUtils.isEmpty(relayState)) ? createRelayState() : relayState;
    }

    private String createRelayState() {
        return UUID.randomUUID().toString();
    }

    private eu.eidas.auth.commons.light.impl.ResponseStatus buildLightResponseStatus(ResponseStatus specificResponseStatus, StatusCodeTranslator statusCodeTranslator) {
        final eu.eidas.auth.commons.light.impl.ResponseStatus.Builder responseStatusBuilder = eu.eidas.auth.commons.light.impl.ResponseStatus.builder();
        responseStatusBuilder.statusCode(statusCodeTranslator.stringEidasStatusCode());

        if (statusCodeTranslator == StatusCodeTranslator.FAILURE) {
            responseStatusBuilder
                    .subStatusCode(StatusCodeTranslator.SAML_STATUS_PREFIX + specificResponseStatus.getSubStatusCode())
                    .statusMessage(specificResponseStatus.getStatusMessage())
                    .failure(true);
        }
        return responseStatusBuilder.build();
    }

    /**
     * Maps the attributes from MS Specific Response to Light Response attribute map.
     * <p/>
     * Assumes that the name of the attributes of the simple protocol will be the friendly name of the eIDAS attributes
     * and uses this to retrieve the definition from the attribute registry in order to create the attribute to be included
     * in the {@link LightResponse}.
     *
     * @param response the MS Specific Response
     * @return the map containing the attributes to be added to the {@link LightResponse}
     */
    private ImmutableAttributeMap translateToEidasAttributesValues(@Nonnull final Response response) {
        final ImmutableAttributeMap.Builder immutableAttributeMapBuilder = new ImmutableAttributeMap.Builder();
        final List<Attribute> attributes = response.getAttributes();

        //in case of error simple responses the attributes can be null
        if (attributes == null)
            return immutableAttributeMapBuilder.build();

        for (Attribute attribute : attributes) {
            final String name = attribute.getName();
            AttributeDefinition<?> attributeDefinition = getAttributeDefinitionByFriendlyName(name);
            if (attribute instanceof StringAttribute) {
                final String value = ((StringAttribute) attribute).getValue();
                if (value != null)
                    immutableAttributeMapBuilder.put(attributeDefinition, value);
                else
                    immutableAttributeMapBuilder.put(attributeDefinition);
            } else if (attribute instanceof DateAttribute) {
                final Date value = ((DateAttribute) attribute).getValue();
                if (value != null)
                    immutableAttributeMapBuilder.put(attributeDefinition, (AttributeValue) new DateTimeAttributeValue(new DateTime(value)));
                else
                    immutableAttributeMapBuilder.put(attributeDefinition);
            } else if (attribute instanceof StringListAttribute) {
                final List<StringListValue> stringListValues = ((StringListAttribute) attribute).getValues();
                //convert to List of Strings
                final ArrayList<String> strings = new ArrayList<>();
                for (StringListValue value : stringListValues)
                    strings.add(value.getValue());

                immutableAttributeMapBuilder.putPrimaryValues(attributeDefinition, strings);
            } else if (attribute instanceof AddressAttribute) {
                final ComplexAddressAttribute complexAddressAttribute = ((AddressAttribute) attribute).getValue();
                final PostalAddress postalAddress = translateComplexAddressAttribute(complexAddressAttribute);
                final AttributeValue postalAddressAttributeValue = new PostalAddressAttributeValue(postalAddress);
                if (postalAddressAttributeValue != null)
                    immutableAttributeMapBuilder.put(attributeDefinition, postalAddressAttributeValue);
                else
                    immutableAttributeMapBuilder.put(attributeDefinition);
            }
        }

        return immutableAttributeMapBuilder.build();
    }

    private AttributeDefinition<?> getAttributeDefinitionForRepresentativeUserName(String name) {

        switch (name) {
            case "RepresentativeLegalAddress":
                return getEidasAttributeRegistry().getByName("http://eidas.europa.eu/attributes/legalperson/representative/LegalPersonAddress");
            case "RepresentativeVATRegistration":
                return getEidasAttributeRegistry().getByName("http://eidas.europa.eu/attributes/legalperson/representative/VATRegistrationNumber");
            case "LegalAddress":
                return getEidasAttributeRegistry().getByName("http://eidas.europa.eu/attributes/legalperson/LegalPersonAddress");
            case "VATRegistration":
                return getEidasAttributeRegistry().getByName("http://eidas.europa.eu/attributes/legalperson/VATRegistrationNumber");
            default:
                return null;
        }
    }

    private AttributeDefinition<?> getAttributeDefinitionByFriendlyName(String name) {

        AttributeDefinition<?> attributeDefinition = getAttributeDefinitionForRepresentativeUserName(name);

        if (attributeDefinition == null)
            return (getEidasAttributeRegistry().getByFriendlyName(name).size() == 0) ? getAdditionalAttributeRegistry().getByFriendlyName(name).first() : getEidasAttributeRegistry().getByFriendlyName(name).first();
        return attributeDefinition;
    }

    private PostalAddress translateComplexAddressAttribute(@Nonnull final ComplexAddressAttribute complexAddressAttribute) {
        PostalAddress.Builder postalAddressBuilder = new PostalAddress.Builder()
                .adminUnitFirstLine(complexAddressAttribute.getAdminUnitFirstLine())
                .adminUnitSecondLine(complexAddressAttribute.getAdminUnitSecondLine())
                .postName(complexAddressAttribute.getPostName())
                .fullCvaddress(complexAddressAttribute.getFullCVAddress())
                .locatorDesignator(complexAddressAttribute.getLocatorDesignator())
                .locatorName(complexAddressAttribute.getLocatorName())
                .poBox(complexAddressAttribute.getPoBox())
                .postCode(complexAddressAttribute.getPostCode())
                .thoroughfare(complexAddressAttribute.getThoroughFare())
                .addressId(complexAddressAttribute.getAddressId())
                .cvAddressArea(complexAddressAttribute.getAddressArea());

        return postalAddressBuilder.build();
    }

    /**
     * Method that translates from the MS Specific Request to the Node Response.
     *
     * @param specificResponse the MS Specific Request in Base64
     * @return the LightResponse translated from the MS Specific Response
     * @throws JAXBException if cannot unmarshall the MS Specific Response.
     */
    public ILightResponse translateSpecificResponse(@Nonnull final String specificResponse) throws JAXBException, ServletException {
        final Response response = unmarshallSpecificResponse(specificResponse);
        if ((response.getStatus() != null) && (response.getStatus().getStatusCode() != null) && (StatusCodeTranslator.fromSmsspStatusCodeString(response.getStatus().getStatusCode()) != null)
                && (!StatusCodeTranslator.FAILURE.equals(StatusCodeTranslator.fromSmsspStatusCodeString(response.getStatus().getStatusCode()))))
            validateMandatoryFields(response);
        final String inResponseToId = response.getInResponseTo();
        final ILightRequest iLightRequest = getRemoveCorrelatediLightRequest(inResponseToId);
        return createLightResponse(response, iLightRequest.getId(), iLightRequest.getRelayState());
    }

    private Response unmarshallSpecificResponse(@Nonnull final String specificResponseBase64) throws JAXBException {
        final String specificResponse = EidasStringUtil.decodeStringFromBase64(specificResponseBase64);
        final SimpleProtocolProcess simpleProtocolProcess = new SimpleProtocolProcess();
        return simpleProtocolProcess.convertFromJson(new StringReader(specificResponse), Response.class);
    }

    private ILightRequest getRemoveCorrelatediLightRequest(@Nonnull final String inResponseToId) {
        final CorrelatedRequestsHolder correlatedRequestsHolder = specificMSIdpRequestCorrelationMap.get(inResponseToId);
        ILightRequest iLightRequest = correlatedRequestsHolder.getiLightRequest();
        if (iLightRequest != null)
            specificMSIdpRequestCorrelationMap.remove(inResponseToId);
        return iLightRequest;
    }

    public void setSpecificMSIdpRequestCorrelationMap(@Nonnull final CorrelationMap<CorrelatedRequestsHolder> specificMSIdpRequestCorrelationMap) {
        this.specificMSIdpRequestCorrelationMap = specificMSIdpRequestCorrelationMap;
    }

    public String getIdpUrl() {
        return idpUrl;
    }

    public void setIdpUrl(String idpUrl) {
        this.idpUrl = idpUrl;
    }

    public String getSpecificIdpResponseServiceUrl() {
        return (isSpecificProxyServiceDeployedJar()) ? getDefaultSpecificIdpResponseServiceUrl() : specificIdpResponseServiceUrl;
    }

    public void setSpecificIdpResponseServiceUrl(String specificIdpResponseServiceUrl) {
        this.specificIdpResponseServiceUrl = specificIdpResponseServiceUrl;
    }

    public AttributeRegistry getEidasAttributeRegistry() {
        if (eidasAttributeRegistry == null)
            eidasAttributeRegistry = AttributeRegistries.fromFiles(getEidasAttributesFile(), null);
        return eidasAttributeRegistry;
    }

    public AttributeRegistry getAdditionalAttributeRegistry() {
        if (additionalAttributeRegistry == null)
            additionalAttributeRegistry = AttributeRegistries.fromFiles(getAdditionalAttributesFile(), null);
        return additionalAttributeRegistry;
    }

    public String getEidasAttributesFile() {
        return eidasAttributesFile;
    }

    public void setEidasAttributesFile(String eidasAttributesFile) {
        this.eidasAttributesFile = eidasAttributesFile;
    }

    public String getAdditionalAttributesFile() {
        return additionalAttributesFile;
    }

    public void setAdditionalAttributesFile(String additionalAttributesFile) {
        this.additionalAttributesFile = additionalAttributesFile;
    }

    public boolean isAskConsentRequest() {
        return askConsentRequest;
    }

    public void setAskConsentRequest(boolean askConsentRequest) {
        this.askConsentRequest = askConsentRequest;
    }

    public boolean isAskConsentResponse() {
        return askConsentResponse;
    }

    public void setAskConsentResponse(boolean askConsentResponse) {
        this.askConsentResponse = askConsentResponse;
    }

    private void storeBinaryTokenLightRequest(String id, ILightRequest lightRequest) {
        getTokenRequestCorrelationMap().put(id, lightRequest);
    }

    private ILightRequest getRemoveBinaryTokenRequest(String id) {
        final ILightRequest lightRequest = getTokenRequestCorrelationMap().get(id);
        if (lightRequest != null)
            getTokenRequestCorrelationMap().remove(id);
        return lightRequest;
    }

    private void storeBinaryTokenLightResponse(String id, ILightResponse iLightResponse) {
        getTokenResponseCorrelationMap().put(id, iLightResponse);
    }

    private ILightResponse getRemoveBinaryTokenResponse(String id) {
        final ILightResponse iLightResponse = getTokenResponseCorrelationMap().get(id);
        if (null != iLightResponse) {
            getTokenRequestCorrelationMap().remove(id);
        }

        return iLightResponse;
    }

    public CorrelationMap<ILightRequest> getTokenRequestCorrelationMap() {
        return tokenRequestCorrelationMap;
    }

    public void setTokenRequestCorrelationMap(CorrelationMap<ILightRequest> specificMSTokenRequestCorrelationMap) {
        this.tokenRequestCorrelationMap = specificMSTokenRequestCorrelationMap;
    }

    public CorrelationMap<ILightResponse> getTokenResponseCorrelationMap() {
        return tokenResponseCorrelationMap;
    }

    public void setTokenResponseCorrelationMap(CorrelationMap<ILightResponse> tokenResponseCorrelationMap) {
        this.tokenResponseCorrelationMap = tokenResponseCorrelationMap;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public ILightResponse getIlightResponse(HttpServletRequest httpServletRequest) throws SpecificCommunicationException {
        final String binaryLightTokenBase64 = BinaryLightTokenHelper.getBinaryToken(httpServletRequest, SpecificProxyServiceParameterNames.BINARY_LIGHT_TOKEN.toString());

        if (StringUtils.isNotEmpty(binaryLightTokenBase64)) {
            final String lightTokenId = BinaryLightTokenHelper.getBinaryLightTokenId(binaryLightTokenBase64,
                    getConsentResponseLightTokenSecret(),
                    getConsentResponseLightTokenAlgorithm());
            return getRemoveBinaryTokenResponse(lightTokenId);
        } else {
            return null;
        }
    }

    public ILightRequest getIlightRequest(HttpServletRequest httpServletRequest) throws SpecificCommunicationException {
        final String binaryLightTokenBase64 = BinaryLightTokenHelper.getBinaryToken(httpServletRequest, SpecificProxyServiceParameterNames.BINARY_LIGHT_TOKEN.toString());

        if (StringUtils.isNotEmpty(binaryLightTokenBase64)) {
            final String lightTokenId = BinaryLightTokenHelper.getBinaryLightTokenId(binaryLightTokenBase64,
                    getConsentRequestLightTokenSecret(),
                    getConsentRequestLightTokenAlgorithm());
            return getRemoveBinaryTokenRequest(lightTokenId);
        }

        return null;
    }

    public String createStoreBinaryLightTokenRequestBase64(ILightRequest lightRequest) throws SpecificCommunicationException {
        final BinaryLightToken binaryLightToken = BinaryLightTokenHelper.createBinaryLightToken(getIssuerName(),
                getConsentRequestLightTokenSecret(),
                getConsentRequestLightTokenAlgorithm());
        final String binaryLightTokenBase64 = BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightToken);
        storeBinaryTokenLightRequest(binaryLightToken.getToken().getId(), lightRequest);
        return binaryLightTokenBase64;
    }

    public String createStoreBinaryLightTokenResponseBase64(ILightResponse lightResponse) throws SpecificCommunicationException {
        final BinaryLightToken binaryLightToken = BinaryLightTokenHelper.createBinaryLightToken(getIssuerName(),
                getConsentResponseLightTokenSecret(),
                getConsentResponseLightTokenAlgorithm());
        final String binaryTokenResponse = BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightToken);
        storeBinaryTokenLightResponse(binaryLightToken.getToken().getId(), lightResponse);
        return binaryTokenResponse;
    }

    private String getConsentRequestLightTokenSecret() {
        return consentRequestLightTokenSecret;
    }

    public void setConsentRequestLightTokenSecret(String consentRequestLightTokenSecret) {
        this.consentRequestLightTokenSecret = consentRequestLightTokenSecret;
    }

    private String getConsentRequestLightTokenAlgorithm() {
        return consentRequestLightTokenAlgorithm;
    }

    public void setConsentRequestLightTokenAlgorithm(String consentRequestLightTokenAlgorithm) {
        this.consentRequestLightTokenAlgorithm = consentRequestLightTokenAlgorithm;
    }

    private String getConsentResponseLightTokenSecret() {
        return consentResponseLightTokenSecret;
    }

    public void setConsentResponseLightTokenSecret(String consentResponseLightTokenSecret) {
        this.consentResponseLightTokenSecret = consentResponseLightTokenSecret;
    }

    private String getConsentResponseLightTokenAlgorithm() {
        return consentResponseLightTokenAlgorithm;
    }

    public void setConsentResponseLightTokenAlgorithm(String consentResponseLightTokenAlgorithm) {
        this.consentResponseLightTokenAlgorithm = consentResponseLightTokenAlgorithm;
    }

    public boolean isAskConsentResponseShowOnlyEidasAttributes() {
        return askConsentResponseShowOnlyEidasAttributes;
    }

    public void setAskConsentResponseShowOnlyEidasAttributes(boolean askConsentResponseShowOnlyEidasAttributes) {
        this.askConsentResponseShowOnlyEidasAttributes = askConsentResponseShowOnlyEidasAttributes;
    }

    public boolean isAskConsentResponseShowAttributeValues() {
        return askConsentResponseShowAttributeValues;
    }

    public void setAskConsentResponseShowAttributeValues(boolean askConsentResponseShowAttributeValues) {
        this.askConsentResponseShowAttributeValues = askConsentResponseShowAttributeValues;
    }

    public void setSpecificProxyServiceDeployedJar(Boolean specificProxyServiceDeployedJar) {
        this.specificProxyServiceDeployedJar = specificProxyServiceDeployedJar;
    }

    public Boolean isSpecificProxyServiceDeployedJar() {
        return specificProxyServiceDeployedJar;
    }

    public void setSpecificProxyserviceResponseUrl(String specificProxyserviceResponseUrl) {
        this.specificProxyserviceResponseUrl = specificProxyserviceResponseUrl;
    }

    public String getSpecificProxyserviceResponseUrl() {
        return specificProxyserviceResponseUrl;
    }

    public void setDefaultSpecificIdpResponseServiceUrl(String defaultSpecificIdpResponseServiceUrl) {
        this.defaultSpecificIdpResponseServiceUrl = defaultSpecificIdpResponseServiceUrl;
    }

    public String getDefaultSpecificIdpResponseServiceUrl() {
        return defaultSpecificIdpResponseServiceUrl;
    }
}
