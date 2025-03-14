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

package eu.eidas.node.connector;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.ConnectorBeanNames;
import eu.eidas.node.connector.exceptions.ConnectorError;
import eu.eidas.node.utils.PropertiesUtil;
import eu.eidas.node.utils.ReflectionUtils;
import eu.eidas.security.ConfigurationSecurityBean;
import eu.eidas.security.ExtendedServletResponseWrapper;
import eu.eidas.security.header.SecurityResponseHeaderHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

/**
 * Test class for {@link ConnectorErrorServlet}
 */
public class ConnectorErrorServletTest {

    private static final String JAVAX_SERVLET_ERROR_EXCEPTION = "javax.servlet.error.exception";

    private ApplicationContext oldContext = null;
    private ConnectorErrorServlet connectorErrorServlet;

    private WebApplicationContext mockApplicationContext = Mockito.mock(WebApplicationContext.class);
    private HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
    private HttpServletResponse mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);

    private MockedStatic<ContextLoader> mockContextLoader;

    @Before
    public void setUp() throws Exception {
        mockContextLoader = Mockito.mockStatic(ContextLoader.class);
        mockContextLoader.when(ContextLoader::getCurrentWebApplicationContext).thenReturn(mockApplicationContext);
        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        oldContext = (ApplicationContext) contextField.get(null);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);


        Map<String, String> mockMap = mock(Map.class);
        ReflectionTestUtils.setField(PropertiesUtil.class, "propertiesMap", mockMap);

        mockConfigurationSecurityBean();
        connectorErrorServlet = new ConnectorErrorServlet();
        mockResourceBundleMessageSource();
    }

    @After
    public void tearDown(){
        mockContextLoader.close();
        if (oldContext != null) {
            ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", oldContext);
            oldContext = null;
        }
    }

    /**
     * Test method for
     * {@link ConnectorErrorServlet#doPost(HttpServletRequest, HttpServletResponse)}
     * <p>
     * Must succeed
     */
    @Test
    public void testDoPost() throws Exception {
        String errorCode = EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorCode());
        String errorMessage = EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorMessage());
        final ConnectorError connectorError = new ConnectorError(errorCode, errorMessage);

        Mockito.when(mockHttpServletRequest.getAttribute(JAVAX_SERVLET_ERROR_EXCEPTION)).thenReturn(connectorError);

        mockRequestDispatcher();

        connectorErrorServlet.doPost(mockHttpServletRequest, mockHttpServletResponse);
    }

    /**
     * Test method for
     * {@link ConnectorErrorServlet#doGet(HttpServletRequest, HttpServletResponse)}
     * <p>
     * Must succeed
     */
    @Test
    public void testDoGet() throws Exception {
        String errorCode = EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorCode());
        String errorMessage = EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorMessage());
        final ConnectorError connectorError = new ConnectorError(errorCode, errorMessage);

        mockRequestDispatcher();

        Mockito.when(mockHttpServletRequest.getAttribute(JAVAX_SERVLET_ERROR_EXCEPTION)).thenReturn(connectorError);

        connectorErrorServlet.doGet(mockHttpServletRequest, mockHttpServletResponse);
    }

    /**
     * Test method for
     * {@link ConnectorErrorServlet#populateResponseHeader(HttpServletRequest, HttpServletResponse)}
     * <p>
     * Must succeed
     */
    @Test
    public void testPopulateResponseHeader() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method populateResponseHeader = ConnectorErrorServlet.class.getDeclaredMethod("populateResponseHeader", SecurityResponseHeaderHelper.class,
                HttpServletRequest.class, HttpServletResponse.class);
        populateResponseHeader.setAccessible(true);

        final SecurityResponseHeaderHelper mockSecurityResponseHeaderHelper = Mockito.mock(SecurityResponseHeaderHelper.class);
        final ExtendedServletResponseWrapper extendedServletResponseWrapper = new ExtendedServletResponseWrapper(mockHttpServletResponse);
        populateResponseHeader.invoke(connectorErrorServlet, mockSecurityResponseHeaderHelper, mockHttpServletRequest, extendedServletResponseWrapper);
    }

    /**
     * Test method for
     * {@link ConnectorErrorServlet#populateResponseHeader(HttpServletRequest, HttpServletResponse)}
     * when {@link ServletException} is caught by the catch block
     */
    @Test
    public void testPopulateResponseHeaderWhenServletExceptionIsCaught() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ServletException {
        final Method populateResponseHeader = ConnectorErrorServlet.class.getDeclaredMethod("populateResponseHeader", SecurityResponseHeaderHelper.class,
                HttpServletRequest.class, HttpServletResponse.class);
        populateResponseHeader.setAccessible(true);

        final SecurityResponseHeaderHelper mockSecurityResponseHeaderHelper = Mockito.mock(SecurityResponseHeaderHelper.class);
        Mockito.doThrow(ServletException.class).when(mockSecurityResponseHeaderHelper).populateResponseHeader(any(), any());

        final ExtendedServletResponseWrapper extendedServletResponseWrapper = new ExtendedServletResponseWrapper(mockHttpServletResponse);
        populateResponseHeader.invoke(connectorErrorServlet, mockSecurityResponseHeaderHelper, mockHttpServletRequest, extendedServletResponseWrapper);
    }

    /**
     * Test method for
     * {@link ConnectorErrorServlet#getLogger()}
     * <p>
     * Must succeed
     */
    @Test
    public void testGetLogger() {
        connectorErrorServlet.getLogger();
    }

    private void mockRequestDispatcher() throws Exception {
        final ServletContext mockServletContext = mockServletContext();
        final RequestDispatcher mockRequestDispatcher = mock(RequestDispatcher.class);

        Mockito.when(mockServletContext.getRequestDispatcher(any())).thenReturn(mockRequestDispatcher);
    }

    private ServletContext mockServletContext() throws Exception {
        final ServletContext servletContext = Mockito.mock(ServletContext.class);
        final ServletConfig mockServletConfig = Mockito.mock(ServletConfig.class);

        Mockito.when(mockServletConfig.getServletContext()).thenReturn(servletContext);
        connectorErrorServlet.init(mockServletConfig);

        return servletContext;
    }

    private void mockConfigurationSecurityBean() {
        final ConfigurationSecurityBean mockConfigurationSecurityBean = Mockito.mock(ConfigurationSecurityBean.class);
        Mockito.when(mockApplicationContext.getBean(ConnectorBeanNames.SECURITY_CONFIG.toString())).thenReturn(mockConfigurationSecurityBean);
        Mockito.when(mockApplicationContext.getBean(any(Class.class))).thenReturn(mockConfigurationSecurityBean);
    }

    private void mockResourceBundleMessageSource() {
        final ResourceBundleMessageSource mockResourceBundleMessageSource = Mockito.mock(ResourceBundleMessageSource.class);
        Mockito.when(mockApplicationContext.getBean(ConnectorBeanNames.SYSADMIN_MESSAGE_RESOURCES.toString())).thenReturn(mockResourceBundleMessageSource);
    }
}
