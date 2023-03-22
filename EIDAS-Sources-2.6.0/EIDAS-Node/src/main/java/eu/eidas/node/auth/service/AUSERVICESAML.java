/*
 * Copyright (c) 2021 by European Commission
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
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.exceptions.SecurityEIDASException;
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
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.utils.EidasNodeErrorUtil;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
    protected static final Logger LOGGER = LoggerFactory.getLogger(AUSERVICESAML.class);
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
                                                       String ipUserAddress) {
        try {
            ProtocolEngineI engine = getSamlEngine();
            LOGGER.trace("check assertion consumer url of the partner requesting this");
            LOGGER.info(LoggingMarkerMDC.SAML_EXCHANGE, "ProxyService - Generating SAML Response to request with ID {}",
                    originalRequest.getId());
            AuthenticationResponse.Builder authnResponseBuilder = new AuthenticationResponse.Builder(response);

            serviceUtil.setMetadatUrlToAuthnResponse(getServiceMetadataUrl(), authnResponseBuilder);

            if (response.getLevelOfAssurance() == null) {
                // Invalid response - LoA is mandatory
                throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
                        EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());
            }

            //TODO EIDINT-1271 - ip address usage
            String ipAddress = null;
            if (StringUtils.isNotBlank(response.getIPAddress())) {
                ipAddress = ipUserAddress;
            }
            // Generate SAMLResponse.
            LOGGER.debug("Generate SAMLResponse.");
            IResponseMessage signedResponse =
                    engine.generateResponseMessage(originalRequest, authnResponseBuilder.build(),
                            ipAddress);

            return signedResponse;
        } catch (EIDASSAMLEngineException e) {
            LOGGER.info("BUSINESS EXCEPTION : Error generating SAMLToken", e.getMessage());
            LOGGER.debug("BUSINESS EXCEPTION : Error generating SAMLToken", e);
            EidasNodeErrorUtil.processSAMLEngineException(e, LOGGER, EidasErrorKey.IDP_SAML_RESPONSE);
            throw new InternalErrorEIDASException(EidasErrors.get(EidasErrorKey.IDP_SAML_RESPONSE.errorCode()),
                    EidasErrors.get(EidasErrorKey.IDP_SAML_RESPONSE.errorMessage()),
                    e);
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

            LOGGER.debug(LoggingMarkerMDC.SAML_EXCHANGE,
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
            LOGGER.info("BUSINESS EXCEPTION : Error generating SAMLToken", e.getMessage());
            LOGGER.debug("BUSINESS EXCEPTION : Error generating SAMLToken", e);
            EidasNodeErrorUtil.processSAMLEngineException(e, LOGGER, EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML);
            throw new InternalErrorEIDASException(
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_ERROR_CREATE_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_ERROR_CREATE_SAML.errorMessage()), e);
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
            LOGGER.trace("Validating the SAML token");
            // validates SAML Token
            final ProtocolEngineI engine = getSamlEngine();
            IAuthenticationRequest authnRequest = engine.unmarshallRequestAndValidate(samlObj, countryCode);
            final EidasMetadataParametersI issuerMetadataParameters = getIssuerMetadataParameters(engine, authnRequest.getIssuer());
            final EidasMetadataRoleParametersI connectorRoleMetadata = MetadataUtil.getSPRoleDescriptor(issuerMetadataParameters);

            // retrieve AssertionConsumerURL from the metadata
            final String metadataAssertionConsumerUrl = connectorRoleMetadata.getDefaultAssertionConsumerUrl();
            authnRequest = ifNullReplaceAssertionConsumerServiceURL(authnRequest, metadataAssertionConsumerUrl);

            //the validation which follow should be able to generate fail responses if necessary
            LOGGER.info(LoggingMarkerMDC.SAML_EXCHANGE, "ProxyService - Processing SAML Request with ID {}", authnRequest.getId());
            checkRequesterID(authnRequest, issuerMetadataParameters, ipUserAddress, relayState);
            checkCountryCode(authnRequest, ipUserAddress, relayState);
            checkAttributeList(authnRequest, ipUserAddress, relayState);
            checkBinding(authnRequest, bindingFromHttp);
            checkNameIDFormat(authnRequest, ipUserAddress, relayState);

            //TODO refactor this destination check to ProtocolEngine after metadata is easy to access there, because it is a SAML2 requirement
            LOGGER.info("validate destination match");
            EidasNodeValidationUtil.validateServiceDestination(authnRequest, serviceUtil, bindingFromHttp,
                    EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL);

            if (authnRequest instanceof IEidasAuthenticationRequest) {
                final IEidasAuthenticationRequest eidasAuthenticationRequest = (IEidasAuthenticationRequest) authnRequest;
                authnRequest = ifNullReplaceCitizenCountryCode(authnRequest, countryCode);

                final String semicolonsSeparatedLoAs = serviceUtil.getProperty(EIDASValues.EIDAS_SERVICE_LOA.toString());
                final List<String> servicePublishedLoAs = EidasStringUtil.getDistinctValues(semicolonsSeparatedLoAs);

                checkServicePublishedLoas(servicePublishedLoAs, issuerMetadataParameters, authnRequest, ipUserAddress, relayState);

                final boolean isLevelOfAssuranceSupported =
                        EidasNodeValidationUtil.isRequestLoAValid(eidasAuthenticationRequest, servicePublishedLoAs);
                final Boolean validateBindingConfig = Boolean.valueOf(serviceUtil.getProperty(EidasParameterKeys.VALIDATE_BINDING.toString()));
                LOGGER.debug("Checking validation for eidas 1,0 - allowed loAs configured {}, validate binding config {}",
                        servicePublishedLoAs, validateBindingConfig);

                if (!isLevelOfAssuranceSupported) {
                    LOGGER.error("BUSINESS EXCEPTION : Invalid Level of Assurance value");
                    EidasErrorKey errorKey = EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA;
                    throwResponseFailureError(errorKey, authnRequest, ipUserAddress, relayState, EIDASStatusCode.REQUESTER_URI);
                } else if (!engine.getProtocolProcessor()
                        .isAcceptableHttpRequest(authnRequest, validateBindingConfig ? bindingFromHttp : null)) {
                    LOGGER.error("BUSINESS EXCEPTION : Invalid request HTTP binding or SPType");
                    EidasErrorKey errorKey = EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML;
                    throwResponseFailureError(errorKey, authnRequest, ipUserAddress, relayState, EIDASStatusCode.REQUESTER_URI);
                }

                //put spType in the request
                final String spTypeFromMetadata = issuerMetadataParameters.getSpType();
                authnRequest = ifNullReplaceSpType(authnRequest, spTypeFromMetadata);

                //store relayState to the Request
                authnRequest = storeRelayStateInRequest(authnRequest, relayState);

            } else {
                // Non eidas Messages are not supported
                EidasErrorKey errorKey = EidasErrorKey.MESSAGE_FORMAT_UNSUPPORTED;
                EIDASStatusCode responderStatusCode = EIDASStatusCode.RESPONDER_URI;
                throwResponseFailureError(errorKey, authnRequest, ipUserAddress, relayState, responderStatusCode);
            }

            // Checking for antiReplay
            checkAntiReplay(samlObj, authnRequest);
            LOGGER.trace("Eidas Audit");
            return authnRequest;

        } catch (EIDASSAMLEngineException e) {
            if (EidasErrors.get(EidasErrorKey.INVALID_LOA_VALUE.errorCode()).equals(e.getErrorCode())) {
                throw new EidasNodeException(EidasErrors.get(EidasErrorKey.INVALID_LOA_VALUE.errorCode()),
                        EidasErrors.get(EidasErrorKey.INVALID_LOA_VALUE.errorMessage()), e);
            }
            if (EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA.errorCode()).equals(e.getErrorCode())) {
                throw new EidasNodeException(EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA.errorCode()),
                        EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA.errorMessage()), e);
            }
            if (EidasErrors.get(EidasErrorKey.EIDAS_REPRESENTATIVE_ATTRIBUTES.errorCode()).equals(e.getErrorCode())) {
                throw new EidasNodeException(EidasErrors.get(EidasErrorKey.EIDAS_REPRESENTATIVE_ATTRIBUTES.errorCode()),
                        EidasErrors.get(EidasErrorKey.EIDAS_REPRESENTATIVE_ATTRIBUTES.errorMessage()), e);
            }
            LOGGER.info("BUSINESS EXCEPTION : Error validating SAMLToken", e);
            EidasNodeErrorUtil.processSAMLEngineException(e, LOGGER, EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML);
            throw new EidasNodeException(EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()), e);
        }
    }

    private void checkServicePublishedLoas(List<String> servicePublishedLoAs, EidasMetadataParametersI issuerMetadataParameters, IAuthenticationRequest authnRequest, String ipUserAddress, String relayState) {
        final EidasProtocolVersion highestProtocolVersion = MetadataUtil.getHighestEidasProtocolVersion(issuerMetadataParameters);
        if (EidasProtocolVersion.PROTOCOL_VERSION_1_1.equals(highestProtocolVersion)) {
            if (!EidasNodeValidationUtil.isFirstLoaIsHighestNotifiedLoa(servicePublishedLoAs)) {
                throwResponseFailureError(EidasErrorKey.SERVICE_PROVIDER_INVALID_LOA, authnRequest, ipUserAddress, relayState, EIDASStatusCode.REQUESTER_URI);
            }
        }
    }

    private void checkBinding(IAuthenticationRequest authnRequest, String bindingFromHttp) {
        final Boolean validateBindingConfig = Boolean.valueOf(serviceUtil.getProperty(EidasParameterKeys.VALIDATE_BINDING.toString()));
        if (validateBindingConfig) {
            LOGGER.info("validate request binding against message");
            EidasNodeValidationUtil.validateBinding(authnRequest, BindingMethod.fromString(bindingFromHttp), EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML);
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
            LOGGER.info("validate assertion consumer service url");
            EidasNodeValidationUtil.validateAssertionConsumerURL(authnRequest, metadataAssertionConsumerUrl, EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML);
        } else if (authnRequest instanceof IEidasAuthenticationRequest) {
            EidasAuthenticationRequest.Builder builder =
                    EidasAuthenticationRequest.builder((IEidasAuthenticationRequest) authnRequest);
            builder.assertionConsumerServiceURL(metadataAssertionConsumerUrl);
            authnRequest = builder.build();
        }
        return authnRequest;
    }

    private void checkRequesterID(IAuthenticationRequest authnRequest, EidasMetadataParametersI issuerMetadataParameters, String ipUserAddress, String relayState) {
        EidasErrorKey error = getRequesterIdErrorKey(authnRequest, issuerMetadataParameters);
        if (error != null) {
            EIDASStatusCode requesterStatusCode = EIDASStatusCode.REQUESTER_URI;
            throwResponseFailureError(error, authnRequest, ipUserAddress, relayState, requesterStatusCode);
        }
    }

    @Nonnull
    private EidasMetadataParametersI getIssuerMetadataParameters(ProtocolEngineI engine, String issuer) throws EIDASSAMLEngineException {
        if (StringUtils.isNotBlank(issuer)) {
            try {
                return metadataFetcher.getEidasMetadata(issuer,
                        (MetadataSignerI) engine.getSigner(),
                        (MetadataClockI) engine.getClock());
            } catch (EIDASMetadataException e) {
                throw new EIDASSAMLEngineException(e);
            }
        }
        throw new EIDASSAMLEngineException("Unable to get Issuer metadata.");
    }

    private void checkAntiReplay(byte[] samlObj, IAuthenticationRequest authnRequest) {
        if (!serviceUtil.checkNotPresentInCache(authnRequest.getId(), authnRequest.getCitizenCountryCode())) {
            LOGGER.trace("Eidas Audit");
            throw new SecurityEIDASException(EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SAML.errorCode()),
                    EidasErrors.get(
                            EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SAML.errorMessage()));
        }

    }

    private EidasErrorKey getRequesterIdErrorKey(IAuthenticationRequest authenticationRequest,
                                                 EidasMetadataParametersI connectorMetadata) {
        EidasProtocolVersion connectorProtocolVersion = getConnectorHighestProtocolVersion(connectorMetadata);
        if (!EidasProtocolVersion.PROTOCOL_VERSION_1_1.isHigherProtocolVersion(connectorProtocolVersion)) {
            String requesterIdFlagPropertyValue = getServiceUtil().getProperty(EIDASValues.REQUESTER_ID_FLAG.getValue());
            if (Boolean.parseBoolean(requesterIdFlagPropertyValue)) {
                if (isPrivateRequester(authenticationRequest, connectorMetadata)
                        && (authenticationRequest.getRequesterId() == null
                        || authenticationRequest.getRequesterId().isEmpty())) {
                    return EidasErrorKey.COLLEAGUE_REQ_MISSING_REQUESTER_ID;
                }
            }
        }
        return null;
    }

    private EidasProtocolVersion getConnectorHighestProtocolVersion(EidasMetadataParametersI connectorMetadata) {
        List<EidasProtocolVersion> protocolVersionList = EidasProtocolVersion
                .fromString(connectorMetadata.getEidasProtocolVersions());
        EidasProtocolVersion highestProtocolVersion = EidasProtocolVersion
                .getHighestProtocolVersion(protocolVersionList);
        return highestProtocolVersion;
    }

    private boolean isPrivateRequester(IAuthenticationRequest authenticationRequest,
            EidasMetadataParametersI connectorMetadata) {
        String spTypeValue = authenticationRequest.getSpType();
        if (spTypeValue == null) {
            spTypeValue = connectorMetadata.getSpType();
        }
        return SpType.PRIVATE.equals(SpType.fromString(spTypeValue));
    }

    private void throwResponseFailureError(EidasErrorKey errorKey, IAuthenticationRequest authenticationRequest,
                                           String userIpAddress, String relayState, EIDASStatusCode statusCode) {
        String errorCode = EidasErrors.get(errorKey.errorCode());
        String errorMessage = EidasErrors.get(errorKey.errorMessage());

        byte[] samlTokenFail =
                generateErrorAuthenticationResponse(authenticationRequest, statusCode.toString(),
                        errorCode, null, errorMessage, userIpAddress, true);

        throw new ResponseCarryingServiceException(errorCode, errorMessage,
                EidasStringUtil.encodeToBase64(samlTokenFail), authenticationRequest.getAssertionConsumerServiceURL(),
                relayState);
    }

    private void checkAttributeList(IAuthenticationRequest authnRequest, String ipUserAddress, String relayState) {

        if (authnRequest.getRequestedAttributes().isEmpty()) {

            LOGGER.info("BUSINESS EXCEPTION : Invalid Attribute List");

            throwResponseFailureError(EidasErrorKey.COLLEAGUE_REQ_ATTR_NULL, authnRequest, ipUserAddress,
                    relayState, EIDASStatusCode.REQUESTER_URI);
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
                    LOGGER.info("BUSINESS EXCEPTION : Invalid Attribute List");

                    throwResponseFailureError(EidasErrorKey.COLLEAGUE_REQ_ATTR_LIST, authnRequest, ipUserAddress,
                            relayState, EIDASStatusCode.REQUESTER_URI);
                }
            }
        }
    }

    private void checkNameIDFormat(IAuthenticationRequest authnRequest, String ipUserAddress, String relayState) {
        String nameIdFormat = authnRequest.getNameIdFormat();
        if (nameIdFormat != null) {
            Set<String> serviceNameIDFormats = getNameIdFormatSet();
            if (!serviceNameIDFormats.contains(nameIdFormat)) {

                LOGGER.error("BUSINESS EXCEPTION : Invalid NameID format " + nameIdFormat);

                String errorCode = EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_NAMEID.errorCode());
                String errorMessage = EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_NAMEID.errorMessage());

                byte[] samlTokenFail =
                        generateErrorAuthenticationResponse(authnRequest, EIDASStatusCode.REQUESTER_URI.toString(),
                                errorCode, null, errorMessage, ipUserAddress, true);

                throw new ResponseCarryingServiceException(errorCode, errorMessage,
                        EidasStringUtil.encodeToBase64(samlTokenFail),
                        authnRequest.getAssertionConsumerServiceURL(), relayState);
            }
        }
    }

    private void checkCountryCode(IAuthenticationRequest authnRequest, String ipUserAddress, String relayState) {
        // validates if the current countryCode is the same as the countryCode
        // in the request
        String samlCountryCode = authnRequest.getCitizenCountryCode() == null ? null
                : authnRequest.getCitizenCountryCode()
                .replace(EIDASValues.EIDAS_SERVICE_SUFFIX.toString(), StringUtils.EMPTY);
        if (isEmpty(countryCode) || !countryCode.equals(samlCountryCode)) {

            LOGGER.info("BUSINESS EXCEPTION : Invalid Country Code " + authnRequest.getCitizenCountryCode());

            String errorCode = EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_COUNTRYCODE.errorCode());
            String errorMessage = EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_COUNTRYCODE.errorMessage());

            byte[] samlTokenFail =
                    generateErrorAuthenticationResponse(authnRequest, EIDASStatusCode.REQUESTER_URI.toString(),
                            errorCode, null, errorMessage, ipUserAddress, true);

            throw new ResponseCarryingServiceException(errorCode, errorMessage,
                    EidasStringUtil.encodeToBase64(samlTokenFail),
                    authnRequest.getAssertionConsumerServiceURL(), relayState);
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
        for (String nameIDFormat: optionalNameIdFormats) {
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
                LOGGER.info("Missing attributes: " + attributeDefinition);
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
