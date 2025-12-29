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

import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.IMetadataCachingService;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

class ValidityCheckingMetadataCachingService implements IMetadataCachingService {

    private static final Logger LOG = LoggerFactory.getLogger(ValidityCheckingMetadataCachingService.class);

    private final IMetadataCachingService internalCachingService;
    private final MetadataClockI metadataClock;

    public ValidityCheckingMetadataCachingService(
            MetadataClockI clockI,
            IMetadataCachingService internalCachingService
    ) {
        this.internalCachingService = internalCachingService;
        this.metadataClock = clockI;
    }

    @Override
    public EidasMetadataParametersI getEidasMetadataParameters(String url) throws EIDASMetadataProviderException {
        final EidasMetadataParametersI metadataParameters = internalCachingService.getEidasMetadataParameters(url);
        if (!isValidUntilNow(metadataParameters, metadataClock)) {
            removeFromCache(url);
            LOG.info("Clearing expired metadata from cache for the url {}", url);
            return null;
        }
        return metadataParameters;
    }

    @Override
    public void putEidasMetadataParameters(String url, EidasMetadataParametersI eidasMetadataParameters) {
        internalCachingService.putEidasMetadataParameters(url, eidasMetadataParameters);
    }

    /**
     * Validate "validUntil" field of Metadata (EidasMetadataParametersI form) against saml engine clock
     *
     * @param eidasMetadataParameters the instance of {@link EidasMetadataParametersI}
     * @param metadataClock used to validate
     * @return {@code true} if the field validUntil is not set
     */
    protected boolean isValidUntilNow(EidasMetadataParametersI eidasMetadataParameters, MetadataClockI metadataClock) {
        return Optional.ofNullable(eidasMetadataParameters)
                .map(EidasMetadataParametersI::getValidUntil)
                .map(validUntil -> metadataClock.getCurrentTime().isBefore(validUntil))
                .orElse(true);
    }

    protected void removeFromCache(String url) {
        if (null != internalCachingService) {
            internalCachingService.putEidasMetadataParameters(url, null);
        }
    }
}
