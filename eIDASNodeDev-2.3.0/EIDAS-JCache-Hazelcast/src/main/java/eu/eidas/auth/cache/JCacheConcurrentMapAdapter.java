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

package eu.eidas.auth.cache;


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
import java.util.concurrent.ConcurrentMap;

/**
 * This class is an implementation of an Adapter Design Pattern to adapt a {@link ConcurrentMap<K, V>} to the {@link Cache<K, V>}.
 * <p>
 * Uses, when possible, similar methods of {@link ConcurrentMap<K, V>}.
 * When not possible, implements code to achieve the description of each method in {@link Cache<K, V>} API as much as possible.
 *
 * @param <K> the key instance
 * @param <V> the value instance
 */
public class JCacheConcurrentMapAdapter<K, V> implements Cache<K, V> {


    private ConcurrentMap<K, V> concurrentMap;

    JCacheConcurrentMapAdapter(ConcurrentMap<K, V> map) {
        this.concurrentMap = map;
    }

    @Override
    public V get(K k) {
        return this.concurrentMap.get(k);
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> set) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(K k) {
        return this.concurrentMap.containsKey(k);
    }

    @Override
    public void loadAll(Set<? extends K> set, boolean b, CompletionListener completionListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(K k, V v) {
        this.concurrentMap.put(k, v);
    }

    @Override
    public V getAndPut(K k, V v) {
        V existentValue = this.concurrentMap.get(k);

        if (null != existentValue) {
            this.concurrentMap.remove(k);
        }

        this.concurrentMap.put(k, v);

        return existentValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        this.concurrentMap.putAll(map);
    }

    @Override
    public boolean putIfAbsent(K k, V v) {
        if (!this.concurrentMap.containsKey(k)) {
            this.concurrentMap.put(k, v);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean remove(K k) {

        this.concurrentMap.remove(k);
        return false;
    }

    @Override
    public boolean remove(K k, V v) {
        return this.concurrentMap.remove(k, v);
    }

    @Override
    public V getAndRemove(K k) {
        if (this.concurrentMap.containsKey(k)) {
            V oldValue = this.concurrentMap.get(k);
            this.concurrentMap.remove(k);
            return oldValue;
        } else {
            return null;
        }
    }

    @Override
    public boolean replace(K k, V v, V v1) {
        V existentValue = this.concurrentMap.get(k);

        if (existentValue.equals(v)) {
            this.concurrentMap.replace(k, v1);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean replace(K k, V v) {
        V value = this.concurrentMap.get(k);

        if (null != value) {
            this.concurrentMap.replace(k, v);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public V getAndReplace(K k, V v) {
        if (this.concurrentMap.containsKey(k)) {
            V oldValue = this.concurrentMap.get(k);
            this.concurrentMap.put(k, v);
            return oldValue;
        } else {
            return null;
        }
    }

    @Override
    public void removeAll(Set<? extends K> set) {
        this.concurrentMap.keySet().removeAll(set);
    }

    @Override
    public void removeAll() {
        this.concurrentMap.keySet().removeIf(key -> true);
    }

    @Override
    public void clear() {
        this.concurrentMap.clear();
    }

    @Override
    public <C extends Configuration<K, V>> C getConfiguration(Class<C> aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T invoke(K k, EntryProcessor<K, V, T> entryProcessor, Object... objects) throws EntryProcessorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> set, EntryProcessor<K, V, T> entryProcessor, Object... objects) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CacheManager getCacheManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T unwrap(Class<T> aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        throw new UnsupportedOperationException();
    }
}
