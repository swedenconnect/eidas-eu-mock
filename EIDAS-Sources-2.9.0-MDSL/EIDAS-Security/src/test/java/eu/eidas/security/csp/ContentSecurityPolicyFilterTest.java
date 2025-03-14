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
package eu.eidas.security.csp;

import eu.eidas.security.ConfigurationSecurityBean;
import eu.eidas.security.ReflectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentSecurityPolicyFilterTest {

    private HttpServletRequest mockHttpServletRequest;
    private HttpServletResponse mockHttpServletResponse;
    private FilterChain mockFilterChain;
    private ContentSecurityPolicyFilter contentSecurityPolicyFilter = new ContentSecurityPolicyFilter();
    private ApplicationContext oldContext;
    private WebApplicationContext mockApplicationContext;
    private ConfigurationSecurityBean configurationSecurityBean;
    private MockedStatic<ContextLoader> mockContextLoader;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        mockHttpServletRequest = mock(HttpServletRequest.class);
        mockHttpServletResponse = mock(HttpServletResponse.class);
        mockFilterChain = mock(FilterChain.class);

        mockApplicationContext = mock(WebApplicationContext.class);
        configurationSecurityBean = new ConfigurationSecurityBean();

        mockContextLoader = Mockito.mockStatic(ContextLoader.class);
        mockContextLoader.when(ContextLoader::getCurrentWebApplicationContext).thenReturn(mockApplicationContext);
        when(mockApplicationContext.getBean(ConfigurationSecurityBean.class)).thenReturn(configurationSecurityBean);
    }

    @After
    public void tearDown(){
        mockContextLoader.close();
    }

    @Test
    public void doFilterTest() throws ServletException, IOException {
        contentSecurityPolicyFilter.doFilter(mockHttpServletRequest,mockHttpServletResponse,mockFilterChain);
        Mockito.verify(mockFilterChain).doFilter(any(),any());
    }

    @Test
    public void doFilterTestIOException() throws ServletException, IOException {
        expectedException.expect(ServletException.class);

        Mockito.doThrow(IOException.class).when(mockFilterChain).doFilter(any(),any());
        contentSecurityPolicyFilter.doFilter(mockHttpServletRequest,mockHttpServletResponse,mockFilterChain);
    }
}