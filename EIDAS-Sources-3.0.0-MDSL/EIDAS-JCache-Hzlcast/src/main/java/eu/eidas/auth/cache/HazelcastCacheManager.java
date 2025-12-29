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

package eu.eidas.auth.cache;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

public class HazelcastCacheManager implements CacheManager {

    private static final Logger LOG = LoggerFactory.getLogger(HazelcastCacheManager.class.getName());

    private final Map<String, String> functionalToImplCacheName;
    private final HazelcastInstanceInitializer hazelcastInstanceInitializerNode;

    public HazelcastCacheManager(HazelcastInstanceInitializer initializer, Map<String, String> functionalToCacheName) {
        this.functionalToImplCacheName = functionalToCacheName;
        this.hazelcastInstanceInitializerNode = initializer;
    }

    @Override
    public <K, V> Cache<K, V> getCache(String functionalName) {
        final HazelcastInstance instance = hazelcastInstanceInitializerNode.getInstance();
        final String implementationCacheName = functionalToImplCacheName.get(functionalName);

        if (implementationCacheName == null) {
            LOG.debug("{} Cache {} is null", functionalName, implementationCacheName);
            throw new IllegalStateException("Distributed Cache Configuration mismatch");
        }

        final IMap<Object, Object> map = instance.getMap(implementationCacheName);
        final JCacheConcurrentMapAdapter jCacheConcurrentMapAdapter = new JCacheConcurrentMapAdapter(map);
        return jCacheConcurrentMapAdapter;
    }

    @Override
    public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(String cacheName, C configuration) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<String> getCacheNames() {
        return functionalToImplCacheName.keySet();
    }

    @Override
    public void destroyCache(String functionalName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enableManagement(String cacheName, boolean enabled) {

    }

    @Override
    public void enableStatistics(String cacheName, boolean enabled) {

    }

    @Override
    public CachingProvider getCachingProvider() {
        return null;
    }

    @Override
    public URI getURI() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        return null;
    }
}
