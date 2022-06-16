/*
 * Copyright (c) 2021 by European Commission
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

package eu.eidas.node.connector;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.auth.connector.ICONNECTORSAMLService;
import eu.eidas.node.auth.connector.ICONNECTORService;
import eu.eidas.node.utils.EidasNodeMetadataGenerator;
import eu.eidas.node.utils.ReflectionUtils;
import org.junit.After;
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
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * Tests for the {@link ConnectorMetadataGeneratorServlet}.
 *
 * @since 2.4
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectorMetadataGeneratorServletTest {

    private ConnectorMetadataGeneratorServlet connectorMetadataGeneratorServlet;

    @Mock
    private ApplicationContext mockApplicationContext;
    @Mock
    private AUCONNECTORUtil mockAuthConnectorUtil;
    @Mock
    private EidasNodeMetadataGenerator metadataGenerator;
    @Mock
    private Properties mockProperties;
    private ApplicationContext oldContext;

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Before
    public void setupContext() throws NoSuchFieldException, IllegalAccessException {
        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        oldContext = (ApplicationContext) contextField.get(null);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);

        Mockito.when(mockAuthConnectorUtil.getConfigs()).thenReturn(mockProperties);

        Mockito.when(mockApplicationContext.getBean(AUCONNECTORUtil.class))
                .thenReturn(mockAuthConnectorUtil);

        String generatorName = NodeBeanNames.CONNECTOR_METADATA_GENERATOR.toString();
        Mockito.when(mockApplicationContext.getBean(generatorName))
                .thenReturn(metadataGenerator);

        connectorMetadataGeneratorServlet = new ConnectorMetadataGeneratorServlet();
    }

    @After
    public void tearDown(){
        if (oldContext!=null){
            ReflectionUtils.setStaticField(BeanProvider.class,"CONTEXT",oldContext);
            oldContext = null;
        }
    }

    private ProtocolEngineI initConnectorControllerMock() {
        String beanName = NodeBeanNames.EIDAS_CONNECTOR_CONTROLLER.toString();
        ConnectorControllerService controllerService = Mockito.mock(ConnectorControllerService.class);
        Mockito.when(mockApplicationContext.getBean(beanName))
                .thenReturn(controllerService);

        ICONNECTORService connectorService = Mockito.mock(ICONNECTORService.class);
        Mockito.when(controllerService.getConnectorService()).thenReturn(connectorService);

        ICONNECTORSAMLService samlService = Mockito.mock(ICONNECTORSAMLService.class);
        Mockito.when(connectorService.getSamlService()).thenReturn(samlService);

        ProtocolEngineI samlEngine = Mockito.mock(ProtocolEngineI.class);
        Mockito.when(samlService.getSamlEngine()).thenReturn(samlEngine);

        return samlEngine;
    }

    /**
     * Test method for {@link ConnectorMetadataGeneratorServlet#doGet(HttpServletRequest, HttpServletResponse)}
     * When the Connector is not active
     * <p>
     * Must throw an EidasNodeException.
     */
    @Test
    public void testMetadataGenerationRequestWhenConnectorNotActive() throws Exception {
        thrown.expect(EidasNodeException.class);

        Mockito.when(mockProperties.getProperty(EidasParameterKeys.EIDAS_CONNECTOR_ACTIVE.toString()))
                .thenReturn("false");

        HttpServletRequest httpServletRequest = mockHttpServletRequest();
        HttpServletResponse httpServletResponse = mockHttpServletResponse();

        connectorMetadataGeneratorServlet.doGet(httpServletRequest, httpServletResponse);
    }

    /**
     * Test method for {@link ConnectorMetadataGeneratorServlet#doGet(HttpServletRequest, HttpServletResponse)}
     * When the Connector metadata is not active
     * <p>
     * Must send a response error
     */
    @Test
    public void testMetadataGenerationRequestWhenConnectorMetadataNotActive() throws Exception {
        Mockito.when(mockProperties.getProperty(EidasParameterKeys.EIDAS_CONNECTOR_ACTIVE.toString()))
                .thenReturn("true");
        Mockito.when(mockProperties.getProperty(EidasParameterKeys.METADATA_ACTIVE.toString()))
                .thenReturn("false");

        HttpServletRequest httpServletRequest = mockHttpServletRequest();
        HttpServletResponse httpServletResponse = mockHttpServletResponse();

        connectorMetadataGeneratorServlet.doGet(httpServletRequest, httpServletResponse);

        Mockito.verify(httpServletResponse, Mockito.times(1))
                .sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Test method for {@link ConnectorMetadataGeneratorServlet#doGet(HttpServletRequest, HttpServletResponse)}
     * When the Connector is active
     * And connector metadata is active
     * <p>
     * Must succeed
     */
    @Test
    public void testMetadataGenerationRequest() throws Exception {
        Mockito.when(mockProperties.getProperty(EidasParameterKeys.EIDAS_CONNECTOR_ACTIVE.toString()))
                .thenReturn("true");
        Mockito.when(mockProperties.getProperty(EidasParameterKeys.METADATA_ACTIVE.toString()))
                .thenReturn("true");

        String expectedResult = "RESULT OF THE TEST";
        ProtocolEngineI mockedProtocolEngine = initConnectorControllerMock();
        Mockito.when(metadataGenerator.generateConnectorMetadata(mockedProtocolEngine))
                .thenReturn(expectedResult);

        HttpServletRequest httpServletRequest = mockHttpServletRequest();
        HttpServletResponse httpServletResponse = mockHttpServletResponse();
        PrintWriter mockWriter = Mockito.mock(PrintWriter.class);
        Mockito.when(httpServletResponse.getWriter()).thenReturn(mockWriter);

        connectorMetadataGeneratorServlet.doGet(httpServletRequest, httpServletResponse);

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
