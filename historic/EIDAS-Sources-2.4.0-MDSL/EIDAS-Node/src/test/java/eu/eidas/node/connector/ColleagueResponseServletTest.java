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
package eu.eidas.node.connector;


import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.tx.AuthenticationExchange;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.auth.connector.ICONNECTORService;
import eu.eidas.node.utils.ReflectionUtils;
import eu.eidas.specificcommunication.BinaryLightTokenHelper;
import eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames;
import eu.eidas.specificcommunication.protocol.impl.SpecificConnectorCommunicationServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.annotation.Nonnull;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link ColleagueResponseServlet}.
 * Tests for incoming EIDAS response.
 *
 * @since 2.4
 */
@RunWith(MockitoJUnitRunner.class)
public class ColleagueResponseServletTest {

    private ColleagueResponseServlet colleagueResponseServlet;
    @Mock
    private ApplicationContext mockApplicationContext;
    @Mock
    private AUCONNECTORUtil mockAuthConnectorUtil;
    private Properties mockProperties;
    @Mock
    private ICONNECTORService mockConnectorService;

    SpecificConnectorCommunicationServiceImpl mockSpecificCommunicationService;

    private String mockRelayState;

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Before
    public void setupContext() {
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
        initMockProperties();
        Mockito.when(mockAuthConnectorUtil.getConfigs()).thenReturn(mockProperties);
        Mockito.when(mockApplicationContext.getBean(AUCONNECTORUtil.class))
                .thenReturn(mockAuthConnectorUtil);

        mockRelayState = UUID.randomUUID().toString();

        colleagueResponseServlet = new ColleagueResponseServlet();
    }

    private void initMockProperties() {
        mockProperties = new Properties();
    }

    private void mockConnectorService() {
        ConnectorControllerService mockConnectorControllerService = Mockito.mock(ConnectorControllerService.class);
        Mockito.when(mockApplicationContext.getBean(NodeBeanNames.EIDAS_CONNECTOR_CONTROLLER.toString()))
                .thenReturn(mockConnectorControllerService);
        Mockito.when(mockConnectorControllerService.getConnectorService()).thenReturn(mockConnectorService);
    }

    private void mockAuthenticationResponse(boolean success, String eidasRequestRelayState) {
        IAuthenticationRequest mockIAuthenticationRequest = Mockito.mock(IAuthenticationRequest.class);
        Mockito.when(mockIAuthenticationRequest.getRelayState()).thenReturn(eidasRequestRelayState);

        StoredAuthenticationRequest mockStoredAuthRequest = Mockito.mock(StoredAuthenticationRequest.class);
        Mockito.when(mockStoredAuthRequest.getRequest()).thenReturn(mockIAuthenticationRequest);

        AuthenticationResponse fakeResponse = createFakeResponse(createResponseStatus(success));
        AuthenticationExchange authenticationExchange = new AuthenticationExchange(mockStoredAuthRequest, fakeResponse);

        Mockito.when(mockConnectorService.getAuthenticationResponse(any(WebRequest.class)))
                .thenReturn(authenticationExchange);

    }

    private void mockSpecificCommunicationService(BinaryLightToken binaryLightToken) throws Exception {
        mockSpecificCommunicationService =
                Mockito.mock(SpecificConnectorCommunicationServiceImpl.class);
        String beanName = SpecificCommunicationDefinitionBeanNames.SPECIFIC_CONNECTOR_COMMUNICATION_SERVICE.toString();
        Mockito.when(mockApplicationContext.getBean(beanName)).thenReturn(mockSpecificCommunicationService);
        beanName = NodeBeanNames.SPECIFIC_CONNECTOR_DEPLOYED_JAR.toString();
        Mockito.when(mockApplicationContext.getBean(beanName)).thenReturn(true);
        Mockito.when(mockSpecificCommunicationService.putResponse(any())).thenReturn(binaryLightToken);
    }

    private ServletContext mockServletContext() throws Exception {
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        ServletConfig mockServletConfig = Mockito.mock(ServletConfig.class);
        Mockito.when(mockServletConfig.getServletContext()).thenReturn(servletContext);
        colleagueResponseServlet.init(mockServletConfig);
        return servletContext;
    }

    private ResponseStatus createResponseStatus(boolean success) {
        return ResponseStatus.builder()
                .failure(!success)
                .statusCode(success ? "200": "500")
                .build();
    }

    private AuthenticationResponse createFakeResponse(ResponseStatus status) {
        return AuthenticationResponse.builder()
                .id(UUID.randomUUID().toString())
                .subject("TestSubject")
                .subjectNameIdFormat("unspecified")
                .levelOfAssurance("low")
                .issuer("CA")
                .responseStatus(status)
                .inResponseTo("Me")
                .build();
    }

    /**
     * Test method for {@link ColleagueResponseServlet#doPost(HttpServletRequest, HttpServletResponse)}.
     * when the {@link HttpServletRequest} does contain a parameter with key {@link EidasParameterKeys#RELAY_STATE}
     * and non-empty value but different from the one in the originating eIDAS SAML request.
     *
     * Must ignore the value of parameter relay {@link EidasParameterKeys#RELAY_STATE}
     * and set the relay state in the stored originating eIDAS SAML request.
     *
     */
    @Test
    public void relayStateInStoredEidasSamlRequestSetInLightResponse() throws Exception {
        mockProperties.setProperty(EidasParameterKeys.EIDAS_CONNECTOR_ACTIVE.toString(), Boolean.TRUE.toString());
        mockConnectorService();

        final String eidasSamlRequestRelayState = "eidasSamlRequestRelayState";

        mockAuthenticationResponse(true, eidasSamlRequestRelayState);
        BinaryLightToken binaryLightToken =
                BinaryLightTokenHelper.createBinaryLightToken("CA", "test", "SHA-256");
        mockSpecificCommunicationService(binaryLightToken);

        ServletContext mockServletContext = mockServletContext();
        RequestDispatcher mockDispatcher = mock(RequestDispatcher.class);
        when(mockServletContext.getRequestDispatcher(any())).thenReturn(mockDispatcher);

        final String eidasSamlResponseRelayState = "eidasSamlResponseRelayState";
        Assert.assertNotEquals(eidasSamlRequestRelayState, eidasSamlResponseRelayState);

        HttpServletRequest mockRequest = createMockRequestWithSamlResponseAndRelayState(eidasSamlResponseRelayState);
        colleagueResponseServlet.doPost(mockRequest, new MockHttpServletResponse());

        ArgumentCaptor<ILightResponse> argument = ArgumentCaptor.forClass(ILightResponse.class);
        Mockito.verify(mockSpecificCommunicationService).putResponse(argument.capture());
        String actualRelayState = argument.getValue().getRelayState();
        String expectedRelayState = eidasSamlRequestRelayState;
        assertEquals(expectedRelayState, actualRelayState);

        verify(mockDispatcher).forward(isA(ServletRequest.class), isA(ServletResponse.class));
    }

    /**
     * Test method for {@link ColleagueResponseServlet#doPost(HttpServletRequest, HttpServletResponse)}.
     * when the the original Light Request contains
     * a empty value but different from the one in the originating eIDAS SAML request.
     *
     * Must ignore the value of parameter relay {@link EidasParameterKeys#RELAY_STATE}
     * and set the relay state in the stored originating eIDAS SAML request.
     *
     */
    @Test
    public void relayStateInStoredEidasSamlRequestNullSetInLightResponse() throws Exception {
        mockProperties.setProperty(EidasParameterKeys.EIDAS_CONNECTOR_ACTIVE.toString(), Boolean.TRUE.toString());
        mockConnectorService();

        final String eidasSamlRequestRelayState = null;

        mockAuthenticationResponse(true, eidasSamlRequestRelayState);
        BinaryLightToken binaryLightToken =
                BinaryLightTokenHelper.createBinaryLightToken("CA", "test", "SHA-256");
        mockSpecificCommunicationService(binaryLightToken);

        ServletContext mockServletContext = mockServletContext();
        RequestDispatcher mockDispatcher = mock(RequestDispatcher.class);
        when(mockServletContext.getRequestDispatcher(any())).thenReturn(mockDispatcher);

        final String eidasSamlResponseRelayState = "eidasSamlResponseRelayState";
        Assert.assertNotEquals(eidasSamlRequestRelayState, eidasSamlResponseRelayState);

        HttpServletRequest mockRequest = createMockRequestWithSamlResponseAndRelayState(eidasSamlResponseRelayState);
        colleagueResponseServlet.doPost(mockRequest, new MockHttpServletResponse());

        ArgumentCaptor<ILightResponse> argument = ArgumentCaptor.forClass(ILightResponse.class);
        Mockito.verify(mockSpecificCommunicationService).putResponse(argument.capture());
        String actualRelayState = argument.getValue().getRelayState();
        String expectedRelayState = eidasSamlRequestRelayState;
        assertEquals(expectedRelayState, actualRelayState);

        verify(mockDispatcher).forward(isA(ServletRequest.class), isA(ServletResponse.class));
    }

    /**
     * Test method for {@link ColleagueResponseServlet#doPost(HttpServletRequest, HttpServletResponse)}.
     * In this method, we check if the ColleagueResponseServlet is correctly executed when module is active.
     */
    @Test
    public void doPostWithActiveConnectorModule() throws Exception {
        mockProperties.setProperty(EidasParameterKeys.EIDAS_CONNECTOR_ACTIVE.toString(), Boolean.TRUE.toString());
        mockConnectorService();
        mockAuthenticationResponse(true, mockRelayState);
        BinaryLightToken binaryLightToken =
                BinaryLightTokenHelper.createBinaryLightToken("CA", "test", "SHA-256");
        mockSpecificCommunicationService(binaryLightToken);
        ServletContext mockServletContext = mockServletContext();
        RequestDispatcher mockDispatcher = mock(RequestDispatcher.class);
        when(mockServletContext.getRequestDispatcher(any())).thenReturn(mockDispatcher);

        HttpServletRequest mockRequest = createMockRequestSamlResponseOnly();
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        colleagueResponseServlet.doPost(mockRequest, httpServletResponse);

        verify(mockDispatcher).forward(isA(ServletRequest.class), isA(ServletResponse.class));
    }

    /**
    * Test method for {@link ColleagueResponseServlet#doPost(HttpServletRequest, HttpServletResponse)}.
    * In this method, we check if we correctly return an error because of connector module inactivation.
    */
    @Test(expected = EidasNodeException.class)
    public void doPostWithInactiveConnectorModule() throws ServletException {
        mockProperties.setProperty(EidasParameterKeys.EIDAS_CONNECTOR_ACTIVE.toString(), Boolean.FALSE.toString());

        HttpServletRequest mockRequest = createMockRequestSamlResponseOnly();
        colleagueResponseServlet.doPost(mockRequest, new MockHttpServletResponse());
    }

    @Nonnull
    private HttpServletRequest createMockRequestWithSamlResponseAndRelayState(String mockRelayState) {
        MockHttpServletRequest mockRequest = createMockHttpServletRequest();
        mockRequest.setParameter(EidasParameterKeys.RELAY_STATE.toString(), mockRelayState);

        return mockRequest;
    }

    @Nonnull
    private HttpServletRequest createMockRequestSamlResponseOnly() {
        MockHttpServletRequest mockRequest = createMockHttpServletRequest();
        return mockRequest;
    }

    @Nonnull
    private MockHttpServletRequest createMockHttpServletRequest() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.POST.toString(), null);
        mockRequest.setParameter(EidasParameterKeys.SAML_RESPONSE.toString(), "Fake response");
        return mockRequest;
    }

}