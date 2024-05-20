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
package eu.eidas.auth.cache;

import com.google.common.cache.CacheBuilder;
import eu.eidas.auth.commons.cache.ConcurrentCacheService;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of the cache provider - this implementation is default one, not production ready, there is no clustering and expiration implemented.
 */
public class ConcurrentMapJcacheServiceDefaultImpl implements ConcurrentCacheService {


    private Long expireAfterAccess = 1800L;
    private Long maximumSize = 1000000L;

    @Override
    public javax.cache.Cache getConfiguredCache() {
        ConcurrentMap objectObjectConcurrentMap = CacheBuilder.newBuilder()
                .expireAfterAccess(getExpireAfterAccess(), TimeUnit.SECONDS)
                .maximumSize(getMaximumSize()).build().asMap();

        return new JCacheConcurrentMapAdapter(objectObjectConcurrentMap);
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
