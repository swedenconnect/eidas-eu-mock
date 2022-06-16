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
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.utils.ReflectionUtils;
import eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.impl.SpecificConnectorCommunicationServiceImpl;
import eu.eidas.specificcommunication.protocol.validation.IncomingLightRequestValidatorLoAComponent;
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
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Test class for the {@link SpecificConnectorRequestServlet}
 */
@RunWith(MockitoJUnitRunner.class)
public class SpecificConnectorRequestServletTest {

    private HttpServletRequest mockHttpServletRequest;
    private SpecificConnectorRequestServlet specificConnectorRequestServlet;
    private SpecificConnectorCommunicationServiceImpl mockSpecificConnectorCommunicationService;
    private ApplicationContext oldContext = null;

    @Mock
    private ApplicationContext mockApplicationContext;

    @Mock
    private Collection mockCollection;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        oldContext = (ApplicationContext) contextField.get(null);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
        mockSpecificConnectorCommunicationService();

        mockHttpServletRequest = createMockHttpServletRequest();
        specificConnectorRequestServlet = new SpecificConnectorRequestServlet();
    }

    @After
    public void tearDown(){
        if (oldContext!=null){
            ReflectionUtils.setStaticField(BeanProvider.class,"CONTEXT",oldContext);
            oldContext = null;
        }
    }

    /**
     * Test method for
     * {@link SpecificConnectorRequestServlet#getiLightRequest(HttpServletRequest, Collection)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetLightRequest() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, SpecificCommunicationException {
        final Method getLightRequestMethod = getLightRequestMethod();
        final ILightRequest mockLightRequest = Mockito.mock(ILightRequest.class);

        Mockito.when(mockSpecificConnectorCommunicationService.getAndRemoveRequest(anyString(), any())).thenReturn(mockLightRequest);

        getLightRequestMethod.invoke(specificConnectorRequestServlet, mockHttpServletRequest, mockCollection);
    }

    /**
     * Test method for
     * {@link SpecificConnectorRequestServlet#getiLightRequest(HttpServletRequest, Collection)}
     * when {@link ILightRequest} is null
     * <p>
     * Must fail.
     */
    @Test
    public void testGetLightRequestWithLightRequestNull() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(ServletException.class));

        final Method getLightRequestMethod = getLightRequestMethod();

        getLightRequestMethod.invoke(specificConnectorRequestServlet, mockHttpServletRequest, mockCollection);
    }

    /**
     * Test method for
     * {@link SpecificConnectorRequestServlet#getiLightRequest(HttpServletRequest, Collection)}
     * when {@link IllegalArgumentException} is thrown inside {@link SpecificConnectorRequestServlet#getiLightRequest(HttpServletRequest, Collection)}
     * <p>
     * Must fail.
     */
    @Test
    public void testGetLightRequestWhenIllegalArgumentExceptionIsCaught() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, SpecificCommunicationException {
        expectedException.expectCause(isA(InvalidParameterEIDASException.class));

        final Method getLightRequestMethod = getLightRequestMethod();

        Mockito.doThrow(IllegalArgumentException.class).when(mockSpecificConnectorCommunicationService).getAndRemoveRequest(anyString(), any());

        getLightRequestMethod.invoke(specificConnectorRequestServlet, mockHttpServletRequest, mockCollection);
    }

    /**
     * Test method for
     * {@link SpecificConnectorRequestServlet#getiLightRequest(HttpServletRequest, Collection)}
     * when {@link SpecificCommunicationException} is thrown inside {@link SpecificConnectorRequestServlet#getiLightRequest(HttpServletRequest, Collection)}
     * the errorMessage contains {@link IncomingLightRequestValidatorLoAComponent#ERROR_LIGHT_REQUEST_BASE} constant value from {@link IncomingLightRequestValidatorLoAComponent}
     * <p>
     * Must fail.
     */
    @Test
    public void testGetLightRequestWhenSpecificCommunicationExceptionIsCaught() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, SpecificCommunicationException {
        expectedException.expectCause(isA(InvalidParameterEIDASException.class));

        final Method getLightRequestMethod = getLightRequestMethod();
        final String errorMessage = IncomingLightRequestValidatorLoAComponent.ERROR_LIGHT_REQUEST_BASE;

        Mockito.doThrow(new SpecificCommunicationException(errorMessage)).when(mockSpecificConnectorCommunicationService).getAndRemoveRequest(anyString(), any());

        getLightRequestMethod.invoke(specificConnectorRequestServlet, mockHttpServletRequest, mockCollection);
    }

    private Method getLightRequestMethod() throws NoSuchMethodException {
        final Method getLightRequestMethod = SpecificConnectorRequestServlet.class.getDeclaredMethod("getiLightRequest", HttpServletRequest.class, Collection.class);
        getLightRequestMethod.setAccessible(true);

        return getLightRequestMethod;
    }

    private void mockSpecificConnectorCommunicationService() {
        final String beanName = SpecificCommunicationDefinitionBeanNames.SPECIFIC_CONNECTOR_COMMUNICATION_SERVICE.toString();
        mockSpecificConnectorCommunicationService = Mockito.mock(SpecificConnectorCommunicationServiceImpl.class);
        Mockito.when(mockApplicationContext.getBean(beanName)).thenReturn(mockSpecificConnectorCommunicationService);
    }

    private HttpServletRequest createMockHttpServletRequest() {
        final MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest(HttpMethod.POST.toString(), null);
        mockHttpServletRequest.setParameter(EidasParameterKeys.LIGHT_REQUEST.toString(), "Fake request");
        mockHttpServletRequest.setParameter(EidasParameterKeys.TOKEN.toString(), "fakeToken");

        return mockHttpServletRequest;
    }

}