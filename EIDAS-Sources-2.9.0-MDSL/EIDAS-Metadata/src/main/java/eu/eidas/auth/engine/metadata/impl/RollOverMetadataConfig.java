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

package eu.eidas.auth.engine.metadata.impl;

import eu.eidas.auth.engine.metadata.IMetadataCachingService;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;

/**
 * Holds configuration for a virtual roll-over of metadata between the still valid cache and a newly fetched version.
 */
public class RollOverMetadataConfig {

    private IMetadataCachingService rolloverCache;
    private long cacheLifeSpan;
    private boolean reloadCacheOnMessageEnabled;

    public IMetadataCachingService getRolloverCache(MetadataSignerI metadataSigner, MetadataClockI metadataClock) {
        return new ValidityCheckingMetadataCachingService(metadataClock, rolloverCache);
    }

    public void setRolloverCache(IMetadataCachingService rolloverCache) {
        this.rolloverCache = rolloverCache;
    }

    public long getCacheLifeSpan() {
        return cacheLifeSpan;
    }

    public void setCacheLifeSpan(long cacheLifeSpan) {
        if (cacheLifeSpan < 0) throw new IllegalArgumentException("Lifespan cannot be negative");
        this.cacheLifeSpan = cacheLifeSpan;
    }

    public boolean isReloadCacheOnMessageCertificateEnabled() {
        return reloadCacheOnMessageEnabled;
    }

    public void setReloadCacheOnMessageEnabled(boolean reloadCacheOnMessageEnabled) {
        this.reloadCacheOnMessageEnabled = reloadCacheOnMessageEnabled;
    }
}
