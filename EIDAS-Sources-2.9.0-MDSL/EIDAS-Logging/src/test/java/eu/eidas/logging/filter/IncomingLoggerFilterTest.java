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

package eu.eidas.logging.filter;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import eu.eidas.logging.IFullMessageLogger;
import eu.eidas.logging.logger.EidasResponseLogger;
import eu.eidas.logging.utils.ReflectionUtils;
import eu.eidas.logging.utils.logger.LevelFilter;
import eu.eidas.logging.utils.logger.LoggerTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link IncomingLoggerFilter}.
 *
 * @since 2.3
 */

public class IncomingLoggerFilterTest {

    /**
     * Logger used to capture logging performed by the class under test.
     * This logger will ONLY test the capturing of all the log statement produced by the Filter.
     */

    private static final Logger logger = (Logger) LoggerFactory.getLogger(MessageLoggerFilter.class.getName());

    private HttpServletRequest mockHttpServletRequest;
    private HttpServletResponse mockHttpServletResponse;
    private FilterChain mockFilterChain;
    private IFullMessageLogger iFullMessageLogger;

    private Filter incomingLoggerFilter;

    private MockedStatic<ContextLoader> mockContextLoader;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException, ServletException {
        mockHttpServletRequest = mock(HttpServletRequest.class);
        mockHttpServletResponse = mock(HttpServletResponse.class);
        mockFilterChain = mock(FilterChain.class);
        iFullMessageLogger = mock(EidasResponseLogger.class);
        final String fullMessageLoggerBeanAlias = "customLogger";

        WebApplicationContext mockApplicationContext = mock(WebApplicationContext.class);
        Mockito.when(mockApplicationContext.getBean(fullMessageLoggerBeanAlias, IFullMessageLogger.class)).thenReturn(iFullMessageLogger);

        mockContextLoader = Mockito.mockStatic(ContextLoader.class);
        mockContextLoader.when(ContextLoader::getCurrentWebApplicationContext).thenReturn(mockApplicationContext);

        final FilterConfig mockFilterConfig = mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getInitParameter("IFullMessageLogger"))
                .thenReturn(fullMessageLoggerBeanAlias);

        incomingLoggerFilter = new IncomingLoggerFilter();
        incomingLoggerFilter.init(mockFilterConfig);
    }

    @After
    public void tearDown(){
        mockContextLoader.close();
    }


    /**
     * Test method for {@link IncomingLoggerFilter#init(FilterConfig)}.
     * Must succeed
     */

    @Test
    public void init() throws ServletException {
        ListAppender<ILoggingEvent> infoAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);
        FilterConfig mockFilterConfig = mock(FilterConfig.class);

        incomingLoggerFilter.init(mockFilterConfig);
        assertThat(infoAppender.list.size(), is(1));
    }


    /**
     * Test method for {@link IncomingLoggerFilter#destroy()}.
     * Must succeed
     */

    @Test
    public void destroy() {
        ListAppender<ILoggingEvent> infoAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);

        incomingLoggerFilter.destroy();
        assertThat(infoAppender.list.size(), is(1));
    }


    /**
     * Test method for {@link IncomingLoggerFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}.
     * Using the mockito verify, it checks if the filter is calling the expected methods in the excpected order and it checks
     * that a trace message is produced when tracing is enabled.
     * Must succeed.
     */

    @Test
    public void doFilter() throws IOException, ServletException {
        ListAppender<ILoggingEvent> traceAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.TRACE));
        logger.setLevel(Level.TRACE);
        logger.addAppender(traceAppender);

        incomingLoggerFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        InOrder orderVerifier = inOrder(iFullMessageLogger, mockFilterChain);
        orderVerifier.verify(iFullMessageLogger).logMessage(mockHttpServletRequest);
        orderVerifier.verify(iFullMessageLogger).logFullMessage(mockHttpServletRequest);
        orderVerifier.verify(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);

        assertLogging(iFullMessageLogger.getClass().getSimpleName() +" FILTER for", traceAppender);
    }


    /**
     * Test method for {@link IncomingLoggerFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}.
     * Checks exception logging when {@link ServletException} is thrown
     * Must succeed.
     */

    @Test
    public void doFilterServletException() throws IOException {
        ListAppender<ILoggingEvent> infoAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.INFO));
        ListAppender<ILoggingEvent> debugAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.DEBUG));
        logger.addAppender(infoAppender);
        logger.addAppender(debugAppender);

        try {
            doNothing().when(iFullMessageLogger).logFullMessage(mockHttpServletRequest);
            doNothing().when(iFullMessageLogger).logMessage(mockHttpServletRequest);
            doThrow(new ServletException()).when(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
            incomingLoggerFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);
        } catch (ServletException e) {
            assertLogging("ServletException", infoAppender);
            assertLogging("ServletException", debugAppender);
        }
    }


    /**
     * Test method for {@link IncomingLoggerFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}.
     * Checks exception logging when {@link IOException} is thrown
     * Must succeed.
     */

    @Test
    public void doFilterIOException() throws ServletException {
        ListAppender<ILoggingEvent> infoAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.INFO));
        ListAppender<ILoggingEvent> debugAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.DEBUG));
        logger.addAppender(infoAppender);
        logger.addAppender(debugAppender);

        try {
            doNothing().when(iFullMessageLogger).logFullMessage(mockHttpServletRequest);
            doNothing().when(iFullMessageLogger).logMessage(mockHttpServletRequest);
            doThrow(new IOException()).when(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
            incomingLoggerFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);
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
