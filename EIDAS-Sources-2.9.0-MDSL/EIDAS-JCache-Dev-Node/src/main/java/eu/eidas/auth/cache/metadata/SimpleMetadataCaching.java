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
package eu.eidas.auth.cache.metadata;

import com.google.common.cache.CacheBuilder;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * implements a caching service using hashmap
 */
public final class SimpleMetadataCaching extends AbstractMetadataCaching {

    private final ConcurrentMap<String, EidasMetadataParametersI> map;

    public SimpleMetadataCaching(long retentionPeriod) {
        map = CacheBuilder.newBuilder()
                .expireAfterAccess(retentionPeriod, TimeUnit.SECONDS)
                .maximumSize(10000L).<String, EidasMetadataParametersI>build().asMap();
    }

    @Override
    protected Map<String, EidasMetadataParametersI> getMap() {
        return map;
    }

}
