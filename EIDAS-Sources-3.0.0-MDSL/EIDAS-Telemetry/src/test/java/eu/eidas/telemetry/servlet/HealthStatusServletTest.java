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

package eu.eidas.telemetry.servlet;

import eu.eidas.telemetry.HealthCheckResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * Test class for {@link HealthStatusServlet}
 */
public class HealthStatusServletTest {

    private HealthStatusServlet servlet;
    private SimpleMeterRegistry testRegistry;
    private MockedStatic<ContextLoader> contextLoaderMock;

    @Before
    public void setUp() {
        servlet = new HealthStatusServlet();

        // In this test, we use a SimpleMeterRegistry (in-memory) to simulate the real MeterRegistry used in production
        // It allows capturing and asserting metrics without needing external metric systems
        testRegistry = new SimpleMeterRegistry();

        contextLoaderMock = Mockito.mockStatic(ContextLoader.class);
        WebApplicationContext mockContext = Mockito.mock(WebApplicationContext.class);
        Mockito.when(mockContext.getBean(MeterRegistry.class)).thenReturn(testRegistry);
        contextLoaderMock.when(ContextLoader::getCurrentWebApplicationContext).thenReturn(mockContext);
    }

    /**
     * Test method for {@link HealthStatusServlet#checkHealthIndicators(MeterRegistry)}.
     * A single node must have at least one additional connection to MS specific side.
     * <p>
     * Must succeed.
     */
    @Test
    public void testHealthWhenRunningSingleNodeConnectedToSpecific() {
        mockGauge("cache.cluster.remotes", Tags.of("specificCommunication", "false"), 0);
        mockGauge("cache.cluster.remotes", Tags.of("specificCommunication", "true"), 1);


        HealthCheckResult result = servlet.checkHealthIndicators(testRegistry);
        Assert.assertTrue(result.healthy());
        Assert.assertTrue(result.failureReasons().isEmpty());
    }

    /**
     * Test method for {@link HealthStatusServlet#checkHealthIndicators(MeterRegistry)}.
     * A single node must have at least one additional connection to MS specific side.
     * <p>
     * Must fail due no additional connections.
     */
    @Test
    public void testHealthWhenRunningSingleNodeDisconnectedToSpecific() {
        mockGauge("cache.cluster.remotes", Tags.of("specificCommunication", "false"), 0);
        mockGauge("cache.cluster.remotes", Tags.of("specificCommunication", "true"), 0);


        HealthCheckResult result = servlet.checkHealthIndicators(testRegistry);
        Assert.assertFalse(result.healthy());
        Assert.assertFalse(result.failureReasons().isEmpty());
        final String reason = "Specific Communication interrupted: Not connected to external distributed cache nodes";
        Assert.assertTrue(result.failureReasons().contains(reason));
    }

    /**
     * Test method for {@link HealthStatusServlet#checkHealthIndicators(MeterRegistry)}.
     * A cluster of node replicas must have at least one additional connection to MS specific side.
     * <p>
     * Must succeed.
     */
    @Test
    public void testHealthWhenRunningClusterNodeConnectedToSpecific() {
        mockGauge("cache.cluster.remotes", Tags.of("specificCommunication", "false"), 2);
        mockGauge("cache.cluster.remotes", Tags.of("specificCommunication", "true"), 3);


        HealthCheckResult result = servlet.checkHealthIndicators(testRegistry);
        Assert.assertTrue(result.healthy());
        Assert.assertTrue(result.failureReasons().isEmpty());
    }

    /**
     * Test method for {@link HealthStatusServlet#checkHealthIndicators(MeterRegistry)}.
     * A cluster of node replicas must have at least one additional connection to MS specific side.
     * <p>
     * Must fail due no additional connections.
     */
    @Test
    public void testHealthWhenRunningClusterNodeDisconnectedToSpecific() {
        mockGauge("cache.cluster.remotes", Tags.of("specificCommunication", "false"), 2);
        mockGauge("cache.cluster.remotes", Tags.of("specificCommunication", "true"), 2);


        HealthCheckResult result = servlet.checkHealthIndicators(testRegistry);
        Assert.assertFalse(result.healthy());
        Assert.assertFalse(result.failureReasons().isEmpty());
        final String reason = "Specific Communication interrupted: Not connected to external distributed cache nodes";
        Assert.assertTrue(result.failureReasons().contains(reason));
    }

    /**
     * Test method for {@link HealthStatusServlet#checkHealthIndicators(MeterRegistry)}.
     * A cluster of node replicas must have at least one additional connection to MS specific side.
     * <p>
     * Must succeed.
     */
    @Test
    public void testHealthWhenRunningClusterNodeConnectedToClusterSpecific() {
        mockGauge("cache.cluster.remotes", Tags.of("specificCommunication", "false"), 2);
        mockGauge("cache.cluster.remotes", Tags.of("specificCommunication", "true"), 5);


        HealthCheckResult result = servlet.checkHealthIndicators(testRegistry);
        Assert.assertTrue(result.healthy());
        Assert.assertTrue(result.failureReasons().isEmpty());
    }

    private void mockGauge(String name, Tags tags, double value) {
        testRegistry.gauge(name, tags, value);
    }

    @After
    public void tearDown() {
        contextLoaderMock.close();
    }
}