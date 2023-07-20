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
package eu.eidas.node.logging;

import com.google.common.collect.ImmutableSortedSet;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.light.IResponseStatus;
import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.ProtocolEngine;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.impl.CachingMetadataFetcher;
import eu.eidas.auth.engine.xml.opensaml.ResponseUtil;
import eu.eidas.auth.engine.xml.opensaml.XmlSchemaUtil;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.ConnectorBeanNames;
import eu.eidas.node.NodeSpecificViewNames;
import eu.eidas.node.connector.ConnectorControllerService;
import eu.eidas.node.utils.PropertiesUtil;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static eu.eidas.node.BeanProvider.getBean;
import static eu.eidas.node.ConnectorBeanNames.CONNECTOR_METADATA_FETCHER;
import static eu.eidas.node.ConnectorBeanNames.EIDAS_CONNECTOR_CONTROLLER;
import static eu.eidas.node.ConnectorBeanNames.NODE_PROTOCOL_ENGINE_FACTORY;
import static eu.eidas.node.logging.LoggingConstants.NOT_APPLICABLE;

/**
 * Utility class for logging the incoming and outgoing requests/responses from/to of the Eidas Proxy Service and Eidas Connector.
 *
 */
public final class MessageLoggerUtils {

    /**
     * Logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageLoggerUtils.class.getName());

    private String samlConnectorServiceInstance;

    private String samlEngineProxyInstanceName;

    private boolean logMessage;

    private boolean logCompleteMessage;

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
    public final void setLogMessage(boolean logMessage) {
        this.logMessage = logMessage;
    }

    /**
     * Check if the logging of the complete Light or eIDAS messages should be logged
     * This flag is active if full.audit.logging property from eidas.xml configuration file is set to true.
     *
     * @return true/false depending of the configuration
     */
    public final boolean isLogCompleteMessage() {
        return logCompleteMessage;
    }

    /**
     * Setter for the flag to check if complete message should be log.
     *
     * @param logCompleteMessage The value of the flag to set for the log of the complete message.
     */
    public final void setLogCompleteMessage(boolean logCompleteMessage) {
        this.logCompleteMessage = logCompleteMessage;
    }

    /**
     * Set samlConnectorServiceInstance.
     *
     * @param samlConnectorServiceInstance the new samlConnectorServiceInstance value.
     */
    public final void setSamlConnectorServiceInstance(String samlConnectorServiceInstance) {
        this.samlConnectorServiceInstance = samlConnectorServiceInstance;
    }

    /**
     * Set samlEngineProxyInstanceName.
     *
     * @param samlEngineProxyInstanceName the new samlEngineProxyInstanceName value.
     */
    public final void setSamlEngineProxyInstanceName(String samlEngineProxyInstanceName) {
        this.samlEngineProxyInstanceName = samlEngineProxyInstanceName;
    }

    /**
     * get entityId from Connector or Proxy Service metadata
     *
     * @param metaDataUrl the Url to metadata
     * @param metadataFetcherBeanName the name of the metadataFetcherBean
     * @param samlEngine {@link ProtocolEngine} instance
     * @return the entityId within metadata
     */
    private String getEntityId(String metaDataUrl, String metadataFetcherBeanName, ProtocolEngineI samlEngine) {
        CachingMetadataFetcher metadataFetcher = getBean(CachingMetadataFetcher.class, metadataFetcherBeanName);
        if (null == metadataFetcher) {
            return null;
        }

        String entityId = null;
        if (StringUtils.isNotBlank(metaDataUrl)){
            try {
                EidasMetadataParametersI eidasMetadataParameters = metadataFetcher.getEidasMetadata(metaDataUrl,
                        (MetadataSignerI) samlEngine.getSigner(), (MetadataClockI) samlEngine.getClock());

                entityId = eidasMetadataParameters.getEntityID();
            } catch (EIDASMetadataException e) {
                LOGGER.info("EIDASMetadataException {}", e.getMessage());
                LOGGER.debug("EIDASMetadataException ", e);
            }
        }
        return entityId;
    }

    /**
     * get entityId from Connector metadata
     *
     * @param metaDataUrl the Url to metadata
     * @return the entityId within metadata
     */
    public final String getConnectorEntityId(String metaDataUrl) {
        String metadataFetcherBeanName = CONNECTOR_METADATA_FETCHER.toString();
        return getEntityId(metaDataUrl, metadataFetcherBeanName, getConnectorSamlEngine());
    }

    /**
     * Get all the attributes supported i.e. both standard and sector-specific attributes.
     * @return the Connector's Attributes
     */
    public final Collection<AttributeDefinition<?>> retrieveConnectorAttributes() {
        String beanName = EIDAS_CONNECTOR_CONTROLLER.toString();
        ConnectorControllerService connectorControllerService = getBean(ConnectorControllerService.class, beanName);
        return ImmutableSortedSet.copyOf(connectorControllerService
                .getConnectorService()
                .getSamlService()
                .getSamlEngine()
                .getProtocolProcessor()
                .getAllSupportedAttributes());
    }

    /**
     * Get the Destination Url of the outgoing LightResponse to the Specific
     * @return the Connector Redirect Url
     */
    public final String getConnectorRedirectUrl() {
        String beanName = ConnectorBeanNames.SPECIFIC_CONNECTOR_DEPLOYED_JAR.toString();
        final boolean isSpecificConnectorJar = getBean(Boolean.class, beanName);
        if (isSpecificConnectorJar) {
            return NodeSpecificViewNames.MONOLITH_SPECIFIC_CONNECTOR_RESPONSE.toString();
        } else {
            return PropertiesUtil.getProperty(EidasParameterKeys.SPECIFIC_CONNECTOR_RESPONSE_RECEIVER.toString());
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
            throw new EIDASSAMLEngineException(e);
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
            throw new EIDASSAMLEngineException(e);
        }

        return iAuthenticationResponse;
    }

    private EidasAuthenticationRequest createIAuthenticationRequest(AuthnRequest authenticationRequest) throws EIDASSAMLEngineException {

        if (null == authenticationRequest.getIssuer()) {
            throw new EIDASSAMLEngineException(
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()));
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
            throw new EIDASSAMLEngineException(e);
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

    /**
     *
     * Returns a default ProtocolEngine instance matching the given name retrieved from the configuration file.
     *
     * @return the Connector ProtocolEngine instance matching the given name retrieved from the configuration file
     */
    private ProtocolEngineI getConnectorSamlEngine() {
        String beanName = NODE_PROTOCOL_ENGINE_FACTORY.toString();
        ProtocolEngineFactory protocolEngineFactory = getBean(ProtocolEngineFactory.class, beanName);
        return protocolEngineFactory.getProtocolEngine(samlConnectorServiceInstance);
    }

}
