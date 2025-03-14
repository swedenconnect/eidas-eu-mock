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
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TrustMatchingAndCacheClearingMetadataCachingService implements IMetadataCachingService {

    private static final Logger LOG = LoggerFactory.getLogger(TrustMatchingAndCacheClearingMetadataCachingService.class);

    private final IMetadataCachingService internalCachingService;
    private final MetadataSignerI metadataSigner;

    public TrustMatchingAndCacheClearingMetadataCachingService(
            MetadataSignerI metadataSigner,
            IMetadataCachingService internalCachingService
    ) {
        this.internalCachingService = internalCachingService;
        this.metadataSigner = metadataSigner;
    }

    @Override
    public EidasMetadataParametersI getEidasMetadataParameters(String url) throws EIDASMetadataProviderException {
        final EidasMetadataParametersI metadataParameters = internalCachingService.getEidasMetadataParameters(url);
        if (!isPresentInTrustStore(metadataParameters, metadataSigner)) {
            removeFromCache(url);
            LOG.info("Clearing untrusted metadata from cache for the url {}", url);
            return null;
        }
        return metadataParameters;
    }

    @Override
    public void putEidasMetadataParameters(String url, EidasMetadataParametersI eidasMetadataParameters) {
        internalCachingService.putEidasMetadataParameters(url, eidasMetadataParameters);
    }

    /**
     * Checks if the trust chain of the given metadata parameters contains a trust anchor present in the trust store.
     *
     * @param eidasMetadataParameters the instance of {@link EidasMetadataParametersI}
     * @param metadataSigner          instance of {@link MetadataSignerI}
     * @return {@code true} if at least one certificate in the trust chain is present in the trust store, {@code false} otherwise
     */
    protected boolean isPresentInTrustStore(EidasMetadataParametersI eidasMetadataParameters, MetadataSignerI metadataSigner) {
        if (null != eidasMetadataParameters) {
            return metadataSigner.checkMetadataTrustAnchorAgainstTrustStore(eidasMetadataParameters.getTrustChain());
        }
        return true;
    }

    protected void removeFromCache(String url) {
        if (null != internalCachingService) {
            internalCachingService.putEidasMetadataParameters(url, null);
        }
    }
}
