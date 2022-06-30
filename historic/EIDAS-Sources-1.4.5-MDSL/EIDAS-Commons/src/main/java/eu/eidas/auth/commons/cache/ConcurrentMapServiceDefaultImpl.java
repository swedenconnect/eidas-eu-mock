package eu.eidas.auth.commons.cache;

import com.google.common.cache.CacheBuilder;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of the cache provider - this implementation is default one, not production ready, there is no clustering and expiration implemented.
 */
public class ConcurrentMapServiceDefaultImpl implements ConcurrentMapService {


    private Long expireAfterAccess = 1800L;
    private Long maximumSize = 1000000L;

    @Override
    public ConcurrentMap getConfiguredMapCache() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(getExpireAfterAccess(), TimeUnit.SECONDS)
                .maximumSize(getMaximumSize()).build().asMap();
    }

    public Long getExpireAfterAccess() {
        return expireAfterAccess;
    }

    public void setExpireAfterAccess(Long expireAfterAccess) {
        this.expireAfterAccess = expireAfterAccess;
    }

    public Long getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(Long maximumSize) {
        this.maximumSize = maximumSize;
    }

}
