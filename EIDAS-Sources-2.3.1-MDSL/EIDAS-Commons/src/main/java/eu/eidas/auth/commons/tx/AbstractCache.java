/*
 * Copyright (c) 2018 by European Commission
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
package eu.eidas.auth.commons.tx;

import eu.eidas.auth.commons.cache.ConcurrentCacheService;
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Base implementation of the {@link Cache} interface.
 *
 * @since 2.3
 * @param <K> the key
 * @param <V> the value
 */
public abstract class AbstractCache<K,V> implements Cache<K,V> {

    @Nonnull
    protected volatile Cache<K, V> cache;

    protected AbstractCache(@Nonnull ConcurrentCacheService concurrentCacheService) {
        Preconditions.checkNotNull(concurrentCacheService, "concurrentCacheService");
        Cache configuredMapCache = concurrentCacheService.getConfiguredCache();
        setCache(configuredMapCache);
    }

    protected void setCache(@Nonnull Cache<K, V> cache) {
        Preconditions.checkNotNull(cache, "cache");
        this.cache = cache;
    }

    @Override
    public V get(K k) {
        return this.cache.get(k);
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> set) {
        return this.cache.getAll(set);
    }

    @Override
    public boolean containsKey(K k) {
        return this.cache.containsKey(k);
    }

    @Override
    public void loadAll(Set<? extends K> set, boolean b, CompletionListener completionListener) {
        this.cache.loadAll(set, b, completionListener);
    }

    @Override
    public void put(K k, V v) {
        this.cache.put(k, v);

    }

    @Override
    public V getAndPut(K k, V v) {
        return this.cache.getAndPut(k, v);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        this.cache.putAll(map);
    }

    @Override
    public boolean putIfAbsent(K k, V v) {
        return this.cache.putIfAbsent(k,v);
    }

    @Override
    public boolean remove(K k) {
        return this.cache.remove(k);
    }

    @Override
    public boolean remove(K k, V v) {
        return this.cache.remove(k,v);
    }

    @Override
    public V getAndRemove(K k) {
        return this.cache.getAndRemove(k);
    }

    @Override
    public boolean replace(K k, V v, V v1) {
        return this.cache.replace(k, v, v1);
    }

    @Override
    public boolean replace(K k, V v) {
        return this.replace(k,v);
    }

    @Override
    public V getAndReplace(K k, V v) {
        return this.cache.getAndReplace(k, v);
    }

    @Override
    public void removeAll(Set<? extends K> set) {
        this.cache.removeAll(set);
    }

    @Override
    public void removeAll() {
        this.cache.removeAll();
    }

    @Override
    public void clear() {
        this.cache.clear();
    }

    @Override
    public <C extends Configuration<K, V>> C getConfiguration(Class<C> aClass) {
        return this.cache.getConfiguration(aClass);
    }

    @Override
    public <T> T invoke(K k, EntryProcessor<K, V, T> entryProcessor, Object... objects) throws EntryProcessorException {
        return this.invoke(k, entryProcessor, objects);
    }

    @Override
    public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> set, EntryProcessor<K, V, T> entryProcessor, Object... objects) {
        return this.cache.invokeAll(set, entryProcessor, objects);
    }

    @Override
    public String getName() {
        return this.cache.getName();
    }

    @Override
    public CacheManager getCacheManager() {
        return this.cache.getCacheManager();
    }

    @Override
    public void close() {
        this.cache.close();
    }

    @Override
    public boolean isClosed() {
        return this.cache.isClosed();
    }

    @Override
    public <T> T unwrap(Class<T> aClass) {
        return this.cache.unwrap(aClass);
    }

    @Override
    public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        this.cache.registerCacheEntryListener(cacheEntryListenerConfiguration);
    }

    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        cache.deregisterCacheEntryListener(cacheEntryListenerConfiguration);
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return cache.iterator();
    }
}
