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

package eu.eidas.security.header;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.security.ConfigurationSecurityBean;
import eu.eidas.security.ReflectionUtils;
import eu.eidas.security.SecurityRequestFilter;
import eu.eidas.security.exceptions.SecurityError;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Tests for {@link SecurityRequestFilter}.
 */
public class SecurityRequestFilterTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ConfigurationSecurityBean mockConfigurationSecurityBean;
    private ServletResponse servletResponse;
    private FilterChain filterChain;
    private ApplicationContext oldContext = null;

    public final String SP_URL = EidasParameterKeys.SP_URL.toString();

    private MockedStatic<ContextLoader> mockContextLoader;


    @Before
    public void setUp() throws Exception {
        final WebApplicationContext mockApplicationContext = Mockito.mock(WebApplicationContext.class);
        mockContextLoader = Mockito.mockStatic(ContextLoader.class);
        mockContextLoader.when(ContextLoader::getCurrentWebApplicationContext).thenReturn(mockApplicationContext);

        mockConfigurationSecurityBean = Mockito.mock(ConfigurationSecurityBean.class);
        Mockito.when(mockConfigurationSecurityBean.getBypassValidation())
                .thenReturn(false);
        Mockito.when(mockConfigurationSecurityBean.getTrustedDomains())
                .thenReturn("all");
        Mockito.when(mockConfigurationSecurityBean.getIpMaxTime())
                .thenReturn(1000);
        Mockito.when(mockConfigurationSecurityBean.getIpMaxRequests())
                .thenReturn(3);
        Mockito.when(mockConfigurationSecurityBean.getSpMaxTime())
                .thenReturn(1000);
        Mockito.when(mockConfigurationSecurityBean.getSpMaxRequests())
                .thenReturn(3);
        Mockito.when(mockApplicationContext.getBean(ConfigurationSecurityBean.class))
                .thenReturn(mockConfigurationSecurityBean);

        servletResponse = Mockito.mock(ServletResponse.class);
        filterChain = Mockito.mock(FilterChain.class);
    }

    @After
    public void tearDown(){
        mockContextLoader.close();
    }

    /**
     * Test method for
     * {@link SecurityRequestFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * when the servlet is not part of the included servlets
     * then the {@link SecurityRequestFilter} takes no further actions
     * <p>
     * Must succeed.
     */
    @Test
    public void doFilterServletNotFiltered() throws ServletException, IOException {
        final FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getInitParameter(anyString()))
                .thenReturn("ServletA,ServletB,ServletC");
        final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getServletPath())
                .thenReturn("/ServletD");
        final SecurityRequestFilter securityRequestFilter = new SecurityRequestFilter();
        securityRequestFilter.init(mockFilterConfig);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);

        Mockito.verify(filterChain).doFilter(any(), any());
        Mockito.verify(servletRequest, Mockito.never()).getHeader(anyString());
    }

    /**
     * Test method for
     * {@link SecurityRequestFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * when the servlet is part of the included servlets
     * but {@link ConfigurationSecurityBean#getBypassValidation()} returns true
     * then the {@link SecurityRequestFilter} takes no further actions
     * <p>
     * Must succeed.
     */
    @Test
    public void doFilterBypassValidation() throws ServletException, IOException {
        final FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getInitParameter(anyString()))
                .thenReturn("ServletA,ServletB,ServletC");
        Mockito.when(mockConfigurationSecurityBean.getBypassValidation())
                .thenReturn(true);
        final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getServletPath())
                .thenReturn("/ServletA");
        Mockito.when(servletRequest.getHeader(EIDASValues.REFERER.toString()))
                .thenReturn("http://eidas.eu");
        final SecurityRequestFilter securityRequestFilter = new SecurityRequestFilter();
        securityRequestFilter.init(mockFilterConfig);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);

        Mockito.verify(filterChain).doFilter(any(), any());
        Mockito.verify(servletRequest, Mockito.never()).getParameterMap();
    }

    /**
     * Test method for
     * {@link SecurityRequestFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * when the servlet is part of the included servlets but the servlet is the cspReportHandler
     * then the {@link SecurityRequestFilter} takes no further actions
     * <p>
     * Must succeed.
     */
    @Test
    public void doFilterBypassCspReportHandlerServlet() throws ServletException, IOException {
        final FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getInitParameter(anyString()))
                .thenReturn("ServletA,ServletB,ServletC,cspReportHandler");
        final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getServletPath())
                .thenReturn("/cspReportHandler");
        Mockito.when(servletRequest.getHeader(EIDASValues.REFERER.toString()))
                .thenReturn("http://eidas.eu");
        final SecurityRequestFilter securityRequestFilter = new SecurityRequestFilter();
        securityRequestFilter.init(mockFilterConfig);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);

        Mockito.verify(filterChain).doFilter(any(), any());
        Mockito.verify(servletRequest, Mockito.never()).getParameterMap();
    }

    /**
     * Test method for
     * {@link SecurityRequestFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * when the {@link HttpServletRequest} does not contain a domain in the REFERER header
     * <p>
     * Must fail
     * and throw {@link SecurityError}
     */
    @Test
    public void doFilterDomainIsNull() throws ServletException, IOException {
        exception.expect(SecurityError.class);
        exception.expectMessage("invalid.sp.domain");

        final FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getInitParameter(anyString()))
                .thenReturn("ServletA,ServletB,ServiceProvider");
        final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getServletPath())
                .thenReturn("/ServiceProvider");
        Mockito.when(servletRequest.getHeader(EIDASValues.REFERER.toString()))
                .thenReturn(null);

        final SecurityRequestFilter securityRequestFilter = new SecurityRequestFilter();
        securityRequestFilter.init(mockFilterConfig);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
    }

    /**
     * Test method for
     * {@link SecurityRequestFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * when the Request has a REFERER header that matches the hostname of the SP_URL in the ParameterMap
     * <p>
     * Must succeed.
     */
    @Test
    public void doFilterDomain() throws ServletException, IOException {
        disableIpRateLimitCheck();
        final FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getInitParameter(anyString()))
                .thenReturn("ServletA,ServletB,ServiceProvider");
        final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getServletPath())
                .thenReturn("/ServiceProvider");
        Mockito.when(servletRequest.getHeader(EIDASValues.REFERER.toString()))
                .thenReturn("http://eidas.eu");
        Mockito.when(servletRequest.getParameterMap())
                .thenReturn(Collections.singletonMap(SP_URL, new String[]{"https://eidas.eu/something"}));
        Mockito.when(servletRequest.getMethod())
                .thenReturn(HttpMethod.POST.toString());

        final SecurityRequestFilter securityRequestFilter = new SecurityRequestFilter();
        securityRequestFilter.init(mockFilterConfig);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
    }

    /**
     * Test method for
     * {@link SecurityRequestFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * when the Request has a REFERER header that matches the hostname of the SP_URL in the ParameterMap
     * removing the tailing "/" before matching
     * <p>
     * Must succeed.
     */
    @Test
    public void doFilterDomainRemoveTailingSlash() throws ServletException, IOException {
        disableIpRateLimitCheck();
        final FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getInitParameter(anyString()))
                .thenReturn("ServletA,ServletB,ServiceProvider");
        final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getServletPath())
                .thenReturn("/ServiceProvider");
        Mockito.when(servletRequest.getHeader(EIDASValues.REFERER.toString()))
                .thenReturn("http://eidas.eu/");
        Mockito.when(servletRequest.getParameterMap())
                .thenReturn(Collections.singletonMap(SP_URL, new String[]{"https://eidas.eu/something"}));
        Mockito.when(servletRequest.getMethod())
                .thenReturn(HttpMethod.POST.toString());

        final SecurityRequestFilter securityRequestFilter = new SecurityRequestFilter();
        securityRequestFilter.init(mockFilterConfig);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
    }

    /**
     * Test method for
     * {@link SecurityRequestFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * when the Request has a REFERER header that does not match the hostname of the SP_URL in the ParameterMap
     * <p>
     * Must fail
     * and throw {@link SecurityError}
     */
    @Test
    public void doFilterDomainInvalidSpUrl() throws ServletException, IOException {
        exception.expect(SecurityError.class);
        exception.expectMessage("invalid.sp.domain");

        disableIpRateLimitCheck();
        final FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getInitParameter(anyString()))
                .thenReturn("ServletA,ServletB,ServiceProvider");
        final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getServletPath())
                .thenReturn("/ServiceProvider");
        Mockito.when(servletRequest.getHeader(EIDASValues.REFERER.toString()))
                .thenReturn("http://eidas.eu}");
        Mockito.when(servletRequest.getParameterMap())
                .thenReturn( Collections.singletonMap(SP_URL, new String[]{"http://3idas/something"}));
        Mockito.when(servletRequest.getMethod())
                .thenReturn(HttpMethod.POST.toString());

        final SecurityRequestFilter securityRequestFilter = new SecurityRequestFilter();
        securityRequestFilter.init(mockFilterConfig);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
    }

    /**
     * Test method for
     * {@link SecurityRequestFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * when a list of trusted domains is defined and the Request has a REFERER header that does not match this list
     * <p>
     * Must fail
     * and throw {@link SecurityError}
     */
    @Test
    public void doFilterDomainTrustedDomains() throws ServletException, IOException {
        exception.expect(SecurityError.class);
        exception.expectMessage("invalid.sp.domain");

        Mockito.when(mockConfigurationSecurityBean.getTrustedDomains())
                .thenReturn("localhost:8080;localhost:8081;cef-eid-build-1:8080;cef-eid-build-1:8081");
        final FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getInitParameter(anyString()))
                .thenReturn("ServletA,ServletB,ServiceProvider");
        final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getServletPath())
                .thenReturn("/ServiceProvider");
        Mockito.when(servletRequest.getHeader(EIDASValues.REFERER.toString()))
                .thenReturn("http://eidas.eu");

        final SecurityRequestFilter securityRequestFilter = new SecurityRequestFilter();
        securityRequestFilter.init(mockFilterConfig);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
    }

    /**
     * Test method for
     * {@link SecurityRequestFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * when a list of trusted domains is not defined and configuration default is set to trust none
     * <p>
     * Must fail
     * and throw {@link SecurityError}.
     */
    @Test
    public void doFilterDomainNoDomains() throws ServletException, IOException {
        exception.expect(SecurityError.class);
        exception.expectMessage("invalid.sp.domain");

        Mockito.when(mockConfigurationSecurityBean.getTrustedDomains())
                .thenReturn("none");
        final FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getInitParameter(anyString()))
                .thenReturn("ServletA,ServletB,ServiceProvider");
        final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getServletPath())
                .thenReturn("/ServiceProvider");
        Mockito.when(servletRequest.getHeader(EIDASValues.REFERER.toString()))
                .thenReturn("http://eidas.eu");

        final SecurityRequestFilter securityRequestFilter = new SecurityRequestFilter();
        securityRequestFilter.init(mockFilterConfig);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
    }

    /**
     * Test method for
     * {@link SecurityRequestFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * when multiple request from an IP address do not exceed the rate limit
     * <p>
     * Must succeed.
     */
    @Test
    public void doFilterRateLimitIP() throws ServletException, IOException {
        final FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getInitParameter(anyString()))
                .thenReturn("ServletA,ServletB,ServiceProvider");
        final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getServletPath())
                .thenReturn("/ServiceProvider");
        Mockito.when(servletRequest.getHeader(EIDASValues.REFERER.toString()))
                .thenReturn("http://eidas.eu");
        Mockito.when(servletRequest.getParameterMap())
                .thenReturn(Collections.singletonMap(SP_URL, new String[]{"https://eidas.eu/something"}));
        Mockito.when(servletRequest.getMethod())
                .thenReturn(HttpMethod.POST.toString());
        Mockito.when(servletRequest.getRemoteAddr())
                .thenReturn("10.0.0.1");

        final SecurityRequestFilter securityRequestFilter = new SecurityRequestFilter();
        securityRequestFilter.init(mockFilterConfig);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
    }

    /**
     * Test method for
     * {@link SecurityRequestFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * when multiple request from an IP address exceeds the rate limit
     * <p>
     * Must fail
     * and throw {@link SecurityError}.
     */
    @Test
    public void doFilterRateLimitIPFourTimes() throws ServletException, IOException {
        exception.expect(SecurityError.class);

        final FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getInitParameter(anyString()))
                .thenReturn("ServletA,ServletB,ServiceProvider");
        final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getServletPath())
                .thenReturn("/ServiceProvider");
        Mockito.when(servletRequest.getHeader(EIDASValues.REFERER.toString()))
                .thenReturn("http://eidas.eu");
        Mockito.when(servletRequest.getParameterMap())
                .thenReturn( Collections.singletonMap(SP_URL, new String[]{"https://eidas.eu/something"}));
        Mockito.when(servletRequest.getMethod())
                .thenReturn(HttpMethod.POST.toString());
        Mockito.when(servletRequest.getRemoteAddr())
                .thenReturn("10.0.0.2");

        final SecurityRequestFilter securityRequestFilter = new SecurityRequestFilter();
        securityRequestFilter.init(mockFilterConfig);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
    }

    /**
     * Test method for
     * {@link SecurityRequestFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * when multiple request from an IP address has sufficient time in between
     * then old enough entries get removed
     * <p>
     * Must succeed.
     */
    @Test
    public void doFilterRateLimitIPForgivingFourTimes() throws ServletException, IOException, InterruptedException {
        Mockito.when(mockConfigurationSecurityBean.getIpMaxTime()).thenReturn(-2000);
        Mockito.when(mockConfigurationSecurityBean.getSpMaxRequests()).thenReturn(-1);

        final FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getInitParameter(anyString()))
                .thenReturn("ServletA,ServletB,ServiceProvider");
        final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getServletPath())
                .thenReturn("/ServiceProvider");
        Mockito.when(servletRequest.getHeader(EIDASValues.REFERER.toString()))
                .thenReturn("http://eidas.eu");
        Mockito.when(servletRequest.getParameterMap())
                .thenReturn(Collections.singletonMap(SP_URL, new String[]{"https://eidas.eu/something"}));
        Mockito.when(servletRequest.getMethod())
                .thenReturn(HttpMethod.POST.toString());
        Mockito.when(servletRequest.getRemoteAddr())
                .thenReturn("10.0.0.3");

        final SecurityRequestFilter securityRequestFilter = new SecurityRequestFilter();
        securityRequestFilter.init(mockFilterConfig);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
    }

    /**
     * Test method for
     * {@link SecurityRequestFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}
     * when multiple request from a claimed referer domain exceeds the rate limit
     * <p>
     * Must fail
     * and throw {@link SecurityError}.
     */
    @Test
    public void doFilterRateLimitRefererDomainFourTimes() throws ServletException, IOException {
        exception.expect(SecurityError.class);

        final FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getInitParameter(anyString()))
                .thenReturn("ServletA,ServletB,ServiceProvider");
        final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getServletPath())
                .thenReturn("/ServiceProvider");
        Mockito.when(servletRequest.getHeader(EIDASValues.REFERER.toString()))
                .thenReturn("http://eidas.eu");
        Mockito.when(servletRequest.getParameterMap())
                .thenReturn( Collections.singletonMap(SP_URL, new String[]{"https://eidas.eu/something"}));
        Mockito.when(servletRequest.getMethod())
                .thenReturn(HttpMethod.POST.toString());

        final SecurityRequestFilter securityRequestFilter = new SecurityRequestFilter();
        securityRequestFilter.init(mockFilterConfig);
        Mockito.when(servletRequest.getRemoteAddr()).thenReturn("10.0.1.0");
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
        Mockito.when(servletRequest.getRemoteAddr()).thenReturn("10.0.1.1");
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
        Mockito.when(servletRequest.getRemoteAddr()).thenReturn("10.0.1.2");
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
        Mockito.when(servletRequest.getRemoteAddr()).thenReturn("10.0.1.3");
        securityRequestFilter.doFilter(servletRequest, servletResponse, filterChain);
    }

    private void disableIpRateLimitCheck() {
        Mockito.when(mockConfigurationSecurityBean.getIpMaxRequests())
                .thenReturn(-1);
    }
}