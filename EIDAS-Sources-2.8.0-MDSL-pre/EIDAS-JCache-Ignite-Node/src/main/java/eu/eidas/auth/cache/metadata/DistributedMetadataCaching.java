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
package eu.eidas.auth.cache.metadata;
import eu.eidas.auth.cache.IgniteInstanceInitializerNode;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import org.apache.ignite.Ignite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;

/**
 * implements a caching service using ignite
 */
public class DistributedMetadataCaching extends AbstractMetadataCaching {

    private static final Logger LOG = LoggerFactory.getLogger(DistributedMetadataCaching.class.getName());

    protected Cache<String, EidasMetadataParametersI> cache;

    protected String cacheName;

    protected IgniteInstanceInitializerNode igniteInstanceInitializer;

    @Override
    protected Cache<String, EidasMetadataParametersI> getCache(){
        if (getCacheName() == null) {
            LOG.debug("Cache Name is null");
            throw new InvalidParameterEIDASException("Distributed Cache Configuration mismatch");
        }

        final Ignite instance = igniteInstanceInitializer.getInstance();
        Cache<String, EidasMetadataParametersI> cache = instance.cache(cacheName);

        if (null == cache) {
            LOG.debug("Cache is null");
            throw new InvalidParameterEIDASException("Distributed Cache Configuration mismatch");
        }

        return cache;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public void setIgniteInstanceInitializer(IgniteInstanceInitializerNode igniteInstanceInitializer) {
        this.igniteInstanceInitializer = igniteInstanceInitializer;
    }

}
