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

import com.hazelcast.cluster.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import eu.eidas.auth.cache.HazelcastInstanceInitializer;
import eu.eidas.auth.cache.JCacheConcurrentMapAdapter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.cache.HazelcastCacheMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A {@link MeterBinder} implementation that binds metrics for Hazelcast caches
 * to a Micrometer {@link io.micrometer.core.instrument.MeterRegistry}.
 * <p>
 * It collects statistics such as size, hits, misses, puts, and removals for each
 * configured cache by accessing their live {@link HazelcastInstance} instances.
 */
public class HazelcastCacheMetricsBinder implements MeterBinder {

    private static final Logger LOG = LoggerFactory.getLogger(HazelcastCacheMetricsBinder.class);

    private final List<Cache> caches;

    private HazelcastInstanceInitializer hazelcastInstanceInitializer;

    private boolean specificCommunication;

    public HazelcastCacheMetricsBinder(List<Cache> cacheList) {
        this.caches = cacheList;
    }

    public HazelcastCacheMetricsBinder(CacheManager cacheManager) {
        this.caches = StreamSupport.stream(cacheManager.getCacheNames().spliterator(), true)
                .map(cacheManager::getCache)
                .collect(Collectors.toList());
    }

    /**
     * Registers Hazelcast cache metrics to the provided {@link MeterRegistry}.
     * <p>
     * This method iterates through all configured caches and, for each cache backed by a {@link com.hazelcast.map.IMap},
     * it binds metrics such as hits, misses, puts, removals, and size using {@link HazelcastCacheMetrics}.
     * <p>
     * If a cache does not wrap a Hazelcast {@code IMap}, it is logged and skipped silently.
     *
     * @param registry The {@link MeterRegistry} to bind metrics to.
     */
    @Override
    public void bindTo(MeterRegistry registry) {
        for (Cache<?, ?> jCache : caches) {
            if (null == jCache) {
                LOG.warn("Cache instance is null, skipping.");
                continue;
            }

            ConcurrentMap<?, ?> internalMap = ((JCacheConcurrentMapAdapter<?, ?>) jCache).getInternalMap();

            if (internalMap instanceof IMap<?, ?>) {
                IMap<?, ?> hazelcastMap = (IMap<?, ?>) internalMap;
                Tags tags = Tags.of("cache", hazelcastMap.getName());
                HazelcastCacheMetrics.monitor(registry, hazelcastMap, tags);
                LOG.info("Registered Hazelcast metrics for: {}", hazelcastMap.getName());
            } else {
                LOG.warn("JCacheConcurrentMapAdapter does not wrap a Hazelcast IMap: {}", internalMap.getClass().getName());
            }
        }
        registerInfrastructureMetrics(registry);
    }

    private void registerInfrastructureMetrics(MeterRegistry registry) {
        if (hazelcastInstanceInitializer != null) {
            final Cluster cluster = hazelcastInstanceInitializer.getInstance().getCluster();
            Tags tags = Tags.of(
                    "name", hazelcastInstanceInitializer.getInstance().getName(),
                    "specificCommunication", Boolean.toString(specificCommunication)
            );
            Gauge.builder("cache.cluster.remotes", cluster, c -> c.getMembers().size() - 1)
                    .description("Number of hazelcast participants not counting this one")
                    .tags(tags)
                    .register(registry);
        }
    }

    public void setHazelcastInstanceInitializer(HazelcastInstanceInitializer hazelcastInstanceInitializer) {
        this.hazelcastInstanceInitializer = hazelcastInstanceInitializer;
    }

    public void setSpecificCommunication(boolean specificCommunication) {
        this.specificCommunication = specificCommunication;
    }
}