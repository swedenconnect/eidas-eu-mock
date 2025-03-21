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
package eu.eidas.node.auth.service;

import eu.eidas.auth.commons.EIDASStatusCode;
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
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.node.ProxyBeanNames;
import eu.eidas.node.exceptions.SamlFailureResponseException;
import eu.eidas.node.service.ServiceControllerService;
import eu.eidas.node.service.exceptions.ProxyServiceError;
import eu.eidas.node.utils.EidasNodeValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.cache.Cache;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static eu.eidas.node.BeanProvider.getBean;

/**
 * The AUSERVICE class deals with the requests coming from the Connector. This class communicates with the IdP and APs
 * in order to authenticate the citizen, validate the attributes provided by him/her, and to request the values of the
 * citizen's attributes.
 *
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
            throw new ProxyServiceError(
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()),
                    "SAML Token is null");
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
            return throwSamlFailureException(proxyServiceRequest, validationError.get());
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
            return Optional.of(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES);
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

        if (loaComparison.equals(LevelOfAssuranceComparison.MINIMUM)) {
            if (!EidasNodeValidationUtil.isLoAValid(LevelOfAssuranceComparison.MINIMUM,
                    originalRequest.getLevelOfAssurance(), IdpResponseLoA)) {

                LOG.error("ERROR : IdP response Level of Assurance is to low: requested="
                        + originalRequest.getLevelOfAssurance() +
                        " vs response=" + IdpResponseLoA);
                return Optional.of(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE);
            }
        }

        if (loaComparison.equals(LevelOfAssuranceComparison.EXACT)) {
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
        for (final Map.Entry<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> entry : idpResponse
                .getAttributes().getAttributeMap().entrySet()) {
            AttributeDefinition<?> definition = entry.getKey();
            Set<? extends AttributeValue<?>> values = entry.getValue();

            for (final AttributeValue<?> attributeValue : values) {
                try {
                    AttributeValidator.of(definition).validate(attributeValue);
                }
                catch (InvalidParameterEIDASException e) {
                    throw new ProxyServiceError(e.getErrorCode(),e.getErrorMessage());
                }
            }
        }
    }

    @Nonnull
    private ImmutableAttributeMap updateResponseAttributes(@Nonnull IAuthenticationRequest request,
                                                           @Nonnull ImmutableAttributeMap responseAttributes) {
        boolean modified = false;

        ImmutableAttributeMap.Builder updatedResponseAttributes = ImmutableAttributeMap.builder();

        try {
            for (final Map.Entry<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> entry : responseAttributes
                    .getAttributeMap()
                    .entrySet()) {
                AttributeDefinition<?> definition = entry.getKey();
                Set<? extends AttributeValue<?>> values = entry.getValue();
                if (definition.isUniqueIdentifier()) {
                    String identifierPrefix = getIdentifierPrefix(request);
                    AttributeDefinition<String> identifierAttributeDefinition = (AttributeDefinition<String>) definition;
                    Set<AttributeValue<String>> identifiersValues = (Set<AttributeValue<String>>) values;
                    identifiersValues = updateIdentifierAttribute(identifierAttributeDefinition, identifiersValues, identifierPrefix);
                    if (identifiersValues != null) {
                        modified = true;
                        values = identifiersValues;
                    }
                }
                updatedResponseAttributes.put(definition, (Set) values);
            }
        } catch (AttributeValueMarshallingException e) {
            throw new ProxyServiceError(
                    EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_VALUE.errorCode()),
                    EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_VALUE.errorMessage()), e);
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

    private <V extends AttributeValue<String>> Set<V> updateIdentifierAttribute(AttributeDefinition<String> identifierAttributeDefinition,
                                                                                Set<V> identifiersValues, String identifierPrefix)
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
        Set<V> updatedValues = new HashSet<>();
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
            return updatedValues;
        }
        return null;
    }

    private String getCountryPrefix(@Nonnull IAuthenticationRequest request) {
        String originCountryCode = request.getServiceProviderCountryCode();
        String proxyServiceCountryCode = samlService.getCountryCode();

        return proxyServiceCountryCode + "/" + originCountryCode + "/";
    }

    private IResponseMessage throwSamlFailureException(StoredAuthenticationRequest storedRequest, EidasErrorKey error) {
        IAuthenticationRequest authenticationRequest = storedRequest.getRequest();
        String errorCode = EidasErrors.get(error.errorCode());
        String errorMessage = EidasErrors.get(error.errorMessage());
        if (null == errorCode) {
            errorCode = EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_LIST.errorCode());
        }
        if (null == errorMessage) {
            errorMessage = EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_LIST.errorMessage());
        }

        //puts request back in correlation cache to be used in the message logging
        putStoredRequestInCorrelationCache(storedRequest);

        SamlFailureResponseException serviceSamlFailureResponseError = new SamlFailureResponseException(errorCode, errorMessage,
                EIDASStatusCode.RESPONDER_URI.toString(), authenticationRequest.getRelayState(), storedRequest);

        throw serviceSamlFailureResponseError;
    }

    private void putStoredRequestInCorrelationCache(StoredAuthenticationRequest storedRequest) {
        String beanName = ProxyBeanNames.EIDAS_SERVICE_CONTROLLER.toString();
        ServiceControllerService controllerService = getBean(ServiceControllerService.class, beanName);
        Cache<String, StoredAuthenticationRequest> requestCorrelationMap = controllerService.getProxyServiceRequestCorrelationCache();
        requestCorrelationMap.put(storedRequest.getRequest().getId(), storedRequest);
    }

    /**
     * Generates a exception with an embedded SAML token.
     *
     * @param webRequest A map of parameters to generate the error token.
     * @see Map
     */
    private void sendErrorPage(WebRequest webRequest) {
        if (webRequest.getRequestState().getErrorCode() != null) {
            throw new ProxyServiceError(
                    EidasErrors.get(EidasErrorKey.AUTHENTICATION_FAILED_ERROR.errorCode()),
                    EidasErrors.get(EidasErrorKey.AUTHENTICATION_FAILED_ERROR.errorMessage()));
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
