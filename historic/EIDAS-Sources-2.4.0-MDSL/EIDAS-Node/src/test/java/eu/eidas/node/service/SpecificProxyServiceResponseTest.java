/*
 *
 * Copyright (c) 2019 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *  https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence
 *
 */
package eu.eidas.node.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.commons.exceptions.SecurityEIDASException;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.auth.service.AUSERVICEUtil;
import eu.eidas.node.auth.service.ISERVICESAMLService;
import eu.eidas.node.auth.service.ISERVICEService;
import eu.eidas.node.utils.ReflectionUtils;
import eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.impl.SpecificProxyserviceCommunicationServiceImpl;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.cache.Cache;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpecificProxyServiceResponseTest {

    private SpecificProxyServiceResponse specificProxyServiceResponse;
    private Properties mockProperties;

    @Mock
    private ApplicationContext mockApplicationContext;
    @Mock
    private AUCONNECTORUtil mockAuthConnectorUtil;
    @Mock
    private AUSERVICEUtil mockAuserviceUtil;
    @Mock
    private ISERVICEService mockServiceService;

    @Before
    public void setupContext() {
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
        initMockProperties();
        when(mockAuthConnectorUtil.getConfigs()).thenReturn(mockProperties);

        when(mockApplicationContext.getBean(AUCONNECTORUtil.class)).thenReturn(mockAuthConnectorUtil);
        when(mockApplicationContext.getBean(AUSERVICEUtil.class)).thenReturn(mockAuserviceUtil);

        specificProxyServiceResponse = new SpecificProxyServiceResponse();
    }

    private void initMockProperties() {
        mockProperties = new Properties();
    }

    /**
     * Test method for {@link SpecificProxyServiceResponse#doPost(HttpServletRequest, HttpServletResponse)}.
     * In this method, we check if the SpecificProxyServiceResponse is correctly executed when module is active.
     */
    @Test
    public void doPostWithActiveProxyServiceModule() throws Exception {
        mockProperties.setProperty(EidasParameterKeys.EIDAS_SERVICE_ACTIVE.toString(), Boolean.TRUE.toString());
        ServiceControllerService mockServiceControllerService = mockServiceControllerServiceInAppContext();
        mockSpecificProxyServiceCommunicationServiceImpl(mockServiceControllerService);
        mockAuthenticationRequest(mockServiceControllerService);

        ServletContext mockServletContext = mockServletContext();
        RequestDispatcher mockDispatcher = Mockito.mock(RequestDispatcher.class);
        when(mockServletContext.getRequestDispatcher(any())).thenReturn(mockDispatcher);
        when(mockAuserviceUtil.checkNotPresentInCache(anyString())).thenReturn(true);

        specificProxyServiceResponse.doPost(createMockRequest(), new MockHttpServletResponse());
        verify(mockDispatcher).forward(isA(ServletRequest.class), isA(ServletResponse.class));
    }

    /**
     * Test method for {@link SpecificProxyServiceResponse#doPost(HttpServletRequest, HttpServletResponse)}.
     * Checks the {@link eu.eidas.auth.commons.light.impl.LightResponse} anti-replay behavior.
     */
    @Test(expected = SecurityEIDASException.class)
    public void doPostWithAntiReplayTriggered() throws ServletException, IOException, SpecificCommunicationException {
        ServiceControllerService mockServiceControllerService = mockServiceControllerServiceInAppContext();
        mockSpecificProxyServiceCommunicationServiceImpl(mockServiceControllerService);

        boolean notPresentInCache = false;
        when(mockAuserviceUtil.checkNotPresentInCache(anyString())).thenReturn(notPresentInCache);

        specificProxyServiceResponse.doPost(createMockRequest(), new MockHttpServletResponse());
    }


    /**
     * Test method for {@link SpecificProxyServiceResponse#doPost(HttpServletRequest, HttpServletResponse)} (HttpServletRequest, HttpServletResponse)}.
     * In this method, we check if we correctly return an error because of Proxy Service module inactivation.
     */
    @Test(expected = EidasNodeException.class)
    public void doPostWithInactiveProxyServiceModule() throws ServletException, IOException {
        mockProperties.setProperty(EidasParameterKeys.EIDAS_SERVICE_ACTIVE.toString(), Boolean.FALSE.toString());
        specificProxyServiceResponse.doPost(createMockRequest(), new MockHttpServletResponse());
    }

    private ServiceControllerService mockServiceControllerServiceInAppContext() {
        ServiceControllerService mockServiceControllerService = Mockito.mock(ServiceControllerService.class);
        when(mockApplicationContext.getBean(NodeBeanNames.EIDAS_SERVICE_CONTROLLER.toString()))
                .thenReturn(mockServiceControllerService);

        return mockServiceControllerService;
    }

    private HttpServletRequest createMockRequest() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.POST.toString(), null);
        mockRequest.setParameter(EidasParameterKeys.LIGHT_RESPONSE.toString(), "Fake response");

        return mockRequest;
    }

    private void retrieveAttributes(ServiceControllerService mockServiceControllerService) {
        when(mockServiceControllerService.getProxyService()).thenReturn(mockServiceService);

        ISERVICESAMLService mockSamlService = Mockito.mock(ISERVICESAMLService.class);
        when(mockServiceService.getSamlService()).thenReturn(mockSamlService);

        ProtocolEngineI mockProtocolEngine = Mockito.mock(ProtocolEngineI.class);
        when(mockSamlService.getSamlEngine()).thenReturn(mockProtocolEngine);

        ProtocolProcessorI mockProtocolProcessor = Mockito.mock(ProtocolProcessorI.class);
        when(mockProtocolEngine.getProtocolProcessor()).thenReturn(mockProtocolProcessor);

        AttributeDefinition[] expectedAttributeDefinitions = new AttributeDefinition[0];
        when(mockProtocolProcessor.getAllSupportedAttributes()).thenReturn(ImmutableSortedSet.copyOf(expectedAttributeDefinitions));
    }

    private ServletContext mockServletContext() throws Exception {
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        ServletConfig mockServletConfig = Mockito.mock(ServletConfig.class);
        when(mockServletConfig.getServletContext()).thenReturn(servletContext);
        specificProxyServiceResponse.init(mockServletConfig);

        return servletContext;
    }

    private ILightResponse mockILightResponse() {
        String inResponseTo = String.format("inResponseTo_%s", RandomStringUtils.randomAlphabetic(10));
        ResponseStatus mockResponseStatus = Mockito.mock(ResponseStatus.class);
        ILightResponse mockILightResponse = Mockito.mock(ILightResponse.class);
        when(mockILightResponse.getInResponseToId()).thenReturn(inResponseTo);
        when(mockILightResponse.getStatus()).thenReturn(mockResponseStatus);

        return mockILightResponse;
    }

    private void mockSpecificProxyServiceCommunicationServiceImpl(ServiceControllerService mockServiceControllerService) throws SpecificCommunicationException {
        SpecificProxyserviceCommunicationServiceImpl mockSpecificProxyServiceCommunicationServiceImpl = Mockito.mock(SpecificProxyserviceCommunicationServiceImpl.class);
        String beanName = SpecificCommunicationDefinitionBeanNames.SPECIFIC_PROXYSERVICE_COMMUNICATION_SERVICE.toString();
        when(mockApplicationContext.getBean(beanName)).thenReturn(mockSpecificProxyServiceCommunicationServiceImpl);

        ILightResponse mockILightResponse = mockILightResponse();
        retrieveAttributes(mockServiceControllerService);
        when(mockSpecificProxyServiceCommunicationServiceImpl.getAndRemoveResponse(Mockito.isNull(), any())).thenReturn(mockILightResponse);
    }

    private void mockAuthenticationRequest(ServiceControllerService mockServiceControllerService) {
        StoredAuthenticationRequest storedAuthenticationRequest = Mockito.mock(StoredAuthenticationRequest.class);
        IAuthenticationRequest mockIAuthenticationRequest = Mockito.mock(IAuthenticationRequest.class);
        Cache mockCache = Mockito.mock(Cache.class);
        when(mockCache.getAndRemove(anyString())).thenReturn(storedAuthenticationRequest);
        when(mockServiceControllerService.getProxyServiceRequestCorrelationCache()).thenReturn(mockCache);
        when(storedAuthenticationRequest.getRequest()).thenReturn(mockIAuthenticationRequest);

        mockIResponseMessage();
    }

    private void mockIResponseMessage() {
        IAuthenticationResponse mockIAuthenticationResponse = Mockito.mock(IAuthenticationResponse.class);
        IResponseMessage mockIResponseMessage = Mockito.mock(IResponseMessage.class);
        when(mockServiceService.processIdpResponse(any(WebRequest.class), any(StoredAuthenticationRequest.class), any(ILightResponse.class))).thenReturn(mockIResponseMessage);
        when(mockIResponseMessage.getMessageBytes()).thenReturn(new byte[0]);
        when(mockIResponseMessage.getResponse()).thenReturn(mockIAuthenticationResponse);

        mockAttributesMap(mockIAuthenticationResponse);
    }

    private void mockAttributesMap(IAuthenticationResponse mockIAuthenticationResponse) {
        ImmutableAttributeMap mockImmutableAttributeMap = Mockito.mock(ImmutableAttributeMap.class);
        ImmutableMap mockImmutableMap = Mockito.mock(ImmutableMap.class);
        when(mockIAuthenticationResponse.getAttributes()).thenReturn(mockImmutableAttributeMap);
        when(mockImmutableAttributeMap.getAttributeMap()).thenReturn(mockImmutableMap);

        AttributeDefinition[] expectedAttributeDefinitions = new AttributeDefinition[0];
        when(mockImmutableMap.keySet()).thenReturn(ImmutableSortedSet.copyOf(expectedAttributeDefinitions));
    }

}
