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
package eu.eidas.auth.engine.metadata.impl;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base implementation of the {@link MetadataFetcherI} interface with caching capabilities.
 *
 * @since 1.1
 */
public abstract class AbstractCachingMetadataFetcher extends BaseMetadataFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCachingMetadataFetcher.class);

    @Nonnull
    @Override
    public EidasMetadataParametersI getEidasMetadata(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner, MetadataClockI metadataClock)
            throws EIDASMetadataException {

        EidasMetadataParametersI metadataParameters = getFromCache(url, metadataSigner);

        if (null != metadataParameters && !isValidUntilNow(metadataParameters, metadataClock)) {
            // cached metadata has expired:
            removeFromCache(url);
            metadataParameters = null;
            LOG.info("Clearing expired metadata from cache for the url " + url);
        }

        if (metadataParameters == null && isHttpRetrievalEnabled() && isAllowedMetadataUrl(url)) {

            EidasMetadataParametersI fetchedMetadataParameters = super.getEidasMetadata(url, metadataSigner, metadataClock);

            if (isValidUntilNow(fetchedMetadataParameters, metadataClock)) {
                putInCache(url, fetchedMetadataParameters);
                metadataParameters = fetchedMetadataParameters;
                LOG.info("Obtained entity descriptor from metadata retrieved from url " + url);
            } else {
                metadataParameters = null;
                LOG.info("Invalid (expired) metadata received from " + url);
            }
        }

        if (metadataParameters == null) {
            throw new EIDASMetadataProviderException(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorCode(),
                    EidasErrorKey.SAML_ENGINE_NO_METADATA.errorMessage(),
                    "No entity descriptor for URL " + url);
        }
        return metadataParameters;
    }

    @Nullable
    protected abstract EidasMetadataParametersI getFromCache(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner) throws EIDASMetadataException;

    protected boolean isHttpRetrievalEnabled() {
        return true;
    }

    protected abstract void putInCache(@Nonnull String url, @Nonnull EidasMetadataParametersI metadataParameters);

    protected abstract void removeFromCache(@Nonnull String url);
}
