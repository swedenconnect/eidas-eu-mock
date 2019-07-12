/*
 * Copyright (c) 2019 by European Commission
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
 * limitations under the Licence
 */

package eu.eidas.auth.cache;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import eu.eidas.auth.commons.cache.ConcurrentCacheService;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;

/**
 * Hazelcast Distributed hashMap implementation of the cache provider.
 */
public class ConcurrentMapServiceDistributedImpl implements ConcurrentCacheService {

    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentMapServiceDistributedImpl.class.getName());

    protected String cacheName;

    protected HazelcastInstanceInitializer hazelcastInstanceInitializer;

    /**
     * Method to get the cache from the {@link ConcurrentMapServiceDistributedImpl#cacheName}.
     *
     * @return the instance of {@link Cache} related to {@link ConcurrentMapServiceDistributedImpl#cacheName}.
     */
    @Override
    public Cache getConfiguredCache() {
        final String cacheName = getCacheName();
        if (cacheName == null) {
            LOG.debug("The cacheName parameter is null.");
            throw new InvalidParameterEIDASException(null,"Distributed Cache Configuration mismatch");
        }

        final String hazelcastInstanceName = hazelcastInstanceInitializer.getHazelcastInstanceName();
        final HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName(hazelcastInstanceName);
        final IMap<Object, Object> map = instance.getMap(cacheName);
        final JCacheConcurrentMapAdapter jCacheConcurrentMapAdapter = new JCacheConcurrentMapAdapter(map);

        return jCacheConcurrentMapAdapter;
    }

    /**
     * Getter for {@link ConcurrentMapServiceDistributedImpl#cacheName}.
     *
     * @return the {@link ConcurrentMapServiceDistributedImpl#cacheName}
     */
    public String getCacheName() {
        return cacheName;
    }

    /**
     * Setter for  {@link ConcurrentMapServiceDistributedImpl#cacheName}.
     *
     * @param cacheName the instance to be set.
     */
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    /**
     * Getter for {@link HazelcastInstanceInitializer}
     *
     * @return the {@link HazelcastInstanceInitializer}
     */
    public HazelcastInstanceInitializer getHazelcastInstanceInitializer() {
        return hazelcastInstanceInitializer;
    }

    /**
     * Setter for {@link ConcurrentMapServiceDistributedImpl#hazelcastInstanceInitializer}.
     *
     * @param hazelcastInstanceInitializer the instance to be set.
     */
    public void setHazelcastInstanceInitializer(HazelcastInstanceInitializer hazelcastInstanceInitializer) {
        this.hazelcastInstanceInitializer = hazelcastInstanceInitializer;
    }

}
