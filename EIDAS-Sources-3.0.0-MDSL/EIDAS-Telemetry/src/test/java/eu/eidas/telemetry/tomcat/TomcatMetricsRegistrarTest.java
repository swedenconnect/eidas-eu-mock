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

package eu.eidas.telemetry.tomcat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.webresources.StandardRoot;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for {@link TomcatMetricsRegistrar}
 */
public class TomcatMetricsRegistrarTest {

    private MockedStatic<ContextLoader> contextLoaderMock;
    private MeterRegistry testRegistry;
    private ApplicationContext mockApplicationContext;
    private ServletContext mockServletContext;
    private TomcatMetricsRegistrar tomcatRegistrar;

    @Before
    public void setUp() {
        testRegistry = new SimpleMeterRegistry();
        contextLoaderMock = Mockito.mockStatic(ContextLoader.class);
        mockApplicationContext = Mockito.mock(ApplicationContext.class);
        mockServletContext = Mockito.mock(ServletContext.class);
        tomcatRegistrar = new TomcatMetricsRegistrar(mockServletContext);

        WebApplicationContext mockContext = Mockito.mock(WebApplicationContext.class);
        Mockito.when(mockContext.getBean(MeterRegistry.class)).thenReturn(testRegistry);
        Mockito.when(mockApplicationContext.getBean(ServletContext.class)).thenReturn(Mockito.mock(ServletContext.class));

        contextLoaderMock.when(ContextLoader::getCurrentWebApplicationContext).thenReturn(mockContext);
    }

    /**
     * Test method for {@link TomcatMetricsRegistrar#bindTo(MeterRegistry)}.
     * Simulates Tomcat environment and checks if Tomcat metrics are registered correctly.
     */
    @Test
    public void testTomcatSessionMetricsAreRegisteredOnSpringStartupOrRefresh() {
        final ServletContext mockServletContext = Mockito.mock(ServletContext.class);
        final StandardRoot mockStandardRoot = Mockito.mock(StandardRoot.class);
        final StandardContext mockStandardContext = Mockito.mock(StandardContext.class);
        final StandardManager mockStandardManager = Mockito.mock(StandardManager.class);
        final Context mockContext = Mockito.mock(Context.class);

        Mockito.when(mockServletContext.getAttribute("org.apache.catalina.resources")).thenReturn(mockStandardRoot);
        Mockito.when(mockStandardRoot.getContext()).thenReturn(mockStandardContext);
        Mockito.when(mockStandardContext.getManager()).thenReturn(mockStandardManager);
        Mockito.when(mockStandardManager.getContext()).thenReturn(mockContext);
        Mockito.when(mockStandardManager.getMaxActive()).thenReturn(42);


        final TomcatMetricsRegistrar registrar = new TomcatMetricsRegistrar(mockServletContext);

        registrar.bindTo(testRegistry);

        Assert.assertNotNull(testRegistry.find("tomcat.sessions.active.max").gauge());
        final double value = testRegistry.find("tomcat.sessions.active.max").gauge().value();
        Assert.assertEquals(42, value, 0);
    }

    /**
     * Test method for {@link TomcatMetricsRegistrar#bindTo(MeterRegistry)}
     * Simulates Wildfly environment and checks that no Tomcat metrics are registered.
     */
    @Test
    public void testNoTomcatMetricsRegisteredOnWildfly() {
        final ServletContext mockServletContext = Mockito.mock(ServletContext.class);
        Mockito.when(mockServletContext.getAttribute("org.apache.catalina.resources")).thenReturn(null);

        final TomcatMetricsRegistrar registrar = new TomcatMetricsRegistrar(mockServletContext);

        registrar.bindTo(testRegistry);

        Assert.assertNull(testRegistry.find("tomcat.sessions.active.max").gauge());
    }

    /**
     * Test method for {@link TomcatMetricsRegistrar#bindTo(MeterRegistry)}.
     * Simulates a scenario where the Spring ApplicationContext is null and verifies that no Tomcat metrics are registered.
     * <p>
     * Must ensure that when {@code ContextLoader.getCurrentWebApplicationContext()} returns {@code null},
     * the metric registration process is skipped without errors.
     */
    @Test
    public void testNoMetricsRegisteredIfSpringContextIsNull() {
        contextLoaderMock.when(ContextLoader::getCurrentWebApplicationContext).thenReturn(null);

        final ContextRefreshedEvent event = new ContextRefreshedEvent(mockApplicationContext);
        final TomcatMetricsRegistrar registrar = new TomcatMetricsRegistrar(mockServletContext);

        registrar.bindTo(testRegistry);

        Assert.assertNull(testRegistry.find("tomcat.sessions.active.max").gauge());
    }

    /**
     * Test method for {@link TomcatMetricsRegistrar#removeObsoleteServletMetrics(ServletContext, MeterRegistry)}.
     * Ensures that servlet metrics with names not registered in ServletContext are removed.
     * <p>
     * Must remove obsolete meters.
     */
    @Test
    public void testRemovesObsoleteServletMetrics() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Map<String, ServletRegistration> registrations = new HashMap<>();
        registrations.put("HealthStatusServlet", Mockito.mock(ServletRegistration.class));
        registrations.put("ContentSecurityPolicyReportServlet", Mockito.mock(ServletRegistration.class));
        Mockito.when(mockServletContext.getServletRegistrations()).thenReturn((Map) registrations);

        Tags tags1 = Tags.of("name", "HealthStatusServlet");
        Tags tags2 = Tags.of("name", "ContentSecurityPolicyReportServlet");
        Tags tags3 = Tags.of("name", "IdpResponseServlet");

        testRegistry.counter("tomcat.servlet.request", tags1);
        testRegistry.counter("tomcat.servlet.request", tags2);
        testRegistry.counter("tomcat.servlet.request", tags3);

        Assert.assertEquals(3, testRegistry.getMeters().size());

        Method removeObsoleteServletMetricsMethod = TomcatMetricsRegistrar.class.getDeclaredMethod("removeObsoleteServletMetrics", ServletContext.class, MeterRegistry.class);
        removeObsoleteServletMetricsMethod.setAccessible(true);

        removeObsoleteServletMetricsMethod.invoke(tomcatRegistrar, mockServletContext, testRegistry);

        Assert.assertEquals(2, testRegistry.getMeters().size());
        Assert.assertNotNull(testRegistry.find("tomcat.servlet.request").tag("name", "HealthStatusServlet").counter());
        Assert.assertNotNull(testRegistry.find("tomcat.servlet.request").tag("name", "ContentSecurityPolicyReportServlet").counter());
        Assert.assertNull(testRegistry.find("tomcat.servlet.request").tag("name", "IdpResponseServlet").counter());
    }

    /**
     * Test method for {@link TomcatMetricsRegistrar#removeObsoleteServletMetrics(ServletContext, MeterRegistry)}.
     * Ensures that only obsolete servlet metrics are removed, and valid ones are preserved.
     * <p>
     * Must preserve valid servlet metrics.
     */
    @Test
    public void testPreservesValidServletMetrics() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Map<String, ServletRegistration> registrations = new HashMap<>();
        registrations.put("ConnectorErrorServlet", Mockito.mock(ServletRegistration.class));
        registrations.put("SpecificConnectorRequestServlet", Mockito.mock(ServletRegistration.class));
        Mockito.when(mockServletContext.getServletRegistrations()).thenReturn((Map) registrations);

        testRegistry.counter("tomcat.servlet.request", Tags.of("name", "ConnectorErrorServlet"));
        testRegistry.counter("tomcat.servlet.request", Tags.of("name", "SpecificConnectorRequestServlet"));

        Method removeObsoleteServletMetricsMethod = TomcatMetricsRegistrar.class.getDeclaredMethod("removeObsoleteServletMetrics", ServletContext.class, MeterRegistry.class);
        removeObsoleteServletMetricsMethod.setAccessible(true);

        removeObsoleteServletMetricsMethod.invoke(tomcatRegistrar, mockServletContext, testRegistry);

        Assert.assertEquals(2, testRegistry.getMeters().size());
        Assert.assertNotNull(testRegistry.find("tomcat.servlet.request").tag("name", "ConnectorErrorServlet").counter());
        Assert.assertNotNull(testRegistry.find("tomcat.servlet.request").tag("name", "SpecificConnectorRequestServlet").counter());
    }

    @After
    public void tearDown() {
        testRegistry.close();
        contextLoaderMock.close();
    }

}