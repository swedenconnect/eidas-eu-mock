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

package eu.eidas.node.security;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static eu.eidas.node.security.SecurityResponseHeaderHelper.CONTENT_SECURITY_POLICY_HEADER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContentSecurityPolicyReportServletTest {

    private HttpServletRequest mockHttpServletRequest;
    private HttpServletResponse mockHttpServletResponse;

    @Before
    public void setUp() {
        mockHttpServletRequest = mock(HttpServletRequest.class);
        mockHttpServletResponse = mock(HttpServletResponse.class);
    }

    /**
     * Test method for doGet in {@link ContentSecurityPolicyReportServlet} .
     * If CSP is deactivated there must be no CSP report-uri directives in http response headers.
     * @throws IOException
     * Must succeed
     */
    @Test
    public void doGet_cspInactive() throws IOException {
        when(mockHttpServletResponse.getHeader(CONTENT_SECURITY_POLICY_HEADER)).thenReturn("NoReportDirective");

        ContentSecurityPolicyReportServlet cspReportServlet = new ContentSecurityPolicyReportServlet();
        cspReportServlet.doGet(mockHttpServletRequest, mockHttpServletResponse);
        verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.SC_FORBIDDEN);
    }

    /**
     * Test method for doPost in {@link ContentSecurityPolicyReportServlet} .
     * If CSP is deactivated there must be no CSP report-uri directives in http response headers.
     * @throws IOException
     * Must succeed
     */
    @Test
    public void doPost_cspInactive() throws IOException {
        when(mockHttpServletResponse.getHeader(CONTENT_SECURITY_POLICY_HEADER)).thenReturn("NoReportDirective");

        ContentSecurityPolicyReportServlet cspReportServlet = new ContentSecurityPolicyReportServlet();
        cspReportServlet.doPost(mockHttpServletRequest, mockHttpServletResponse);
        verify(mockHttpServletResponse, times(1)).sendError(HttpStatus.SC_FORBIDDEN);
    }

    /**
     * Test method for doGet in {@link ContentSecurityPolicyReportServlet} .
     * If CSP is aactivated there must be a CSP report-uri directives in http response headers.
     * @throws IOException
     * Must succeed
     */
    @Test
    public void doGet_cspActive() throws IOException {
        when(mockHttpServletResponse.getHeader(CONTENT_SECURITY_POLICY_HEADER)).thenReturn("CSP report-uri directive");

        BufferedReader bufferedReader = new BufferedReader(new StringReader("report-uri CSP line"));
        when(mockHttpServletRequest.getReader()).thenReturn(bufferedReader);

        ContentSecurityPolicyReportServlet cspReportServlet = new ContentSecurityPolicyReportServlet();
        cspReportServlet.doGet(mockHttpServletRequest, mockHttpServletResponse);
        verify(mockHttpServletRequest, times(1)).getReader();
    }

    /**
     * Test method for doGet in {@link ContentSecurityPolicyReportServlet} .
     * If CSP is activated there must be a CSP report-uri directives in http response headers.
     * @throws IOException
     * Must succeed
     */
    @Test
    public void doPost_cspActive() throws IOException {
        when(mockHttpServletResponse.getHeader(CONTENT_SECURITY_POLICY_HEADER)).thenReturn("CSP report-uri directive");

        BufferedReader bufferedReader = new BufferedReader(new StringReader("report-uri CSP line"));
        when(mockHttpServletRequest.getReader()).thenReturn(bufferedReader);

        ContentSecurityPolicyReportServlet cspReportServlet = new ContentSecurityPolicyReportServlet();
        cspReportServlet.doPost(mockHttpServletRequest, mockHttpServletResponse);
        verify(mockHttpServletRequest, times(1)).getReader();
    }

}