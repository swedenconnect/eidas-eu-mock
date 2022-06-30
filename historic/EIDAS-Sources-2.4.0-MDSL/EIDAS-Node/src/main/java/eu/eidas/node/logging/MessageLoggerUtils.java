/*
 * Copyright (c) 2019 by European Commission
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
 * limitations under the Licence
 */
package eu.eidas.node.logging;

import com.google.common.collect.ImmutableSortedSet;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.light.IResponseStatus;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
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
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.NodeSpecificViewNames;
import eu.eidas.node.connector.ConnectorControllerService;
import eu.eidas.node.service.ServiceControllerService;
import eu.eidas.node.utils.PropertiesUtil;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static eu.eidas.node.BeanProvider.getBean;
import static eu.eidas.node.NodeBeanNames.CONNECTOR_METADATA_FETCHER;
import static eu.eidas.node.NodeBeanNames.EIDAS_CONNECTOR_CONTROLLER;
import static eu.eidas.node.NodeBeanNames.NODE_METADATA_FETCHER;
import static eu.eidas.node.NodeBeanNames.NODE_PROTOCOL_ENGINE_FACTORY;
import static eu.eidas.node.NodeBeanNames.PROXYSERVICE_METADATA_FETCHER;
import static eu.eidas.node.logging.AbstractLogger.NOT_APPLICABLE;

/**
 * Utility class for logging the incoming and outgoing requests/responses from/to of the Eidas Proxy Service and Eidas Connector.
 *
 * @since 2.3
 */
public final class MessageLoggerUtils {

    /**
     * Logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageLoggerUtils.class.getName());

    private String samlConnectorServiceInstance;

    private String samlEngineProxyInstanceName;

    private boolean logMessage;

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
            }catch (EIDASMetadataException e) {
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
     * get entityId from Proxy Service metadata
     *
     * @param metaDataUrl the Url to metadata
     * @return the entityId within metadata
     */
    public final String getProxyServiceEntityId(String metaDataUrl) {
        String metadataFetcherBeanName = PROXYSERVICE_METADATA_FETCHER.toString();
        return getEntityId(metaDataUrl, metadataFetcherBeanName, getProxyServiceSamlEngine());
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
        String beanName = NodeBeanNames.SPECIFIC_CONNECTOR_DEPLOYED_JAR.toString();
        final boolean isSpecificConnectorJar = getBean(Boolean.class, beanName);
        if (isSpecificConnectorJar) {
            return NodeSpecificViewNames.SPECIFIC_SP_RESPONSE.toString();
        } else {
            return PropertiesUtil.getProperty(EidasParameterKeys.SPECIFIC_CONNECTOR_RESPONSE_RECEIVER.toString());
        }
    }

    /**
     * Get the Destination Url of the outgoing LightRequest to the Specific
     * @return the ProxyService Redirect Url
     */
    public final String getProxyServiceRedirectUrl() {
        String beanName = NodeBeanNames.SPECIFIC_PROXYSERVICE_DEPLOYED_JAR.toString();
        final boolean isSpecificProxyServiceJar = getBean(Boolean.class, beanName);
        if (isSpecificProxyServiceJar) {
            return NodeSpecificViewNames.IDP_REQUEST.toString();
        } else {
            return PropertiesUtil.getProperty(EidasParameterKeys.SPECIFIC_PROXYSERVICE_REQUEST_RECEIVER.toString());
        }
    }

    /**
     * Gets the issuer related to the requestId from either {@link HttpServletRequest} attributes or
     * if not possible, gets the request from the correlation cache and the issuer from the retrieved request.
     *
     * @param requestId          the id for the request
     * @param httpServletRequest the instance of the {@link HttpServletRequest}
     * @return the issuer of that request
     */
    public final String getIssuer(String requestId, HttpServletRequest httpServletRequest) {

        final String issuer = (String) httpServletRequest.getAttribute(EidasParameterKeys.ISSUER.toString());

        if (StringUtils.isBlank(issuer)) {

            final String beanName = NodeBeanNames.EIDAS_SERVICE_CONTROLLER.toString();
            final ServiceControllerService controllerService = getBean(ServiceControllerService.class, beanName);
            final Cache<String, StoredAuthenticationRequest> requestCorrelationMap =
                    controllerService.getProxyServiceRequestCorrelationCache();
            final StoredAuthenticationRequest storedAuthenticationRequest = requestCorrelationMap.getAndRemove(requestId);

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
        } catch (Exception e) {
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

    private EidasAuthenticationRequest createIAuthenticationRequest(AuthnRequest authenticationRequest) {

        String issuer = authenticationRequest.getIssuer().getValue();

        EidasAuthenticationRequest.Builder eidasAuthenticationRequestBuilder = EidasAuthenticationRequest.builder();
        eidasAuthenticationRequestBuilder.id(authenticationRequest.getID())
                .destination(authenticationRequest.getDestination())
                .issuer(issuer)
                .citizenCountryCode(NOT_APPLICABLE);

        return eidasAuthenticationRequestBuilder.build();
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
     * Returns a default ProtocolEngine instance matching the given name retrieved from the configuration file.
     *
     * @return the Proxy Service ProtocolEngine instance matching the given name retrieved from the configuration file
     */
    private ProtocolEngineI getProxyServiceSamlEngine() {
        String beanName = NODE_PROTOCOL_ENGINE_FACTORY.toString();
        ProtocolEngineFactory protocolEngineFactory = getBean(ProtocolEngineFactory.class, beanName);
        return protocolEngineFactory.getProtocolEngine(samlEngineProxyInstanceName);
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
