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
 * limitations under the Licence.
 *
 */
package eu.eidas.auth.commons.cache;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

/**
 * Hazelcast Distributed hashMap implementation of the cache provider.
 */
public class ConcurrentMapServiceDistributedImpl implements ConcurrentMapService {
    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentMapServiceDistributedImpl.class.getName());

    protected String cacheName;
    protected HazelcastInstanceInitializer hazelcastInstanceInitializer;

    @Override
    public ConcurrentMap getConfiguredMapCache() {
        if (getCacheName() == null) {
            throw new InvalidParameterEIDASException("Distributed Cache Configuration mismatch");
        }
        HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName(hazelcastInstanceInitializer.getHazelcastInstanceName());
        return instance.getMap(getCacheName());
    }

    public String getCacheName() {
        return cacheName;
    }
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public HazelcastInstanceInitializer getHazelcastInstanceInitializer() {
        return hazelcastInstanceInitializer;
    }

    public void setHazelcastInstanceInitializer(HazelcastInstanceInitializer hazelcastInstanceInitializer) {
        this.hazelcastInstanceInitializer = hazelcastInstanceInitializer;
    }

}
