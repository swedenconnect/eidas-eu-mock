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
package eu.eidas.node.service;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.service.messages.ProxyServiceIncomingEidasRequestLogger;
import eu.eidas.node.utils.LevelFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static eu.eidas.node.utils.LoggerFilterTestUtils.createStartListAppender;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link ProxyServiceIncomingEidasRequestLoggerFilter}.
 *
 * @since 2.3
 */
public class ProxyServiceIncomingEidasRequestLoggerFilterTest {

    Logger initLogger = (Logger) LoggerFactory.getLogger(ProxyServiceIncomingEidasRequestLoggerFilter.class.getName());

    Logger packageLogger = (Logger) LoggerFactory.getLogger(EIDASValues.EIDAS_PACKAGE_REQUEST_LOGGER_VALUE.toString() + "_" + ProxyServiceIncomingEidasRequestLoggerFilter.class.getSimpleName());

    private final static LevelFilter filter = new LevelFilter(Level.INFO);

    private ListAppender<ILoggingEvent> listAppender;

    @Before
    public void setup() {
        listAppender = createStartListAppender(filter);

        initLogger.addAppender(listAppender);
        packageLogger.addAppender(listAppender);
    }

    @After
    public void teardown() {
        initLogger.detachAndStopAllAppenders();
        packageLogger.detachAndStopAllAppenders();
    }

    /**
     * Test method for {@link ProxyServiceIncomingEidasRequestLoggerFilter#init(FilterConfig)}.
     * At implementation time the init method had an empty implementation. Should this method be implemented,
     * this test will fail and should be properly adapted.
     * Must succeed
     */
    @Test
    public void init() {
        ProxyServiceIncomingEidasRequestLoggerFilter proxyServiceIncomingEidasRequestLoggerFilter = new ProxyServiceIncomingEidasRequestLoggerFilter();
        FilterConfig mockFilterConfig = mock(FilterConfig.class);
        proxyServiceIncomingEidasRequestLoggerFilter.init(mockFilterConfig);
        assertThat(listAppender.list.size(), is(1));
    }

    /**
     * Test method for {@link ProxyServiceIncomingEidasRequestLoggerFilter#destroy()}.
     * At implementation time the destroy method had an empty implementation. Should this method be implemented,
     * this test will fail and should be properly adapted.
     * <p>
     * Must succeed
     */
    @Test
    public void destroy() {
        ProxyServiceIncomingEidasRequestLoggerFilter proxyServiceIncomingEidasRequestLoggerFilter = new ProxyServiceIncomingEidasRequestLoggerFilter();
        proxyServiceIncomingEidasRequestLoggerFilter.destroy();
        assertThat(listAppender.list.size(), is(1));
    }

    /**
     * Test method for
     * {@link ProxyServiceIncomingEidasRequestLoggerFilter#doFilter(ServletRequest, ServletResponse, FilterChain)}.
     * <p>
     * Must succeed.
     */
    @Test
    public void doFilter() throws IOException, ServletException {
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        ServletResponse mockServletResponse = mock(ServletResponse.class);
        FilterChain mockFilterChain = mock(FilterChain.class);

        ProxyServiceIncomingEidasRequestLogger mockProxyServiceIncomingEidasRequestLogger = mock(ProxyServiceIncomingEidasRequestLogger.class);

        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        ProxyServiceIncomingEidasRequestLogger proxyServiceIncomingEidasRequestLogger = new ProxyServiceIncomingEidasRequestLogger();
        Mockito.when(mockApplicationContext.getBean(anyString())).thenReturn(proxyServiceIncomingEidasRequestLogger);
        ReflectionTestUtils.setField(BeanProvider.class, "CONTEXT", mockApplicationContext);

        ProxyServiceIncomingEidasRequestLoggerFilter spyProxyServiceIncomingEidasRequestLoggerFilter = Mockito.spy(new ProxyServiceIncomingEidasRequestLoggerFilter());
        Mockito.when(spyProxyServiceIncomingEidasRequestLoggerFilter.getProxyServiceIncomingEidasRequestLoggerImpl()).thenReturn(mockProxyServiceIncomingEidasRequestLogger);
        spyProxyServiceIncomingEidasRequestLoggerFilter.doFilter(mockHttpServletRequest, mockServletResponse, mockFilterChain);

        assertThat(listAppender.list.size(), is(0));
    }

}