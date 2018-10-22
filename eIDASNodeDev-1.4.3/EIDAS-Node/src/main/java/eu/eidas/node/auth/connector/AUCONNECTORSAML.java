/*
 * Copyright (c) 2015 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
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

package eu.eidas.node.auth.connector;

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.DateUtil;
import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasDigestUtil;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IEIDASLogger;
import eu.eidas.auth.commons.RequestState;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.exceptions.InvalidSessionEIDASException;
import eu.eidas.auth.commons.exceptions.SecurityEIDASException;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.protocol.impl.EidasSamlBinding;
import eu.eidas.auth.commons.protocol.stork.IStorkAuthenticationRequest;
import eu.eidas.auth.commons.tx.AuthenticationExchange;
import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredLightRequest;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.auth.engine.Correlated;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.eidas.spec.EidasSpec;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.utils.EidasNodeErrorUtil;
import eu.eidas.node.utils.EidasNodeValidationUtil;
import eu.eidas.node.utils.PropertiesUtil;
import eu.eidas.node.utils.SessionHolder;
import org.apache.commons.lang.StringUtils;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * This class is used by {@link AUCONNECTOR} to get, process and generate SAML Tokens.
 *
 * @see ICONNECTORSAMLService
 */
public final class AUCONNECTORSAML implements ICONNECTORSAMLService {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AUCONNECTORSAML.class);

    /**
     * Request logging.
     */
    private static final Logger LOGGER_COM_REQ = LoggerFactory.getLogger(
            EIDASValues.EIDAS_PACKAGE_REQUEST_LOGGER_VALUE.toString() + "." + AUCONNECTOR.class.getSimpleName());

    /**
     * Response logging.
     */
    private static final Logger LOGGER_COM_RESP = LoggerFactory.getLogger(
            EIDASValues.EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE.toString() + "." + AUCONNECTOR.class.getSimpleName());

    /**
     * Logger bean.
     */
    private IEIDASLogger loggerBean;

    /**
     * SAML instance to communicate with ServiceProxy.
     */
    private String samlServiceInstance;

    /**
     * Connector's processAuthenticationResponse class.
     */
    private AUCONNECTORUtil connectorUtil;

    /**
     * metadata url to be put in requests generated by the Connector module.
     */
    private String metadataUrl;

    /**
     * metadata url to be put in responses generated by the Connector module.
     */
    private String metadataResponderUrl;

    /**
     * Resource bundle to translate messages from ServiceProxy/VIdP.
     */
    private MessageSource messageSource;

    private boolean checkCitizenCertificateServiceCertificate;

    private MetadataFetcherI metadataFetcher;

    private ProtocolEngineFactory nodeProtocolEngineFactory;

	private static final Pattern SERVICE_METADATA_WHITELIST_PATTERN = Pattern.compile("service.+\\.metadata\\.url");
	private Collection<String> serviceMetadataWhitelist;

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
        samlResponseFail.issuer(getConnectorResponderMetadataUrl());
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

        String strSamlToken;
        strSamlToken = webRequest.getEncodedLastParameterValue(EidasParameterKeys.SAML_RESPONSE);

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
            String serviceCode = getCountryCode(lightRequest, webRequest);

            LOG.debug("Requested country: " + serviceCode);

            String serviceMetadataURL = getConnectorUtil().loadConfigServiceMetadataURL(serviceCode);

            String serviceUrl = connectorUtil.loadConfigServiceURL(serviceCode);

            LOG.debug("Citizen Country URL " + serviceCode + " URL " + serviceUrl);
            NormalParameterValidator.paramName(EidasErrorKey.SERVICE_REDIRECT_URL.toString())
                    .paramValue(serviceUrl)
                    .eidasError(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_COUNTRY)
                    .validate();

            LOG.info(LoggingMarkerMDC.SAML_EXCHANGE, "Connector - Processing LightRequest with ID {}",
                     lightRequest.getId());

            // Get Personal Attribute List and validate
            ImmutableAttributeMap requestedAttributes = lightRequest.getRequestedAttributes();

            NormalParameterValidator.paramName(EidasParameterKeys.ATTRIBUTE_LIST)
                    .paramValue(requestedAttributes.isEmpty() ? null : "dummy")
                    .eidasError(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_ATTR)
                    .validate();

            String levelOfAssurance = lightRequest.getLevelOfAssurance();
            RequestState requestState = webRequest.getRequestState();
            if (null != levelOfAssurance) {
                requestState.setLevelOfAssurance(levelOfAssurance);
            }

            // Get ProviderName and validate
            String providerName = lightRequest.getProviderName();
            NormalParameterValidator.paramName(EidasParameterKeys.PROVIDER_NAME_VALUE)
                    .paramValue(providerName)
                    .eidasError(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SP_PROVIDERNAME)
                    .validate();

            requestState.setProviderName(providerName);

            IAuthenticationRequest authnRequest = EidasAuthenticationRequest.builder()
                    .lightRequest(lightRequest)
                    .destination(serviceUrl)
                    .citizenCountryCode(serviceCode)
                    .build();

            validateRequestLoA(authnRequest, connectorUtil.loadConfigServiceMetadataURL(serviceCode));

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
            String metaDataUrl = webRequest.getEncodedLastParameterValue(EidasParameterKeys.SP_METADATA_URL);
            if (null != metaDataUrl && isIssuedBySelf(authnRequest)) {
                EidasAuthenticationRequest.Builder eIDASAuthnRequestBuilder =
                        EidasAuthenticationRequest.builder((IEidasAuthenticationRequest) authnRequest);
                eIDASAuthnRequestBuilder.issuer(metaDataUrl);
                authnRequest = eIDASAuthnRequestBuilder.build();
            }
            // Checking for antiReplay
            if (!connectorUtil.checkNotPresentInCache(authnRequest.getId(), authnRequest.getCitizenCountryCode())
                    .booleanValue()) {
                throw new SecurityEIDASException(
                        EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SAML.errorCode()),
                        EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SAML.errorMessage()));
            }

            return authnRequest;
        } catch (EIDASSAMLEngineException e) {
            // Special case for propagating the error in case of xxe
            EidasNodeErrorUtil.processSAMLEngineException(e, LOG, getConnectorRedirectError(e,
                                                                                            EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SAML));
            throw new InternalErrorEIDASException(
                    EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SAML.errorMessage()), e);
        }
    }

    private void validateRequestLoA(IAuthenticationRequest authRequest, String idpUrl) throws EIDASSAMLEngineException {
        if (null == metadataFetcher) {
            return;
        }
        String colleagueLoA = MetadataUtil.getServiceLevelOfAssurance(metadataFetcher.getEntityDescriptor(idpUrl,
                                                                                                          (MetadataSignerI) getSamlEngine(samlServiceInstance).getSigner()));
        if (!StringUtils.isEmpty(colleagueLoA) && !EidasNodeValidationUtil.isRequestLoAValid(authRequest,
                                                                                             colleagueLoA)) {
            throw new InternalErrorEIDASException(
                    EidasErrors.get(EidasErrorKey.SERVICE_PROVIDER_INVALID_LOA.errorCode()),
                    EidasErrors.get(EidasErrorKey.SERVICE_PROVIDER_INVALID_LOA.errorMessage()));
        }
    }

    private boolean isIssuedBySelf(IAuthenticationRequest authnRequest) {
        String connectorMetadataUrl = getConnectorMetadataUrl();
        return connectorMetadataUrl != null && connectorMetadataUrl.equalsIgnoreCase(authnRequest.getIssuer());
    }

    /**
     * Gets the Country Code.
     *
     * @param lightRequest The light authentication Request object.
     * @param webRequest the webRequest.
     * @return the country code value.
     */
    private static String getCountryCode(ILightRequest lightRequest, WebRequest webRequest) {
        // Country: Mandatory if the destination is a ProxyService.
        String serviceCode;
        if (lightRequest.getCitizenCountryCode() == null) {
            serviceCode = webRequest.getEncodedLastParameterValue(EidasParameterKeys.COUNTRY);

        } else {
            serviceCode = lightRequest.getCitizenCountryCode();
        }

        // Compatibility
        if (null != serviceCode && serviceCode.endsWith(EIDASValues.EIDAS_SERVICE_SUFFIX.toString())) {
            serviceCode = serviceCode.replace(EIDASValues.EIDAS_SERVICE_SUFFIX.toString(), StringUtils.EMPTY);
        }

        return serviceCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRequestMessage generateServiceAuthnRequest(@Nonnull WebRequest webRequest, @Nonnull IAuthenticationRequest request) {

        String serviceCountryCode = getCountryCode(request, webRequest);

        //TODO check if tempAuthData creation could be avoided
        IRequestMessage tempAuthData = generateAuthenticationRequest(samlServiceInstance, request, serviceCountryCode);

        prepareReqLoggerBean(EIDASValues.EIDAS_CONNECTOR_REQUEST.toString(), tempAuthData.getMessageBytes(),
                             tempAuthData.getRequest(), tempAuthData.getRequest().getId());

        saveLog(AUCONNECTORSAML.LOGGER_COM_REQ);
        LOG.trace("Logging communication");

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
                                                              @Nonnull CorrelationMap<StoredAuthenticationRequest> connectorRequestCorrelationMap,
                                                              @Nonnull CorrelationMap<StoredLightRequest> specificSpRequestCorrelationMap)
            throws InternalErrorEIDASException {
        try {

            LOG.trace("Getting SAML Token");
            byte[] responseFromProxyService = this.extractResponseSAMLToken(webRequest);

            // validates SAML Token
            ProtocolEngineI engine = getSamlEngine(samlServiceInstance);

            Correlated proxyServiceSamlResponse = engine.unmarshallResponse(responseFromProxyService, 
            		serviceMetadataWhitelist, true);

            String connectorRequestId = proxyServiceSamlResponse.getInResponseToId();

            if (StringUtils.isBlank(connectorRequestId)) {
                LOG.error("ERROR : SAML Response \"" + proxyServiceSamlResponse.getId() + "\" has no InResponseTo");
                throw new InvalidSessionEIDASException(EidasErrors.get(EidasErrorKey.AU_REQUEST_ID.errorCode()),
                                                       EidasErrors.get(EidasErrorKey.AU_REQUEST_ID.errorMessage()));
            }

            StoredAuthenticationRequest storedConnectorRequest = connectorRequestCorrelationMap.get(connectorRequestId);
            StoredLightRequest storedServiceProviderRequest = specificSpRequestCorrelationMap.get(connectorRequestId);
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

            prepareRespLoggerBean(EIDASValues.EIDAS_CONNECTOR_RESPONSE.toString(), authnResponse, connectorRequestId);
            saveLog(AUCONNECTORSAML.LOGGER_COM_RESP);

            checkAntiReplay(responseFromProxyService, connectorAuthnRequest, authnResponse);

            checkServiceCountryToCitizenCountry(responseFromProxyService, connectorAuthnRequest, authnResponse);

            if (!authnResponse.isFailure()) {
                checkResponseLoA(responseFromProxyService, connectorAuthnRequest, authnResponse);
                checkIdentifierFormat(authnResponse);
            }

            ILightRequest serviceProviderRequest = storedServiceProviderRequest.getRequest();
            String serviceProviderRequestSamlId = serviceProviderRequest.getId();

            LOG.trace("Checking status code");
            if (!EIDASStatusCode.SUCCESS_URI.toString().equals(authnResponse.getStatusCode())) {
                LOG.info("ERROR : Auth not succeed!");

                String errorCode =  EidasErrors.get(authnResponse.getStatusMessage());
                // We only change the error message if we get any error code on the Message!
                // Backwards compatibility
                String errorMessage = authnResponse.getStatusMessage();
                if (StringUtils.isNotBlank(errorCode)) {
                    errorMessage = extractErrorMessage(errorMessage, errorCode);
                }
                authnResponse = AuthenticationResponse.builder(authnResponse).statusMessage(errorMessage).build();
            }

            if (authnResponse.getAudienceRestriction() != null) {
                LOG.trace("Checking audience...");
                checkAudienceRestriction(connectorAuthnRequest.getIssuer(), authnResponse.getAudienceRestriction());
            }

            AuthenticationResponse connectorResponse =
                    new AuthenticationResponse.Builder(authnResponse).inResponseTo(serviceProviderRequestSamlId)
                            .issuer(getConnectorResponderMetadataUrl())
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

    private void validateAttributeValueFormat(String value,
                                              String currentAttrName,
                                              String attrNameToTest,
                                              String pattern) throws ValidationException {
        if (currentAttrName.equals(attrNameToTest) && !Pattern.matches(pattern, value)) {
            throw new ValidationException(attrNameToTest + " has incorrect format.");
        }

    }

	@Autowired
	private void setup(PropertiesFactoryBean propertiesFactoryBean)throws EIDASSAMLEngineException{
        try {
        	serviceMetadataWhitelist=new ArrayList<>();
        	Properties properties = propertiesFactoryBean.getObject();
        	for (String propName : properties.stringPropertyNames()){
        		if (SERVICE_METADATA_WHITELIST_PATTERN.matcher(propName).find()){
        			serviceMetadataWhitelist.add(properties.getProperty(propName));
        		}
        	}
        }catch(IOException e){
			throw new EIDASSAMLEngineException(e);
        }
    }

	@SuppressWarnings("squid:S2583")
    private void checkIdentifierFormat(IAuthenticationResponse authnResponse) throws InternalErrorEIDASException {
        String patterEidentifier = "^[A-Z]{2}/[A-Z]{2}/.+$";
        if (authnResponse.getAttributes() != null){
            ImmutableSet personIdentifier = authnResponse.getAttributes().getAttributeValuesByNameUri(EidasSpec.Definitions.PERSON_IDENTIFIER.getNameUri().toASCIIString());
            if (personIdentifier != null && !personIdentifier.isEmpty()){
                if (!Pattern.matches(patterEidentifier, ((AttributeValue<String>)personIdentifier.iterator().next()).getValue())) {
                    throw new InternalErrorEIDASException(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.errorCode(), "Person Identifier has an invalid format.");
                }
            }
            ImmutableSet legalPersonIdentifier = authnResponse.getAttributes().getAttributeValuesByNameUri(EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER.getNameUri().toASCIIString());
            if (legalPersonIdentifier != null  && !legalPersonIdentifier.isEmpty()){
                if (!Pattern.matches(patterEidentifier, ((AttributeValue<String>)legalPersonIdentifier.iterator().next()).getValue())) {
                    throw new InternalErrorEIDASException(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.errorCode(), "Legal person Identifier has an invalid format.");
                }

            }
        }

    }

    /**
     * Compares the stored SAML request id to the incoming SAML response id.
     *
     * @param auRequestID The stored Id of the SAML request.
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
     * Check if the citizen country code is the same than the Service signing certificate
     *
     * @param samlToken the samlToken received
     * @param spAuthnRequest the initial authnRequest
     * @param authnResponse the authnResponse
     */
    private void checkServiceCountryToCitizenCountry(byte[] samlToken,
                                                     IAuthenticationRequest spAuthnRequest,
                                                     IAuthenticationResponse authnResponse) {
        if (checkCitizenCertificateServiceCertificate && !spAuthnRequest.getCitizenCountryCode()
                .equals(authnResponse.getCountry())) {
            LOG.warn("ERROR : Signing country for Service " + authnResponse.getCountry()
                             + " is not the same than the citizen country code "
                             + spAuthnRequest.getCitizenCountryCode());
            prepareReqLoggerBean(EIDASValues.SP_REQUEST.toString(), samlToken, spAuthnRequest, spAuthnRequest.getId());
            saveLog(AUCONNECTORSAML.LOGGER_COM_REQ);
            throw new InvalidSessionEIDASException(
                    EidasErrors.get(EidasErrorKey.INVALID_RESPONSE_COUNTRY_ISOCODE.errorCode()),
                    EidasErrors.get(EidasErrorKey.INVALID_RESPONSE_COUNTRY_ISOCODE.errorMessage()));
        }
    }

    /**
     * check the LoA in the response against connector's own LoA
     *
     * @param samlToken
     * @param spAuthnRequest
     * @param authnResponse
     */
    private void checkResponseLoA(byte[] samlToken,
                                  IAuthenticationRequest spAuthnRequest,
                                  IAuthenticationResponse authnResponse) {
        LevelOfAssurance requestedLevel = LevelOfAssurance.getLevel(spAuthnRequest.getLevelOfAssurance());
        LevelOfAssurance responseLevel = LevelOfAssurance.getLevel(authnResponse.getLevelOfAssurance());
        if (requestedLevel != null && (responseLevel == null || !EidasNodeValidationUtil.isRequestLoAValid(
                spAuthnRequest, responseLevel.stringValue()))) {
            LOG.info("ERROR : the level of assurance in the response " + authnResponse.getLevelOfAssurance()
                             + " does not satisfy the requested level " + requestedLevel);
            prepareReqLoggerBean(EIDASValues.SP_REQUEST.toString(), samlToken, spAuthnRequest, spAuthnRequest.getId());
            saveLog(AUCONNECTORSAML.LOGGER_COM_REQ);
            throw new InvalidSessionEIDASException(EidasErrors.get(EidasErrorKey.INTERNAL_ERROR.errorCode()),
                                                   EidasErrors.get(EidasErrorKey.INTERNAL_ERROR.errorMessage()));
        }
    }

    /**
     * Check the antireplay cache to control if the samlId has not yet been submitted
     *
     * @param samlToken the samlToken received
     * @param spAuthnRequest the initial authnRequest
     * @param authnResponse the authnResponse
     */
    private void checkAntiReplay(byte[] samlToken,
                                 IAuthenticationRequest spAuthnRequest,
                                 IAuthenticationResponse authnResponse) {
        if (!connectorUtil.checkNotPresentInCache(authnResponse.getId(), authnResponse.getCountry())) {
            LOG.info("ERROR : SAMLID " + authnResponse.getId() + "+ for response found in Antireplay cache");
            prepareReqLoggerBean(EIDASValues.SP_REQUEST.toString(), samlToken, spAuthnRequest, spAuthnRequest.getId());
            saveLog(AUCONNECTORSAML.LOGGER_COM_REQ);
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
        EidasAuthenticationRequest.Builder builder = null;
        IEidasAuthenticationRequest eidasRequest = (IEidasAuthenticationRequest) request;

        if (!EidasSamlBinding.EMPTY.getName().equals(eidasRequest.getBinding())) {
            builder = EidasAuthenticationRequest.builder(eidasRequest);
            builder.binding(EidasSamlBinding.EMPTY.getName());
            modified = true;
        }

        // If there is no SP Country, Then we get it from SAML's Certificate
        if (StringUtils.isBlank(request.getServiceProviderCountryCode())) {
            if (null == builder) {
                builder = EidasAuthenticationRequest.builder(eidasRequest);
            }
            builder.serviceProviderCountryCode(request.getOriginCountryCode());
            modified = true;
        }

        String connectorMetadataUrl = getConnectorMetadataUrl();
        if (connectorMetadataUrl != null && !connectorMetadataUrl.isEmpty() && PropertiesUtil.isMetadataEnabled()
                && !request.getIssuer().equals(connectorMetadataUrl)) {
            if (null == builder) {
                builder = EidasAuthenticationRequest.builder(eidasRequest);
            }
            builder.originalIssuer(request.getIssuer());
            builder.issuer(connectorMetadataUrl);
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

            return engine.generateRequestMessage(request, serviceMetadataURL);

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
     * Sets all the fields to audit the request.
     *
     * @param opType The operation type.
     * @param samlObj The SAML token byte[].
     * @param authnRequest The Authentication Request object.
     * @param spSamlId The SP's SAML ID.
     * @see EidasAuthenticationRequest
     */
    private void prepareReqLoggerBean(String opType,
                                      byte[] samlObj,
                                      IAuthenticationRequest authnRequest,
                                      String spSamlId) {
        String hashClassName =
                getConnectorUtil() != null && getConnectorUtil().getConfigs() != null ? getConnectorUtil().getConfigs()
                        .getProperty(EidasParameterKeys.HASH_DIGEST_CLASS.toString()) : null;
        byte[] tokenHash = EidasDigestUtil.hashPersonalToken(samlObj, hashClassName);
        loggerBean.setTimestamp(DateUtil.currentTimeStamp().toString());
        loggerBean.setOpType(opType);
        loggerBean.setOrigin(authnRequest.getAssertionConsumerServiceURL());
        loggerBean.setDestination(authnRequest.getDestination());
        loggerBean.setProviderName(authnRequest.getProviderName());
        loggerBean.setCountry(authnRequest.getCitizenCountryCode());
        if (authnRequest instanceof IStorkAuthenticationRequest) {
            IStorkAuthenticationRequest storkAuthenticationRequest = (IStorkAuthenticationRequest) authnRequest;
            loggerBean.setSpApplication(storkAuthenticationRequest.getSpApplication());
            loggerBean.setQaaLevel(storkAuthenticationRequest.getQaa());
        }
        loggerBean.setSamlHash(tokenHash);
        loggerBean.setSPMsgId(spSamlId);
        loggerBean.setMsgId(authnRequest.getId());
    }

    /**
     * Sets all the fields to the audit the response.
     *
     * @param opType The Operation Type.
     * @param authnResponse The Authentication Response object.
     * @param inResponseToSPReq The SP's SAML Id.
     * @see EidasAuthenticationRequest
     */
    private void prepareRespLoggerBean(String opType, IAuthenticationResponse authnResponse, String inResponseToSPReq) {
        String message = EIDASValues.SUCCESS.toString() + EIDASValues.EID_SEPARATOR.toString()
                + EIDASValues.CITIZEN_CONSENT_LOG.toString();
        loggerBean.setTimestamp(DateUtil.currentTimeStamp().toString());
        loggerBean.setOpType(opType);
        loggerBean.setInResponseTo(authnResponse.getInResponseToId());
        loggerBean.setInResponseToSPReq(inResponseToSPReq);
        loggerBean.setMessage(message);
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
     * Setters and getters
     */

    /**
     * Setter for loggerBean.
     *
     * @param nLoggerBean The loggerBean to set.
     * @see IEIDASLogger
     */
    public void setLoggerBean(IEIDASLogger nLoggerBean) {
        this.loggerBean = nLoggerBean;
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

    public String getConnectorResponderMetadataUrl() {
        return metadataResponderUrl;
    }

    public void setConnectorResponderMetadataUrl(String metadataResponderUrl) {
        this.metadataResponderUrl = metadataResponderUrl;
    }

    public void setNodeProtocolEngineFactory(ProtocolEngineFactory nodeProtocolEngineFactory) {
        this.nodeProtocolEngineFactory = nodeProtocolEngineFactory;
    }
}
