/*
 * Copyright (c) 2025 by European Commission
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
package eu.eidas.logging.filter;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import eu.eidas.logging.IFullMessageLogger;
import eu.eidas.logging.utils.logger.LevelFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static eu.eidas.logging.utils.logger.LoggerTestUtils.createStartListAppender;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link OutgoingLoggerFilter}.
 *
 * @since 2.3
 */
public class OutgoingLoggerFilterTest {

    /**
     * Logger used to capture logging performed by the class under test.
     * This logger will ONLY test the capturing of all the log statement produced by the Filter.
     */
    private final Logger logger = (Logger) LoggerFactory.getLogger(MessageLoggerFilter.class.getName());

    private HttpServletRequest mockHttpServletRequest;
    private HttpServletResponse mockHttpServletResponse;
    private FilterChain mockFilterChain;
    private IFullMessageLogger iFullMessageLogger;
    private OutgoingLoggerFilter outgoingLoggerFilter;

    MockedStatic<ContextLoader> mockContextLoader;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        mockHttpServletRequest = mock(HttpServletRequest.class);
        mockHttpServletResponse = mock(HttpServletResponse.class);
        mockFilterChain = mock(FilterChain.class);
        iFullMessageLogger = mock(IFullMessageLogger.class);
        final String fullMessageLoggerBeanAlias = "customLogger";

        WebApplicationContext mockApplicationContext = mock(WebApplicationContext.class);
        Mockito.when(mockApplicationContext.getBean(fullMessageLoggerBeanAlias, IFullMessageLogger.class)).thenReturn(iFullMessageLogger);

        mockContextLoader = Mockito.mockStatic(ContextLoader.class);
        mockContextLoader.when(ContextLoader::getCurrentWebApplicationContext).thenReturn(mockApplicationContext);

        final FilterConfig mockFilterConfig = mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getInitParameter("IFullMessageLogger"))
                .thenReturn(fullMessageLoggerBeanAlias);

        outgoingLoggerFilter = new OutgoingLoggerFilter();
        outgoingLoggerFilter.init(mockFilterConfig);
    }

    @After
    public void tearDown() {
        mockContextLoader.close();
    }

    /**
     * Test method for {@link OutgoingLoggerFilter#init(FilterConfig)}.
     * Must succeed
     */
    @Test
    public void init() {
        ListAppender<ILoggingEvent> infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);
        FilterConfig mockFilterConfig = mock(FilterConfig.class);

        outgoingLoggerFilter.init(mockFilterConfig);
        assertThat(infoAppender.list.size(), is(1));
    }

    /**
     * Test method for {@link OutgoingLoggerFilter#destroy()}.
     * Must succeed
     */
    @Test
    public void destroy() {
        ListAppender<ILoggingEvent> infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);

        outgoingLoggerFilter.destroy();
        assertThat(infoAppender.list.size(), is(1));
    }

    /**
     * Test method for {@link OutgoingLoggerFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}.
     * Using the mockito verify, it checks if the filter is calling the expected methods in the excpected order and it checks
     * that a trace message is produced when tracing is enabled.
     * Must succeed.
     */
    @Test
    public void doFilter() throws IOException, ServletException {
        ListAppender<ILoggingEvent> traceAppender = createStartListAppender(new LevelFilter(Level.TRACE));
        logger.setLevel(Level.TRACE);
        logger.addAppender(traceAppender);

        outgoingLoggerFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        InOrder orderVerifier = inOrder(mockFilterChain, iFullMessageLogger);
        orderVerifier.verify(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
        orderVerifier.verify(iFullMessageLogger).logMessage(mockHttpServletRequest);

        assertLogging(iFullMessageLogger.getClass().getSimpleName() + " FILTER for", traceAppender);
    }

    /**
     * Test method for {@link OutgoingLoggerFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}.
     * Checks exception logging when {@link ServletException} is thrown
     * Must succeed.
     */
    @Test
    public void doFilterServletException() throws IOException {
        ListAppender<ILoggingEvent> infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        ListAppender<ILoggingEvent> debugAppender = createStartListAppender(new LevelFilter(Level.DEBUG));
        logger.addAppender(infoAppender);
        logger.addAppender(debugAppender);

        try {
            doNothing().when(iFullMessageLogger).logMessage(mockHttpServletRequest);
            doThrow(new ServletException()).when(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
            outgoingLoggerFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);
        } catch (ServletException e) {
            assertLogging("ServletException", infoAppender);
            assertLogging("ServletException", debugAppender);
        }
    }

    /**
     * Test method for {@link OutgoingLoggerFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}.
     * Checks exception logging when {@link IOException} is thrown
     * Must succeed.
     */
    @Test
    public void doFilterIOException() throws ServletException {
        ListAppender<ILoggingEvent> infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        ListAppender<ILoggingEvent> debugAppender = createStartListAppender(new LevelFilter(Level.DEBUG));
        logger.addAppender(infoAppender);
        logger.addAppender(debugAppender);

        try {
            doNothing().when(iFullMessageLogger).logMessage(mockHttpServletRequest);
            doThrow(new IOException()).when(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
            outgoingLoggerFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);
        } catch (IOException e) {
            assertLogging("IOException", infoAppender);
            assertLogging("IOException", debugAppender);
        }
    }

    private void assertLogging(String expected, ListAppender appender) {
        assertThat(appender.list.size(), is(1));
        String loggedd = ((LoggingEvent) appender.list.get(0)).getMessage();
        assertThat(loggedd, containsString(expected));
    }

}