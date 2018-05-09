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
package eu.eidas.node.auth.service;

import eu.eidas.auth.commons.*;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.exceptions.SecurityEIDASException;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.*;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.utils.EidasNodeErrorUtil;
import eu.eidas.node.utils.EidasNodeValidationUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

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
     * Logger bean.
     */
    private IEIDASLogger loggerBean;

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
     * Minimum QAA Level Allowed.
     */
    private int minQAA;

    /**
     * Maximum QAA Level Allowed.
     */
    private int maxQAA;

    /**
     * Max QAA Level that this ProxyService can authenticate.
     */
    private int maxQAAlevel;

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

            boolean generateSignedAssertion = Boolean.parseBoolean(serviceUtil.getConfigs() == null ? null : serviceUtil
                    .getConfigs()
                    .getProperty(EidasParameterKeys.RESPONSE_SIGN_ASSERTION.toString()));
            serviceUtil.setMetadatUrlToAuthnResponse(getServiceMetadataUrl(), authnResponseBuilder);
            // TODO : Question : Is that even correct
            if (response.getLevelOfAssurance() == null) {
                authnResponseBuilder.levelOfAssurance(
                        serviceUtil.getProperty(EIDASValues.EIDAS_SERVICE_LOA.toString()));
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
                            generateSignedAssertion, ipAddress);

            // Audit
            String message = EIDASValues.SUCCESS.toString() + EIDASValues.EID_SEPARATOR.toString()
                    + EIDASValues.CITIZEN_CONSENT_LOG.toString();

            prepareRespLoggerBean(signedResponse, message);
            saveLog(AUSERVICESAML.LOGGER_COM_RESP);

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
            serviceUtil.setMetadatUrlToAuthnResponse(getServiceMetadataUrl(), eidasAuthnResponseError);

            eidasAuthnResponseError.levelOfAssurance(serviceUtil.getProperty(EIDASValues.EIDAS_SERVICE_LOA.toString()));

            if (isAuditable) {
                // Fix a SAML Engine bug: Don't set InResponseTo
                eidasAuthnResponseError.inResponseTo(authData.getId());
            }

            eidasAuthnResponseError.id(SAMLEngineUtils.generateNCName());
            eidasAuthnResponseError.inResponseTo(authData.getId());

            eidasAuthnResponseError.failure(true);

            IResponseMessage responseMessage =
                    engine.generateResponseErrorMessage(authData, eidasAuthnResponseError.build(), ipUserAddress);

            if (isAuditable) {
                prepareRespLoggerBean(responseMessage, errorMessage);
                saveLog(AUSERVICESAML.LOGGER_COM_RESP);
            }

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
            ProtocolEngineI engine = getSamlEngine();
            IAuthenticationRequest authnRequest = engine.unmarshallRequestAndValidate(samlObj, countryCode);
            EidasAuthenticationRequest.Builder eIDASAuthnRequestBuilder = null;
            EidasMetadataParametersI eidasMetadataParameters = null;
            String issuer = authnRequest.getIssuer();
            if (StringUtils.isNotBlank(issuer)) {
                try {
                    eidasMetadataParameters = metadataFetcher.getEidasMetadata(issuer,
                            (MetadataSignerI) engine.getSigner(),
                            (MetadataClockI) engine.getClock());
                } catch (EIDASMetadataException e) {
                    throw new EIDASSAMLEngineException(e);
                }
            }
            EidasMetadataRoleParametersI connectorRoleMetadata = MetadataUtil.getSPRoleDescriptor(eidasMetadataParameters);
            // retrieve AssertionConsumerURL from the metadata
            String assertionConsumerUrl = connectorRoleMetadata.getDefaultAssertionConsumerUrl();

            // check AssertionConsumerURL if provided in the request
            if (isNotEmpty(authnRequest.getAssertionConsumerServiceURL())) {
                LOGGER.info("validate assertion consumer service url");
                EidasNodeValidationUtil.validateAssertionConsumerURL(authnRequest, assertionConsumerUrl, EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML);
            } else {
                if (authnRequest instanceof IEidasAuthenticationRequest) {
                    EidasAuthenticationRequest.Builder builder =
                            EidasAuthenticationRequest.builder((IEidasAuthenticationRequest) authnRequest);
                    builder.assertionConsumerServiceURL(assertionConsumerUrl);
                    authnRequest = builder.build();
                }
            }

            //the validation which follow should be able to generate fail responses if necessary

            LOGGER.info(LoggingMarkerMDC.SAML_EXCHANGE, "ProxyService - Processing SAML Request with ID {}",
                    authnRequest.getId());

            checkCountryCode(authnRequest, ipUserAddress, relayState);
            checkAttributeList(authnRequest, ipUserAddress, relayState);

            Boolean validateBindingConfig =
                    Boolean.valueOf(serviceUtil.getProperty(EidasParameterKeys.VALIDATE_BINDING.toString()));
            LOGGER.info("validate request binding against message");
            if (validateBindingConfig.booleanValue()) {
                EidasNodeValidationUtil.validateBinding(authnRequest,
                        BindingMethod.fromString(bindingFromHttp),
                        EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML);
            }

            //TODO refactor this destination check to ProtocolEngine after metadata is easy to access there, because it is a SAML2 requirement
            LOGGER.info("validate destination match");
            EidasNodeValidationUtil.validateServiceDestination(authnRequest, serviceUtil, bindingFromHttp,
                    EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL);

            // Validates Connector's Redirect URL
            if (authnRequest instanceof IEidasAuthenticationRequest) {

                IEidasAuthenticationRequest eidasAuthenticationRequest = (IEidasAuthenticationRequest) authnRequest;

                if (isEmpty(authnRequest.getCitizenCountryCode())) {
                    eIDASAuthnRequestBuilder = EidasAuthenticationRequest.builder(eidasAuthenticationRequest);
                    eIDASAuthnRequestBuilder.citizenCountryCode(countryCode);
                }

                if (null != eIDASAuthnRequestBuilder) {
                    authnRequest = eIDASAuthnRequestBuilder.build();
                }

                String highestLevelOfAssuranceSupported =
                        serviceUtil.getProperty(EIDASValues.EIDAS_SERVICE_LOA.toString());
                boolean isLevelOfAssuranceSupported =
                        EidasNodeValidationUtil.isRequestLoAValid(authnRequest, highestLevelOfAssuranceSupported);

                LOGGER.debug("Checking validation for eidas 1,0 - max loa configured {}, validate binding config {}",
                        highestLevelOfAssuranceSupported, validateBindingConfig);

                if (!isLevelOfAssuranceSupported || !engine.getProtocolProcessor()
                        .isAcceptableHttpRequest(authnRequest,
                                validateBindingConfig.booleanValue() ? bindingFromHttp : null)) {


                    String errorMsgCons;
                    String errorCodeCons;
                    if (!isLevelOfAssuranceSupported) {
                        LOGGER.error("BUSINESS EXCEPTION : Invalid Level of Assurance value");
                        errorMsgCons = EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA.errorMessage();
                        errorCodeCons = EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA.errorCode();
                    } else {
                        LOGGER.error("BUSINESS EXCEPTION : Invalid request HTTP binding or SPType");
                        errorMsgCons = EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode();
                        errorCodeCons = EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage();
                    }

                    String errorMessage = EidasErrors.get(errorMsgCons);
                    String errorCode = EidasErrors.get(errorCodeCons);

                    byte[] samlTokenFail =
                            generateErrorAuthenticationResponse(authnRequest, EIDASStatusCode.REQUESTER_URI.toString(),
                                    errorCode, null, errorMessage, ipUserAddress, true);

                    throw new ResponseCarryingServiceException(errorCode, errorMessage,
                            EidasStringUtil.encodeToBase64(samlTokenFail),
                            assertionConsumerUrl, relayState);
                }
                //put spType in the request
                if (isEmpty(authnRequest.getSpType())) {
                    // retrieve TypeFromMetadata from the metadata
                    String spTypeFromMetadata = eidasMetadataParameters.getSpType();
                    eIDASAuthnRequestBuilder = EidasAuthenticationRequest.builder((IEidasAuthenticationRequest) authnRequest);
                    eIDASAuthnRequestBuilder.spType(spTypeFromMetadata);
                    if (null != eIDASAuthnRequestBuilder) {
                        authnRequest = eIDASAuthnRequestBuilder.build();
                    }
                }

                //store relayState to the Request
                if (!isEmpty(relayState)) {
                    if (eIDASAuthnRequestBuilder == null) {
                        eIDASAuthnRequestBuilder = EidasAuthenticationRequest.builder((IEidasAuthenticationRequest) authnRequest);
                    }
                    eIDASAuthnRequestBuilder.relayState(relayState);
                }

                if (null != eIDASAuthnRequestBuilder) {
                    authnRequest = eIDASAuthnRequestBuilder.build();
                }

            } else {
                // Non eidas Messages are not supported
                assertionConsumerUrl = authnRequest.getAssertionConsumerServiceURL();
                String errorCode = EidasErrors.get(EidasErrorKey.MESSAGE_FORMAT_UNSUPPORTED.errorCode());
                String errorMessage = EidasErrors.get(EidasErrorKey.MESSAGE_FORMAT_UNSUPPORTED.errorMessage());
                byte[] samlTokenFail =
                        generateErrorAuthenticationResponse(authnRequest, EIDASStatusCode.RESPONDER_URI.toString(),
                                errorCode, null, errorMessage, ipUserAddress, true);

                throw new ResponseCarryingServiceException(errorCode, errorMessage,
                        EidasStringUtil.encodeToBase64(samlTokenFail),
                        assertionConsumerUrl, relayState);

            }

            // Checking for antiReplay
            checkAntiReplay(samlObj, authnRequest);
            // Logging
            LOGGER.trace("Eidas Audit");
            prepareReqLoggerBean(samlObj, authnRequest);
            saveLog(AUSERVICESAML.LOGGER_COM_REQ);

            return authnRequest;
        } catch (EIDASSAMLEngineException e) {
            LOGGER.info("BUSINESS EXCEPTION : Error validating SAMLToken", e);
            if (EidasErrors.get(EidasErrorKey.INVALID_LOA_VALUE.errorCode()).equals(e.getErrorCode())) {
                throw new EidasNodeException(EidasErrors.get(EidasErrorKey.INVALID_LOA_VALUE.errorCode()),
                        EidasErrors.get(EidasErrorKey.INVALID_LOA_VALUE.errorMessage()), e);
            }
            EidasNodeErrorUtil.processSAMLEngineException(e, LOGGER, EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML);
            throw new EidasNodeException(EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()), e);
        }
    }

    private void checkAntiReplay(byte[] samlObj, IAuthenticationRequest authnRequest) {
        if (!serviceUtil.checkNotPresentInCache(authnRequest.getId(), authnRequest.getCitizenCountryCode())) {
            LOGGER.trace("Eidas Audit");
            prepareReqLoggerBean(samlObj, authnRequest);
            saveLog(AUSERVICESAML.LOGGER_COM_REQ);
            throw new SecurityEIDASException(EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SAML.errorCode()),
                    EidasErrors.get(
                            EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SAML.errorMessage()));
        }

    }

    private void checkAttributeList(IAuthenticationRequest authnRequest, String ipUserAddress, String relayState) {

        if (authnRequest.getRequestedAttributes().isEmpty()) {

            LOGGER.info("BUSINESS EXCEPTION : Invalid Attribute List");

            String errorCode = EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_ATTR_NULL.errorCode());
            String errorMessage = EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_ATTR_NULL.errorMessage());

            byte[] samlTokenFail =
                    generateErrorAuthenticationResponse(authnRequest, EIDASStatusCode.REQUESTER_URI.toString(),
                            errorCode, null, errorMessage, ipUserAddress, true);

            throw new ResponseCarryingServiceException(errorCode, errorMessage,
                    EidasStringUtil.encodeToBase64(samlTokenFail),
                    authnRequest.getAssertionConsumerServiceURL(), relayState);
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

    @Override
    public boolean checkMandatoryAttributeSet(@Nullable ImmutableAttributeMap attributes) {
        ProtocolEngineI engine = getSamlEngine();

        return engine.getProtocolProcessor().checkMandatoryAttributes(attributes);
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
     * Sets all the fields to audit the request.
     *
     * @param samlObj      The SAML token byte[].
     * @param authnRequest The Authentication Request object.
     * @see EidasAuthenticationRequest
     */
    private void prepareReqLoggerBean(byte[] samlObj, IAuthenticationRequest authnRequest) {
        String hashClassName = serviceUtil.getProperty(EidasParameterKeys.HASH_DIGEST_CLASS.toString());
        byte[] tokenHash = EidasDigestUtil.hashPersonalToken(samlObj, hashClassName);
        loggerBean.setTimestamp(DateUtil.currentTimeStamp().toString());
        loggerBean.setOpType(EIDASValues.EIDAS_SERVICE_REQUEST.toString());
        loggerBean.setOrigin(authnRequest.getAssertionConsumerServiceURL());
        loggerBean.setDestination(authnRequest.getDestination());
        loggerBean.setProviderName(authnRequest.getProviderName());
        loggerBean.setCountry(authnRequest.getServiceProviderCountryCode());
        loggerBean.setSamlHash(tokenHash);
        loggerBean.setMsgId(authnRequest.getId());
    }

    /**
     * Sets all the fields to the audit the response.
     *
     * @param message The Saml response message.
     * @param message The message.
     * @see EidasAuthenticationRequest
     */
    protected void prepareRespLoggerBean(IResponseMessage responseMessage, String message) {
        String hashClassName = serviceUtil.getProperty(EidasParameterKeys.HASH_DIGEST_CLASS.toString());
        byte[] tokenHash = EidasDigestUtil.hashPersonalToken(responseMessage.getMessageBytes(), hashClassName);
        loggerBean.setTimestamp(DateUtil.currentTimeStamp().toString());
        loggerBean.setOpType(EIDASValues.EIDAS_SERVICE_RESPONSE.toString());
        IAuthenticationResponse authnResponse = responseMessage.getResponse();
        loggerBean.setInResponseTo(authnResponse.getInResponseToId());
        loggerBean.setMessage(message);
        loggerBean.setSamlHash(tokenHash);
        loggerBean.setMsgId(authnResponse.getId());
    }

    /**
     * Logs the transaction with the Audit log.
     *
     * @param logger The Audit Logger.
     */
    public void saveLog(Logger logger) {
        logger.info(LoggingMarkerMDC.SAML_EXCHANGE, loggerBean.toString());
    }

    /**
     * Getter for loggerBean.
     *
     * @return The loggerBean value.
     * @see IEIDASLogger
     */
    public IEIDASLogger getLoggerBean() {
        return loggerBean;
    }

    /**
     * Setter for loggerBean.
     *
     * @param nLoggerBean The new loggerBean value.
     * @see IEIDASLogger
     */
    public void setLoggerBean(IEIDASLogger nLoggerBean) {
        this.loggerBean = nLoggerBean;
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
     * Getter for maxQAAlevel.
     *
     * @return The maxQAAlevel value.
     */
    public int getMaxQAAlevel() {
        if (maxQAAlevel < getMinQAA() || maxQAAlevel > getMaxQAA()) {
            throw new InvalidParameterEIDASException(EidasErrors.get(EidasErrorKey.QAALEVEL.errorCode()),
                    EidasErrors.get(EidasErrorKey.QAALEVEL.errorMessage()));
        }
        return maxQAAlevel;
    }

    /**
     * Setter for maxQAAlevel.
     *
     * @param nMaxQAAlevel The new maxQAAlevel value.
     */
    public void setMaxQAAlevel(int nMaxQAAlevel) {
        this.maxQAAlevel = nMaxQAAlevel;
    }

    /**
     * Getter for minQAA.
     *
     * @return The minQAA value.
     */
    public int getMinQAA() {
        return minQAA;
    }

    /**
     * Setter for minQAA.
     *
     * @param nMinQAA The new minQAA value.
     */
    public void setMinQAA(int nMinQAA) {
        this.minQAA = nMinQAA;
    }

    /**
     * Getter for maxQAA.
     *
     * @return The maxQAA value.
     */
    public int getMaxQAA() {
        return maxQAA;
    }

    /**
     * Setter for maxQAA Level allowed.
     *
     * @param nMaxQAA The new maxQAA value.
     */
    public void setMaxQAA(int nMaxQAA) {
        this.maxQAA = nMaxQAA;
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
