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
