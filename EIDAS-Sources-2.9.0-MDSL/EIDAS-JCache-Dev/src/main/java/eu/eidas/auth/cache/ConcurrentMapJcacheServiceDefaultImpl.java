/*
 * Copyright (c) 2024 by European Commission
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

import com.google.common.cache.CacheBuilder;
import eu.eidas.auth.commons.cache.ConcurrentCacheService;
import eu.eidas.auth.commons.cache.ConcurrentMapService;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of the cache provider using ExpiringBoundedCache.
 * This implementation is not production-ready as there is no clustering.
 */
public class ConcurrentMapJcacheServiceDefaultImpl implements ConcurrentCacheService, ConcurrentMapService {

    private Long expireAfterAccess = 1800L;
    private Long maximumSize = 1000000L;

    /**
     * Obtains the JCache-compliant cache.
     *
     * @return a JCache-compliant cache view of the ExpiringBoundedCache
     */
    @Override
    public javax.cache.Cache getConfiguredCache() {
        // Returning a JCache-compliant cache view of the ExpiringBoundedCache for compatibility
        return new JCacheConcurrentMapAdapter(getConfiguredMapCache());
    }

    /**
     * Obtains the map cache.
     *
     * @return a concurrent map view of the ExpiringBoundedCache
     */
    @Override
    public ConcurrentMap getConfiguredMapCache() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(getExpireAfterAccess(), TimeUnit.SECONDS)
                .maximumSize(getMaximumSize()).build().asMap();
    }

    /**
     * Gets the expiration time after access.
     *
     * @return the expiration time in seconds
     */
    public Long getExpireAfterAccess() {
        return expireAfterAccess;
    }

    /**
     * Sets the expiration time after access.
     *
     * @param expireAfterAccess the expiration time in seconds
     */
    public void setExpireAfterAccess(Long expireAfterAccess) {
        this.expireAfterAccess = expireAfterAccess;
    }

    /**
     * Gets the maximum size of the cache.
     *
     * @return the maximum size of the cache
     */
    public Long getMaximumSize() {
        return maximumSize;
    }

    /**
     * Sets the maximum size of the cache.
     *
     * @param maximumSize the maximum size of the cache
     */
    public void setMaximumSize(Long maximumSize) {
        this.maximumSize = maximumSize;
    }

}
