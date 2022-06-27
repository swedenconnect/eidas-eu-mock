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
package eu.eidas.node.auth.connector;

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.AttributeUtil;
import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.RequestState;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValidator;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.exceptions.InvalidSessionEIDASException;
import eu.eidas.auth.commons.exceptions.SecurityEIDASException;
import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.impl.LevelOfAssuranceUtils;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.EidasProtocolVersion;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.SpType;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.Gender;
import eu.eidas.auth.commons.protocol.eidas.impl.GenderAttributeValue;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.protocol.impl.EidasSamlBinding;
import eu.eidas.auth.commons.tx.AuthenticationExchange;
import eu.eidas.auth.commons.tx.FlowIdCache;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredLightRequest;
import eu.eidas.auth.commons.validation.GenderProtocolVersionValidator;
import eu.eidas.auth.commons.validation.LengthParameterValidator;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.auth.engine.Correlated;
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
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.utils.EidasNodeErrorUtil;
import eu.eidas.node.utils.EidasNodeValidationUtil;
import eu.eidas.node.utils.PropertiesUtil;
import eu.eidas.node.utils.SessionHolder;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml.saml2.core.NameIDType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static eu.eidas.node.BeanProvider.getBean;

/**
 * This class is used by {@link AUCONNECTOR} to get, process and generate SAML Tokens.
 *
 * @see ICONNECTORSAMLService
 */
public final class AUCONNECTORSAML implements ICONNECTORSAMLService {

    /** Logger object */
    private static final Logger LOG = LoggerFactory.getLogger(AUCONNECTORSAML.class);

    /**Request logging.*/
    private static final Logger LOGGER_COM_REQ = LoggerFactory.getLogger(
            EIDASValues.EIDAS_PACKAGE_REQUEST_LOGGER_VALUE.toString() + "." + AUCONNECTOR.class.getSimpleName());

    /** Response logging. */
    private static final Logger LOGGER_COM_RESP = LoggerFactory.getLogger(
            EIDASValues.EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE.toString() + "." + AUCONNECTOR.class.getSimpleName());

    /** Maximum length allowed for nameIdFormat for {@link NameIDType#PERSISTENT} and {@link NameIDType#TRANSIENT}*/
    private static final int MAX_LENGTH_FOR_NAME_ID_FORMAT_PERSISTENT_TRANSIENT = 256;

    /** Maximum length allowed for nameIdFormat for {@link NameIDType#ENTITY} */
    private static final int MAX_LENGTH_FOR_NAME_ID_FORMAT_ENTITY = 1024;

    /** Unspecified value for NameID format*/
    public static final String URN_OASIS_NAMES_TC_SAML_1_1_NAMEID_FORMAT_UNSPECIFIED = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";

    /**SAML instance to communicate with ServiceProxy. */
    private String samlServiceInstance;

    /** Connector's processAuthenticationResponse class. */
    private AUCONNECTORUtil connectorUtil;

    /** metadata url to be put in requests generated by the Connector module. */
    private String metadataUrl;

    /** metadata url to be put in responses generated by the Connector module. */
    private String metadataResponderUrl;

    /**Resource bundle to translate messages from ServiceProxy/VIdP. */
    private MessageSource messageSource;

    private boolean checkCitizenCertificateServiceCertificate;

    private MetadataFetcherI metadataFetcher;

    private ProtocolEngineFactory nodeProtocolEngineFactory;

    private boolean validatePrefixCountryCodeIdentifiers;

	private static final Pattern SERVICE_METADATA_WHITELIST_PATTERN = Pattern.compile("service.+\\.metadata\\.url");

    private FlowIdCache getFlowIdCache() {
        String beanName = NodeBeanNames.EIDAS_CONNECTOR_FLOWID_CACHE.toString();
        FlowIdCache flowIdCache = getBean(FlowIdCache.class, beanName);
        return flowIdCache;
    }

    public void setCheckCitizenCertificateServiceCertificate(boolean checkCitizenCertificateServiceCertificate) {
        this.checkCitizenCertificateServiceCertificate = checkCitizenCertificateServiceCertificate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateErrorAuthenticationResponse(HttpServletRequest httpRequest,
                                                      String destination,
                                                      String statusCode,
                                                      String subCode,
                                                      String message) {
        EidasAuthenticationRequest.Builder request = new EidasAuthenticationRequest.Builder();
        request.id(EidasNodeErrorUtil.getInResponseTo(httpRequest));
        request.issuer(EidasNodeErrorUtil.getIssuer(httpRequest));
        request.destination(destination);
        request.citizenCountryCode(EidasNodeErrorUtil.getCitizenCountryCode(httpRequest));
        request.assertionConsumerServiceURL(destination);
        IAuthenticationRequest dummyRequest = request.build();

        return generateErrorAuthenticationResponse(dummyRequest, httpRequest.getRemoteAddr(), statusCode, subCode,
                                                   message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateErrorAuthenticationResponse(IAuthenticationRequest request,
                                                      String ipUserAddress,
                                                      String statusCode,
                                                      String subCode,
                                                      String message) {
        AuthenticationResponse.Builder samlResponseFail = new AuthenticationResponse.Builder();
        samlResponseFail.statusCode(statusCode);
        samlResponseFail.subStatusCode(subCode);
        samlResponseFail.statusMessage(message);
        samlResponseFail.issuer(getConnectorMetadataUrl());
        samlResponseFail.inResponseTo(request.getId());
        samlResponseFail.id(SAMLEngineUtils.generateNCName());
        IAuthenticationResponse response = samlResponseFail.build();

        return generateErrorAuthenticationResponse(request, ipUserAddress, response, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateErrorAuthenticationResponse(IAuthenticationRequest request,
                                                      String ipUserAddress,
                                                      IAuthenticationResponse response,
                                                      String message) {
        //TODO vargata - removed temporary - error response genereation (if needed) to be relocated to specific
        return new byte[0];
        /*
        try {
            ProtocolEngineI engine = getSamlEngine(samlSpInstance);
            // Generate SAMLResponse Fail.
            String inResponseTo = request.getId();

            AuthenticationResponse.Builder samlResponseFail = new AuthenticationResponse.Builder();
            samlResponseFail.id(response.getId());
            samlResponseFail.statusCode(response.getSubStatusCode());
            samlResponseFail.subStatusCode(response.getSubStatusCode());
            samlResponseFail.statusMessage(message);
            samlResponseFail.issuer(getConnectorResponderMetadataUrl());
            samlResponseFail.inResponseTo(inResponseTo);

            IResponseMessage responseMessage =
                    engine.generateResponseErrorMessage(request, samlResponseFail.build(), ipUserAddress);

            prepareRespLoggerBean(EIDASValues.SP_RESPONSE.toString(), responseMessage.getResponse(), inResponseTo);
            saveLog(AUCONNECTORSAML.LOGGER_COM_RESP);
            LOG.info(LoggingMarkerMDC.SAML_EXCHANGE,
                     "Connector - Generating ERROR SAML Response to request with ID {}, error is {} {}", inResponseTo,
                     response.getSubStatusCode(), message);

            return responseMessage.getMessageBytes();
        } catch (EIDASSAMLEngineException e) {
            LOG.info("BUSINESS EXCEPTION : Error generating SAMLToken", e);
            EidasNodeErrorUtil.processSAMLEngineException(e, LOG, getConnectorRedirectError(e,
                                                                                            EidasErrorKey.SPROVIDER_SELECTOR_ERROR_CREATE_SAML));
            throw new InternalErrorEIDASException(
                    EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_ERROR_CREATE_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_ERROR_CREATE_SAML.errorMessage()), e);
        }*/
    }

    private EidasErrorKey getConnectorRedirectError(EIDASSAMLEngineException exc, EidasErrorKey defaultError) {
        EidasErrorKey redirectError = defaultError;
        EidasErrorKey actualError = EidasErrorKey.fromCode(exc.getErrorCode());
        if (actualError != null && actualError.isShowToUser()) {
            redirectError = actualError;
        }
        return redirectError;
    }

    /**
     * {@inheritDoc}
     */
    public byte[] extractResponseSAMLToken(WebRequest webRequest) {

        String strSamlToken = webRequest.getEncodedLastParameterValue(EidasParameterKeys.SAML_RESPONSE);

        NormalParameterValidator.paramName(EidasParameterKeys.SAML_RESPONSE)
                .paramValue(strSamlToken)
                .eidasError(EidasErrorKey.valueOf(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.name()))
                .validate();

        if (StringUtils.isBlank(strSamlToken)) {
            return null;
        }

        return EidasStringUtil.decodeBytesFromBase64(strSamlToken);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAuthenticationRequest processSpRequest(@Nonnull ILightRequest lightRequest, WebRequest webRequest) {
        try {
            final String serviceCode = connectorUtil.getCountryCode(lightRequest, webRequest);
            LOG.debug("Requested country: " + serviceCode);
            final String serviceMetadataURL = getConnectorUtil().loadConfigServiceMetadataURL(serviceCode);
            validateServiceRedirectUrlValue(serviceMetadataURL);

            final EidasMetadataParametersI serviceMetadataParameters = getEidasMetadataParameters(serviceMetadataURL);
            final String serviceUrl = getRequestServiceUrl(webRequest, serviceMetadataParameters);
            LOG.debug("Citizen Country URL " + serviceCode + " URL " + serviceUrl);
            validateServiceRedirectUrlValue(serviceUrl);

            IEidasAuthenticationRequest authnRequest = EidasAuthenticationRequest.builder()
                    .lightRequest(lightRequest)
                    .destination(serviceUrl)
                    .citizenCountryCode(serviceCode)
                    .build();

            LOG.info(LoggingMarkerMDC.SAML_EXCHANGE, "Connector - Processing LightRequest with ID {}",
                    lightRequest.getId());

            // Get Personal Attribute List and validate
            final ImmutableAttributeMap requestedAttributes = lightRequest.getRequestedAttributes();
            NormalParameterValidator.paramName(EidasParameterKeys.ATTRIBUTE_LIST)
                    .paramValue(requestedAttributes.isEmpty() ? null : "dummy")
                    .eidasError(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_ATTR)
                    .validate();

            final List<String> levelsOfAssuranceValues = convertToLevelsOfAssuranceValueList(lightRequest.getLevelsOfAssurance());
            final RequestState requestState = webRequest.getRequestState();
            if (levelsOfAssuranceValues != null && !levelsOfAssuranceValues.isEmpty()) {
                requestState.setLevelsOfAssurance(levelsOfAssuranceValues);
            }

            requestState.setProviderName(lightRequest.getProviderName());

            authnRequest = ifServiceProtocolVersion1_1RemoveNonNotifiedLoas(authnRequest, serviceMetadataParameters);

            authnRequest = ifServiceProtocolVersion1_1IfNameIdFormatNullUnspecified(authnRequest, serviceMetadataParameters);

            validateRequestAgainstPublishedProxyServiceLoas(authnRequest, serviceMetadataParameters);
            validateRequestNameID(authnRequest, MetadataUtil.getIDPRoleDescriptor(serviceMetadataParameters));

            if (SessionHolder.getId() != null) {
                HttpSession session = SessionHolder.getId();
                session.setAttribute(EidasParameterKeys.SAML_IN_RESPONSE_TO.toString(), lightRequest.getId());
                session.setAttribute(EidasParameterKeys.ISSUER.toString(), lightRequest.getIssuer());
            }

            LOG.trace("Checking if SP is reliable");
            requestState.setInResponseTo(authnRequest.getId());
            requestState.setIssuer(authnRequest.getIssuer());
            requestState.setServiceUrl(authnRequest.getAssertionConsumerServiceURL());

            // Validate if SP has valid qaalevel and is trustworthy
            if (!connectorUtil.validateSP(webRequest)) {
                throw new InvalidParameterEIDASException(
                        EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SPQAAID.errorCode()),
                        EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SPQAAID.errorMessage()));
            }
            validateRequesterId(authnRequest, serviceMetadataParameters.isRequesterIdFlag());

            return authnRequest;
        } catch (EIDASSAMLEngineException e) {
            // Special case for propagating the error in case of xxe
            EidasNodeErrorUtil.processSAMLEngineException(e, LOG, getConnectorRedirectError(e,
                    EidasErrorKey.SAML_ENGINE_NO_METADATA));
            throw new InternalErrorEIDASException(
                    EidasErrors.get(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorCode()),
                    EidasErrors.get(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorMessage()), e);
        }
    }

    @Nullable
    private String getRequestServiceUrl(WebRequest webRequest, EidasMetadataParametersI serviceMetadataParameters) {
        final EidasMetadataRoleParametersI spDesc = MetadataUtil.getIDPRoleDescriptor(serviceMetadataParameters);
        final String httpMethod = webRequest.getMethod().getValue();
        String lastValue = null;
        for (Map.Entry<String, String> metadataBinding : spDesc.getProtocolBindingLocations().entrySet()) {
            lastValue = metadataBinding.getValue();
            final String metadataBindingMethod = metadataBinding.getKey();
            if (httpMethod.equalsIgnoreCase(metadataBindingMethod)) {
                return lastValue;
            }
        }
        return lastValue;
    }

    private void validateRequestAgainstPublishedProxyServiceLoas(IEidasAuthenticationRequest authnRequest, EidasMetadataParametersI serviceMetadataParameters) {
        final List<String> versionStrings = serviceMetadataParameters.getEidasProtocolVersions();
        final List<EidasProtocolVersion> eidasProtocolVersions = EidasProtocolVersion.fromString(versionStrings);
        final List<String> proxyServiceLoAs = serviceMetadataParameters.getAssuranceLevels();
        if (proxyServiceLoAs != null && !proxyServiceLoAs.isEmpty() && (
                !EidasNodeValidationUtil.isRequestLoAValid(authnRequest, proxyServiceLoAs) ||
                        eidasProtocolVersions.contains(EidasProtocolVersion.PROTOCOL_VERSION_1_1) &&
                                !EidasNodeValidationUtil.isFirstLoaIsHighestNotifiedLoa(proxyServiceLoAs)
        )) {
            throw new InternalErrorEIDASException(
                    EidasErrors.get(EidasErrorKey.SERVICE_PROVIDER_INVALID_LOA.errorCode()),
                    EidasErrors.get(EidasErrorKey.SERVICE_PROVIDER_INVALID_LOA.errorMessage()));
        }
    }

    private void validateRequestNameID(IEidasAuthenticationRequest authnRequest, EidasMetadataRoleParametersI serviceMetadataParameters) {
        String requestNameIdFormat = authnRequest.getNameIdFormat();
        if (requestNameIdFormat != null) {
            Set<String> serviceNameIDFormats = serviceMetadataParameters.getNameIDFormats();
            Set<String> connectorNameIDFormats = getNameIdFormatsSet();
            if (!serviceNameIDFormats.contains(requestNameIdFormat)
                    || !connectorNameIDFormats.contains(requestNameIdFormat)) {
                LOG.error("Invalid Request: NameId " + requestNameIdFormat + " is unavailable");
                throw new InternalErrorEIDASException(
                        EidasErrors.get(EidasErrorKey.SP_REQUEST_INVALID.errorCode()),
                        EidasErrors.get(EidasErrorKey.SP_REQUEST_INVALID.errorMessage()));
            }
        }
    }

    private IEidasAuthenticationRequest ifServiceProtocolVersion1_1RemoveNonNotifiedLoas(IEidasAuthenticationRequest authnRequest, EidasMetadataParametersI eidasMetadataParameters) {
        final List<String> versionStrings = eidasMetadataParameters.getEidasProtocolVersions();
        final List<EidasProtocolVersion> eidasProtocolVersions = EidasProtocolVersion.fromString(versionStrings);
        final EidasProtocolVersion highestProtocolVersion = EidasProtocolVersion.getHighestProtocolVersion(eidasProtocolVersions);
        if (EidasProtocolVersion.PROTOCOL_VERSION_1_1.equals(highestProtocolVersion)) {
            final List<ILevelOfAssurance> onlyNotifiedLoas = authnRequest.getLevelsOfAssurance().stream()
                    .filter(LevelOfAssuranceUtils::isNotified)
                    .collect(Collectors.toList());
            if (onlyNotifiedLoas.size() < 1) {
                throw new InternalErrorEIDASException(
                        EidasErrors.get(EidasErrorKey.SERVICE_PROVIDER_INVALID_LOA.errorCode()),
                        EidasErrors.get(EidasErrorKey.SERVICE_PROVIDER_INVALID_LOA.errorMessage()));
            }
            authnRequest = new EidasAuthenticationRequest.Builder(authnRequest)
                    .levelsOfAssurance(onlyNotifiedLoas)
                    .build();
        }
        return authnRequest;
    }

    private IEidasAuthenticationRequest ifServiceProtocolVersion1_1IfNameIdFormatNullUnspecified(IEidasAuthenticationRequest authnRequest, EidasMetadataParametersI eidasMetadataParameters) {
        final List<String> versionStrings = eidasMetadataParameters.getEidasProtocolVersions();
        final List<EidasProtocolVersion> eidasProtocolVersions = EidasProtocolVersion.fromString(versionStrings);
        final EidasProtocolVersion highestProtocolVersion = EidasProtocolVersion.getHighestProtocolVersion(eidasProtocolVersions);
        if (EidasProtocolVersion.PROTOCOL_VERSION_1_1.equals(highestProtocolVersion)) {
            if (authnRequest.getNameIdFormat() == null) {
                authnRequest = new EidasAuthenticationRequest.Builder(authnRequest)
                        .nameIdFormat(NameIDType.UNSPECIFIED)
                        .build();
            }
        }
        return authnRequest;
    }

    private void validateServiceRedirectUrlValue(String url) {
        NormalParameterValidator.paramName(EidasErrorKey.SERVICE_REDIRECT_URL.toString())
                .paramValue(url)
                .eidasError(EidasErrorKey.SERVICE_REDIRECT_URL)
                .validate();
    }

    /**
     * RequesterId must be present when request is made by a private SP
     * and RequesterId is required by proxyService
     *
     * If RequesterId is present (even if not mandatory), it must be valid (an URI of not more than 1024 characters)
     *
     * @param authenticationRequest the request from which the presence and the value of the requesterId needs to be validated
     * @param isRequesterIdRequired ProxyService metadata flag indicating if requester id is needed
     */
    private void validateRequesterId(IAuthenticationRequest authenticationRequest, boolean isRequesterIdRequired) {
        boolean isPrivateSpType = isPrivateSpType(authenticationRequest);
        if ((StringUtils.isBlank(authenticationRequest.getRequesterId())
                && (isPrivateSpType && isRequesterIdRequired))
                || (StringUtils.isNotBlank(authenticationRequest.getRequesterId())
                && !isRequesterIdValueValid(authenticationRequest.getRequesterId()))) {
            String errorCode = EidasErrors.get(EidasErrorKey.SPROVIDER_INVALID_REQUESTERID.errorCode());
            String errorMessage = EidasErrors.get(EidasErrorKey.SPROVIDER_INVALID_REQUESTERID.errorMessage());
            throw new InvalidParameterEIDASException(errorCode, errorMessage);
        }
    }

    private boolean isPrivateSpType(IAuthenticationRequest authenticationRequest) {
        String spType = connectorUtil.getSPType();
        if (spType == null) {
            spType = authenticationRequest.getSpType();
        }
        return SpType.PRIVATE.getValue().equals(spType);
    }


    private boolean isRequesterIdValueValid(String requesterId) {
        try {
            new URI(requesterId);
        } catch (URISyntaxException e) {
            return false;
        }
        boolean isRequesterIdValid = LengthParameterValidator
                .forParam(EidasParameterKeys.REQUESTER_ID)
                .isValid(requesterId);
        return isRequesterIdValid;
    }

    private boolean isIssuedBySelf(IAuthenticationRequest authnRequest) {
        String connectorMetadataUrl = getConnectorMetadataUrl();
        return connectorMetadataUrl != null && connectorMetadataUrl.equalsIgnoreCase(authnRequest.getIssuer());
    }

    /**
     * Convert the list of {@link ILevelOfAssurance} to a list of its values
     * @return the list of the values of the levels Of Assurance.
     */
    private static List<String> convertToLevelsOfAssuranceValueList(final List<ILevelOfAssurance> levelsOfAssurance) {
        if (levelsOfAssurance == null || levelsOfAssurance.isEmpty()) {
            return null;
        }
        List<String> levelsOfAssuranceValues = levelsOfAssurance.stream()
                .map(ILevelOfAssurance::getValue)
                .collect(Collectors.toList());
        return levelsOfAssuranceValues;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRequestMessage generateServiceAuthnRequest(@Nonnull WebRequest webRequest, @Nonnull IAuthenticationRequest request) {

        String serviceCountryCode = connectorUtil.getCountryCode(request, webRequest);

        //TODO check if tempAuthData creation could be avoided
        IRequestMessage tempAuthData = generateAuthenticationRequest(samlServiceInstance, request, serviceCountryCode);

        return tempAuthData;
    }

    private String extractErrorMessage(String defaultMsg, String errorCode) {
        String newErrorMessage = defaultMsg;
        try {
            newErrorMessage = messageSource.getMessage(errorCode, new Object[] {errorCode}, Locale.getDefault());
        } catch (NoSuchMessageException nsme) {
            LOG.warn("Cannot found the message with the id {} - {}", errorCode, nsme);
        }
        return newErrorMessage;
    }

    public AuthenticationExchange processProxyServiceResponse(@Nonnull WebRequest webRequest,
                                                              @Nonnull Cache<String, StoredAuthenticationRequest> connectorRequestCorrelationCache,
                                                              @Nonnull Cache<String, StoredLightRequest> specificSpRequestCorrelationCache)
            throws InternalErrorEIDASException {
        try {

            LOG.trace("Getting SAML Token");
            byte[] responseFromProxyService = this.extractResponseSAMLToken(webRequest);

            // validates SAML Token
            ProtocolEngineI engine = getSamlEngine(samlServiceInstance);

            Correlated proxyServiceSamlResponse = engine.unmarshallResponse(responseFromProxyService);

            String connectorRequestId = proxyServiceSamlResponse.getInResponseToId();

            if (StringUtils.isBlank(connectorRequestId)) {
                LOG.error("ERROR : SAML Response \"" + proxyServiceSamlResponse.getId() + "\" has no InResponseTo");
                throw new InvalidSessionEIDASException(EidasErrors.get(EidasErrorKey.AU_REQUEST_ID.errorCode()),
                                                       EidasErrors.get(EidasErrorKey.AU_REQUEST_ID.errorMessage()));
            }

            StoredAuthenticationRequest storedConnectorRequest = connectorRequestCorrelationCache.get(connectorRequestId);
            StoredLightRequest storedServiceProviderRequest = specificSpRequestCorrelationCache.get(connectorRequestId);
            if (null == storedConnectorRequest || null == storedServiceProviderRequest) {
                LOG.error("ERROR : SAML Response InResponseTo \"" + connectorRequestId
                                  + "\" cannot be found in requestCorrelationMap");
                throw new InvalidSessionEIDASException(EidasErrors.get(EidasErrorKey.AU_REQUEST_ID.errorCode()),
                                                       EidasErrors.get(EidasErrorKey.AU_REQUEST_ID.errorMessage()));
            }

            String citizenIpAddress = storedConnectorRequest.getRemoteIpAddress();
            IAuthenticationRequest connectorAuthnRequest = storedConnectorRequest.getRequest();

            Long beforeServiceSkew =
                    connectorUtil.loadConfigServiceTimeSkewInMillis(connectorAuthnRequest.getCitizenCountryCode(), AUCONNECTORUtil.CONSUMER_SKEW_TIME.BEFORE);
            Long afterServiceSkew =
                    connectorUtil.loadConfigServiceTimeSkewInMillis(connectorAuthnRequest.getCitizenCountryCode(), AUCONNECTORUtil.CONSUMER_SKEW_TIME.AFTER);
            IAuthenticationResponse authnResponse =
                    engine.validateUnmarshalledResponse(proxyServiceSamlResponse, citizenIpAddress, beforeServiceSkew, afterServiceSkew, null);

            LOG.info(LoggingMarkerMDC.SAML_EXCHANGE, "Connector - Processing SAML Response to request with ID {}",
                    connectorRequestId);

            checkAntiReplay(authnResponse);

            checkServiceCountryToCitizenCountry(connectorAuthnRequest, authnResponse);

            if (!authnResponse.isFailure()) {
                checkResponseLoA(connectorAuthnRequest, authnResponse);
                if (isValidatePrefixCountryCodeIdentifiers()) {
                    checkIdentifierFormat(authnResponse);
                    checkIdentifierCountryCodeMatchesServiceCountryCode(connectorAuthnRequest, authnResponse);
                }
                validateAttributes(authnResponse.getAttributes());
                authnResponse = checkNameIdFormat(connectorAuthnRequest, authnResponse);
            } else {
                LOG.info("ERROR : Auth not succeed!");
                final String errorCode = EidasErrors.get(authnResponse.getStatusMessage());
                final String errorMessage = (StringUtils.isNotBlank(errorCode)) ?
                        extractErrorMessage(authnResponse.getStatusMessage(), errorCode) :
                        authnResponse.getStatusMessage();
                authnResponse = AuthenticationResponse.builder(authnResponse).statusMessage(errorMessage).build();
            }

            if (authnResponse.getAudienceRestriction() != null) {
                LOG.trace("Checking audience...");
                checkAudienceRestriction(connectorAuthnRequest.getIssuer(), authnResponse.getAudienceRestriction());
            }

            final ILightRequest serviceProviderRequest = storedServiceProviderRequest.getRequest();
            final String serviceProviderRequestSamlId = serviceProviderRequest.getId();
            final AuthenticationResponse connectorResponse = new AuthenticationResponse.Builder(authnResponse)
                    .inResponseTo(serviceProviderRequestSamlId)
                    .issuer(getConnectorMetadataUrl())
                    .build();

            return new AuthenticationExchange(storedConnectorRequest, connectorResponse);

        } catch (EIDASSAMLEngineException e) {
            LOG.info("BUSINESS EXCEPTION : SAML validation error", e.getMessage());
            LOG.debug("BUSINESS EXCEPTION : SAML validation error", e);
            EidasNodeErrorUtil.processSAMLEngineException(e, LOG, getConnectorRedirectError(e,
                                                                                            EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML));
            //normal processing of the above line will already cause the throw of the below exception
            throw new InternalErrorEIDASException(
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.errorMessage()), e);
        }
    }

    private void validateAttributes(ImmutableAttributeMap attributes) {
        for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> entry :
                attributes.getAttributeMap().entrySet()) {
            AttributeDefinition<?> definition = entry.getKey();
            ImmutableSet<? extends AttributeValue<?>> values = entry.getValue();

            for (final AttributeValue<?> attributeValue : values) {
                AttributeValidator.of(definition).validate(attributeValue);
                if (AttributeUtil.isGenderAttributeDefinition(definition)) {
                    validateGenderAgainstProtocolVersion(attributeValue);
                }
            }
        }
    }

    private void validateGenderAgainstProtocolVersion(AttributeValue attributeValue) {
        if (attributeValue instanceof GenderAttributeValue) {
            GenderProtocolVersionValidator genderProtocolVersionValidator = buildGenderProtocolVersionValidator();
            Gender gender = ((GenderAttributeValue) attributeValue).getValue();
            if (!genderProtocolVersionValidator.isValid(gender)) {
                String errorMessage = "Invalid Gender attribute for the protocol versions";
                LOG.error(errorMessage);
                throw new InvalidParameterEIDASException(EidasErrorKey.INVALID_ATTRIBUTE_VALUE.errorCode(), errorMessage);
            }
        }
    }

    private GenderProtocolVersionValidator buildGenderProtocolVersionValidator() {
        List<EidasProtocolVersion> protocolVersions = getConnectorProtocolVersions();
        return GenderProtocolVersionValidator
                .Builder()
                .protocolVersions(protocolVersions)
                .build();
    }

    private List<EidasProtocolVersion> getConnectorProtocolVersions() {
        String protocolVersionsAsString = connectorUtil.getConfigs()
                .getProperty(EIDASValues.EIDAS_PROTOCOL_VERSION.toString());
        List<EidasProtocolVersion> connectorProtocolVersions = new ArrayList<>();
        List<String> protocolVersionsValues = EidasStringUtil.getDistinctValues(protocolVersionsAsString);
        if (protocolVersionsValues != null) {
            for (String protocolVersion : protocolVersionsValues) {
                EidasProtocolVersion eidasProtocolVersion = EidasProtocolVersion.fromString(protocolVersion);
                if (eidasProtocolVersion != null) {
                    connectorProtocolVersions.add(eidasProtocolVersion);
                }
            }
        }
        return connectorProtocolVersions;
    }

	@SuppressWarnings("squid:S2583")
    private void checkIdentifierFormat(IAuthenticationResponse authnResponse) throws InternalErrorEIDASException {
        String patterEidentifier = "^[A-Z]{2}/[A-Z]{2}/.+$";
        if (authnResponse.getAttributes() != null) {
            ImmutableSet personIdentifier = authnResponse.getAttributes().getAttributeValuesByNameUri(EidasSpec.Definitions.PERSON_IDENTIFIER.getNameUri().toASCIIString());
            if (personIdentifier != null && !personIdentifier.isEmpty()) {
                if (!Pattern.matches(patterEidentifier, ((AttributeValue<String>) personIdentifier.iterator().next()).getValue())) {
                    throw new InternalErrorEIDASException(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.errorCode(), "Person Identifier has an invalid format.");
                }
            }
            ImmutableSet legalPersonIdentifier = authnResponse.getAttributes().getAttributeValuesByNameUri(EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER.getNameUri().toASCIIString());
            if (legalPersonIdentifier != null && !legalPersonIdentifier.isEmpty()) {
                if (!Pattern.matches(patterEidentifier, ((AttributeValue<String>) legalPersonIdentifier.iterator().next()).getValue())) {
                    throw new InternalErrorEIDASException(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.errorCode(), "Legal person Identifier has an invalid format.");
                }

            }
        }

    }

    private void checkIdentifierCountryCodeMatchesServiceCountryCode(IAuthenticationRequest correlationCacheAuthnRequest, IAuthenticationResponse authnResponse) throws InternalErrorEIDASException {
        final String proxyServiceCountryCode = correlationCacheAuthnRequest.getCitizenCountryCode();
        final List<AttributeDefinition<String>> identifierAttributeDefinitions = Arrays.asList(
                EidasSpec.Definitions.PERSON_IDENTIFIER,
                EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER,
                EidasSpec.Definitions.REPV_PERSON_IDENTIFIER,
                EidasSpec.Definitions.REPV_LEGAL_PERSON_IDENTIFIER
        );

        identifierAttributeDefinitions.stream()
                .<ImmutableSet<? extends AttributeValue<String>>>map(authnResponse.getAttributes()::getAttributeValues)
                .filter(attributeValue -> attributeValue != null && !attributeValue.isEmpty())
                .filter(attributeValue -> !isExpectedProxyServiceCountryCode(proxyServiceCountryCode, attributeValue))
                .forEach(attributeValue -> errorIdentifierMissingServiceCountry(attributeValue, proxyServiceCountryCode));
    }

    private boolean isExpectedProxyServiceCountryCode(String proxyServiceCountryCode,
                                                      ImmutableSet<? extends AttributeValue<String>> attributeValue) throws InternalErrorEIDASException {
        Optional<? extends AttributeValue<String>> firstAttributeValue = attributeValue.stream().findFirst();
        if (!firstAttributeValue.isPresent()) {
            throw new InternalErrorEIDASException(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.errorCode(),
                    "SAML response identifier is missing");
        }
        String prefixProxyServiceCountryCode = firstAttributeValue.get().getValue().substring(0, 2);

        return prefixProxyServiceCountryCode.equalsIgnoreCase(proxyServiceCountryCode);
    }

    private void errorIdentifierMissingServiceCountry(final ImmutableSet<? extends AttributeValue<String>> attributeValue,
                                                      String proxyServiceCountryCode) {
        LOG.error("First two characters of attribute value: " + attributeValue
                + " do not match expected Country Code " + proxyServiceCountryCode);
        throw new InternalErrorEIDASException(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.errorCode(),
                "First two characters of the SAML response identifier do not match country code of Proxy Service");
    }

    /**
     * Compares the stored SAML request id to the incoming SAML response id.
     *
     * @param auRequestID      The stored Id of the SAML request.
     * @param currentRequestId The Id of the incoming SAML response.
     */
    private void checkInResponseTo(String auRequestID, String currentRequestId) {

        if (auRequestID == null || !auRequestID.equals(currentRequestId)) {
            LOG.info(LoggingMarkerMDC.SECURITY_WARNING,
                     "ERROR : Stored request Id ({}) is not the same than response request id ({})", auRequestID,
                     currentRequestId);
            throw new InvalidSessionEIDASException(EidasErrors.get(EidasErrorKey.AU_REQUEST_ID.errorCode()),
                                                   EidasErrors.get(EidasErrorKey.AU_REQUEST_ID.errorMessage()));
        }
    }

    /**
     * Check if the citizen country code is the same than the Service country code
     * If the service is not providing the NodeCountry in its metadata, the service country code
     * present in the response was determined based on the signing certificate's subject.
     *
     * @param spAuthnRequest the initial authnRequest
     * @param authnResponse the authnResponse
     */
    private void checkServiceCountryToCitizenCountry(IAuthenticationRequest spAuthnRequest,
            IAuthenticationResponse authnResponse) {
        if (isCountryCodeFromMetadata(authnResponse.getIssuer()) || checkCitizenCertificateServiceCertificate) {
            if (!spAuthnRequest.getCitizenCountryCode().equals(authnResponse.getCountry())) {
                LOG.warn("ERROR : Signing country for Service " + authnResponse.getCountry()
                        + " is not the same than the citizen country code " + spAuthnRequest.getCitizenCountryCode());
                throw new InvalidSessionEIDASException(
                        EidasErrors.get(EidasErrorKey.INVALID_RESPONSE_COUNTRY_ISOCODE.errorCode()),
                        EidasErrors.get(EidasErrorKey.INVALID_RESPONSE_COUNTRY_ISOCODE.errorMessage()));
            }
        }
    }

    private boolean isCountryCodeFromMetadata(String issuer) {
        Optional<String> countryCode = getSamlEngine().getProtocolProcessor().getMetadataNodeCountryCode(issuer);
        return countryCode.isPresent();
    }

    /**
     * check the LoA in the response against the LoA in the connector's cache and the LoA in the service's metadata
     * Request loAs <= Response loAs <= Metadata loAs
     *
     * @param storedAuthnRequest the stored authentication request
     * @param authnResponse the authentication response
     */
    private void checkResponseLoA(IAuthenticationRequest storedAuthnRequest, IAuthenticationResponse authnResponse) throws EIDASSAMLEngineException {
        final EidasMetadataParametersI eidasMetadataParameters = getEidasMetadataParameters(storedAuthnRequest);
        // We are only storing IEidasAuthenticationRequest in StoredAuthenticationRequest cache
        if (storedAuthnRequest instanceof IEidasAuthenticationRequest) {
            final IEidasAuthenticationRequest eidasAuthnRequest = (IEidasAuthenticationRequest) storedAuthnRequest;

            final List<String> responseLoAs = Arrays.asList(authnResponse.getLevelOfAssurance());
            final List<String> requestLoAs = eidasAuthnRequest.getLevelsOfAssurance().stream().map(ILevelOfAssurance::getValue).collect(Collectors.toList());
            final List<String> publishedLoAs = eidasMetadataParameters.getAssuranceLevels();
            final EidasProtocolVersion highestProtocolVersion = MetadataUtil.getHighestEidasProtocolVersion(eidasMetadataParameters);

            if (!EidasNodeValidationUtil.isEqualOrBetterLoAs(requestLoAs, responseLoAs)) {
                LOG.info("ERROR : the level of assurance in the response " + authnResponse.getLevelOfAssurance()
                        + " does not satisfy with the requested levels");
                throw new InvalidSessionEIDASException(
                        EidasErrors.get(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE.errorCode()),
                        EidasErrors.get(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE.errorMessage()));
            }

            if (!(EidasNodeValidationUtil.hasCommonLoa(responseLoAs, publishedLoAs) ||
                    EidasProtocolVersion.PROTOCOL_VERSION_1_1.equals(highestProtocolVersion) &&
                            EidasNodeValidationUtil.isEqualOrBetterLoAs(responseLoAs, publishedLoAs))) {
                LOG.info("ERROR : the level of assurance in the response " + authnResponse.getLevelOfAssurance()
                        + " does not satisfy with the service metadata levels");
                throw new InvalidSessionEIDASException(
                        EidasErrors.get(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE_UNPUBLISHED.errorCode()),
                        EidasErrors.get(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE_UNPUBLISHED.errorMessage()));
            }
        } else throw new IllegalStateException();
    }

    private EidasMetadataParametersI getEidasMetadataParameters(ILightRequest spAuthnRequest) throws EIDASSAMLEngineException {
        final String citizenCountryCode = spAuthnRequest.getCitizenCountryCode();
        final String serviceMetadataURL = getConnectorUtil().loadConfigServiceMetadataURL(citizenCountryCode);
        validateServiceRedirectUrlValue(serviceMetadataURL);
        return getEidasMetadataParameters(serviceMetadataURL);
    }

    /**
     * Check the antireplay cache to control if the samlId has not yet been submitted
     *
     * @param authnResponse the authnResponse
     */
    private void checkAntiReplay(IAuthenticationResponse authnResponse) {
        if (!connectorUtil.checkNotPresentInCache(authnResponse.getId(), authnResponse.getCountry())) {
            LOG.info("ERROR : SAMLID " + authnResponse.getId() + "+ for response found in Antireplay cache");
            throw new SecurityEIDASException(EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SAML.errorCode()),
                                             EidasErrors.get(
                                                     EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SAML.errorMessage()));
        }
    }

    /**
     * Generates a request SAML token based on an authentication request.
     *
     * @param instance String containing the SAML configuration to load.
     * @param request An authentication request to generate the SAML token.
     * @return An authentication request with the embedded SAML token.
     * @see EidasAuthenticationRequest
     */
    private IRequestMessage generateAuthenticationRequest(@Nonnull String instance,
                                                          @Nonnull IAuthenticationRequest request,
                                                          @Nonnull String serviceCountryCode) {

        if (!(request instanceof IEidasAuthenticationRequest)) {
            // Send an error SAML message back - the use of InternalErrorEIDASException should have triggered an error page
            throw new InvalidParameterEIDASException(
                    EidasErrors.get(EidasErrorKey.MESSAGE_FORMAT_UNSUPPORTED.errorCode()),
                    EidasErrors.get(EidasErrorKey.MESSAGE_FORMAT_UNSUPPORTED.errorMessage()));
        }

        boolean modified = false;
        IEidasAuthenticationRequest eidasRequest = (IEidasAuthenticationRequest) request;
        EidasAuthenticationRequest.Builder builder = EidasAuthenticationRequest.builder(eidasRequest);

        if (!EidasSamlBinding.EMPTY.getName().equals(eidasRequest.getBinding())) {
            builder = EidasAuthenticationRequest.builder(eidasRequest);
            builder.binding(EidasSamlBinding.EMPTY.getName());
            modified = true;
        }

        // If there is no SP Country, Then we get it from SAML's Certificate
        if (StringUtils.isBlank(request.getServiceProviderCountryCode())) {
            builder.serviceProviderCountryCode(request.getOriginCountryCode());
            modified = true;
        }

        if (shouldModifyEidasAuthenticationRequestIssuer(request)) {
            builder.originalIssuer(request.getIssuer());
            builder.issuer(getConnectorMetadataUrl());
            modified = true;
        }

        try {
            ProtocolEngineI engine = getSamlEngine(instance);

            LOG.info(LoggingMarkerMDC.SAML_EXCHANGE, "Connector - Processing SAML Request with ID {}", request.getId());

            String serviceMetadataURL = getConnectorUtil().loadConfigServiceMetadataURL(serviceCountryCode);
            if (StringUtils.isEmpty(serviceMetadataURL)) {
                String message = "The service metadata URL for \"" + serviceCountryCode + "\" is not configured";
                LOG.error(message);
                throw new InternalErrorEIDASException(
                        EidasErrors.get(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorCode()),
                        EidasErrors.get(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorMessage()), message);
            }

            if (modified) {
                request = builder.build();
            }

            IRequestMessage tempAuthData = engine.generateRequestMessage(request, serviceMetadataURL);

            final String flowId = getFlowIdCache().get(request.getId());
            if (StringUtils.isNotEmpty(flowId))
                getFlowIdCache().put(tempAuthData.getRequest().getId(), flowId);

            return tempAuthData;

        } catch (EIDASSAMLEngineException e) {
            LOG.info(instance + " : Error generating SAML Token", e.getMessage());
            LOG.debug(instance + " : Error generating SAML Token", e);
            EidasNodeErrorUtil.processSAMLEngineException(e, LOG, getConnectorRedirectError(e,
                                                                                            EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SAML));
            throw new InternalErrorEIDASException(
                    EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_ERROR_CREATE_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_ERROR_CREATE_SAML.errorMessage()), e);
        }
    }

    /**
     * Compares the issuer to the audience restriction.
     *
     * @param issuer The stored SAML request issuer.
     * @param audience The SAML response audience.
     */
    private void checkAudienceRestriction(String issuer, String audience) {

        if (issuer == null || !issuer.equals(audience)) {
            LOG.info("ERROR : Audience is null or not valid: audienceRestriction=\"" + audience + "\" vs issuer=\""
                             + issuer + "\"");
            throw new InvalidSessionEIDASException(EidasErrors.get(EidasErrorKey.AUDIENCE_RESTRICTION.errorCode()),
                                                   EidasErrors.get(EidasErrorKey.AUDIENCE_RESTRICTION.errorMessage()));
        }
    }

    private boolean shouldModifyEidasAuthenticationRequestIssuer(@Nonnull IAuthenticationRequest request) {
        String connectorMetadataUrl = getConnectorMetadataUrl();
        return connectorMetadataUrl != null
                && !connectorMetadataUrl.isEmpty()
                && PropertiesUtil.isConnectorMetadataEnabled()
                && !connectorMetadataUrl.equals(request.getIssuer());
    }

    @Override
    public boolean checkMandatoryAttributes(@Nonnull ImmutableAttributeMap attributes) {
        ProtocolEngineI engine = getSamlEngine(samlServiceInstance);
        return engine.getProtocolProcessor().checkMandatoryAttributes(attributes);
    }

    @Override
    public boolean checkRepresentativeAttributes(@Nonnull ImmutableAttributeMap attributes) {
        ProtocolEngineI engine = getSamlEngine(samlServiceInstance);
        return engine.getProtocolProcessor().checkRepresentativeAttributes(attributes);
    }

    public EidasMetadataParametersI getEidasMetadataParameters(String serviceMetadataURL)
            throws EIDASSAMLEngineException {
        EidasMetadataParametersI eidasMetadataParameters = null;
        try {
            eidasMetadataParameters = metadataFetcher.getEidasMetadata(serviceMetadataURL,
                    (MetadataSignerI) getSamlEngine(samlServiceInstance).getSigner(),
                    (MetadataClockI) getSamlEngine(samlServiceInstance).getClock());
        } catch (EIDASMetadataException e) {
            throw new EIDASSAMLEngineException(e);
        }
        return eidasMetadataParameters;
    }

    /**
     * Method to check if the NameID format in the eIDAS response matches the NameID format from the eIDAS request
     *
     * @param iAuthenticationRequest the initial authnRequest
     * @param iAuthenticationResponse         the authnResponse
     */
    private IAuthenticationResponse checkNameIdFormat(IAuthenticationRequest iAuthenticationRequest, IAuthenticationResponse iAuthenticationResponse) {
        final String requestNameIdFormat = ifNullReplace(iAuthenticationRequest.getNameIdFormat(), NameIDType.UNSPECIFIED);
        final String responseSubjectNameIdFormat = ifNullReplace(iAuthenticationResponse.getSubjectNameIdFormat(), NameIDType.UNSPECIFIED);
        final String responseSubject = iAuthenticationResponse.getSubject();

        validateSamlCoreNameIdFormatIdentifier(requestNameIdFormat);
        validateSamlCoreNameIdFormatIdentifier(responseSubjectNameIdFormat);

        if (NameIDType.PERSISTENT.equals(responseSubjectNameIdFormat) || NameIDType.TRANSIENT.equals(responseSubjectNameIdFormat)) {
            validateSubjectNameIdFormatLengthCharacters(responseSubject, MAX_LENGTH_FOR_NAME_ID_FORMAT_PERSISTENT_TRANSIENT);
        }

        if (NameIDType.ENTITY.equals(responseSubjectNameIdFormat)) {
            validateURI(responseSubject);
            validateSubjectNameIdFormatLengthCharacters(responseSubject, MAX_LENGTH_FOR_NAME_ID_FORMAT_ENTITY);
        }

        if (!NameIDType.UNSPECIFIED.equals(requestNameIdFormat) && !requestNameIdFormat.equals(responseSubjectNameIdFormat)) {
            iAuthenticationResponse = buildInvalidNameIdPolicyIAuthenticationResponse(iAuthenticationResponse);
        }

        return iAuthenticationResponse;
    }

    private <T> T ifNullReplace(@Nullable T primaryValue, @Nonnull T backupValue) {
        return (primaryValue != null) ? primaryValue : backupValue;
    }

    private void validateSamlCoreNameIdFormatIdentifier(String nameIdFormat) {
        if (!getNameIdFormatsSet().contains(nameIdFormat)) {
            LOG.error("Unsupported NameIdFormat: " + nameIdFormat);
            throw new InternalErrorEIDASException(EidasErrors.get(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.errorMessage()));
        }
    }

    private Set<String> getNameIdFormatsSet() {
        Set<String> nameIdFormatSet = new HashSet<>();
        nameIdFormatSet.add(NameIDType.PERSISTENT);
        nameIdFormatSet.add(NameIDType.TRANSIENT);
        nameIdFormatSet.add(NameIDType.UNSPECIFIED);

        String optionalNameIdFormatsStringList = connectorUtil.getConfigs()
                .getProperty(EidasParameterKeys.EIDAS_CONNECTOR_NAMEID_FORMATS.getValue());
        List<String> optionalNameIdFormats = EidasStringUtil.getDistinctValues(optionalNameIdFormatsStringList);
        for (String nameIDFormat: optionalNameIdFormats) {
            nameIdFormatSet.add(nameIDFormat);
        }

        return nameIdFormatSet;
    }

    private void validateURI(String responseNameId) {
        try {
            new URI(responseNameId);
        } catch (URISyntaxException e) {
            LOG.error("Invalid URI syntax for NameID: " + responseNameId);
            throw new InternalErrorEIDASException(EidasErrors.get(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.errorMessage()));
        }
    }

    private IAuthenticationResponse buildInvalidNameIdPolicyIAuthenticationResponse(IAuthenticationResponse authnResponse) {
        authnResponse = AuthenticationResponse.builder(authnResponse)
                .statusCode(EIDASStatusCode.RESPONDER_URI.toString())
                .failure(true)
                //not necessarily need for a certain message, since subStatusCode is descriptive enough
                .statusMessage(null)
                .subStatusCode(EIDASSubStatusCode.INVALID_NAMEID_POLICY_URI.toString())
                .build();
        return authnResponse;
    }

    private void validateSubjectNameIdFormatLengthCharacters(String responseSubject, int maxLength) {
        if (responseSubject != null && responseSubject.length() > maxLength) {
            LOG.error("ERROR : Invalid length for Subject");
            LOG.error("The length of Subject exceeded the allowed value of " + maxLength + " characters");
            throw new InternalErrorEIDASException(EidasErrors.get(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.errorMessage()));
        }
    }

    public ProtocolEngineI getSamlEngine(@Nonnull String instanceName) {
        return nodeProtocolEngineFactory.getProtocolEngine(instanceName);
    }

    /**
     * Setter for samlServiceInstance.
     *
     * @param samlServiceInstance The new samlServiceInstance value.
     */
    public void setSamlServiceInstance(String samlServiceInstance) {
        this.samlServiceInstance = samlServiceInstance;
    }

    /**
     * Setter for connectorUtil.
     *
     * @param connectorUtil The new connectorUtil value.
     * @see AUCONNECTORUtil
     */
    public void setConnectorUtil(AUCONNECTORUtil connectorUtil) {
        this.connectorUtil = connectorUtil;
    }

    /**
     * Getter for connectorUtil.
     *
     * @return The connectorUtil value.
     * @see AUCONNECTORUtil
     */
    public AUCONNECTORUtil getConnectorUtil() {
        return connectorUtil;
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

    private String getConnectorMetadataUrl() {
        return metadataUrl;
    }

    public void setConnectorMetadataUrl(String metadataUrl) {
        this.metadataUrl = metadataUrl;
    }

    public MetadataFetcherI getMetadataFetcher() {
        return metadataFetcher;
    }

    public void setMetadataFetcher(MetadataFetcherI metadataFetcher) {
        this.metadataFetcher = metadataFetcher;
    }

    public void setNodeProtocolEngineFactory(ProtocolEngineFactory nodeProtocolEngineFactory) {
        this.nodeProtocolEngineFactory = nodeProtocolEngineFactory;
    }

    public ProtocolEngineI getSamlEngine() {
        return getSamlEngine(samlServiceInstance);
    }

    /**
     * Getter for validatePrefixCountryCodeIdentifiers
     *
     * @return The validatePrefixCountryCodeIdentifiers value
     */
    public boolean isValidatePrefixCountryCodeIdentifiers() {
        return validatePrefixCountryCodeIdentifiers;
    }

    /**
     * Setter for validatePrefixCountryCodeIdentifiers.
     *
     * @param validatePrefixCountryCodeIdentifiers The new validatePrefixCountryCodeIdentifiers value.
     */
    public void setValidatePrefixCountryCodeIdentifiers(boolean validatePrefixCountryCodeIdentifiers) {
        this.validatePrefixCountryCodeIdentifiers = validatePrefixCountryCodeIdentifiers;
    }

}
