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

package eu.eidas.node.service;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.auth.service.AUSERVICEUtil;
import eu.eidas.node.auth.service.ISERVICESAMLService;
import eu.eidas.node.auth.service.ISERVICEService;
import eu.eidas.node.utils.EidasNodeMetadataGenerator;
import eu.eidas.node.utils.ReflectionUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * Tests for the {@link ProxyServiceMetadataGeneratorServlet}.
 *
 * @since 2.4
 */
@RunWith(MockitoJUnitRunner.class)
public class ProxyServiceMetadataGeneratorServletTest {

    private ProxyServiceMetadataGeneratorServlet proxyServiceMetadataGeneratorServlet;

    @Mock
    private ApplicationContext mockApplicationContext;
    @Mock
    private AUSERVICEUtil mockAuthServiceUtil;
    @Mock
    private EidasNodeMetadataGenerator metadataGenerator;
    @Mock
    private Properties mockProperties;

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Before
    public void setupContext() {
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);

        Mockito.when(mockAuthServiceUtil.getConfigs()).thenReturn(mockProperties);
        Mockito.when(mockApplicationContext.getBean(AUSERVICEUtil.class))
                .thenReturn(mockAuthServiceUtil);

        String generatorName = NodeBeanNames.SERVICE_METADATA_GENERATOR.toString();
        Mockito.when(mockApplicationContext.getBean(generatorName))
                .thenReturn(metadataGenerator);

        proxyServiceMetadataGeneratorServlet = new ProxyServiceMetadataGeneratorServlet();
    }

    private ProtocolEngineI initServiceControllerMock() {
        String beanName = NodeBeanNames.EIDAS_SERVICE_CONTROLLER.toString();
        ServiceControllerService controllerService = Mockito.mock(ServiceControllerService.class);
        Mockito.when(mockApplicationContext.getBean(beanName))
                .thenReturn(controllerService);

        ISERVICEService proxyService = Mockito.mock(ISERVICEService.class);
        Mockito.when(controllerService.getProxyService()).thenReturn(proxyService);

        ISERVICESAMLService samlService = Mockito.mock(ISERVICESAMLService.class);
        Mockito.when(proxyService.getSamlService()).thenReturn(samlService);

        ProtocolEngineI samlEngine = Mockito.mock(ProtocolEngineI.class);
        Mockito.when(samlService.getSamlEngine()).thenReturn(samlEngine);

        return samlEngine;
    }

    /**
     * Test method for {@link ProxyServiceMetadataGeneratorServlet#doGet(HttpServletRequest, HttpServletResponse)}
     * When the ProxyService is not active
     * <p>
     * Must throw an EidasNodeException.
     */
    @Test
    public void testMetadataGenerationRequestWhenProxyServiceNotActive() throws Exception {
        thrown.expect(EidasNodeException.class);

        Mockito.when(mockProperties.getProperty(EidasParameterKeys.EIDAS_SERVICE_ACTIVE.toString()))
                .thenReturn("false");

        HttpServletRequest httpServletRequest = mockHttpServletRequest();
        HttpServletResponse httpServletResponse = mockHttpServletResponse();

        proxyServiceMetadataGeneratorServlet.doGet(httpServletRequest, httpServletResponse);
    }

    /**
     * Test method for {@link ProxyServiceMetadataGeneratorServlet#doGet(HttpServletRequest, HttpServletResponse)}
     * When the ProxyService metadata is not active
     * <p>
     * Must send a response error.
     */
    @Test
    public void testMetadataGenerationRequestWhenProxyServiceMetadataNotActive() throws Exception {
        Mockito.when(mockProperties.getProperty(EidasParameterKeys.EIDAS_SERVICE_ACTIVE.toString()))
                .thenReturn("true");
        Mockito.when(mockProperties.getProperty(EidasParameterKeys.METADATA_ACTIVE.toString()))
                .thenReturn("false");

        HttpServletRequest httpServletRequest = mockHttpServletRequest();
        HttpServletResponse httpServletResponse = mockHttpServletResponse();

        proxyServiceMetadataGeneratorServlet.doGet(httpServletRequest, httpServletResponse);

        Mockito.verify(httpServletResponse, Mockito.times(1))
                .sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Test method for {@link ProxyServiceMetadataGeneratorServlet#doGet(HttpServletRequest, HttpServletResponse)}
     * When the ProxyService is active
     * And metadata is active
     * <p>
     * Must succeed
     */
    @Test
    public void testMetadataGenerationRequest() throws Exception {
        Mockito.when(mockProperties.getProperty(EidasParameterKeys.EIDAS_SERVICE_ACTIVE.toString()))
                .thenReturn("true");
        Mockito.when(mockProperties.getProperty(EidasParameterKeys.METADATA_ACTIVE.toString()))
                .thenReturn("true");

        String expectedResult = "RESULT OF THE TEST";
        ProtocolEngineI mockedProtocolEngine = initServiceControllerMock();
        Mockito.when(metadataGenerator.generateProxyServiceMetadata(mockedProtocolEngine))
                .thenReturn(expectedResult);

        HttpServletRequest httpServletRequest = mockHttpServletRequest();
        HttpServletResponse httpServletResponse = mockHttpServletResponse();
        PrintWriter mockWriter = Mockito.mock(PrintWriter.class);
        Mockito.when(httpServletResponse.getWriter()).thenReturn(mockWriter);

        proxyServiceMetadataGeneratorServlet.doGet(httpServletRequest, httpServletResponse);

        Mockito.verify(httpServletResponse, Mockito.times(1))
                .setContentType("text/xml");
        Mockito.verify(httpServletResponse, Mockito.times(1))
                .setCharacterEncoding("UTF-8");
        Mockito.verify(mockWriter, Mockito.times(1))
                .print(expectedResult);
    }

    private HttpServletRequest mockHttpServletRequest() {
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        return httpServletRequest;
    }

    private HttpServletResponse mockHttpServletResponse() {
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        return httpServletResponse;
    }

}
