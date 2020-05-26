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
package eu.eidas.node.logging.connector;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.logging.connector.messages.ConnectorIncomingLightRequestLogger;
import eu.eidas.node.utils.logger.LevelFilter;
import eu.eidas.node.utils.ReflectionUtils;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import org.apache.commons.lang.StringUtils;
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

import static eu.eidas.auth.commons.EIDASValues.EIDAS_PACKAGE_REQUEST_LOGGER_VALUE;
import static eu.eidas.node.utils.logger.LoggerTestUtils.createStartListAppender;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link ConnectorIncomingLightRequestLoggerFilter}.
 *
 * @since 2.3
 */
public class ConnectorIncomingLightRequestLoggerFilterTest {

    /**
     * Logger used to capture logging performed by the class under test.
     * This logger will ONLY test the capturing of all the log statement produced by the Filter.
     */
    private static final Logger logger = (Logger)LoggerFactory.getLogger(ConnectorIncomingLightRequestLoggerFilter.class.getName());

    private HttpServletRequest mockHttpServletRequest;
    private HttpServletResponse mockHttpServletResponse;
    private FilterChain mockFilterChain;
    private ConnectorIncomingLightRequestLogger mockConnectorIncomingLightRequestLogger;
    private ConnectorIncomingLightRequestLoggerFilter connectorIncomingLightRequestLoggerFilter;

    @Before
    public void setup() {
        mockHttpServletRequest = mock(HttpServletRequest.class);
        mockHttpServletResponse = mock(HttpServletResponse.class);
        mockFilterChain = mock(FilterChain.class);
        mockConnectorIncomingLightRequestLogger = mock(ConnectorIncomingLightRequestLogger.class);
        setUpBeanProvider(mockConnectorIncomingLightRequestLogger);

        connectorIncomingLightRequestLoggerFilter = new ConnectorIncomingLightRequestLoggerFilter();
    }

    private void setUpBeanProvider(ConnectorIncomingLightRequestLogger connectorIncomingLightRequestLogger) {
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        Mockito.when(mockApplicationContext.getBean(anyString())).thenReturn(connectorIncomingLightRequestLogger);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
    }

    @After
    public void teardown() {
        logger.detachAndStopAllAppenders();
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLoggerFilter#init(FilterConfig)}.
     * At implementation time the init method had an empty implementation. Should this method be implemented,
     * this test will fail and should be properly adapted.
     * Must succeed
     */
    @Test
    public void init() {
        ListAppender<ILoggingEvent> infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);
        FilterConfig mockFilterConfig = mock(FilterConfig.class);

        connectorIncomingLightRequestLoggerFilter.init(mockFilterConfig);
        assertThat(infoAppender.list.size(), is(1));
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLoggerFilter#destroy()}.
     * At implementation time the destroy method had an empty implementation. Should this method be implemented,
     * this test will fail and should be properly adapted.
     * Must succeed
     */
    @Test
    public void destroy() {
        ListAppender<ILoggingEvent> infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);

        connectorIncomingLightRequestLoggerFilter.destroy();
        assertThat(infoAppender.list.size(), is(1));
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLoggerFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}.
     * Using the mockito verify, it checks if the filter is calling the expected methods in the excpected order and it checks
     * that a trace message is produced when tracing is enabled.
     * Must succeed.
     */
    @Test
    public void doFilter() throws IOException, ServletException, SpecificCommunicationException {
        ListAppender<ILoggingEvent> traceAppender = createStartListAppender(new LevelFilter(Level.TRACE));
        logger.setLevel(Level.TRACE);
        logger.addAppender(traceAppender);

        connectorIncomingLightRequestLoggerFilter.doFilter(mockHttpServletRequest,mockHttpServletResponse, mockFilterChain);

        Logger messageLogger = getMessageLogger();
        InOrder orderVerifier = inOrder(mockConnectorIncomingLightRequestLogger, mockFilterChain);
        orderVerifier.verify(mockConnectorIncomingLightRequestLogger).logMessage(messageLogger, mockHttpServletRequest);
        orderVerifier.verify(mockFilterChain).doFilter(mockHttpServletRequest,mockHttpServletResponse);

        assertLogging("LightRequestIncomingLogger FILTER for", traceAppender);
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLoggerFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}.
     * Checks exception logging when {@link ServletException} is thrown
     * Must succeed.
     */
    @Test
    public void doFilterServletException() throws IOException, SpecificCommunicationException {
        ListAppender<ILoggingEvent> infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        ListAppender<ILoggingEvent> debugAppender = createStartListAppender(new LevelFilter(Level.DEBUG));
        logger.addAppender(infoAppender);
        logger.addAppender(debugAppender);

        try {
            Logger messageLogger = getMessageLogger();
            doNothing().when(mockConnectorIncomingLightRequestLogger).logMessage(messageLogger, mockHttpServletRequest);
            doThrow(new ServletException()).when(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
            connectorIncomingLightRequestLoggerFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);
        } catch (ServletException e) {
            assertLogging("ServletException", infoAppender);
            assertLogging("ServletException", debugAppender);
        }
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLoggerFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}.
     * Checks exception logging when {@link IOException} is thrown
     * Must succeed.
     */
    @Test
    public void doFilterIOException() throws ServletException, SpecificCommunicationException{
        ListAppender<ILoggingEvent> infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        ListAppender<ILoggingEvent> debugAppender = createStartListAppender(new LevelFilter(Level.DEBUG));
        logger.addAppender(infoAppender);
        logger.addAppender(debugAppender);

        try {
            Logger messageLogger = getMessageLogger();
            doNothing().when(mockConnectorIncomingLightRequestLogger).logMessage(messageLogger, mockHttpServletRequest);
            doThrow(new IOException()).when(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
            connectorIncomingLightRequestLoggerFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);
        } catch (IOException e) {
            assertLogging("IOException", infoAppender);
            assertLogging("IOException", debugAppender);
        }
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLoggerFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}.
     * Checks exception logging when {@link SpecificCommunicationException} is thrown
     * Must succeed.
     */
    @Test
    public void doFilterSpecificCommunicationException() throws ServletException, IOException, SpecificCommunicationException {
        Exception expectedException = new SpecificCommunicationException(StringUtils.EMPTY);

        ListAppender<ILoggingEvent> infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        ListAppender<ILoggingEvent> debugAppender = createStartListAppender(new LevelFilter(Level.DEBUG));
        logger.addAppender(infoAppender);
        logger.addAppender(debugAppender);

        Logger messageLogger = getMessageLogger();
        doThrow(expectedException).when(mockConnectorIncomingLightRequestLogger).logMessage(messageLogger, mockHttpServletRequest);

        connectorIncomingLightRequestLoggerFilter.doFilter(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);
        assertLogging("SpecificCommunicationException", infoAppender);
        assertLogging("SpecificCommunicationException", debugAppender);
    }

    private void assertLogging(String expected, ListAppender appender) {
        assertThat(appender.list.size(), is(1));
        String loggedd = ((LoggingEvent)appender.list.get(0)).getMessage();
        assertThat(loggedd, containsString(expected));
    }

    private Logger getMessageLogger() {
        return (Logger) LoggerFactory.getLogger( String.format("%s_%s", EIDAS_PACKAGE_REQUEST_LOGGER_VALUE.toString(),
                ConnectorIncomingLightRequestLoggerFilter.class.getSimpleName()));
    }
}