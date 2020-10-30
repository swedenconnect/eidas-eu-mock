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
package eu.eidas.node.auth.service;

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.AttributeUtil;
import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValidator;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.EidasProtocolVersion;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.Gender;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.validation.GenderProtocolVersionValidator;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.service.ServiceControllerService;
import eu.eidas.node.utils.EidasNodeValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.cache.Cache;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static eu.eidas.node.BeanProvider.getBean;

/**
 * The AUSERVICE class deals with the requests coming from the Connector. This class communicates with the IdP and APs
 * in order to authenticate the citizen, validate the attributes provided by him/her, and to request the values of the
 * citizen's attributes.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com, luis.felix@multicert.com,
 *         hugo.magalhaes@multicert.com, paulo.ribeiro@multicert.com
 * @version $Revision: 1.82 $, $Date: 2011-07-07 20:53:51 $
 * @see ISERVICEService
 */
public final class AUSERVICE implements ISERVICEService {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AUSERVICE.class.getName());

    /**
     * Service for citizen related operations.
     */
    private ISERVICECitizenService citizenService;

    /**
     * Service for SAML related operations.
     */
    private ISERVICESAMLService samlService;

    /**
     * Service's Util class.
     */
    private AUSERVICEUtil serviceUtil;

    private String serviceMetadataUrl;

    private boolean isPrefixIdentifiersCountryCode;

    /**
     * {@inheritDoc}
     */
    @Override
    public IAuthenticationRequest processAuthenticationRequest(@Nonnull WebRequest webRequest,
                                                               @Nullable String relayState,
                                                               @Nonnull Cache<String, StoredAuthenticationRequest> requestCorrelationCache,
                                                               @Nonnull String remoteIpAddress) {

        String stringSamlToken = webRequest.getEncodedLastParameterValue(EidasParameterKeys.SAML_REQUEST);

        if (stringSamlToken == null) {
            LOG.info("BUSINESS EXCEPTION : SAML Token is null");
            throw new InvalidParameterEIDASException(
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()));
        }

        byte[] samlToken = EidasStringUtil.decodeBytesFromBase64(stringSamlToken);

        // validate samlToken and populate AuthenticationData
        IAuthenticationRequest authnRequest =
                samlService.processConnectorRequest(webRequest.getMethod().getValue(), samlToken, remoteIpAddress,
                                                    relayState);

        LOG.trace("Validating destination");
        NormalParameterValidator.paramName(EidasErrorKey.SERVICE_REDIRECT_URL.toString())
                .paramValue(authnRequest.getDestination())
                .validate();

        // TODO: should we add an indirection in the returned SAML Request ID here
        // TODO: to prevent that the SAML Request ID sent to the IdP is the same as the one sent by the Connector

        citizenService.checkMandatoryAttributes(authnRequest.getRequestedAttributes());

        return authnRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IResponseMessage processIdpResponse(@Nonnull WebRequest webRequest,
                                               @Nonnull StoredAuthenticationRequest proxyServiceRequest,
                                               @Nonnull ILightResponse idpResponse) {

        IAuthenticationRequest originalRequest = proxyServiceRequest.getRequest();

        Optional<EidasErrorKey> validationError = validateIdpResponse(idpResponse, originalRequest);
        if (validationError.isPresent()) {
            return sendFailure(proxyServiceRequest, validationError.get());
        }

        // update Response Attributes

        IAuthenticationRequest request = proxyServiceRequest.getRequest();
        ImmutableAttributeMap responseAttributes = idpResponse.getAttributes();

        ImmutableAttributeMap updatedResponseAttributes = updateResponseAttributes(request, responseAttributes);

        AuthenticationResponse.Builder authenticationResponseBuilder = AuthenticationResponse.builder();
        authenticationResponseBuilder.levelOfAssurance(idpResponse.getLevelOfAssurance())
                .attributes(updatedResponseAttributes)
                .inResponseTo(originalRequest.getId())
                .ipAddress(idpResponse.getIPAddress())
                .id(SAMLEngineUtils.generateNCName())
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .issuer(getServiceMetadataUrl())
                .subject(idpResponse.getSubject())
                .subjectNameIdFormat(idpResponse.getSubjectNameIdFormat())
                .consent(idpResponse.getConsent());
        serviceUtil.setMetadatUrlToAuthnResponse(getServiceMetadataUrl(), authenticationResponseBuilder);

        String currentIpAddress = webRequest.getRemoteIpAddress();

        return samlService.processIdpSpecificResponse(originalRequest, authenticationResponseBuilder.build(),
                                                      currentIpAddress);
    }

    private Optional<EidasErrorKey> validateIdpResponse(ILightResponse idpResponse,
                                                        IAuthenticationRequest originalRequest) {

        if (null == idpResponse || idpResponse.getStatus().isFailure()) {
            LOG.info("ERROR : IdP response Personal Attribute List is null!");
            return Optional.of(EidasErrorKey.INVALID_ATTRIBUTE_LIST);
        }

        if (!samlService.checkRepresentationResponse(idpResponse.getAttributes())) {
            LOG.info("BUSINESS EXCEPTION : Representation response should always provide 2 sets of MDS");
            return Optional.of(EidasErrorKey.INVALID_ATTRIBUTE_LIST);
        }

        // checks if all mandatory attributes have values.
        if (!samlService.checkMandatoryAttributes(originalRequest.getRequestedAttributes(),
                idpResponse.getAttributes())) {
            LOG.info("BUSINESS EXCEPTION : Mandatory attribute is missing!");
            return Optional.of(EidasErrorKey.ATT_VERIFICATION_MANDATORY);
        }

        // check minimum data set
        if (!samlService.checkMandatoryAttributeSet(idpResponse.getAttributes())) {
            LOG.info("ERROR : IdP response Personal Attribute List is missing mandatory values!");
            return Optional.of(EidasErrorKey.INVALID_ATTRIBUTE_LIST);
        }

        validateAttributes(idpResponse);

        return validateLevelsOfAssuranceErrorKey(originalRequest, idpResponse);
    }

    @Nonnull
    private Optional<EidasErrorKey> validateLevelsOfAssuranceErrorKey(IAuthenticationRequest originalRequest, ILightResponse idpResponse) {
        final List<String> servicePublishedLoAs = EidasStringUtil.getDistinctValues(serviceUtil.getProperty(EIDASValues.EIDAS_SERVICE_LOA.toString()));
        final String IdpResponseLoA = idpResponse.getLevelOfAssurance();
        final LevelOfAssuranceComparison loaComparison = ((EidasAuthenticationRequest) originalRequest).getLevelOfAssuranceComparison();

        if (!EidasNodeValidationUtil.hasCommonLoa(Arrays.asList(IdpResponseLoA), servicePublishedLoAs)) {
            LOG.error("ERROR : IdP response Level of Assurance is not equal to the service's metadata loas"
                    + servicePublishedLoAs +
                    " vs response=" + IdpResponseLoA);
            return Optional.of(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE_UNPUBLISHED);
        }

        if (loaComparison.equals(LevelOfAssuranceComparison.MINIMUM)){
            if (!EidasNodeValidationUtil.isLoAValid(LevelOfAssuranceComparison.MINIMUM,
                    originalRequest.getLevelOfAssurance(), IdpResponseLoA)) {

                LOG.error("ERROR : IdP response Level of Assurance is to low: requested="
                        + originalRequest.getLevelOfAssurance() +
                        " vs response=" + IdpResponseLoA);
                return Optional.of(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE);
            }
        }

        if (loaComparison.equals(LevelOfAssuranceComparison.EXACT)){
            if (!EidasNodeValidationUtil.isNonNotifiedLoAValid(LevelOfAssuranceComparison.EXACT,
                    originalRequest.getLevelsOfAssurance(), IdpResponseLoA)) {

                LOG.error("ERROR : IdP response Level of Assurance does not match: requested="
                        + originalRequest.getLevelsOfAssurance().toString() +
                        " vs response=" + IdpResponseLoA);
                return Optional.of(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE);
            }
        }
        return Optional.empty();
    }

    private void validateAttributes(ILightResponse idpResponse) {
        for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> entry : idpResponse
                .getAttributes().getAttributeMap().entrySet()) {
            AttributeDefinition<?> definition = entry.getKey();
            ImmutableSet<? extends AttributeValue<?>> values = entry.getValue();

            for (final AttributeValue<?> attributeValue : values) {
                AttributeValidator.of(definition).validate(attributeValue);
            }
        }
    }

    @Nonnull
    private ImmutableAttributeMap updateResponseAttributes(@Nonnull IAuthenticationRequest request,
                                                           @Nonnull ImmutableAttributeMap responseAttributes) {
        boolean modified = false;

        ImmutableAttributeMap.Builder updatedResponseAttributes = ImmutableAttributeMap.builder();

        try {
            for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> entry : responseAttributes
                .getAttributeMap()
                .entrySet()) {
                AttributeDefinition<?> definition = entry.getKey();
                ImmutableSet<? extends AttributeValue<?>> values = entry.getValue();
                if (definition.isUniqueIdentifier()) {
                    String identifierPrefix = getIdentifierPrefix(request);
                    AttributeDefinition<String> identifierAttributeDefinition = (AttributeDefinition<String>) definition;
                    ImmutableSet<AttributeValue<String>> identifiersValues = (ImmutableSet<AttributeValue<String>>) values;
                    identifiersValues = updateIdentifierAttribute(identifierAttributeDefinition, identifiersValues, identifierPrefix);
                    if (identifiersValues != null) {
                        modified = true;
                        values = identifiersValues;
                    }
                }
                if (AttributeUtil.isGenderAttributeDefinition(definition)) {
                    AttributeDefinition<Gender> genderAttributeDefinition = (AttributeDefinition<Gender>) definition;
                    ImmutableSet<AttributeValue<Gender>> genderValues = (ImmutableSet<AttributeValue<Gender>>) values;
                    EidasProtocolVersion colleagueProtocolVersion = getDestinationProtocolVersion(request);
                    genderValues = updateGenderAttribute(genderAttributeDefinition, genderValues, colleagueProtocolVersion);
                    if (genderValues != null) {
                        modified = true;
                        values = genderValues;
                    }
                }
                updatedResponseAttributes.put(definition, (ImmutableSet) values);
            }
        } catch (AttributeValueMarshallingException e) {
            throw new IllegalStateException(e);
        }

        if (modified) {
            return updatedResponseAttributes.build();
        }

        return responseAttributes;
    }

    private String getIdentifierPrefix(@Nonnull IAuthenticationRequest request) {
        String identifierPrefix;
        if (!isPrefixIdentifiersCountryCode()) {
            identifierPrefix = "";
        } else {
            identifierPrefix = getCountryPrefix(request);
        }
        return identifierPrefix;
    }

    private <V extends AttributeValue<String>> ImmutableSet<V> updateIdentifierAttribute(AttributeDefinition<String> identifierAttributeDefinition,
            ImmutableSet<V> identifiersValues, String identifierPrefix)
            throws AttributeValueMarshallingException {
        // As per the spec:
        // The uniqueness identifier consists of:
        // 1. The first part is the Nationality Code of the identifier
        // \uF0B7 This is one of the ISO 3166-1 alpha-2 codes, followed by a slash (\u201C/\u201C))
        // 2. The second part is the Nationality Code of the destination country or international organization1
        // \uF0B7 This is one of the ISO 3166-1 alpha-2 codes, followed by a slash (\u201C/\u201C)
        // 3. The third part a combination of readable characters
        // \uF0B7 This uniquely identifies the identity asserted in the country of origin but does not necessarily reveal
        // any discernible correspondence with the subject's actual identifier (for example, username, fiscal number etc)
        // Example: ES/AT/02635542Y (Spanish eIDNumber for an Austrian SP)

        AttributeValueMarshaller<String> attributeValueMarshaller = identifierAttributeDefinition.getAttributeValueMarshaller();
        ImmutableSet.Builder<V> updatedValues = ImmutableSet.builder();
        boolean modifiedValues = false;
        for (final V attributeValue : identifiersValues) {
            String value = attributeValueMarshaller.marshal((AttributeValue) attributeValue);
            if (!value.startsWith(identifierPrefix)) {
                modifiedValues = true;
                V updated = (V) attributeValueMarshaller.unmarshal(identifierPrefix + value,
                            attributeValue.isNonLatinScriptAlternateVersion());
                updatedValues.add(updated);
            } else {
                updatedValues.add(attributeValue);
            }
        }
        if (modifiedValues) {
            return updatedValues.build();
        }
        return null;
    }

    private <V extends AttributeValue<Gender>> ImmutableSet<V> updateGenderAttribute(AttributeDefinition<Gender> genderAttributeDefinition,
            ImmutableSet<V> genderValues, EidasProtocolVersion targetProtocolVersion)
            throws AttributeValueMarshallingException {
        AttributeValueMarshaller<Gender> attributeValueMarshaller = genderAttributeDefinition.getAttributeValueMarshaller();
        ImmutableSet.Builder<V> updatedValues = ImmutableSet.builder();
        boolean modifiedValues = false;
        for (final V attributeValue : genderValues) {
            Gender genderValue = attributeValue.getValue();
            if (!isGenderValueValidForProtocolVersion(genderValue, targetProtocolVersion)) {
                Gender convertedGender = convertGenderForProtocolVersion(genderValue, targetProtocolVersion);
                modifiedValues = true;
                V updated = (V) attributeValueMarshaller.unmarshal(convertedGender.getValue(),
                        attributeValue.isNonLatinScriptAlternateVersion());
                updatedValues.add(updated);
            } else {
                updatedValues.add(attributeValue);
            }
        }
        if (modifiedValues) {
            return updatedValues.build();
        }
        return null;
    }

    private boolean isGenderValueValidForProtocolVersion(Gender gender, EidasProtocolVersion targetProtocolVersion) {
        return GenderProtocolVersionValidator.forProtocolVersion(targetProtocolVersion).isValid(gender);
    }

    private Gender convertGenderForProtocolVersion(Gender gender, EidasProtocolVersion targetProtocolVersion) {
        if (Gender.UNSPECIFIED.equals(gender)
                && EidasProtocolVersion.PROTOCOL_VERSION_1_1.equals(targetProtocolVersion)) {
            return Gender.NOT_SPECIFIED;
        }
        if (Gender.NOT_SPECIFIED.equals(gender)
                && EidasProtocolVersion.PROTOCOL_VERSION_1_2.equals(targetProtocolVersion)) {
            return Gender.UNSPECIFIED;
        }
        if (Gender.NOT_SPECIFIED.equals(gender)){
            return Gender.UNSPECIFIED;
        }
        return gender;
    }

    private EidasProtocolVersion getDestinationProtocolVersion(IAuthenticationRequest request) {
        String connectorMetadataUrl = request.getIssuer();
        // Get Destination connector protocol versions through metadata
        List<EidasProtocolVersion> supportedProtocolVersions =
            samlService.getSamlEngine().getProtocolProcessor().getMetadataProtocolVersions(connectorMetadataUrl);
        EidasProtocolVersion highestProtocolVersion = EidasProtocolVersion.getHighestProtocolVersion(supportedProtocolVersions);

        return highestProtocolVersion;
    }

    private String getCountryPrefix(@Nonnull IAuthenticationRequest request) {
        String originCountryCode = request.getOriginCountryCode();
        String proxyServiceCountryCode = samlService.getCountryCode();

        return proxyServiceCountryCode + "/" + originCountryCode + "/";
    }

    private IResponseMessage sendFailure(StoredAuthenticationRequest storedRequest, EidasErrorKey error) {
        IAuthenticationRequest originalRequest = storedRequest.getRequest();
        String errorCode = EidasErrors.get(error.errorCode());
        String errorMessage = EidasErrors.get(error.errorMessage());
        if (null == errorCode) {
            errorCode = EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_LIST.errorCode());
        }
        if (null == errorMessage) {
            errorMessage = EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_LIST.errorMessage());
        }
        byte[] samlTokenFail = samlService.generateErrorAuthenticationResponse(originalRequest,
                                                                               EIDASStatusCode.RESPONDER_URI.toString(),
                                                                               errorCode, null, errorMessage,
                                                                               storedRequest.getRemoteIpAddress(),
                                                                               true);
        //puts request back in correlation cache to be used in the message logging
        putStoredRequestInCorrelationCache(storedRequest);

        throw new ResponseCarryingServiceException(errorCode, errorMessage,
                                                   EidasStringUtil.encodeToBase64(samlTokenFail),
                                                   originalRequest.getAssertionConsumerServiceURL(),
                                                   storedRequest.getRequest().getRelayState());
    }

    private void putStoredRequestInCorrelationCache(StoredAuthenticationRequest storedRequest) {
        String beanName = NodeBeanNames.EIDAS_SERVICE_CONTROLLER.toString();
        ServiceControllerService controllerService = getBean(ServiceControllerService.class, beanName);
        Cache<String, StoredAuthenticationRequest> requestCorrelationMap = controllerService.getProxyServiceRequestCorrelationCache();
        requestCorrelationMap.put(storedRequest.getRequest().getId(), storedRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateSamlTokenFail(IAuthenticationRequest authData,
                                        String statusCode,
                                        EidasErrorKey error,
                                        String ipUserAddress) {

        return generateSamlTokenFail(authData, statusCode, EidasErrors.get(error.errorCode()),
                                     EIDASSubStatusCode.REQUEST_DENIED_URI.toString(),
                                     EidasErrors.get(error.errorMessage()), ipUserAddress, false);
    }

    @Override
    public String generateSamlTokenFail(IAuthenticationRequest originalRequest,
                                        String statusCode,
                                        String errorCode,
                                        String subCode,
                                        String errorMessage,
                                        String ipUserAddress,
                                        boolean isAuditable) {
        byte[] samlTokenFail =
                samlService.generateErrorAuthenticationResponse(originalRequest, statusCode, errorCode, subCode,
                                                                errorMessage, ipUserAddress, isAuditable);

        return EidasStringUtil.encodeToBase64(samlTokenFail);
    }

    /**
     * Generates a exception with an embedded SAML token.
     *
     * @param webRequest A map of parameters to generate the error token.
     * @see Map
     */
    private void sendErrorPage(WebRequest webRequest) {
        if (webRequest.getRequestState().getErrorCode() != null) {
            String exErrorCode = EidasErrors.get(EidasErrorKey.AUTHENTICATION_FAILED_ERROR.errorCode());
            String exErrorMessage = EidasErrors.get(EidasErrorKey.AUTHENTICATION_FAILED_ERROR.errorMessage());
            throw new EIDASServiceException(exErrorCode, exErrorMessage, null);
        }
    }

    /**
     * Setter for citizenService.
     *
     * @param nCitizenService The new citizenService value.
     * @see ISERVICECitizenService
     */
    public void setCitizenService(final ISERVICECitizenService nCitizenService) {
        this.citizenService = nCitizenService;
    }

    /**
     * Getter for citizenService.
     *
     * @return The citizenService value.
     * @see ISERVICECitizenService
     */
    public ISERVICECitizenService getCitizenService() {
        return citizenService;
    }

    /**
     * Setter for samlService.
     *
     * @param nSamlService The new samlService value.
     * @see ISERVICESAMLService
     */
    public void setSamlService(final ISERVICESAMLService nSamlService) {
        this.samlService = nSamlService;
    }

    /**
     * Getter for samlService.
     *
     * @return The samlService value.
     * @see ISERVICESAMLService
     */
    public ISERVICESAMLService getSamlService() {
        return samlService;
    }

    /**
     * Getter for serviceMetadataUrl
     *
     * @return serviceMetadataUrl value
     */
    public String getServiceMetadataUrl() {
        return serviceMetadataUrl;
    }

    /**
     * Setter for serviceMetadataUrl.
     *
     * @param serviceMetadataUrl The service metadata url value.
     */
    public void setServiceMetadataUrl(String serviceMetadataUrl) {
        this.serviceMetadataUrl = serviceMetadataUrl;
    }

    /**
     * Getter for serviceUtil
     *
     * @return The serviceUtil value
     * @see AUSERVICEUtil
     */
    public AUSERVICEUtil getServiceUtil() {
        return serviceUtil;
    }

    /**
     * Setter for serviceUtil.
     *
     * @param serviceUtil The new serviceUtil value.
     */
    public void setServiceUtil(AUSERVICEUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    /**
     * Getter for isPrefixIdentifiersCountryCode
     *
     * @return The isPrefixIdentifiersCountryCode value
     */
    public boolean isPrefixIdentifiersCountryCode() {
        return isPrefixIdentifiersCountryCode;
    }

    /**
     * Setter for isPrefixIdentifiersCountryCode.
     *
     * @param isPrefixIdentifiersCountryCode The new isPrefixIdentifiersCountryCode value.
     */
    public void setIsPrefixIdentifiersCountryCode(boolean isPrefixIdentifiersCountryCode) {
        this.isPrefixIdentifiersCountryCode = isPrefixIdentifiersCountryCode;
    }
}
