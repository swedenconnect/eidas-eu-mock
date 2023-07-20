/*
 * Copyright (c) 2023 by European Commission
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

import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.EidasProtocolVersion;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.SpType;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.EidasMetadataRoleParametersI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.exceptions.SamlFailureResponseException;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.service.exceptions.ProxyServiceError;
import eu.eidas.node.service.utils.ProxyServiceErrorUtil;
import eu.eidas.node.utils.EidasNodeValidationUtil;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml.saml2.core.NameIDType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * This class is used by {@link AUSERVICE} to get, process and generate SAML Tokens. Also, it checks attribute values
 * and mandatory attributes.
 *
 * @see ISERVICESAMLService
 */
public class AUSERVICESAML implements ISERVICESAMLService {

    /**
     * Logger object.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(AUSERVICESAML.class);
    /**
     * Response logging.
     */
    protected static final Logger LOGGER_COM_RESP = LoggerFactory.getLogger(
            EIDASValues.EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE.toString() + "." + AUSERVICE.class.getSimpleName());
    /**
     * Request logging.
     */
    private static final Logger LOGGER_COM_REQ = LoggerFactory.getLogger(
            EIDASValues.EIDAS_PACKAGE_REQUEST_LOGGER_VALUE.toString() + "." + AUSERVICE.class.getSimpleName());
    /**
     * Connector's Util class.
     */
    protected AUSERVICEUtil serviceUtil;

    /**
     * Instance of SAML Engine.
     */
    private String samlInstance;

    private ProtocolEngineFactory nodeProtocolEngineFactory;

    /**
     * Country Code of this ProxyService.
     */
    private String countryCode;

    /**
     * Resource bundle to get error messages.
     */
    private MessageSource messageSource;

    private String serviceMetadataUrl;

    private MetadataFetcherI metadataFetcher;

    @Override
    public String getSamlEngineInstanceName() {
        return samlInstance;
    }

    public void setSamlEngineInstanceName(String samlEngineInstanceName) {
        samlInstance = samlEngineInstanceName;
    }

    @Override
    public ProtocolEngineI getSamlEngine() {
        return nodeProtocolEngineFactory.getProtocolEngine(getSamlEngineInstanceName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IResponseMessage processIdpSpecificResponse(IAuthenticationRequest originalRequest,
                                                       AuthenticationResponse response,
                                                       @Nullable String ipUserAddress) {
        try {
            ProtocolEngineI engine = getSamlEngine();
            LOG.trace("check assertion consumer url of the partner requesting this");
            LOG.info(LoggingMarkerMDC.SAML_EXCHANGE, "ProxyService - Generating SAML Response to request with ID {}",
                    originalRequest.getId());
            AuthenticationResponse.Builder authnResponseBuilder = new AuthenticationResponse.Builder(response);

            serviceUtil.setMetadatUrlToAuthnResponse(getServiceMetadataUrl(), authnResponseBuilder);

            if (response.getLevelOfAssurance() == null) {
                // Invalid response - LoA is mandatory
                throw new ProxyServiceError(
                        EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                        EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());
            }

            //TODO EIDINT-1271 - ip address usage
            String ipAddress = null;
            if (StringUtils.isNotBlank(response.getIPAddress())) {
                ipAddress = ipUserAddress;
            }
            // Generate SAMLResponse.
            LOG.debug("Generate SAMLResponse.");
            IResponseMessage signedResponse =
                    engine.generateResponseMessage(originalRequest, authnResponseBuilder.build(), ipAddress);

            return signedResponse;
        } catch (EIDASSAMLEngineException e) {
            ProxyServiceErrorUtil.processSAMLEngineException(e, LOG, EidasErrorKey.IDP_SAML_RESPONSE);
            throw new ProxyServiceError(
                    EidasErrors.get(EidasErrorKey.IDP_SAML_RESPONSE.errorCode()),
                    EidasErrors.get(EidasErrorKey.IDP_SAML_RESPONSE.errorMessage()),
                    "Error generating SAMLToken", e);
        }
    }

    private String resolveErrorMessage(String errorCode, String statusCode, String errorId) {
        String errorMsg;
        try {
            if (StringUtils.isNumeric(errorCode)) {
                errorMsg = messageSource.getMessage(errorId, new Object[]{errorCode}, Locale.getDefault());
            } else {
                errorMsg = messageSource.getMessage(errorId, new Object[]{statusCode}, Locale.getDefault());
            }
        } catch (NoSuchMessageException nme) {//NOSONAR
            if (errorCode == null)
                errorMsg = errorId;
            else
                errorMsg = errorCode + " - " + errorId;
        }
        return errorMsg;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateErrorAuthenticationResponse(IAuthenticationRequest authData,
                                                      String statusCode,
                                                      String errorCodeVal,
                                                      String subCode,
                                                      String errorMessageVal,
                                                      String ipUserAddress,
                                                      boolean isAuditable) {
        try {
            String errorCode;
            String errorMessage;

            ProtocolEngineI engine = getSamlEngine();
            // create SAML token

            AuthenticationResponse.Builder eidasAuthnResponseError = new AuthenticationResponse.Builder();
            eidasAuthnResponseError.statusCode(statusCode);
            eidasAuthnResponseError.subStatusCode(subCode);

            if (EidasErrorKey.fromID(errorMessageVal) != null) {
                errorCode = EidasErrors.get(EidasErrorKey.fromID(errorMessageVal).errorCode());
                errorMessage = EidasErrors.get(EidasErrorKey.fromID(errorMessageVal).errorMessage());
            } else {
                errorCode = errorCodeVal;
                errorMessage = errorMessageVal;
            }

            LOG.debug(LoggingMarkerMDC.SAML_EXCHANGE,
                    "ProxyService - Generating ERROR SAML Response to request with ID {}, error is {} {}",
                    authData.getId(), errorCode, errorMessage);

            eidasAuthnResponseError.statusMessage(resolveErrorMessage(errorCode, statusCode, errorMessage));
            if (!StringUtils.isEmpty(getServiceMetadataUrl())) {
                eidasAuthnResponseError.issuer(getServiceMetadataUrl());
            }

            if (isAuditable) {
                // Fix a SAML Engine bug: Don't set InResponseTo
                eidasAuthnResponseError.inResponseTo(authData.getId());
            }

            eidasAuthnResponseError.id(SAMLEngineUtils.generateNCName());
            eidasAuthnResponseError.inResponseTo(authData.getId());

            eidasAuthnResponseError.failure(true);

            IResponseMessage responseMessage = generateResponseErrorMessage(authData, engine, eidasAuthnResponseError, ipUserAddress);

            return responseMessage.getMessageBytes();
        } catch (EIDASSAMLEngineException e) {
            ProxyServiceErrorUtil.processSAMLEngineException(e, LOG, EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML);
            throw new ProxyServiceError(
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_ERROR_CREATE_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_ERROR_CREATE_SAML.errorMessage()),
                    "Error generating SAMLToken", e);
        }
    }

    private IResponseMessage generateResponseErrorMessage(IAuthenticationRequest authData, ProtocolEngineI protocolEngine, AuthenticationResponse.Builder eidasAuthnResponseError, String ipUserAddress) throws EIDASSAMLEngineException {
        return protocolEngine.generateResponseErrorMessage(authData, eidasAuthnResponseError.build(), ipUserAddress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAuthenticationRequest processConnectorRequest(String bindingFromHttp,
                                                          byte[] samlObj,
                                                          String ipUserAddress,
                                                          String relayState) {
        try {
            LOG.trace("Validating the SAML token");
            // validates SAML Token
            final ProtocolEngineI engine = getSamlEngine();
            IAuthenticationRequest authnRequest = engine.unmarshallRequestAndValidate(samlObj, countryCode);
            final EidasMetadataParametersI issuerMetadataParameters = getIssuerMetadataParameters(engine, authnRequest.getIssuer());
            validateIssuerProtocolVersions(issuerMetadataParameters);
            final EidasMetadataRoleParametersI connectorRoleMetadata = MetadataUtil.getSPRoleDescriptor(issuerMetadataParameters);

            // retrieve AssertionConsumerURL from the metadata
            final String metadataAssertionConsumerUrl = connectorRoleMetadata.getDefaultAssertionConsumerUrl();
            authnRequest = ifNullReplaceAssertionConsumerServiceURL(authnRequest, metadataAssertionConsumerUrl);

            //the validation which follow should be able to generate fail responses if necessary
            LOG.info(LoggingMarkerMDC.SAML_EXCHANGE, "ProxyService - Processing SAML Request with ID {}", authnRequest.getId());
            checkRequesterID(authnRequest, issuerMetadataParameters, ipUserAddress, relayState);
            checkCountryCode(authnRequest);
            checkAttributeList(authnRequest);
            checkBinding(authnRequest, bindingFromHttp);
            checkNameIDFormat(authnRequest);

            //TODO refactor this destination check to ProtocolEngine after metadata is easy to access there, because it is a SAML2 requirement
            LOG.info("validate destination match");
            EidasNodeValidationUtil.validateServiceDestination(authnRequest, serviceUtil, bindingFromHttp);

            if (authnRequest instanceof IEidasAuthenticationRequest) {
                final IEidasAuthenticationRequest eidasAuthenticationRequest = (IEidasAuthenticationRequest) authnRequest;
                authnRequest = ifNullReplaceCitizenCountryCode(authnRequest, countryCode);

                final String semicolonsSeparatedLoAs = serviceUtil.getProperty(EIDASValues.EIDAS_SERVICE_LOA.toString());
                final List<String> servicePublishedLoAs = EidasStringUtil.getDistinctValues(semicolonsSeparatedLoAs);

                final boolean isLevelOfAssuranceSupported =
                        EidasNodeValidationUtil.isRequestLoAValid(eidasAuthenticationRequest, servicePublishedLoAs);
                final Boolean validateBindingConfig = Boolean.valueOf(serviceUtil.getProperty(EidasParameterKeys.VALIDATE_BINDING.toString()));
                LOG.debug("Checking validation for eidas 1,0 - allowed loAs configured {}, validate binding config {}",
                        servicePublishedLoAs, validateBindingConfig);

                if (!isLevelOfAssuranceSupported) {
                    LOG.error("BUSINESS EXCEPTION : Invalid Level of Assurance value");
                    EidasErrorKey errorKey = EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA;
                    throwSamlFailureException(errorKey, authnRequest);
                } else if (!engine.getProtocolProcessor()
                        .isAcceptableHttpRequest(authnRequest, validateBindingConfig ? bindingFromHttp : null)) {
                    LOG.error("BUSINESS EXCEPTION : Invalid request HTTP binding or SPType");
                    EidasErrorKey errorKey = EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML;
                    throwSamlFailureException(errorKey, authnRequest);
                }

                //put spType in the request
                final String spTypeFromMetadata = issuerMetadataParameters.getSpType();
                authnRequest = ifNullReplaceSpType(authnRequest, spTypeFromMetadata);

                //store relayState to the Request
                authnRequest = storeRelayStateInRequest(authnRequest, relayState);

            } else {
                // Non eidas Messages are not supported
                EidasErrorKey errorKey = EidasErrorKey.MESSAGE_FORMAT_UNSUPPORTED;
                throwSamlFailureException(errorKey, authnRequest);
            }

            // Checking for antiReplay
            checkAntiReplay(samlObj, authnRequest);
            LOG.trace("Eidas Audit");
            return authnRequest;

        } catch (EIDASSAMLEngineException e) {
            String errorCode = e.getErrorCode();
            if (EidasErrors.get(EidasErrorKey.INVALID_LOA_VALUE.errorCode()).equals(errorCode)) {
                throw new ProxyServiceError(
                        EidasErrors.get(EidasErrorKey.INVALID_LOA_VALUE.errorCode()),
                        EidasErrors.get(EidasErrorKey.INVALID_LOA_VALUE.errorMessage()), e);
            }
            if (EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA.errorCode()).equals(errorCode)) {
                throw new ProxyServiceError(
                        EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA.errorCode()),
                        EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA.errorMessage()), e);
            }
            if (EidasErrors.get(EidasErrorKey.EIDAS_REPRESENTATIVE_ATTRIBUTES.errorCode()).equals(errorCode)) {
                throw new ProxyServiceError(
                        EidasErrors.get(EidasErrorKey.EIDAS_REPRESENTATIVE_ATTRIBUTES.errorCode()),
                        EidasErrors.get(EidasErrorKey.EIDAS_REPRESENTATIVE_ATTRIBUTES.errorMessage()), e);
            }
            if (EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INCONSISTENT_SPTYPE.errorCode()).equals(errorCode)) {
                throw new ProxyServiceError(
                        EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INCONSISTENT_SPTYPE.errorCode()),
                        EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INCONSISTENT_SPTYPE.errorMessage()), e);
            }
            if (EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_MISSING_SPTYPE.errorCode()).equals(errorCode)) {
                throw new ProxyServiceError(
                        EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_MISSING_SPTYPE.errorCode()),
                        EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_MISSING_SPTYPE.errorMessage()), e);
            }
            ProxyServiceErrorUtil.processSAMLEngineException(e, LOG, EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML);
            throw new ProxyServiceError(
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()),
                    "Error validating SAMLToken", e);
        }
    }

    private void checkBinding(IAuthenticationRequest authnRequest, String bindingFromHttp) {
        final Boolean validateBindingConfig = Boolean.valueOf(serviceUtil.getProperty(EidasParameterKeys.VALIDATE_BINDING.toString()));
        if (validateBindingConfig) {
            LOG.info("validate request binding against message");
            EidasNodeValidationUtil.validateBinding(authnRequest, BindingMethod.fromString(bindingFromHttp));
        }
    }

    private IAuthenticationRequest storeRelayStateInRequest(IAuthenticationRequest authnRequest, String relayState) {
        if (!isEmpty(relayState)) {
            EidasAuthenticationRequest.Builder eIDASAuthnRequestBuilder = EidasAuthenticationRequest.builder((IEidasAuthenticationRequest) authnRequest);
            eIDASAuthnRequestBuilder.relayState(relayState);
            authnRequest = eIDASAuthnRequestBuilder.build();
        }
        return authnRequest;
    }

    private IAuthenticationRequest ifNullReplaceSpType(IAuthenticationRequest authnRequest, String metadataSpType) {
        if (isEmpty(authnRequest.getSpType())) {
            EidasAuthenticationRequest.Builder eIDASAuthnRequestBuilder = EidasAuthenticationRequest.builder((IEidasAuthenticationRequest) authnRequest);
            eIDASAuthnRequestBuilder.spType(metadataSpType);
            if (null != eIDASAuthnRequestBuilder) {
                authnRequest = eIDASAuthnRequestBuilder.build();
            }
        }
        return authnRequest;
    }

    private IAuthenticationRequest ifNullReplaceCitizenCountryCode(IAuthenticationRequest authnRequest, String countryCode) {
        if (isEmpty(authnRequest.getCitizenCountryCode())) {
            EidasAuthenticationRequest.Builder eIDASAuthnRequestBuilder = EidasAuthenticationRequest.builder((IEidasAuthenticationRequest) authnRequest);
            eIDASAuthnRequestBuilder.citizenCountryCode(countryCode);
            return eIDASAuthnRequestBuilder.build();
        }
        return authnRequest;
    }

    private IAuthenticationRequest ifNullReplaceAssertionConsumerServiceURL(IAuthenticationRequest authnRequest, String metadataAssertionConsumerUrl) {
        // check AssertionConsumerURL if provided in the request
        if (isNotEmpty(authnRequest.getAssertionConsumerServiceURL())) {
            LOG.info("validate assertion consumer service url");
            EidasNodeValidationUtil.validateAssertionConsumerURL(authnRequest, metadataAssertionConsumerUrl);
        } else if (authnRequest instanceof IEidasAuthenticationRequest) {
            EidasAuthenticationRequest.Builder builder =
                    EidasAuthenticationRequest.builder((IEidasAuthenticationRequest) authnRequest);
            builder.assertionConsumerServiceURL(metadataAssertionConsumerUrl);
            authnRequest = builder.build();
        }
        return authnRequest;
    }

    private void checkRequesterID(IAuthenticationRequest authnRequest, EidasMetadataParametersI issuerMetadataParameters, String ipUserAddress, String relayState) {
        validatePrivateRequesterHasRequesterId(authnRequest, issuerMetadataParameters)
                .ifPresent(eidasErrorKey -> throwSamlFailureException(eidasErrorKey, authnRequest));
    }

    @Nonnull
    private EidasMetadataParametersI getIssuerMetadataParameters(ProtocolEngineI engine, String issuer) throws EIDASSAMLEngineException {
        if (StringUtils.isNotBlank(issuer)) {
            try {
                return metadataFetcher.getEidasMetadata(issuer,
                        (MetadataSignerI) engine.getSigner(),
                        (MetadataClockI) engine.getClock());
            } catch (EIDASMetadataException e) {
                throw new ProxyServiceError(
                        EidasErrors.get(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorCode()),
                        EidasErrors.get(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorMessage()), e);
            }
        }
        throw new ProxyServiceError(
                EidasErrors.get(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorCode()),
                EidasErrors.get(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorMessage()));
    }

    private void checkAntiReplay(byte[] samlObj, IAuthenticationRequest authnRequest) {
        if (!serviceUtil.checkNotPresentInCache(authnRequest.getId(), authnRequest.getCitizenCountryCode())) {
            throw new ProxyServiceError(
                    EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SAML.errorMessage()),
                    "Eidas Audit");
        }
    }

    @Nonnull
    private Optional<EidasErrorKey> validatePrivateRequesterHasRequesterId(IAuthenticationRequest authenticationRequest,
                                                                           EidasMetadataParametersI connectorMetadata) {
        final boolean proxyMetadataRequiresRequesterId = Boolean.parseBoolean(
                getServiceUtil().getProperty(EIDASValues.REQUESTER_ID_FLAG.getValue())
        );

        if (proxyMetadataRequiresRequesterId) {
            if (isPrivateRequester(authenticationRequest, connectorMetadata)) {
                final String requesterId = authenticationRequest.getRequesterId();
                if (requesterId == null || requesterId.isEmpty()) {
                    return Optional.of(EidasErrorKey.COLLEAGUE_REQ_MISSING_REQUESTER_ID);
                }
            }
        }
        return Optional.empty();
    }

    private boolean isPrivateRequester(IAuthenticationRequest authenticationRequest,
                                       EidasMetadataParametersI connectorMetadata) {
        String spTypeValue = authenticationRequest.getSpType();
        if (spTypeValue == null) {
            spTypeValue = connectorMetadata.getSpType();
        }
        return SpType.PRIVATE.equals(SpType.fromString(spTypeValue));
    }

    private void throwSamlFailureException(EidasErrorKey errorKey, IAuthenticationRequest authenticationRequest) {
        final String errorCode = EidasErrors.get(errorKey.errorCode());
        final String errorMessage = EidasErrors.get(errorKey.errorMessage());

        throw new SamlFailureResponseException(errorCode, errorMessage, EIDASStatusCode.REQUESTER_URI.toString(), authenticationRequest.getRelayState());
    }

    private void checkAttributeList(IAuthenticationRequest authnRequest) {

        if (authnRequest.getRequestedAttributes().isEmpty()) {

            LOG.info("BUSINESS EXCEPTION : Invalid Attribute List");

            throwSamlFailureException(EidasErrorKey.COLLEAGUE_REQ_ATTR_NULL, authnRequest);
        } else {
            Set<String> unsupportedAttributes = getServiceUtil().getUnsupportedAttributes();
            if (unsupportedAttributes != null && !unsupportedAttributes.isEmpty()) {
                Set<String> unsupportedMandatoryAttributes = authnRequest.getRequestedAttributes().getDefinitions()
                        .stream()
                        .filter(AttributeDefinition::isRequired)
                        .map(AttributeDefinition::getNameUri)
                        .map(URI::toASCIIString)
                        .filter(unsupportedAttributes::contains)
                        .collect(Collectors.toSet());
                if (!unsupportedMandatoryAttributes.isEmpty()) {
                    LOG.info("BUSINESS EXCEPTION : Invalid Attribute List");

                    throwSamlFailureException(EidasErrorKey.COLLEAGUE_REQ_ATTR_LIST, authnRequest);
                }
            }
        }
    }

    private void checkNameIDFormat(IAuthenticationRequest authnRequest) {
        String nameIdFormat = authnRequest.getNameIdFormat();
        if (nameIdFormat != null) {
            Set<String> serviceNameIDFormats = getNameIdFormatSet();
            if (!serviceNameIDFormats.contains(nameIdFormat)) {

                LOG.error("BUSINESS EXCEPTION : Invalid NameID format " + nameIdFormat);

                final String errorCode = EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_NAMEID.errorCode());
                final String errorMessage = EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_NAMEID.errorMessage());

                throw new SamlFailureResponseException(errorCode, errorMessage, EIDASStatusCode.REQUESTER_URI.toString(), authnRequest.getRelayState());
            }
        }
    }

    private void checkCountryCode(IAuthenticationRequest authnRequest) {
        // validates if the current countryCode is the same as the countryCode
        // in the request
        String samlCountryCode = authnRequest.getCitizenCountryCode() == null ? null
                : authnRequest.getCitizenCountryCode()
                .replace(EIDASValues.EIDAS_SERVICE_SUFFIX.toString(), StringUtils.EMPTY);
        if (isEmpty(countryCode) || !countryCode.equals(samlCountryCode)) {

            LOG.info("BUSINESS EXCEPTION : Invalid Country Code " + authnRequest.getCitizenCountryCode());

            final String errorCode = EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_COUNTRYCODE.errorCode());
            final String errorMessage = EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_COUNTRYCODE.errorMessage());

            throw new SamlFailureResponseException(errorCode, errorMessage, EIDASStatusCode.REQUESTER_URI.toString(), authnRequest.getRelayState());
        }

    }

    private Set<String> getNameIdFormatSet() {
        Set<String> nameIdFormatSet = new HashSet<>();
        nameIdFormatSet.add(NameIDType.PERSISTENT);
        nameIdFormatSet.add(NameIDType.TRANSIENT);
        nameIdFormatSet.add(NameIDType.UNSPECIFIED);

        String optionalNameIdFormatsStringList = serviceUtil.getConfigs()
                .getProperty(EidasParameterKeys.EIDAS_SERVICE_NAMEID_FORMATS.getValue());
        List<String> optionalNameIdFormats = EidasStringUtil.getDistinctValues(optionalNameIdFormatsStringList);
        for (String nameIDFormat : optionalNameIdFormats) {
            nameIdFormatSet.add(nameIDFormat);
        }

        return nameIdFormatSet;
    }

    @Override
    public boolean checkMandatoryAttributeSet(@Nullable ImmutableAttributeMap attributes) {
        ProtocolEngineI engine = getSamlEngine();

        return engine.getProtocolProcessor().checkMandatoryAttributes(attributes);
    }

    @Override
    public boolean checkRepresentationResponse(@Nullable ImmutableAttributeMap attributes) {
        ProtocolEngineI engine = getSamlEngine();

        return engine.getProtocolProcessor().checkRepresentationResponse(attributes);
    }

    @Override
    public boolean checkRepresentativeAttributes(@Nullable ImmutableAttributeMap attributes) {
        ProtocolEngineI engine = getSamlEngine();

        return engine.getProtocolProcessor().checkRepresentativeAttributes(attributes);
    }

    @Override
    public boolean checkMandatoryAttributes(@Nonnull ImmutableAttributeMap requestedAttributes,
                                            @Nonnull ImmutableAttributeMap responseAttributes) {

        for (AttributeDefinition<?> attributeDefinition : requestedAttributes.getDefinitions()) {

            if (attributeDefinition == null || !attributeDefinition.isRequired()) {
                continue;
            }

            if (responseAttributes.getValuesByNameUri(attributeDefinition.getNameUri()) == null
                    || responseAttributes.getValuesByNameUri(attributeDefinition.getNameUri()).isEmpty()) {
                LOG.info("Missing attributes: " + attributeDefinition);
                return false;
            }
        }

        return true;
    }

    @Override
    @Nonnull
    public IAuthenticationRequest updateRequest(@Nonnull IAuthenticationRequest authnRequest,
                                                @Nonnull ImmutableAttributeMap updatedAttributes) {
        ProtocolEngineI engine = getSamlEngine();

        return engine.getProtocolProcessor().updateRequestWithConsent(authnRequest, updatedAttributes);
    }


    private void validateIssuerProtocolVersions(EidasMetadataParametersI connectorMetadataParameters) {
        final List<String> destinationProtocolVersions = connectorMetadataParameters.getEidasProtocolVersions();
        final String proxyConfiguredProtocolVersions = serviceUtil.getConfigs()
                .getProperty(EIDASValues.EIDAS_PROTOCOL_VERSION.toString());
        final List<String> proxyProtocolVersions = Arrays.asList(proxyConfiguredProtocolVersions.split(";"));

        if (destinationProtocolVersions.isEmpty() || proxyProtocolVersions.isEmpty()) {
            return;
        }
        if (EidasProtocolVersion.getMatchingProtocolVersionsByString(proxyProtocolVersions,destinationProtocolVersions).isEmpty()){
            throw new ProxyServiceError(EidasErrors.get(EidasErrorKey.PROTOCOL_VERSION_UNSUPPORTED.errorCode()),
                    EidasErrors.get(EidasErrorKey.PROTOCOL_VERSION_UNSUPPORTED.errorMessage()));
        }
    }

    /**
     * Getter for countryCode.
     *
     * @return The countryCode value.
     */
    @Override
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Setter for countryCode.
     *
     * @param code The countryCode to set.
     */
    public void setCountryCode(String code) {
        this.countryCode = code;
    }

    /**
     * Setter for messageSource.
     *
     * @param nMessageSource The new messageSource value.
     * @see MessageSource
     */
    public void setMessageSource(MessageSource nMessageSource) {
        this.messageSource = nMessageSource;
    }

    public AUSERVICEUtil getServiceUtil() {
        return serviceUtil;
    }

    public void setServiceUtil(AUSERVICEUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    public String getServiceMetadataUrl() {
        return serviceMetadataUrl;
    }

    public void setServiceMetadataUrl(String serviceMetadataUrl) {
        this.serviceMetadataUrl = serviceMetadataUrl;
    }

    public void setMetadataFetcher(MetadataFetcherI metadataFetcher) {
        this.metadataFetcher = metadataFetcher;
    }

    public ProtocolEngineFactory getNodeProtocolEngineFactory() {
        return nodeProtocolEngineFactory;
    }

    public void setNodeProtocolEngineFactory(ProtocolEngineFactory nodeProtocolEngineFactory) {
        this.nodeProtocolEngineFactory = nodeProtocolEngineFactory;
    }
}
