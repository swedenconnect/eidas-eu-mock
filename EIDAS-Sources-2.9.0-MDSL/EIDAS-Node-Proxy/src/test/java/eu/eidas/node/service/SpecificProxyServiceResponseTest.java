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
package eu.eidas.node.service;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.ProxyBeanNames;
import eu.eidas.node.auth.service.AUSERVICEUtil;
import eu.eidas.node.auth.service.ISERVICESAMLService;
import eu.eidas.node.auth.service.ISERVICEService;
import eu.eidas.node.service.exceptions.ProxyServiceError;
import eu.eidas.node.utils.ReflectionUtils;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationService;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
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
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link SpecificProxyServiceResponse}
 */
@RunWith(MockitoJUnitRunner.class)
public class SpecificProxyServiceResponseTest {

    private SpecificProxyServiceResponse specificProxyServiceResponse;

    @Mock
    private ApplicationContext mockApplicationContext;
    @Mock
    private AUSERVICEUtil mockAuServiceUtil;
    @Mock
    private ISERVICEService mockServiceService;

    private ApplicationContext oldContext = null;

    @Before
    public void setupContext() throws NoSuchFieldException, IllegalAccessException {
        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        oldContext = (ApplicationContext) contextField.get(null);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
        when(mockApplicationContext.getBean(AUSERVICEUtil.class)).thenReturn(mockAuServiceUtil);
        specificProxyServiceResponse = new SpecificProxyServiceResponse();
    }

    @After
    public void tearDown() {
        if (oldContext != null) {
            ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", oldContext);
            oldContext = null;
        }
    }

    /**
     * Test method for {@link SpecificProxyServiceResponse#doPost(HttpServletRequest, HttpServletResponse)}.
     * In this method, we check if the SpecificProxyServiceResponse is correctly executed when module is active.
     */
    @Test
    public void doPostWithActiveProxyServiceModule() throws Exception {
        ServiceControllerService mockServiceControllerService = mockServiceControllerServiceInAppContext();
        mockSpecificCommunicationService(mockServiceControllerService);
        mockAuthenticationRequest(mockServiceControllerService);

        ServletContext mockServletContext = mockServletContext();
        RequestDispatcher mockDispatcher = Mockito.mock(RequestDispatcher.class);
        when(mockServletContext.getRequestDispatcher(any())).thenReturn(mockDispatcher);
        when(mockAuServiceUtil.checkNotPresentInCache(anyString())).thenReturn(true);

        specificProxyServiceResponse.doPost(createMockRequest(), new MockHttpServletResponse());
        verify(mockDispatcher).forward(isA(ServletRequest.class), isA(ServletResponse.class));
    }

    /**
     * Test method for {@link SpecificProxyServiceResponse#doPost(HttpServletRequest, HttpServletResponse)}.
     * Checks the {@link eu.eidas.auth.commons.light.impl.LightResponse} anti-replay behavior.
     */
    @Test(expected = ProxyServiceError.class)
    public void doPostWithAntiReplayTriggered() throws ServletException, IOException, SpecificCommunicationException {
        ServiceControllerService mockServiceControllerService = mockServiceControllerServiceInAppContext();
        mockSpecificCommunicationService(mockServiceControllerService);

        boolean notPresentInCache = false;
        when(mockAuServiceUtil.checkNotPresentInCache(anyString())).thenReturn(notPresentInCache);

        specificProxyServiceResponse.doPost(createMockRequest(), new MockHttpServletResponse());
    }

    private ServiceControllerService mockServiceControllerServiceInAppContext() {
        ServiceControllerService mockServiceControllerService = Mockito.mock(ServiceControllerService.class);
        when(mockApplicationContext.getBean(ProxyBeanNames.EIDAS_SERVICE_CONTROLLER.toString()))
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
        List<AttributeDefinition<?>> attributeDefinitionList = Arrays.stream(expectedAttributeDefinitions)
                .map(attribute -> (AttributeDefinition<?>) attribute)
                .collect(Collectors.toList());
        SortedSet<AttributeDefinition<?>> sortedAttributeDefinitionSet = new TreeSet<>(attributeDefinitionList);
        when(mockProtocolProcessor.getAllSupportedAttributes()).thenReturn(sortedAttributeDefinitionSet);
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

    private void mockSpecificCommunicationService(ServiceControllerService mockServiceControllerService) throws SpecificCommunicationException {
        SpecificCommunicationService mockSpecificCommunicationService = Mockito.mock(SpecificCommunicationService.class);
        String beanName = ProxyBeanNames.SPECIFIC_PROXYSERVICE_COMMUNICATION_SERVICE.toString();
        when(mockApplicationContext.getBean(beanName)).thenReturn(mockSpecificCommunicationService);

        ILightResponse mockILightResponse = mockILightResponse();
        retrieveAttributes(mockServiceControllerService);
        when(mockSpecificCommunicationService.getAndRemoveResponse(Mockito.isNull(), any())).thenReturn(mockILightResponse);
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
        Map mockMap = Mockito.mock(Map.class);
        when(mockIAuthenticationResponse.getAttributes()).thenReturn(mockImmutableAttributeMap);
        when(mockImmutableAttributeMap.getAttributeMap()).thenReturn(mockMap);

        Set<AttributeDefinition<?>> expectedAttributeDefinitions = new TreeSet<>();
        when(mockMap.keySet()).thenReturn(expectedAttributeDefinitions);
    }

}
