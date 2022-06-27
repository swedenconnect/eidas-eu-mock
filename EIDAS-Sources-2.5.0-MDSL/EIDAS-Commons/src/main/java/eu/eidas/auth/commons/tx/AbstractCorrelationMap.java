/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.auth.commons.tx;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.cache.ConcurrentMapService;
import eu.eidas.util.Preconditions;

/**
 * Base implementation of the {@link CorrelationMap} interface based on a ConcurrentMap.
 * <p>
 * Subclass can provide a distributable ConcurrentMap variant (such as HazelCast or Apache Ignite) or an in-memory one
 * using {@link java.util.concurrent.ConcurrentHashMap}.
 *
 * @param <T> the type of the object being stored in the CorrelationMap (e.g. a request).
 * @since 1.1
 */
public abstract class AbstractCorrelationMap<T> implements CorrelationMap<T> {

    @Nonnull
    protected volatile ConcurrentMap<String, T> map;

    protected AbstractCorrelationMap(@Nonnull ConcurrentMapService concurrentMapService) {
        Preconditions.checkNotNull(concurrentMapService, "concurrentMapService");
        setMap(concurrentMapService.getConfiguredMapCache());
    }

    @Nullable
    @Override
    public final T get(@Nonnull String id) {
        return map.get(id);
    }

    @Nullable
    @Override
    public final T put(@Nonnull String id, @Nonnull T value) {
        return map.put(id, value);
    }

    @Nullable
    @Override
    public final T remove(@Nonnull String id) {
        return map.remove(id);
    }

    protected void setMap(ConcurrentMap<String, T> cacheMap) {
        Preconditions.checkNotNull(cacheMap, "cacheMap");
        this.map = cacheMap;
    }
}
