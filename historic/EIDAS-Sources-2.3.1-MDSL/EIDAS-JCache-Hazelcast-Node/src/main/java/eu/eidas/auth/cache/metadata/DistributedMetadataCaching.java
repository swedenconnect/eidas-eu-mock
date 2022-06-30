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
package eu.eidas.auth.cache.metadata;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import eu.eidas.auth.cache.HazelcastInstanceInitializer;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Implements a caching service using hazelcast
 */
public class DistributedMetadataCaching extends AbstractMetadataCaching {

    /**
     * The map that holds the url,{@link EidasMetadataParametersI} pairs
     */
    protected ConcurrentMap<String, EidasMetadataParametersI> map;

    /**
     * The cache name
     */
    protected String cacheName;


    /**
     * The instance of {@link HazelcastInstanceInitializer}
     */
    protected HazelcastInstanceInitializer hazelcastInstanceInitializer;

    /**
     * Getter for the {@field map}
     *
     * Throws {@link InvalidParameterEIDASException} when both {@field map} and
     * {@link DistributedMetadataCaching#getCacheName()} are null
     *
     * @return the instance of the {@field map}
     */
    @Override
    protected Map<String, EidasMetadataParametersI> getMap(){
        if (map == null) {
            if (getCacheName() == null) {
                throw new InvalidParameterEIDASException("Distributed Cache Configuration mismatch");
            }
            HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName(hazelcastInstanceInitializer.getHazelcastInstanceName());
            return instance.getMap(getCacheName());
        }
        return map;
    }

    /**
     * Getter for the {@field cacheName }
     *
     * @return the string with the cache name
     */
    public String getCacheName() {
        return cacheName;
    }

    /**
     * Setter for the {@field cacheName}
     *
     * @param cacheName the cache name to be set in {@field cacheName}
     */
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    /**
     * Getter for the {@field hazelcastInstanceInitializer}
     *
     * @return the instance of {@link HazelcastInstanceInitializer}
     */
    public HazelcastInstanceInitializer getHazelcastInstanceInitializer() {
        return hazelcastInstanceInitializer;
    }

    /**
     * Setter for the {@field hazelcastInstanceInitializer}
     *
     * @param hazelcastInstanceInitializer to be set in {@field hazelcastInstanceInitializer}
     */
    public void setHazelcastInstanceInitializer(HazelcastInstanceInitializer hazelcastInstanceInitializer) {
        this.hazelcastInstanceInitializer = hazelcastInstanceInitializer;
    }

}
