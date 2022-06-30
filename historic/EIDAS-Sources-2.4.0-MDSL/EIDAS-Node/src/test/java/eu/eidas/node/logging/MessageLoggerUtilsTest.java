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
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.spec.NaturalPersonSpec;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.engine.DefaultProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.impl.CachingMetadataFetcher;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.auth.connector.ICONNECTORSAMLService;
import eu.eidas.node.auth.connector.ICONNECTORService;
import eu.eidas.node.connector.ConnectorControllerService;
import eu.eidas.node.service.ServiceControllerService;
import eu.eidas.node.utils.PropertiesUtil;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;

import static eu.eidas.auth.commons.EidasParameterKeys.SPECIFIC_CONNECTOR_RESPONSE_RECEIVER;
import static eu.eidas.auth.commons.EidasParameterKeys.SPECIFIC_PROXYSERVICE_REQUEST_RECEIVER;
import static eu.eidas.node.NodeBeanNames.CONNECTOR_METADATA_FETCHER;
import static eu.eidas.node.NodeBeanNames.EIDAS_CONNECTOR_CONTROLLER;
import static eu.eidas.node.NodeBeanNames.EIDAS_SERVICE_CONTROLLER;
import static eu.eidas.node.NodeBeanNames.NODE_METADATA_FETCHER;
import static eu.eidas.node.NodeBeanNames.NODE_PROTOCOL_ENGINE_FACTORY;
import static eu.eidas.node.NodeBeanNames.PROXYSERVICE_METADATA_FETCHER;
import static eu.eidas.node.NodeBeanNames.SPECIFIC_CONNECTOR_DEPLOYED_JAR;
import static eu.eidas.node.NodeBeanNames.SPECIFIC_PROXYSERVICE_DEPLOYED_JAR;
import static eu.eidas.node.NodeSpecificViewNames.IDP_REQUEST;
import static eu.eidas.node.NodeSpecificViewNames.SPECIFIC_SP_RESPONSE;
import static eu.eidas.node.auth.util.tests.TestingConstants.CITIZEN_COUNTRY_CODE_CONS;
import static eu.eidas.node.auth.util.tests.TestingConstants.REQUEST_DESTINATION_CONS;
import static eu.eidas.node.auth.util.tests.TestingConstants.REQUEST_ID_CONS;
import static eu.eidas.node.auth.util.tests.TestingConstants.SAML_INSTANCE_CONS;
import static eu.eidas.node.utils.ReadFileUtils.readFileAsByteArray;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link MessageLoggerUtils}.
 *
 * @since 2.3
 */
public class MessageLoggerUtilsTest {

    /**
     * Test method for {@link MessageLoggerUtils#isLogMessages()}. Must succeed.
     */
    @Test
    public void isMessageLogged() {
        MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();
        messageLoggerUtils.setLogMessage(true);
        boolean actual = messageLoggerUtils.isLogMessages();
        assertThat(actual, is(true));
    }

    /**
     * Test method for {@link MessageLoggerUtils#getConnectorRedirectUrl()} when isSpecificConnectorJar is true. Must succeed.
     */
    @Test
    public void getConnectorRedirectUrlTrue() {
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        when(mockApplicationContext.getBean(SPECIFIC_CONNECTOR_DEPLOYED_JAR.toString())).thenReturn(Boolean.TRUE);
        ReflectionTestUtils.setField(BeanProvider.class, "CONTEXT", mockApplicationContext);

        MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();
        String actualRedirectUrl = messageLoggerUtils.getConnectorRedirectUrl();
        assertThat(actualRedirectUrl, is(SPECIFIC_SP_RESPONSE.toString()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getConnectorRedirectUrlFalse() {
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        when(mockApplicationContext.getBean(anyString())).thenReturn(Boolean.FALSE);
        ReflectionTestUtils.setField(BeanProvider.class, "CONTEXT", mockApplicationContext);

        String expectedRedirectUrl = "redirectUrlFalse";
        Map<String, String> mockMap = mock(Map.class);
        when(mockMap.get(SPECIFIC_CONNECTOR_RESPONSE_RECEIVER.toString())).thenReturn(expectedRedirectUrl);
        ReflectionTestUtils.setField(PropertiesUtil.class, "propertiesMap", mockMap);

        MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();
        String actualRedirectUrl = messageLoggerUtils.getConnectorRedirectUrl();
        assertThat(actualRedirectUrl, is(expectedRedirectUrl));
    }

    /**
     * Test method for {@link MessageLoggerUtils#getProxyServiceRedirectUrl()} when isSpecificProxyServiceJar is true. Must succeed.
     */
    @Test
    public void getProxyServiceRedirectUrlTrue() {
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        when(mockApplicationContext.getBean(SPECIFIC_PROXYSERVICE_DEPLOYED_JAR.toString())).thenReturn(Boolean.TRUE);
        ReflectionTestUtils.setField(BeanProvider.class, "CONTEXT", mockApplicationContext);

        MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();
        String actualRedirectUrl = messageLoggerUtils.getProxyServiceRedirectUrl();
        assertThat(actualRedirectUrl, is(IDP_REQUEST.toString()));
    }

    /**
     * Test method for {@link MessageLoggerUtils#getProxyServiceRedirectUrl()} when isSpecificProxyServiceJar is false. Must succeed.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void getProxyServiceRedirectUrlFalse() {
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        Mockito.when(mockApplicationContext.getBean(anyString())).thenReturn(Boolean.FALSE);
        ReflectionTestUtils.setField(BeanProvider.class, "CONTEXT", mockApplicationContext);

        String expectedRedirectUrl = "redirectUrlFalse";
        Map<String, String> mockMap = mock(Map.class);
        when(mockMap.get(SPECIFIC_PROXYSERVICE_REQUEST_RECEIVER.toString())).thenReturn(expectedRedirectUrl);
        ReflectionTestUtils.setField(PropertiesUtil.class, "propertiesMap", mockMap);

        MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();
        String actualRedirectUrl = messageLoggerUtils.getProxyServiceRedirectUrl();
        assertThat(expectedRedirectUrl, is(actualRedirectUrl));
    }

    /**
     * Test method for {@link MessageLoggerUtils#getIAuthenticationProxyRequest(byte[])}. Must succeed.
     */
    @Test
    public void getIAuthenticationProxyRequest() throws EIDASSAMLEngineException {
        MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();

        final byte[] samlObj = readFileAsByteArray("logging/testLoggingSamlRequest.xml");
        IAuthenticationRequest authenticationRequest = messageLoggerUtils.getIAuthenticationProxyRequest(samlObj);

        assertThat(authenticationRequest.getId(), is("_d048cb317813845b32a7a2b75f3bad59"));
        assertThat(authenticationRequest.getCitizenCountryCode(), is("N/A"));
        assertThat(authenticationRequest.getIssuer(), is("http://S-PEPS.gov.xx"));
        assertThat(authenticationRequest.getDestination(), is("http://peps.local:9090/PEPS/ServiceProvider"));
    }

    /**
     * Test method for {@link MessageLoggerUtils#getIAuthenticationResponse(byte[])}.
     *
     * Must succeed.
     */
    @Test
    public void getIAuthenticationResponse() throws EIDASSAMLEngineException {
        MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();

        final byte[] samlObj = readFileAsByteArray("logging/testLoggingSamlResponse.xml");
        IAuthenticationResponse iAuthenticationResponse = messageLoggerUtils.getIAuthenticationResponse(samlObj);

        assertThat(iAuthenticationResponse.getId(), is("_7POlm0XG6RUlrv67ArCVMe4cjc4IjIO5S2fe74-0yYbPRqbjJYl-v2hE4l6sqA2"));
        assertThat(iAuthenticationResponse.getInResponseToId(), is("_BWujh-SYHdY7qT35CXZ8y.yk5tSK793zQoiavTqXe.i_AuVw0f9RTOEQOihenSv"));
        assertThat(iAuthenticationResponse.getStatusCode(), is("urn:oasis:names:tc:SAML:2.0:status:Success"));
        assertThat(iAuthenticationResponse.getIssuer(), is("http://cef-eid-build-1:8080/EidasNode/ServiceMetadata"));
    }

    /**
     * Test method for {@link MessageLoggerUtils#getConnectorEntityId(String)}. Must succeed.
     */
    @Test
    public void getConnectorEntityId() throws EIDASMetadataException {
        String expectedEntityId = "connectorEntityId";

        EidasMetadataParametersI mockEidasMetadataParameters = mock(EidasMetadataParametersI.class);
        when(mockEidasMetadataParameters.getEntityID()).thenReturn(expectedEntityId);

        CachingMetadataFetcher mockCachingMetadataFetcher = mock(CachingMetadataFetcher.class);
        when(mockCachingMetadataFetcher.getEidasMetadata(anyString(), any(), any())).thenReturn(mockEidasMetadataParameters);

        ProtocolEngineFactory  mockProtocolEngineFactory = mock(ProtocolEngineFactory.class);
        ProtocolEngineI protocolEngine = DefaultProtocolEngineFactory.getInstance().getProtocolEngine(SAML_INSTANCE_CONS.toString());
        when(mockProtocolEngineFactory.getProtocolEngine(SAML_INSTANCE_CONS.toString())).thenReturn(protocolEngine);

        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        Mockito.when(mockApplicationContext.getBean(CONNECTOR_METADATA_FETCHER.toString())).thenReturn(mockCachingMetadataFetcher);
        Mockito.when(mockApplicationContext.getBean(NODE_PROTOCOL_ENGINE_FACTORY.toString())).thenReturn(mockProtocolEngineFactory);
        ReflectionTestUtils.setField(BeanProvider.class, "CONTEXT", mockApplicationContext);

        MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();
        messageLoggerUtils.setSamlConnectorServiceInstance(SAML_INSTANCE_CONS.toString());

        String actualEntityId = messageLoggerUtils.getConnectorEntityId("nonBlankMetaDataUrl");
        assertThat(actualEntityId, is(expectedEntityId));
    }

    /**
     * Test method for {@link MessageLoggerUtils#getProxyServiceEntityId(String)}. Must succeed.
     */
    @Test
    public void getProxyServiceEntityId() throws EIDASMetadataException {
        String expectedEntityId = "proxyServiceEntityId";

        EidasMetadataParametersI mockEidasMetadataParameters = mock(EidasMetadataParametersI.class);
        when(mockEidasMetadataParameters.getEntityID()).thenReturn(expectedEntityId);

        CachingMetadataFetcher mockCachingMetadataFetcher = mock(CachingMetadataFetcher.class);
        when(mockCachingMetadataFetcher.getEidasMetadata(anyString(), any(), any())).thenReturn(mockEidasMetadataParameters);

        ProtocolEngineFactory  mockProtocolEngineFactory = mock(ProtocolEngineFactory.class);
        ProtocolEngineI protocolEngine = DefaultProtocolEngineFactory.getInstance().getProtocolEngine(SAML_INSTANCE_CONS.toString());
        when(mockProtocolEngineFactory.getProtocolEngine(SAML_INSTANCE_CONS.toString())).thenReturn(protocolEngine);

        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        Mockito.when(mockApplicationContext.getBean(PROXYSERVICE_METADATA_FETCHER.toString())).thenReturn(mockCachingMetadataFetcher);
        Mockito.when(mockApplicationContext.getBean(NODE_PROTOCOL_ENGINE_FACTORY.toString())).thenReturn(mockProtocolEngineFactory);
        ReflectionTestUtils.setField(BeanProvider.class, "CONTEXT", mockApplicationContext);

        MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();
        messageLoggerUtils.setSamlEngineProxyInstanceName(SAML_INSTANCE_CONS.toString());

        String actualEntityId = messageLoggerUtils.getProxyServiceEntityId("nonBlankMetaDataUrl");
        assertThat(actualEntityId, is(expectedEntityId));

    }

    /**
     * Test method for {@link MessageLoggerUtils#retrieveConnectorAttributes()}. Must succeed.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void retrieveConnectorAttributes() {

        AttributeDefinition[] expectedAttributeDefinitions = new AttributeDefinition[0];
        ProtocolProcessorI mockProtocolProcessor = mock(ProtocolProcessorI.class);
        when(mockProtocolProcessor.getAllSupportedAttributes()).thenReturn(ImmutableSortedSet.copyOf(expectedAttributeDefinitions));

        ProtocolEngineI mockProtocolEngine = mock(ProtocolEngineI.class);
        when(mockProtocolEngine.getProtocolProcessor()).thenReturn(mockProtocolProcessor);

        ICONNECTORSAMLService mockIconnectorsamlService = mock(ICONNECTORSAMLService.class);
        when(mockIconnectorsamlService.getSamlEngine()).thenReturn(mockProtocolEngine);

        ICONNECTORService mockCIconnectorService = mock(ICONNECTORService.class);
        when(mockCIconnectorService.getSamlService()).thenReturn(mockIconnectorsamlService);

        ConnectorControllerService mockConnectorControllerService = mock(ConnectorControllerService.class);
        when(mockConnectorControllerService.getConnectorService()).thenReturn(mockCIconnectorService);

        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        Mockito.when(mockApplicationContext.getBean(EIDAS_CONNECTOR_CONTROLLER.toString())).thenReturn(mockConnectorControllerService);
        ReflectionTestUtils.setField(BeanProvider.class, "CONTEXT", mockApplicationContext);

        MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();
        Collection<AttributeDefinition<?>> attributeDefinitions = messageLoggerUtils.retrieveConnectorAttributes();
        System.out.println(attributeDefinitions);

    }

    /**
     * Test method for {@link MessageLoggerUtils#getIssuer(String, HttpServletRequest)}. Must succeed.
     */
    @Test
    public void getNonBlankServletRequestIssuer() {
        String nonBlankIssuer = "nonBlankIssuer";
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        when(mockHttpServletRequest.getAttribute(EidasParameterKeys.ISSUER.toString())).thenReturn(nonBlankIssuer);

        MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();
        String actualIssuer = messageLoggerUtils.getIssuer("aRequestId", mockHttpServletRequest);
        assertThat(actualIssuer, is(nonBlankIssuer));

    }

    /**
     * Test method for {@link MessageLoggerUtils#getIssuer(String, HttpServletRequest)}. Must succeed.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void getBlankServletRequestIssuer() {
        String expectedIssuer = "expeectedIssuer";
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        when(mockHttpServletRequest.getAttribute(EidasParameterKeys.ISSUER.toString())).thenReturn(StringUtils.EMPTY);

        StoredAuthenticationRequest storedAuthenticationRequest = buildStoredAuthenticationRequest(expectedIssuer);
        Cache mockCache = mock(Cache.class);
        when(mockCache.getAndRemove(anyString())).thenReturn(storedAuthenticationRequest);

        ServiceControllerService mockServiceControllerService = mock(ServiceControllerService.class);
        when(mockServiceControllerService.getProxyServiceRequestCorrelationCache()).thenReturn(mockCache);

        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        Mockito.when(mockApplicationContext.getBean(EIDAS_SERVICE_CONTROLLER.toString())).thenReturn(mockServiceControllerService);
        ReflectionTestUtils.setField(BeanProvider.class, "CONTEXT", mockApplicationContext);

        MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();
        String actualIssuer = messageLoggerUtils.getIssuer("aRequestId", mockHttpServletRequest);
        assertThat(actualIssuer, is(expectedIssuer));

    }

    private StoredAuthenticationRequest buildStoredAuthenticationRequest (String expectedIssuer) {

        final ImmutableAttributeMap.Builder attributeMapBuilder  = ImmutableAttributeMap.builder();
        ImmutableAttributeMap attributeMap = attributeMapBuilder
                           .put(NaturalPersonSpec.Definitions.BIRTH_NAME, "PersonName")
                           .build();

        EidasAuthenticationRequest.Builder eidasAuthenticationRequestBuilder = EidasAuthenticationRequest.builder();
        EidasAuthenticationRequest eidasAuthenticationRequest = eidasAuthenticationRequestBuilder
                .requestedAttributes(attributeMap)
                .destination(REQUEST_DESTINATION_CONS.toString())
                .issuer(expectedIssuer)
                .citizenCountryCode(CITIZEN_COUNTRY_CODE_CONS.toString())
                .id(REQUEST_ID_CONS.toString())
                .build();

        final StoredAuthenticationRequest.Builder storedAuthenticationRequestBuilder = StoredAuthenticationRequest.builder();
        return storedAuthenticationRequestBuilder
                .request(eidasAuthenticationRequest)
                .remoteIpAddress("127.0.0.1")
                .build();

    }

}