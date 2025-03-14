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
package eu.eidas.security.header;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;

public class RemoveHttpHeadersFilterTest {

    private HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
    private HttpServletResponse mockHttpServletResponse = Mockito.mock(HttpServletResponse.class);
    private FilterChain mockFilterChain = Mockito.mock(FilterChain.class);
    private RemoveHttpHeadersFilter removeHttpHeadersFilter = new RemoveHttpHeadersFilter();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void doFilterTest() throws ServletException, IOException {
        mockHttpServletResponse.setHeader("X-Powered-By","testdata");
        mockHttpServletResponse.setHeader("Server","testdata");

        removeHttpHeadersFilter.doFilter(mockHttpServletRequest,mockHttpServletResponse,mockFilterChain);
        Mockito.verify(mockHttpServletResponse).setHeader("X-Powered-By","");
        Mockito.verify(mockHttpServletResponse).setHeader("Server","");
        Mockito.verify(mockFilterChain).doFilter(any(),any());
    }

    @Test
    public void doFilterTestIOException() throws ServletException, IOException {
        expectedException.expect(ServletException.class);

        Mockito.doThrow(IOException.class).when(mockFilterChain).doFilter(any(),any());
        removeHttpHeadersFilter.doFilter(mockHttpServletRequest,mockHttpServletResponse,mockFilterChain);
    }
}
