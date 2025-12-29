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

import com.hazelcast.map.IMap;
import eu.eidas.auth.cache.JCacheConcurrentMapAdapter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HazelcastCacheMetricsBinderTest {

    private MeterRegistry registry;
    private IMap<Object, Object> mockHazelcastMap;
    private JCacheConcurrentMapAdapter<Object, Object> validAdapter;
    private JCacheConcurrentMapAdapter<Object, Object> invalidAdapter;

    @Before
    public void setUp() {
        registry = new SimpleMeterRegistry();

        mockHazelcastMap = Mockito.mock(IMap.class);
        Mockito.when(mockHazelcastMap.getName()).thenReturn("validCache");

        validAdapter = Mockito.mock(JCacheConcurrentMapAdapter.class);
        Mockito.when(validAdapter.getInternalMap()).thenReturn(mockHazelcastMap);

        invalidAdapter = Mockito.mock(JCacheConcurrentMapAdapter.class);
        Mockito.when(invalidAdapter.getInternalMap()).thenReturn(new ConcurrentHashMap<>());
    }

    /**
     * Test method for {@link HazelcastCacheMetricsBinder#bindTo(MeterRegistry)}.
     * This test verifies that a Hazelcast-backed cache is properly registered.
     * <p>
     * Must register metrics such as size, hits, and others for the IMap.
     */
    @Test
    public void testMetricsRegisteredForHazelcastCache() {
        HazelcastCacheMetricsBinder binder = new HazelcastCacheMetricsBinder(List.of(validAdapter));
        binder.bindTo(registry);

        boolean metricExists = registry.getMeters().stream()
                .anyMatch(m -> m.getId().getTags().stream()
                        .anyMatch(t -> "cache".equals(t.getKey()) && "validCache".equals(t.getValue())));
        Assert.assertTrue("Metrics should be registered for Hazelcast-backed cache", metricExists);
    }

    /**
     * Test method for {@link HazelcastCacheMetricsBinder#bindTo(MeterRegistry)}.
     * This test verifies that non-Hazelcast caches are ignored.
     * <p>
     * Must skip caches that are not backed by an IMap.
     */
    @Test
    public void testMetricsSkippedForNonHazelcastCache() {
        HazelcastCacheMetricsBinder binder = new HazelcastCacheMetricsBinder(List.of(invalidAdapter));
        binder.bindTo(registry);

        boolean metricExists = registry.getMeters().stream()
                .anyMatch(m -> m.getId().getTags().stream()
                        .anyMatch(t -> "cache".equals(t.getKey()) && "invalidCache".equals(t.getValue())));
        Assert.assertFalse("Metrics should not be registered for non-Hazelcast cache", metricExists);
    }

    /**
     * Test method for {@link HazelcastCacheMetricsBinder#bindTo(MeterRegistry)}.
     * This test ensures that null cache entries are handled gracefully.
     * <p>
     * Must not throw exceptions or attempt registration when a null entry is encountered.
     */
    @Test
    public void testNullCacheIsHandledGracefully() {
        HazelcastCacheMetricsBinder binder = new HazelcastCacheMetricsBinder(Arrays.asList(validAdapter, null));

        binder.bindTo(registry);

        boolean metricExists = registry.getMeters().stream()
                .anyMatch(m -> m.getId().getTags().stream()
                        .anyMatch(t -> "cache".equals(t.getKey()) && "validCache".equals(t.getValue())));
        Assert.assertTrue("Metrics should be registered for non-null Hazelcast cache", metricExists);
    }

    /**
     * Test method for {@link HazelcastCacheMetricsBinder#bindTo(MeterRegistry)}.
     * This test verifies that multiple Hazelcast caches are supported.
     * <p>
     * Must register metrics for each valid IMap-backed cache provided.
     */
    @Test
    public void testMultipleHazelcastCaches() {
        IMap<Object, Object> secondMap = Mockito.mock(IMap.class);
        Mockito.when(secondMap.getName()).thenReturn("secondCache");

        JCacheConcurrentMapAdapter<Object, Object> secondAdapter = Mockito.mock(JCacheConcurrentMapAdapter.class);
        Mockito.when(secondAdapter.getInternalMap()).thenReturn(secondMap);

        HazelcastCacheMetricsBinder binder = new HazelcastCacheMetricsBinder(List.of(validAdapter, secondAdapter));
        binder.bindTo(registry);

        boolean validMetric = registry.getMeters().stream()
                .anyMatch(m -> m.getId().getTags().stream()
                        .anyMatch(t -> "cache".equals(t.getKey()) && "validCache".equals(t.getValue())));
        boolean secondMetric = registry.getMeters().stream()
                .anyMatch(m -> m.getId().getTags().stream()
                        .anyMatch(t -> "cache".equals(t.getKey()) && "secondCache".equals(t.getValue())));

        Assert.assertTrue("Metrics should be registered for validCache", validMetric);
        Assert.assertTrue("Metrics should be registered for secondCache", secondMetric);
    }

}