/*
 * Copyright (c) 2022 by European Commission
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
package eu.eidas.node.service.utils;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
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
import eu.eidas.logging.LoggingMarkerMDC;
import eu.eidas.node.service.exceptions.ProxyServiceError;
import eu.eidas.node.utils.EidasNodeValidationUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class ProxyServiceSamlFailureUtil {

    private static String countryCode;
    private static MetadataFetcherI metadataFetcher;
    private static MessageSource messageSource;
    private static String samlInstance;
    private static ProtocolEngineFactory nodeProtocolEngineFactory;
    private static String serviceMetadataUrl;

    private static final Logger LOG = LoggerFactory.getLogger(ProxyServiceSamlFailureUtil.class.getName());

    public static String generateSamlFailure(HttpServletRequest request, IAuthenticationRequest storedAuthenticationRequest,
                                                       SamlFailureResponseException exception) {
        final String statusCode = exception.getStatusCode();

        return generateErrorAuthenticationResponse(storedAuthenticationRequest, statusCode, exception.getErrorCode(),
                null, exception.getErrorMessage(), request.getRemoteUser(), true);
    }

    public static String generateErrorAuthenticationResponse(IAuthenticationRequest authData,
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
                eidasAuthnResponseError.inResponseTo(authData.getId());
            }

            eidasAuthnResponseError.id(SAMLEngineUtils.generateNCName());
            eidasAuthnResponseError.inResponseTo(authData.getId());

            eidasAuthnResponseError.failure(true);

            IResponseMessage responseMessage = generateResponseErrorMessage(authData, engine, eidasAuthnResponseError, ipUserAddress);

            return EidasStringUtil.encodeToBase64(responseMessage.getMessageBytes());
        } catch (EIDASSAMLEngineException e) {
            LOG.info("BUSINESS EXCEPTION : Error generating SAMLToken", e.getMessage());
            LOG.debug("BUSINESS EXCEPTION : Error generating SAMLToken", e);
            throw new ProxyServiceError(
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_ERROR_CREATE_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_ERROR_CREATE_SAML.errorMessage()), e);
        }
    }

    public static IAuthenticationRequest getAuthenticationRequestFromHttpServletRequest(HttpServletRequest request) throws EIDASSAMLEngineException {
        WebRequest webRequest = new IncomingRequest(request);
        ProtocolEngineI protocolEngine = nodeProtocolEngineFactory.getProtocolEngine(getSamlEngineInstanceName());
        IAuthenticationRequest authenticationRequest = getAuthenticationRequest(webRequest, protocolEngine);
        final String metadataAssertionConsumerUrl = getMetadataAssertionConsumerUrl(protocolEngine, authenticationRequest);
        return ifNullReplaceAssertionConsumerServiceURL(authenticationRequest, metadataAssertionConsumerUrl);
    }


    private static IAuthenticationRequest getAuthenticationRequest(WebRequest webRequest, ProtocolEngineI protocolEngine) throws EIDASSAMLEngineException {
        byte[] requestBytes = EidasStringUtil.decodeBytesFromBase64(webRequest.getEncodedLastParameterValue(EidasParameterKeys.SAML_REQUEST));
        IAuthenticationRequest authenticationRequest = protocolEngine.unmarshallRequestAndValidate(requestBytes, countryCode);
        return authenticationRequest;
    }


    private static String getMetadataAssertionConsumerUrl(ProtocolEngineI protocolEngine, IAuthenticationRequest authenticationRequest) throws EIDASSAMLEngineException {
        final EidasMetadataParametersI issuerMetadataParameters = getIssuerMetadataParameters(protocolEngine, authenticationRequest.getIssuer());
        final EidasMetadataRoleParametersI connectorRoleMetadata = MetadataUtil.getSPRoleDescriptor(issuerMetadataParameters);
        final String metadataAssertionConsumerUrl = connectorRoleMetadata.getDefaultAssertionConsumerUrl();
        return metadataAssertionConsumerUrl;
    }

    private static String resolveErrorMessage(String errorCode, String statusCode, String errorId) {
        String errorMsg;
        try {
            if (StringUtils.isNumeric(errorCode)) {
                errorMsg = messageSource.getMessage(errorId, new Object[]{errorCode}, Locale.getDefault());
            } else {
                errorMsg = messageSource.getMessage(errorId, new Object[]{statusCode}, Locale.getDefault());
            }
        } catch (NoSuchMessageException nme) {
            if (errorCode == null)
                errorMsg = errorId;
            else
                errorMsg = errorCode + " - " + errorId;
        }
        return errorMsg;
    }

    private static IResponseMessage generateResponseErrorMessage(IAuthenticationRequest authData, ProtocolEngineI protocolEngine, AuthenticationResponse.Builder eidasAuthnResponseError, String ipUserAddress) throws EIDASSAMLEngineException {
        return protocolEngine.generateResponseErrorMessage(authData, eidasAuthnResponseError.build(), ipUserAddress);
    }

    private static EidasMetadataParametersI getIssuerMetadataParameters(ProtocolEngineI engine, String issuer) throws EIDASSAMLEngineException {
        if (StringUtils.isNotBlank(issuer)) {
            try {
                return metadataFetcher.getEidasMetadata(issuer,
                        (MetadataSignerI) engine.getSigner(),
                        (MetadataClockI) engine.getClock());
            } catch (EIDASMetadataException e) {
                throw new ProxyServiceError(e.getErrorCode(), e.getErrorMessage(), e);
            }
        }
        throw new ProxyServiceError(
                EidasErrorKey.SAML_ENGINE_NO_METADATA.errorCode(),
                EidasErrorKey.SAML_ENGINE_NO_METADATA.errorMessage());
    }

    private static IAuthenticationRequest ifNullReplaceAssertionConsumerServiceURL(IAuthenticationRequest authnRequest, String metadataAssertionConsumerUrl) {
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

    public static ProtocolEngineI getSamlEngine() {
        return nodeProtocolEngineFactory.getProtocolEngine(getSamlEngineInstanceName());
    }

    public void setNodeProtocolEngineFactory(ProtocolEngineFactory nodeProtocolEngineFactory) {
        this.nodeProtocolEngineFactory = nodeProtocolEngineFactory;
    }

    public static String getSamlEngineInstanceName() {
        return samlInstance;
    }

    public void setSamlEngineInstanceName(String samlEngineInstanceName) {
        this.samlInstance = samlEngineInstanceName;
    }

    public void setMessageSource(MessageSource nMessageSource) {
        this.messageSource = nMessageSource;
    }

    public static String getServiceMetadataUrl() {
        return serviceMetadataUrl;
    }

    public void setServiceMetadataUrl(String serviceMetadataUrl) {
        this.serviceMetadataUrl = serviceMetadataUrl;
    }

    public void setCountryCode(String code) {
        this.countryCode = code;
    }

    public void setMetadataFetcher(MetadataFetcherI metadataFetcher) {
        this.metadataFetcher = metadataFetcher;
    }

    protected Logger getLogger() {
        return LOG;
    }
}
