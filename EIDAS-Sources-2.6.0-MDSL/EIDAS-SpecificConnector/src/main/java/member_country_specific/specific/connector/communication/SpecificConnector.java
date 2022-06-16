/*
 * Copyright (c) 2020 by European Commission
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
package member_country_specific.specific.connector.communication;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import eu.eidas.SimpleProtocol.AddressAttribute;
import eu.eidas.SimpleProtocol.Attribute;
import eu.eidas.SimpleProtocol.AuthenticationRequest;
import eu.eidas.SimpleProtocol.ComplexAddressAttribute;
import eu.eidas.SimpleProtocol.DateAttribute;
import eu.eidas.SimpleProtocol.RequestedAuthenticationContext;
import eu.eidas.SimpleProtocol.Response;
import eu.eidas.SimpleProtocol.ResponseStatus;
import eu.eidas.SimpleProtocol.StringAttribute;
import eu.eidas.SimpleProtocol.StringListAttribute;
import eu.eidas.SimpleProtocol.StringListValue;
import eu.eidas.SimpleProtocol.utils.ContextClassTranslator;
import eu.eidas.SimpleProtocol.utils.NameIdPolicyTranslator;
import eu.eidas.SimpleProtocol.utils.SimpleProtocolProcess;
import eu.eidas.SimpleProtocol.utils.StatusCodeTranslator;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistries;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;
import eu.eidas.auth.commons.attribute.AttributeValueTransliterator;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.impl.DateTimeAttributeValue;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.LevelOfAssuranceType;
import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.PostalAddress;
import eu.eidas.auth.commons.protocol.eidas.impl.PostalAddressAttributeValue;
import eu.eidas.auth.commons.tx.CorrelationMap;
import member_country_specific.specific.connector.SpecificConnectorParameterNames;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * SpecificConnector: provides a sample implementation for interacting with the SP.
 * For the request: it processes the simple protocol request received from the SP and creates a simple protocol request
 * For the response: it created a simple protocol response from the LightResponse
 *
 * @since 2.0
 */
public class SpecificConnector {

    private String eidasAttributesFile;

    private String additionalAttributesFile;

    private AttributeRegistry coreAttributeRegistry;

    private String issuerName;

    private String specificConnectorRequestUrl;
    
    private boolean relaystateRandomizeNull;

    public boolean getRelaystateRandomizeNull() {
		return relaystateRandomizeNull;
	}

	public void setRelaystateRandomizeNull(boolean relaystateRandomizeNull) {
		this.relaystateRandomizeNull = relaystateRandomizeNull;
	}

	//Correlation Map between the light request Id to be send to the connector and the simple protocol request Object to sent by the SP.
    private CorrelationMap<AuthenticationRequest> specificMSSpRequestCorrelationMap;

    private void validateMandatoryFields(AuthenticationRequest specificRequest) {
        if ((specificRequest.getServiceUrl() == null) || ("".equals(specificRequest.getServiceUrl())))
            throw new InvalidParameterEIDASException("0000", "Invalid ServiceUrl");
        if ((specificRequest.getId() == null) || ("".equals(specificRequest.getId())))
            throw new InvalidParameterEIDASException("0000", "Invalid Id");
        if ((specificRequest.getVersion() == null) || ("".equals(specificRequest.getVersion())))
            throw new InvalidParameterEIDASException("0000", "Invalid Version");
        if ((specificRequest.getCreatedOn() == null) || ("".equals(specificRequest.getCreatedOn())))
            throw new InvalidParameterEIDASException("0000", "Invalid CreatedOn");
        if ((specificRequest.getCitizenCountry() == null) || ("".equals(specificRequest.getCitizenCountry())))
            throw new InvalidParameterEIDASException("0000", "Invalid Citizen Country");
    }

    /**
     * Method that translates from the MS Specific Request to the ILightRequest.
     *
     * @param specificRequestBase64 the MS Specific Request Base64 encoded
     * @return the Light Request resulting from translating the MS Specific Request
     * @throws JAXBException if the MS Specific Request could not be unmarshalled
     */
    public ILightRequest translateSpecificRequest(@Nonnull final String specificRequestBase64) throws JAXBException {
        final AuthenticationRequest specificRequest = unmarshallSpecificRequest(specificRequestBase64);
        validateMandatoryFields(specificRequest);
        final LightRequest lightRequest = createLightRequest(specificRequest);
        getSpecificMSSpRequestCorrelationMap().put(lightRequest.getId(), specificRequest);
        return lightRequest;
    }

    private AuthenticationRequest unmarshallSpecificRequest(@Nonnull String specificRequestBase64) throws JAXBException {
        final String specificRequestJson = EidasStringUtil.decodeStringFromBase64(specificRequestBase64);
        final SimpleProtocolProcess simpleProtocolProcess = new SimpleProtocolProcess();
        return (AuthenticationRequest) simpleProtocolProcess.convertFromJson(new StringReader(specificRequestJson), AuthenticationRequest.class);
    }

    boolean doesRandomize() {
    	return getRelaystateRandomizeNull();
	}
    
    private LightRequest createLightRequest(@Nonnull final AuthenticationRequest specificRequest) {
        List<ILevelOfAssurance> levelsOfAssurance = getLevelsOfAssurance(specificRequest);
        final LightRequest.Builder builder = LightRequest.builder()
                .id(UUID.randomUUID().toString())
                .citizenCountryCode(specificRequest.getCitizenCountry())
                .issuer(getIssuerName())
                .relayState(doesRandomize()? createRelayState():null)
                .levelsOfAssurance(levelsOfAssurance)
                .requesterId(specificRequest.getRequesterId())
                .spType(specificRequest.getSpType());

        if (!StringUtils.isEmpty(specificRequest.getNameIdPolicy())) {
            final NameIdPolicyTranslator nameIdPolicyTranslator = NameIdPolicyTranslator.fromSmsspNameIdPolicyString(specificRequest.getNameIdPolicy());
            builder.nameIdFormat(nameIdPolicyTranslator.stringEidasNameIdPolicy());
        }

        builder.providerName(specificRequest.getProviderName());

        final ImmutableAttributeMap requestedAttributes = translateToEidasAttributes(specificRequest);
        builder.requestedAttributes(requestedAttributes);

        return builder.build();
    }

    private List<ILevelOfAssurance> getLevelsOfAssurance(@Nonnull final AuthenticationRequest specificRequest) {
        final RequestedAuthenticationContext authContext = specificRequest.getAuthContext();
        if (authContext == null) {
            return null;
        }

        final List<ILevelOfAssurance> levelsOfAssurance = new ArrayList<>();
        final List<String> contextClass = authContext.getContextClass();
        final List<ILevelOfAssurance> notifiedLoas = translateNotifiedContextClasses(contextClass);
        levelsOfAssurance.addAll(notifiedLoas);

        List<String> nonNotifiedContextClasses = authContext.getNonNotifiedContextClass();
        if (nonNotifiedContextClasses != null && !nonNotifiedContextClasses.isEmpty()) {
            for (String nonNotifiedContextClass : nonNotifiedContextClasses) {
                LevelOfAssurance levelOfAssurance = LevelOfAssurance.builder()
                        .value(nonNotifiedContextClass)
                        .type(LevelOfAssuranceType.NON_NOTIFIED.stringValue())
                        .build();

                levelsOfAssurance.add(levelOfAssurance);
            }
        }

        return levelsOfAssurance;
    }

    @Nonnull
    private List<ILevelOfAssurance> translateNotifiedContextClasses(final List<String> contextClasses) {
        final List<ILevelOfAssurance> translatedContextClasses = new ArrayList<>();
        if (contextClasses != null) {
            for (String contextClass: contextClasses) {
                String translatedContextClassValue = translateNotifiedContextClass(contextClass);
                LevelOfAssurance notifiedLevelOfAssurance = LevelOfAssurance.builder()
                        .value(translatedContextClassValue)
                        .type(LevelOfAssuranceType.NOTIFIED.stringValue())
                        .build();

                translatedContextClasses.add(notifiedLevelOfAssurance);
            }
        }
        return translatedContextClasses;
    }

    private List<String> emptyArrayWhenNull(final List<String> contextClasses) {
        return contextClasses != null ? contextClasses : new ArrayList<>();
    }

    private String translateNotifiedContextClass(@Nonnull final String contextClass) {
        final String levelOfAssurance = ContextClassTranslator.getLevelOfAssurance(contextClass);
        //propagate contextClass if it cannot be translated to level of assurance
        if (null != levelOfAssurance) {
            return levelOfAssurance;
        } else {
            return contextClass;
        }
    }

    private String createRelayState() {
        return UUID.randomUUID().toString();
    }

    private ImmutableAttributeMap translateToEidasAttributes(@Nonnull final AuthenticationRequest specificRequest) {
        ImmutableAttributeMap.Builder builder = ImmutableAttributeMap.builder();

        final List<Attribute> attributes = specificRequest.getAttributes();
        for (Attribute attribute : attributes) {
            final String name = attribute.getName();
            final ImmutableSortedSet<AttributeDefinition<?>> byFriendlyName = getCoreAttributeRegistry().getByFriendlyName(name);
            if (!byFriendlyName.isEmpty()) {
                final AttributeDefinition<?> attributeDefinition = byFriendlyName.first();

                if (attribute instanceof StringAttribute) {

                    if (((StringAttribute) attribute).getValue() != null) {
                        String value = ((StringAttribute) attribute).getValue();
                        builder.put(attributeDefinition, value);
                    } else
                        builder.put(attributeDefinition);

                } else if (attribute instanceof DateAttribute) {
                    final Date value = ((DateAttribute) attribute).getValue();
                    final AttributeValue dateAttributeValue = new DateTimeAttributeValue(new DateTime(value));
                    builder.put(attributeDefinition, dateAttributeValue);
                } else if (attribute instanceof StringListAttribute) {
                    final List<StringListValue> stringListValues = ((StringListAttribute) attribute).getValues();
                    //convert to List of Strings
                    final ArrayList<String> strings = new ArrayList<>();
                    for (StringListValue value : stringListValues)
                        strings.add(value.getValue());
                    builder.putPrimaryValues(attributeDefinition, strings);
                } else if (attribute instanceof AddressAttribute) {
                    final ComplexAddressAttribute complexAddressAttribute = ((AddressAttribute) attribute).getValue();
                    final PostalAddress postalAddress = translateComplexAddressAttribute(complexAddressAttribute);
                    final AttributeValue postalAddressAttributeValue = new PostalAddressAttributeValue(postalAddress);
                    builder.put(attributeDefinition, postalAddressAttributeValue);
                } else {
                    builder.put(attributeDefinition);
                }
            }
        }

        return builder.build();
    }

    private PostalAddress translateComplexAddressAttribute(@Nonnull final ComplexAddressAttribute complexAddressAttribute) {
        PostalAddress.Builder postalAddressBuilder = new PostalAddress.Builder()
                .adminUnitFirstLine(complexAddressAttribute.getAdminUnitFirstLine())
                .adminUnitSecondLine(complexAddressAttribute.getAdminUnitSecondLine())
                .postName(complexAddressAttribute.getPostName())
                .locatorDesignator(complexAddressAttribute.getLocatorDesignator())
                .locatorName(complexAddressAttribute.getLocatorName())
                .poBox(complexAddressAttribute.getPoBox())
                .postCode(complexAddressAttribute.getPostCode())
                .thoroughfare(complexAddressAttribute.getThoroughFare())
                .cvAddressArea(complexAddressAttribute.getAddressArea());

        return postalAddressBuilder.build();
    }

    /**
     * Method that translates from the Node Response to the MS Specific Response.
     *
     * @param iLightResponse     the Node Response to be translated
     * @param httpServletRequest the http servlet request that will contain the the MS Specific Response
     * @return the MS Specific Response translated from the Node Response
     * @throws JAXBException if the MS Specific Response could not be marshalled
     */
    public String translateNodeResponse(@Nonnull final ILightResponse iLightResponse,
                                        @Nonnull final HttpServletRequest httpServletRequest) throws JAXBException {
        String encodedSpecificResponseJson = null;
        final String inResponseToId = iLightResponse.getInResponseToId();
        final AuthenticationRequest authenticationRequest = getRemoveCorrelatedAuthenticationRequest(inResponseToId);
        if (authenticationRequest != null){
            httpServletRequest.setAttribute(SpecificConnectorParameterNames.SP_URL.toString(), authenticationRequest.getServiceUrl());
            final Response response = createSpecificResponse(iLightResponse, authenticationRequest.getId());
            final String specificResponseJson = new SimpleProtocolProcess().convert2Json(response);
            encodedSpecificResponseJson = EidasStringUtil.encodeToBase64(specificResponseJson);
        }

        return encodedSpecificResponseJson;
    }

    private Response createSpecificResponse(@Nonnull final ILightResponse iLightResponse,
                                            @Nonnull final String inResponseToId) {
        final Response response = new Response();
        response.setId(UUID.randomUUID().toString());
        response.setIssuer(getIssuerName());
        response.setInResponseTo(inResponseToId);

        final String ipAddress = iLightResponse.getIPAddress();
        if (StringUtils.isNotEmpty(ipAddress)) {
            response.setClientIpAddress(ipAddress);
        }
        response.setCreatedOn((new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")).format(Calendar.getInstance().getTime()));
        response.setVersion("1");
        final ResponseStatus responseStatus = new ResponseStatus();
        final StatusCodeTranslator statusCodeTranslator = StatusCodeTranslator.fromEidasStatusCodeString(iLightResponse.getStatus().getStatusCode());
        setSimpleProtocolResponseStatus(iLightResponse, response, responseStatus, statusCodeTranslator);

        if (StatusCodeTranslator.SUCCESS == statusCodeTranslator) {
            final String responseLoa = iLightResponse.getLevelOfAssurance();
            final String contextClass = getContextClass(responseLoa);
            response.setAuthContextClass(contextClass);

            response.setSubject(iLightResponse.getSubject());

            final NameIdPolicyTranslator nameIdPolicyTranslator = NameIdPolicyTranslator.fromEidasNameIdPolicyString(iLightResponse.getSubjectNameIdFormat());
            response.setNameId(nameIdPolicyTranslator.stringSmsspNameIdPolicy());

            final List<Attribute> simpleProtocolAttributes = translateLightRequestAttributes(iLightResponse);
            response.setAttributes(simpleProtocolAttributes);
        }

        return response;
    }

    /**
     * Translate the response loa if possible otherwise return the response level of assurance
     * @param responseLoA the level of assurance in the light request.
     * @return the level of assurance of its contextClass translation.
     */
    private String getContextClass(String responseLoA) {
        final String contextClass = ContextClassTranslator.getContextClass(responseLoA);
        return contextClass != null ? contextClass : responseLoA;
    }

    private AuthenticationRequest getRemoveCorrelatedAuthenticationRequest(@Nonnull final String inResponseToId) {
        final AuthenticationRequest authenticationRequest = getSpecificMSSpRequestCorrelationMap().get(inResponseToId);
        if (null != authenticationRequest) {
            getSpecificMSSpRequestCorrelationMap().remove(inResponseToId);
        }
        return authenticationRequest;
    }

    private void setSimpleProtocolResponseStatus(@Nonnull final ILightResponse lightResponse,
                                                 @Nonnull final Response response,
                                                 @Nonnull final ResponseStatus responseStatus,
                                                 @Nonnull final StatusCodeTranslator statusCodeTranslator) {
        responseStatus.setStatusCode(statusCodeTranslator.stringSmsspStatusCode());

        if (statusCodeTranslator == StatusCodeTranslator.RESPONDER_FAILURE || statusCodeTranslator == StatusCodeTranslator.REQUESTER_FAILURE
                || StatusCodeTranslator.VERSION_MISMATCH.equals(statusCodeTranslator)) {
            final String subStatusCode = StringUtils.remove(lightResponse.getStatus().getSubStatusCode(), StatusCodeTranslator.SAML_STATUS_PREFIX);
            responseStatus.setSubStatusCode(subStatusCode);
            responseStatus.setStatusMessage(lightResponse.getStatus().getStatusMessage());
        }

        response.setStatus(responseStatus);
    }

    private List<Attribute> translateLightRequestAttributes(@Nonnull final ILightResponse lightResponse) {
        final ImmutableAttributeMap lightResponseAttributes = lightResponse.getAttributes();
        final ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> attributeMap = lightResponseAttributes.getAttributeMap();

        final List<Attribute> attributes = new ArrayList<>();
        for (AttributeDefinition attributeDefinition : attributeMap.keySet()) {
            final String friendlyName = attributeDefinition.getFriendlyName();

            final ImmutableList<? extends AttributeValue<?>> attributeValues = attributeMap.get(attributeDefinition).asList();

            final Class parameterizedType = attributeDefinition.getParameterizedType();
            if ((DateTime.class).equals(parameterizedType)) {
                final DateAttribute attribute = translateDateAttribute(friendlyName, attributeValues);
                attributes.add(attribute);
            } else if ((PostalAddress.class).equals(parameterizedType)) {
                final AddressAttribute addressAttribute = translateAddressAttribute(attributeValues, friendlyName);
                attributes.add(addressAttribute);
            } else {
                Attribute attribute = translateStringListAttribute(attributeDefinition, attributeValues);
                attributes.add(attribute);
            }
        }

        return attributes;
    }

    private Attribute translateStringListAttribute(AttributeDefinition<?> attributeDefinition, ImmutableList<? extends AttributeValue<?>> attributeValues) {
        final StringListAttribute stringListAttribute = new StringListAttribute();
        stringListAttribute.setName(attributeDefinition.getFriendlyName());
        final ArrayList<StringListValue> stringListValues = new ArrayList<>();

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

        return stringListAttribute;
    }

    private DateAttribute translateDateAttribute(@Nonnull final String friendlyName, ImmutableList<? extends AttributeValue<?>> attributeValues) {
        final DateAttribute attribute = new DateAttribute();
        attribute.setName(friendlyName);
        if (attributeValues.size() != 0) {
            final AttributeValue<?> firstAttributeValue = attributeValues.get(0);
            final DateTime value = (DateTime) firstAttributeValue.getValue();
            attribute.setValue(value.toDate());
        }
        return attribute;
    }

    private void setStringListValueAttributeLatinScript(String valueString, StringListValue stringListValue) {
        final boolean notLatinScript = AttributeValueTransliterator.needsTransliteration(valueString);
        if (notLatinScript) {
            stringListValue.setLatinScript(false);
        }
    }

    private AddressAttribute translateAddressAttribute(ImmutableList<? extends AttributeValue<?>> attributeValues, String friendlyName) {
        final AddressAttribute addressAttribute = new AddressAttribute();
        final AttributeValue<?> firstAttributeValue = attributeValues.get(0);
        PostalAddress postalAddress = (PostalAddress) firstAttributeValue.getValue();

        final ComplexAddressAttribute complexAddressAttribute = new ComplexAddressAttribute();
        complexAddressAttribute.setAdminUnitFirstLine(postalAddress.getAdminUnitFirstLine());
        complexAddressAttribute.setAdminUnitSecondLine(postalAddress.getAdminUnitSecondLine());
        complexAddressAttribute.setPostName(postalAddress.getPostName());
        complexAddressAttribute.setLocatorDesignator(postalAddress.getLocatorDesignator());
        complexAddressAttribute.setLocatorName(postalAddress.getLocatorName());
        complexAddressAttribute.setPoBox(postalAddress.getPoBox());
        complexAddressAttribute.setPostCode(postalAddress.getPostCode());
        complexAddressAttribute.setThoroughFare(postalAddress.getThoroughfare());
        complexAddressAttribute.setAddressArea(postalAddress.getCvAddressArea());
        addressAttribute.setValue(complexAddressAttribute);
        addressAttribute.setName(friendlyName);
        return addressAttribute;
    }

    public CorrelationMap<AuthenticationRequest> getSpecificMSSpRequestCorrelationMap() {
        return specificMSSpRequestCorrelationMap;
    }

    public void setSpecificMSSpRequestCorrelationMap(@Nonnull final CorrelationMap<AuthenticationRequest> specificMSSpRequestCorrelationMap) {
        this.specificMSSpRequestCorrelationMap = specificMSSpRequestCorrelationMap;
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

    public AttributeRegistry getCoreAttributeRegistry() {
        if (null == coreAttributeRegistry) {
            coreAttributeRegistry = AttributeRegistries.fromFiles(getEidasAttributesFile(), null, getAdditionalAttributesFile());
        }

        return coreAttributeRegistry;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public void setSpecificConnectorRequestUrl(String specificConnectorRequestUrl) {
        this.specificConnectorRequestUrl = specificConnectorRequestUrl;
    }

    public String getSpecificConnectorRequestUrl() {
        return specificConnectorRequestUrl;
    }
}
