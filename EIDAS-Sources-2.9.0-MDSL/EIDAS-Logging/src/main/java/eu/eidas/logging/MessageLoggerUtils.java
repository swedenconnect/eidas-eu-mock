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
package eu.eidas.logging;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.light.IResponseStatus;
import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.xml.opensaml.ResponseUtil;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.auth.engine.xml.opensaml.XmlSchemaUtil;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static eu.eidas.logging.LoggingConstants.NOT_APPLICABLE;

/**
 * Utility class for logging the incoming and outgoing requests/responses from/to of the Eidas Proxy Service and Eidas Connector.
 *
 */
public final class MessageLoggerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageLoggerUtils.class.getName());
    private boolean logMessage;

    private boolean logCompleteMessage;

    private Cache<String, StoredAuthenticationRequest> proxyServiceRequestCorrelationCache;

    private Cache<String, String>  flowIdCache;

    private ProtocolEngineI protocolEngine;

    /**
     * get entityId from cached metadata
     *
     * @param metaDataUrl the Url to metadata
     * @return the entityId within metadata
     */
    @Nullable
    public String getEntityId(String metaDataUrl) {
        try {
            return protocolEngine.getProtocolProcessor().getMetadataParameters(metaDataUrl).getEntityID();
        } catch (EIDASSAMLEngineException e) {
            LOGGER.info("EIDASMetadataException {}", e.getMessage());
            LOGGER.debug("EIDASMetadataException ", e);
        }
        return null;
    }

    /**
     * Get all the attributes supported i.e. both standard and sector-specific attributes.
     * @return the Connector's Attributes
     */
    public final Collection<AttributeDefinition<?>> retrieveAttributes() {
        return Set.copyOf(protocolEngine
                .getProtocolProcessor()
                .getAllSupportedAttributes());
    }

    public final String getIssuer(String requestId, HttpServletRequest httpServletRequest) {

        final String issuer = (String) httpServletRequest.getAttribute(EidasParameterKeys.ISSUER.toString());

        if (StringUtils.isBlank(issuer)) {

            final StoredAuthenticationRequest storedAuthenticationRequest = proxyServiceRequestCorrelationCache.getAndRemove(requestId);
            return storedAuthenticationRequest.getRequest().getIssuer();

        } else {
            return issuer;
        }
    }

    /**
     * Retrieves the {@link IAuthenticationRequest} from the SAML in bytes
     * which with the minimum data needed for the message logging.

     *
     * @param samlObj the token received in http request
     * @return the instance of {@link IAuthenticationRequest}
     * @throws EIDASSAMLEngineException when the authentication request could not be unmarshalled or properly build
     */
    public final IAuthenticationRequest getIAuthenticationProxyRequest(byte[] samlObj) throws EIDASSAMLEngineException {
        Document document = XmlSchemaUtil.validateSamlSchema(samlObj);
        final IAuthenticationRequest iAuthenticationRequest;
        try {
            AuthnRequest authenticationRequest = (AuthnRequest) OpenSamlHelper.unmarshallFromDom(document);
            iAuthenticationRequest = createIAuthenticationRequest(authenticationRequest);
        } catch (UnmarshallException e) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_FORMAT_UNSUPPORTED, "Failed to unmarshall request", e);
        }

        return iAuthenticationRequest;
    }

    /**
     * Retrieves the {@link IAuthenticationResponse} from the SAML in bytes
     * which with the minimum data needed for the message logging.
     *
     * @param samlObj         the token received in http request
     * @return the instance of {@link IAuthenticationRequest}
     * @throws EIDASSAMLEngineException when the authentication request could not be unmarshalled
     */
    public final IAuthenticationResponse getIAuthenticationResponse(byte[] samlObj) throws EIDASSAMLEngineException {
        final Document document = createDocument(samlObj);

        IAuthenticationResponse iAuthenticationResponse;
        try {
            final Response response = unmarshalResponse(document);
            iAuthenticationResponse = createIAuthenticationResponse(response);
        } catch (UnmarshallException e) {
            throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_FORMAT_UNSUPPORTED, "Failed to unmarshall Response", e);
        }

        return iAuthenticationResponse;
    }

    /**
     * Enables the logging of eIDAS messages if saml.audit property from eidas.xml configuration file is set to true.
     *
     * @return true/false depending of the configuration
     */
    public final boolean isLogMessages() {
        return logMessage;
    }

    /**
     * Setter for logMessage.
     *
     * @param logMessage The logMessage to set.
     */
    public void setLogMessage(boolean logMessage) {
        this.logMessage = logMessage;
    }

    /**
     * Check if the logging of the complete Light or eIDAS messages should be logged
     * This flag is active if full.audit.logging property from eidas.xml configuration file is set to true.
     *
     * @return true/false depending of the configuration
     */
    public boolean isLogCompleteMessage() {
        return logCompleteMessage;
    }

    /**
     * Setter for the flag to check if complete message should be log.
     *
     * @param logCompleteMessage The value of the flag to set for the log of the complete message.
     */
    public void setLogCompleteMessage(boolean logCompleteMessage) {
        this.logCompleteMessage = logCompleteMessage;
    }

    public void setProxyServiceRequestCorrelationCache(Cache cache){
        this.proxyServiceRequestCorrelationCache = cache;
    }

    public void setFlowIdCache(Cache<String, String>  flowIdCache) {
        this.flowIdCache = flowIdCache;
    }

    public Cache<String, String>  getFlowIdCache() {
        return flowIdCache;
    }

    public void setProtocolEngine(ProtocolEngineI protocolEngine) {
        this.protocolEngine = protocolEngine;
    }

    @Nonnull
    public String trackMessageFlow(String msgId) {
        return Optional.ofNullable(getFlowIdCache().get(msgId))
                .orElseGet(() -> startTrackingMessageFlow(msgId));
    }

    private String startTrackingMessageFlow(String msgId) {
        final String flowId = SAMLEngineUtils.generateNCName();
        getFlowIdCache().put(msgId, flowId);
        return flowId;
    }

    private EidasAuthenticationRequest createIAuthenticationRequest(AuthnRequest authenticationRequest) throws EIDASSAMLEngineException {

        if (null == authenticationRequest.getIssuer()) {
            throw new EIDASSAMLEngineException(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML, "Request is missing issuer");
        }
        String issuer = authenticationRequest.getIssuer().getValue();

        EidasAuthenticationRequest.Builder eidasAuthenticationRequestBuilder = EidasAuthenticationRequest.builder();
        eidasAuthenticationRequestBuilder.id(authenticationRequest.getID())
                .destination(authenticationRequest.getDestination())
                .issuer(issuer)
                .citizenCountryCode(NOT_APPLICABLE);

        EidasAuthenticationRequest eidasAuthenticationRequest;
        try {
            RequestedAuthnContext requestedAuthnContext = authenticationRequest.getRequestedAuthnContext();
            if (requestedAuthnContext != null && requestedAuthnContext.getAuthnContextClassRefs() != null) {
                List<ILevelOfAssurance> levelsOfAssurance = requestedAuthnContext.getAuthnContextClassRefs().stream()
                        .map(AuthnContextClassRef::getURI)
                        .map(LevelOfAssurance::build).collect(Collectors.toList());

                eidasAuthenticationRequestBuilder.levelsOfAssurance(levelsOfAssurance);
            }

            eidasAuthenticationRequest = eidasAuthenticationRequestBuilder.build();
        } catch (IllegalArgumentException e) {
            throw new EIDASSAMLEngineException(EidasErrorKey.INVALID_LOA_VALUE, "Failed to build Request LoAs", e);
        }
        return eidasAuthenticationRequest;
    }

    private Document createDocument(byte[] samlObj) throws EIDASSAMLEngineException {
        return XmlSchemaUtil.validateSamlSchema(samlObj);
    }

    private Response unmarshalResponse(Document document) throws UnmarshallException {
        return (Response) OpenSamlHelper.unmarshallFromDom(document);
    }

    private IAuthenticationResponse createIAuthenticationResponse(Response samlResponse) {
        IResponseStatus responseStatus = ResponseUtil.extractResponseStatus(samlResponse);
        String issuer = samlResponse.getIssuer().getValue();

        AuthenticationResponse.Builder responseBuilder = new AuthenticationResponse.Builder();
        responseBuilder
                .id(samlResponse.getID())
                .inResponseTo(samlResponse.getInResponseTo())
                .responseStatus(responseStatus)
                .issuer(issuer)
                .subject(NOT_APPLICABLE)
                .subjectNameIdFormat(NOT_APPLICABLE);

        return responseBuilder.build();
    }
}
