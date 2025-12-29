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

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.IMetadataCachingService;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.security.cert.CertificateException;
import java.time.ZonedDateTime;

/**
 * Base implementation of the {@link MetadataFetcherI} interface with caching capabilities.
 *
 * @since 1.1
 */
public abstract class AbstractCachingMetadataFetcher extends BaseMetadataFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCachingMetadataFetcher.class);

    @Nonnull
    @Override
    public EidasMetadataParametersI getEidasMetadata(
            @Nonnull Issuer metadataIssuer,
            KeyInfo signingKeyInfo,
            @Nonnull MetadataSignerI metadataSigner,
            MetadataClockI metadataClock
    ) throws EIDASMetadataException {

        final String url = metadataIssuer.getValue();
        EidasMetadataParametersI metadata = getCache(metadataSigner, metadataClock).getEidasMetadataParameters(url);

        if (metadata == null) {
            metadata = fetchEidasMetadataFromSuper(metadataIssuer, signingKeyInfo, metadataSigner, metadataClock);
            getCache(metadataSigner, metadataClock).putEidasMetadataParameters(url, metadata);

            if (metadata == null) {
                throw new EIDASMetadataProviderException(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorCode(),
                        EidasErrorKey.SAML_ENGINE_NO_METADATA.errorMessage(),
                        "No entity descriptor for URL " + url);
            }
        }

        if (needsToBeMatchedToMetadata(signingKeyInfo)) {
            if (certificateBelongsToCachedMetadata(signingKeyInfo, metadata)) {
                return metadata;
            } else {
                EidasMetadataParametersI rolloverMetadataParameters = getRolloverCache(metadataSigner, metadataClock).getEidasMetadataParameters(url);
                if (rolloverMetadataParameters == null) { // system not in roll over state
                    EidasMetadataParametersI oldMetadata = metadata;
                    metadata = fetchEidasMetadataFromSuper(metadataIssuer, signingKeyInfo, metadataSigner, metadataClock); // synchronized
                    final ZonedDateTime rolloverValidity = metadataClock.getCurrentTime()
                            .plusSeconds(getRollOverMetadataConfig().getCacheLifeSpan());
                    if (oldMetadata.getValidUntil().isAfter(rolloverValidity)) { // only shorten
                        oldMetadata.setValidUntil(rolloverValidity);
                    }
                    getRolloverCache(metadataSigner, metadataClock).putEidasMetadataParameters(url, oldMetadata);
                    getCache(metadataSigner, metadataClock).putEidasMetadataParameters(url, metadata);
                } else if (certificateBelongsToCachedMetadata(signingKeyInfo, rolloverMetadataParameters)) {
                    return rolloverMetadataParameters;
                } else {
                    throw new EIDASMetadataProviderException(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorCode(),
                            EidasErrorKey.SAML_ENGINE_NO_METADATA.errorMessage(),
                            "Metadata reload on cooldown, will not fetch triggered by message " + url);
                }
                if (metadata == null) {
                    throw new EIDASMetadataProviderException(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorCode(),
                            EidasErrorKey.SAML_ENGINE_NO_METADATA.errorMessage(),
                            "No entity descriptor for URL " + url);
                }
            }
        }
        return metadata;
    }


    private boolean needsToBeMatchedToMetadata(KeyInfo msgCertificate) {
        return (getRollOverMetadataConfig() == null || getRollOverMetadataConfig().isReloadCacheOnMessageCertificateEnabled()) &&
                null != msgCertificate;
    }

    private EidasMetadataParametersI fetchEidasMetadataFromSuper(@Nonnull Issuer metadataIssuer, KeyInfo msgCertificate, @Nonnull MetadataSignerI metadataSigner, MetadataClockI metadataClock) throws EIDASMetadataException {
        final String url = metadataIssuer.getValue();
        if(isHttpRetrievalEnabled()) {
            EidasMetadataParametersI fetchedMetadataParameters = super.getEidasMetadata(metadataIssuer, msgCertificate, metadataSigner, metadataClock);
            if (isValidUntilNow(fetchedMetadataParameters, metadataClock)) {
                LOG.info("Obtained entity descriptor from metadata retrieved from url {}", url);
                return fetchedMetadataParameters;
            } else {
                LOG.info("Invalid (expired) metadata received from {}", url);
            }
        }
        return null;
    }

    private static boolean certificateBelongsToCachedMetadata(KeyInfo msgCertificate, EidasMetadataParametersI metadataParameters) {
        return metadataParameters.getRoleDescriptors().stream().anyMatch(rd -> {
            try {
                return null != CertificateUtil.getMatchingCertificate(msgCertificate, rd.getSigningCertificates());
            } catch (CertificateException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private IMetadataCachingService getRolloverCache(MetadataSignerI metadataSigner, MetadataClockI metadataClock) {
        return getRollOverMetadataConfig().getRolloverCache(metadataSigner, metadataClock);
    }

    public abstract IMetadataCachingService getCache(MetadataSignerI metadataSigner, MetadataClockI metadataClock);

    public abstract RollOverMetadataConfig getRollOverMetadataConfig();

    protected boolean isHttpRetrievalEnabled() {
        return true;
    }
}
