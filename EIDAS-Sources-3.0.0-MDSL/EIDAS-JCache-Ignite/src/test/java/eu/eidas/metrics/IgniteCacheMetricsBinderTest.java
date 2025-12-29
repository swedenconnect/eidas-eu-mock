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

package eu.eidas.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheMetrics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.cache.Cache;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Test class for {@link IgniteCacheMetricsBinder}
 */
public class IgniteCacheMetricsBinderTest {

    private MeterRegistry registry;
    private Cache<?, ?> unknownCache;
    private IgniteCache<Object, Object> mockIgniteCache;

    @Before
    public void setUp() {
        registry = new SimpleMeterRegistry();
        unknownCache = Mockito.mock(Cache.class);
        mockIgniteCache = Mockito.mock(IgniteCache.class);
        Mockito.when(mockIgniteCache.getName()).thenReturn("testCache");
        Mockito.when(unknownCache.getName()).thenReturn("unknownCache");
    }

    /**
     * Test method for {@link IgniteCacheMetricsBinder#bindTo(MeterRegistry)}.
     * This test verifies that the size metric is correctly registered and returns the expected value.
     * <p>
     * Must register a valid gauge for cache size.
     */
    @Test
    public void testCacheSizeMetric() {
        Mockito.when(mockIgniteCache.sizeLong()).thenReturn(123L);
        Mockito.when(mockIgniteCache.metrics()).thenReturn(Mockito.mock(CacheMetrics.class));

        IgniteCacheMetricsBinder binder = new IgniteCacheMetricsBinder(List.of(mockIgniteCache));
        binder.bindTo(registry);

        Gauge gauge = registry.find("ignite.cache.size").tag("cache", "testCache").gauge();
        assertNotNull(gauge);
        Assert.assertEquals(123.0, gauge.value(), 0.001);
    }

    /**
     * Test method for {@link IgniteCacheMetricsBinder#bindTo(MeterRegistry)}.
     * This test verifies that the hit count is correctly exposed.
     * <p>
     * Must return the configured number of cache hits.
     */
    @Test
    public void testCacheHitMetric() {
        CacheMetrics metrics = Mockito.mock(CacheMetrics.class);
        Mockito.when(metrics.getCacheHits()).thenReturn(10L);
        Mockito.when(mockIgniteCache.metrics()).thenReturn(metrics);

        IgniteCacheMetricsBinder binder = new IgniteCacheMetricsBinder(List.of(mockIgniteCache));
        binder.bindTo(registry);

        Gauge gauge = registry.find("ignite.cache.hits").tag("cache", "testCache").gauge();
        Assert.assertNotNull(gauge);
        Assert.assertEquals(10.0, gauge.value(), 0.001);
    }

    /**
     * Test method for {@link IgniteCacheMetricsBinder#bindTo(MeterRegistry)}.
     * This test verifies that the miss count is correctly exposed.
     * <p>
     * Must return the configured number of cache misses.
     */
    @Test
    public void testCacheMissMetric() {
        CacheMetrics metrics = Mockito.mock(CacheMetrics.class);
        Mockito.when(metrics.getCacheMisses()).thenReturn(5L);
        Mockito.when(mockIgniteCache.metrics()).thenReturn(metrics);

        IgniteCacheMetricsBinder binder = new IgniteCacheMetricsBinder(List.of(mockIgniteCache));
        binder.bindTo(registry);

        Gauge gauge = registry.find("ignite.cache.misses").tag("cache", "testCache").gauge();
        Assert.assertNotNull(gauge);
        Assert.assertEquals(5.0, gauge.value(), 0.001);
    }

    /**
     * Test method for {@link IgniteCacheMetricsBinder#bindTo(MeterRegistry)}.
     * This test verifies that the put count is correctly exposed.
     * <p>
     * Must return the configured number of cache puts.
     */
    @Test
    public void testCachePutMetric() {
        CacheMetrics metrics = Mockito.mock(CacheMetrics.class);
        Mockito.when(metrics.getCachePuts()).thenReturn(3L);
        Mockito.when(mockIgniteCache.metrics()).thenReturn(metrics);

        IgniteCacheMetricsBinder binder = new IgniteCacheMetricsBinder(List.of(mockIgniteCache));
        binder.bindTo(registry);

        Gauge gauge = registry.find("ignite.cache.puts").tag("cache", "testCache").gauge();
        Assert.assertNotNull(gauge);
        Assert.assertEquals(3.0, gauge.value(), 0.001);
    }

    /**
     * Test method for {@link IgniteCacheMetricsBinder#bindTo(MeterRegistry)}.
     * This test verifies that the removal count is correctly exposed.
     * <p>
     * Must return the configured number of cache removals.
     */
    @Test
    public void testCacheRemovalMetric() {
        CacheMetrics metrics = Mockito.mock(CacheMetrics.class);
        Mockito.when(metrics.getCacheRemovals()).thenReturn(7L);
        Mockito.when(mockIgniteCache.metrics()).thenReturn(metrics);

        IgniteCacheMetricsBinder binder = new IgniteCacheMetricsBinder(List.of(mockIgniteCache));
        binder.bindTo(registry);

        Gauge gauge = registry.find("ignite.cache.removals").tag("cache", "testCache").gauge();
        Assert.assertNotNull(gauge);
        Assert.assertEquals(7.0, gauge.value(), 0.001);
    }

    /**
     * Test method for {@link IgniteCacheMetricsBinder#bindTo(MeterRegistry)}.
     * <p>
     * Verifies that metrics are registered for IgniteCache instances and skipped for unknown caches.
     */
    @Test
    public void testMetricsRegistrationForIgniteCache() {
        final IgniteCacheMetricsBinder binder = new IgniteCacheMetricsBinder(List.of(mockIgniteCache, unknownCache));
        binder.bindTo(registry);

        Gauge igniteGauge = registry.find("ignite.cache.size").tag("cache", "testCache").gauge();
        Assert.assertNotNull("Metrics should be registered for IgniteCache", igniteGauge);

        Gauge unknownGauge = registry.find("ignite.cache.size").tag("cache", "unknownCache").gauge();
        Assert.assertNull("Metrics should not be registered for unknown cache", unknownGauge);
    }

}