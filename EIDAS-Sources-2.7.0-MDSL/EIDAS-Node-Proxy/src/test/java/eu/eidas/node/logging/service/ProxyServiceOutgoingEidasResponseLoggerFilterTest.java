/*
 * Copyright (c) 2022 by European Commission
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
package eu.eidas.node.logging.service;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.logging.service.messages.ProxyServiceOutgoingEidasResponseLogger;
import eu.eidas.node.utils.ReflectionUtils;
import eu.eidas.node.utils.logger.LevelFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;

import static eu.eidas.auth.commons.EIDASValues.EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE;
import static eu.eidas.node.utils.logger.LoggerTestUtils.createStartListAppender;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

/**
 * Test class for {@link ProxyServiceOutgoingEidasResponseLoggerFilter}.
 */
public class ProxyServiceOutgoingEidasResponseLoggerFilterTest {

    /**
     * Logger used to capture logging performed by the class under test.
     * This logger will ONLY test the capturing of all the log statement produced by the Filter.
     */
    private final Logger logger = (Logger) LoggerFactory.getLogger(ProxyServiceOutgoingEidasResponseLoggerFilter.class.getName());

    private HttpServletRequest mockHttpServletRequest;
    private HttpServletResponse mockHttpServletResponse;
    private FilterChain mockFilterChain;
    private ProxyServiceOutgoingEidasResponseLogger mockProxyServiceOutgoingEidasResponseLogger;
    private ProxyServiceOutgoingEidasResponseLoggerFilter proxyServiceOutgoingEidasResponseLoggerFilter;
    private ApplicationContext oldContext = null;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        mockHttpServletRequest = mock(HttpServletRequest.class);
        mockHttpServletResponse = mock(HttpServletResponse.class);
        mockFilterChain = mock(FilterChain.class);
        mockProxyServiceOutgoingEidasResponseLogger = mock(ProxyServiceOutgoingEidasResponseLogger.class);
        setUpBeanProvider(mockProxyServiceOutgoingEidasResponseLogger);

        proxyServiceOutgoingEidasResponseLoggerFilter = new ProxyServiceOutgoingEidasResponseLoggerFilter();
    }

    private void setUpBeanProvider(ProxyServiceOutgoingEidasResponseLogger mockProxyServiceOutgoingEidasResponseLogger) throws NoSuchFieldException, IllegalAccessException {
        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        oldContext = (ApplicationContext) contextField.get(null);
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        Mockito.when(mockApplicationContext.getBean(anyString())).thenReturn(mockProxyServiceOutgoingEidasResponseLogger);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
    }

    @After
    public void teardown() {
        logger.detachAndStopAllAppenders();
        if (oldContext != null) {
            ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", oldContext);
            oldContext = null;
        }
    }

    /**
     * Test method for {@link ProxyServiceOutgoingEidasResponseLoggerFilter#init(FilterConfig)}.
     * Must succeed
     */
    @Test
    public void init() {
        ListAppender<ILoggingEvent> infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);
        FilterConfig mockFilterConfig = mock(FilterConfig.class);

        proxyServiceOutgoingEidasResponseLoggerFilter.init(mockFilterConfig);
        assertThat(infoAppender.list.size(), is(1));
    }

    /**
     * Test method for {@link ProxyServiceOutgoingEidasResponseLoggerFilter#destroy()}.
     * Must succeed
     */
    @Test
    public void destroy() {
        ListAppender<ILoggingEvent> infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);

        proxyServiceOutgoingEidasResponseLoggerFilter.destroy();
        assertThat(infoAppender.list.size(), is(1));
    }

    /**
     * Test method for {@link ProxyServiceOutgoingEidasResponseLoggerFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}.
     * Using the mockito verify, it checks if the filter is calling the expected methods in the excpected order and it checks
     * that a trace message is produced when tracing is enabled.
     * Must succeed.
     */
    @Test
    public void doFilter() throws IOException, ServletException {
        Mockito.when(mockHttpServletRequest.getRequestURI()).thenReturn("connectorRedirect.jsp");
        ListAppender<ILoggingEvent> traceAppender = createStartListAppender(new LevelFilter(Level.TRACE));
        logger.setLevel(Level.TRACE);
        logger.addAppender(traceAppender);

        proxyServiceOutgoingEidasResponseLoggerFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        Logger messageLogger = getMessageLogger();
        InOrder orderVerifier = inOrder(mockFilterChain, mockProxyServiceOutgoingEidasResponseLogger);
        orderVerifier.verify(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
        orderVerifier.verify(mockProxyServiceOutgoingEidasResponseLogger).logMessage(messageLogger, mockHttpServletRequest);
        orderVerifier.verify(mockProxyServiceOutgoingEidasResponseLogger).logFullMessage(mockHttpServletRequest);

        assertLogging("ProxyServiceOutgoingSamlResponseLogger FILTER for", traceAppender);
    }

    /**
     * Test method for {@link ProxyServiceOutgoingEidasResponseLoggerFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}.
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
            Logger messageLogger = getMessageLogger();
            doThrow(new ServletException()).when(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
            proxyServiceOutgoingEidasResponseLoggerFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);
            Mockito.verify(mockProxyServiceOutgoingEidasResponseLogger, never()).logMessage(messageLogger, mockHttpServletRequest);
            Mockito.verify(mockProxyServiceOutgoingEidasResponseLogger, never()).logFullMessage(mockHttpServletRequest);
        } catch (ServletException e) {
            assertLogging("ServletException", infoAppender);
            assertLogging("ServletException", debugAppender);
        }
    }

    /**
     * Test method for {@link ProxyServiceOutgoingEidasResponseLoggerFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}.
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
            Logger messageLogger = getMessageLogger();
            doThrow(new IOException()).when(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
            proxyServiceOutgoingEidasResponseLoggerFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);
            Mockito.verify(mockProxyServiceOutgoingEidasResponseLogger, never()).logMessage(messageLogger, mockHttpServletRequest);
            Mockito.verify(mockProxyServiceOutgoingEidasResponseLogger, never()).logFullMessage(mockHttpServletRequest);
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

    private Logger getMessageLogger() {
        return (Logger) LoggerFactory.getLogger(String.format("%s_%s", EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE.toString(),
                ProxyServiceOutgoingEidasResponseLoggerFilter.class.getSimpleName()));
    }

}