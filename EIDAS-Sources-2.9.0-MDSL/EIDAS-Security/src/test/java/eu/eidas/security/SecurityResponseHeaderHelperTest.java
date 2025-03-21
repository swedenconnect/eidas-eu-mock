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
package eu.eidas.security;

import eu.eidas.security.header.SecurityResponseHeaderHelper;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static eu.eidas.security.header.SecurityResponseHeaderHelper.CONTENT_SECURITY_POLICY_HEADER;
import static eu.eidas.security.header.SecurityResponseHeaderHelper.X_CONTENT_SECURITY_POLICY_HEADER;
import static eu.eidas.security.header.SecurityResponseHeaderHelper.X_WEB_KIT_CSP_HEADER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link SecurityResponseHeaderHelper}.
 *
 * @since 2.4
 */
public class SecurityResponseHeaderHelperTest {

    private HttpServletRequest mockHttpServletRequest;
    private HttpServletResponse httpServletResponse;
    private ConfigurationSecurityBean configurationSecurityBean;
    private WebApplicationContext mockApplicationContext;
    private SecurityResponseHeaderHelper securityResponseHeaderHelper;
    private MockedStatic<ContextLoader> mockContextLoader;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        mockHttpServletRequest = mock(HttpServletRequest.class);
        when(mockHttpServletRequest.getScheme()).thenReturn("http");
        when(mockHttpServletRequest.getServerName()).thenReturn("host");
        when(mockHttpServletRequest.getServerPort()).thenReturn(8080);
        when(mockHttpServletRequest.getContextPath()).thenReturn("/Context");

        httpServletResponse = new HttpRssponseTestWrapper(null);

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

    /**
     * Test method for {@link SecurityResponseHeaderHelper#populateResponseHeader(ServletRequest, ServletResponse)}
     * If CSP is deactivated there must be no CSP directives in http response headers.
     * @throws ServletException
     *
     * Must succeed
     */
    @Test
    public void cspDeactivated() throws ServletException {
        configurationSecurityBean.setIsContentSecurityPolicyActive(false);

        securityResponseHeaderHelper = new SecurityResponseHeaderHelper();
        securityResponseHeaderHelper.populateResponseHeader(mockHttpServletRequest, httpServletResponse);

        assertNull(httpServletResponse.getHeader(CONTENT_SECURITY_POLICY_HEADER));
        assertNull(httpServletResponse.getHeader(X_CONTENT_SECURITY_POLICY_HEADER));
        assertNull(httpServletResponse.getHeader(X_WEB_KIT_CSP_HEADER));
    }

    /**
     * Test method for {@link SecurityResponseHeaderHelper#populateResponseHeader(ServletRequest, ServletResponse)}
     * If CSP is activated with a null or empty report uri, default CSP directives were added in the
     * following default http response header: Content-Security-Policy, X-Content-Security-Policy, X-WebKit-CSP
     * @throws ServletException
     *
     * Must succeed
     */
    @Test
    public void cspActivatedBlankReportUri() throws ServletException {
        configurationSecurityBean.setIsContentSecurityPolicyActive(true);

        securityResponseHeaderHelper = new SecurityResponseHeaderHelper();
        securityResponseHeaderHelper.populateResponseHeader(mockHttpServletRequest, httpServletResponse);

        assertCspPolicies(CONTENT_SECURITY_POLICY_HEADER);
        assertCspPolicies(X_CONTENT_SECURITY_POLICY_HEADER);
        assertCspPolicies(X_WEB_KIT_CSP_HEADER);
    }

    /**
     * Test method for {@link SecurityResponseHeaderHelper#populateResponseHeader(ServletRequest, ServletResponse)}
     * If CSP is activated with a non-null and non-empty report uri, default CSP directives were added in the
     * following default http response header: Content-Security-Policy, X-Content-Security-Policy, X-WebKit-CSP.
     * And all the default response headers must contain the report-uri directive.
     * @throws ServletException
     *
     * Must succeed
     */
    @Test
    public void cspActivatedNonBlankReportUri() throws ServletException {
        configurationSecurityBean.setIsContentSecurityPolicyActive(true);
        configurationSecurityBean.setCspReportingUri("http://host:8080/Context/cspReportHandler");

        securityResponseHeaderHelper = new SecurityResponseHeaderHelper();
        securityResponseHeaderHelper.populateResponseHeader(mockHttpServletRequest, httpServletResponse);

        assertCspPolicies(CONTENT_SECURITY_POLICY_HEADER);
        assertCspPolicies(X_CONTENT_SECURITY_POLICY_HEADER);
        assertCspPolicies(X_WEB_KIT_CSP_HEADER);
        assertReportToHeader(httpServletResponse);
    }

    /**
     * Test method for {@link SecurityResponseHeaderHelper#populateResponseHeader(ServletRequest, ServletResponse)}
     * If CSP is activated with mozilla directive activated, default CSP directives are added in the following
     * default http response header: Content-Security-Policy, X-Content-Security-Policy, X-WebKit-CSP.
     * @throws ServletException
     *
     * Must succeed
     */
    @Test
    public void cspIncludesMozillaDirective() throws ServletException {
        configurationSecurityBean.setIsContentSecurityPolicyActive(true);
        configurationSecurityBean.setIncludeMozillaDirectives(true);

        securityResponseHeaderHelper = new SecurityResponseHeaderHelper();
        securityResponseHeaderHelper.populateResponseHeader(mockHttpServletRequest, httpServletResponse);

        assertCspPolicies(CONTENT_SECURITY_POLICY_HEADER);
        assertCspPolicies(X_CONTENT_SECURITY_POLICY_HEADER);
        assertCspPolicies(X_WEB_KIT_CSP_HEADER);
    }

    /**
     * Test method for {@link SecurityResponseHeaderHelper#populateResponseHeader(ServletRequest, ServletResponse)}
     * If CSP is activated with mozilla directive deactivated, default CSP directives are added in the following
     * default http response header: Content-Security-Policy, X-Content-Security-Policy, X-WebKit-CSP.
     * @throws ServletException
     *
     * Must succeed
     */
    @Test
    public void cspDoesNOtIncludesMozillaDirective() throws ServletException {
        configurationSecurityBean.setIsContentSecurityPolicyActive(true);
        configurationSecurityBean.setIncludeMozillaDirectives(false);

        securityResponseHeaderHelper = new SecurityResponseHeaderHelper();
        securityResponseHeaderHelper.populateResponseHeader(mockHttpServletRequest, httpServletResponse);

        assertCspPolicies(CONTENT_SECURITY_POLICY_HEADER);
        assertCspPolicies(X_CONTENT_SECURITY_POLICY_HEADER);
        assertCspPolicies(X_WEB_KIT_CSP_HEADER);
    }

    private void assertCspPolicies(String cspHeaderName) {
        String cspPolicies = httpServletResponse.getHeader(cspHeaderName);
        if (configurationSecurityBean.getIsContentSecurityPolicyActive()) {
            assertNotNull(cspPolicies);
            assertCspDirectives(cspPolicies);
        }
        assertCspDirectives(cspPolicies);
    }

    private void assertCspDirectives(String cspPolicies) {
        assertTrue(cspPolicies.contains("default-src"));
        assertTrue(cspPolicies.contains("object-src"));
        assertTrue(cspPolicies.contains("style-src"));
        assertTrue(cspPolicies.contains("img-src"));
        assertTrue(cspPolicies.contains("font-src"));
        assertTrue(cspPolicies.contains("connect-src"));
        assertTrue(cspPolicies.contains("script-src"));
        assertTrue(cspPolicies.contains("nonce"));
        assertReportUri(cspPolicies);
    }

    private void assertReportToHeader(HttpServletResponse httpServletResponse) {
        String reportToValue = httpServletResponse.getHeader("Report-To");
        assertTrue(reportToValue.contains("http://host:8080/Context/cspReportHandler"));
    }

    private void assertReportUri(String cspPolicies) {
        String reportURi = configurationSecurityBean.getCspReportingUri();
        if (StringUtils.isBlank(reportURi)) {
            assertFalse(cspPolicies.contains("report-uri"));
        } else {
            assertTrue(cspPolicies.contains("report-uri"));
        }
    }

    /**
     * HttpResponse class used by the test for checking if the various security policies are properly set on the HttpResponse
     */
    private final class HttpRssponseTestWrapper extends HttpServletResponseWrapper {
        private Map<String, String> headers = new HashMap<>();

        private HttpRssponseTestWrapper(HttpServletResponse response) {
            super(mock(HttpServletResponse.class));
        }

        @Override
        public void setHeader(String name, String value) {
            headers.put(name, value);
        }

        @Override
        public String getHeader(String name) {
            return headers.get(name);
        }
    }
}