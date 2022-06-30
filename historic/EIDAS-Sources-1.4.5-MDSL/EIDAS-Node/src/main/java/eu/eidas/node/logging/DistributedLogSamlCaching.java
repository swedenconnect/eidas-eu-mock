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

package eu.eidas.node.logging;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import eu.eidas.auth.commons.cache.HazelcastInstanceInitializer;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * implements a caching service using hazelcast
 */
public class DistributedLogSamlCaching extends AbstractLogSamlCaching {

    protected ConcurrentMap<String, LogSamlHolder> map;
    protected String cacheName;
    protected HazelcastInstanceInitializer hazelcastInstanceInitializer;

    @Override
    protected Map<String, LogSamlHolder> getMap(){
        if (map == null) {
            if (getCacheName() == null) {
                throw new InvalidParameterEIDASException("Distributed Cache Configuration mismatch");
            }
            HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName(hazelcastInstanceInitializer.getHazelcastInstanceName());
            return instance.getMap(getCacheName());
        }
        return map;
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
