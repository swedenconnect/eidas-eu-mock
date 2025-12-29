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

import eu.eidas.ignite.IgniteInstanceInitializer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A {@link MeterBinder} implementation that binds metrics for Apache Ignite caches
 * to a Micrometer {@link io.micrometer.core.instrument.MeterRegistry}.
 * <p>
 * It collects statistics such as size, hits, misses, puts, and removals for each
 * configured cache by accessing their live {@link org.apache.ignite.IgniteCache} instances.
 */
public class IgniteCacheMetricsBinder implements MeterBinder {

    private static final Logger LOG = LoggerFactory.getLogger(IgniteCacheMetricsBinder.class);

    private final List<Cache> caches;

    private Ignite igniteInfrastructure;

    private boolean specificCommunication;

    public IgniteCacheMetricsBinder(List<Cache> cacheList) {
        this.caches = cacheList;
    }

    public IgniteCacheMetricsBinder(CacheManager cacheManager) {
        this.caches = StreamSupport.stream(cacheManager.getCacheNames().spliterator(), true)
                .map(cacheManager::getCache)
                .collect(Collectors.toList());
    }

    /**
     * Registers Apache Ignite cache metrics to the provided {@link MeterRegistry}.
     * <p>
     * This method iterates through all configured caches and registers metrics such as size, hits, misses, puts, and removals.
     * <p>
     * If a cache is an instance of {@link org.apache.ignite.IgniteCache}, its metrics are registered.
     * Otherwise, the cache type is logged as unknown.
     * <p>
     * If a cache instance is null, it is skipped to prevent unnecessary errors.
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
            if (jCache instanceof IgniteCache<?, ?>) {
                registerIgniteMetrics((IgniteCache<?, ?>) jCache, registry);
            } else {
                LOG.warn("Unknown cache type: {}", jCache.getClass().getName());
            }
        }
        registerInfrastructureMetrics(registry);
    }

    /**
     * Registers metrics for a given Apache Ignite cache instance in the provided {@link MeterRegistry}.
     * <p>
     * This method binds various cache-related metrics, including size, hits, misses, puts, and removals,
     * using Micrometer's {@link Gauge} meters.
     * <p>
     * The metrics are tagged with the cache name for better identification in monitoring tools.
     *
     * @param cache    The {@link IgniteCache} instance to register metrics for.
     * @param registry The {@link MeterRegistry} where the metrics will be registered.
     */
    private void registerIgniteMetrics(IgniteCache<?, ?> cache, MeterRegistry registry) {
        Tags tags = Tags.of("cache", cache.getName());
        Gauge.builder("ignite.cache.size", cache, c -> c.sizeLong())
                .description("Number of entries in the cache")
                .tags(tags)
                .register(registry);

        Gauge.builder("ignite.cache.hits", cache, c -> c.metrics().getCacheHits())
                .description("Number of cache hits")
                .tags(tags)
                .register(registry);

        Gauge.builder("ignite.cache.misses", cache, c -> c.metrics().getCacheMisses())
                .description("Number of cache misses")
                .tags(tags)
                .register(registry);

        Gauge.builder("ignite.cache.puts", cache, c -> c.metrics().getCachePuts())
                .description("Number of cache puts")
                .tags(tags)
                .register(registry);

        Gauge.builder("ignite.cache.removals", cache, c -> c.metrics().getCacheRemovals())
                .description("Number of cache removals")
                .tags(tags)
                .register(registry);

        Gauge.builder("ignite.cache.evictions", cache, c -> c.metrics().getCacheEvictions())
                .description("Number of cache removals")
                .tags(tags)
                .register(registry);
    }

    private void registerInfrastructureMetrics(MeterRegistry registry) {
        if(igniteInfrastructure != null) {
            final IgniteCluster cluster = igniteInfrastructure.cluster();
            Tags tags = Tags.of(
                    "name", igniteInfrastructure.name(),
                    "specificCommunication", Boolean.toString(specificCommunication)
            );
            Gauge.builder("cache.cluster.remotes", cluster, c -> c.forRemotes().nodes().size())
                    .description("Number of ignite participants not counting this one")
                    .tags(tags)
                    .register(registry);
        }
    }

    public void setIgniteInstanceInitializer(IgniteInstanceInitializer igniteInstanceInitializer) {
        igniteInfrastructure = igniteInstanceInitializer.getInstance();
    }

    public void setSpecificCommunication(boolean specificCommunication) {
        this.specificCommunication = specificCommunication;
    }
}
