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
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataLoaderPlugin;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * The implementation of the {@link MetadataFetcherI} interface for the Node.
 *
 * @since 1.1
 */
public class CachingMetadataFetcher extends AbstractCachingMetadataFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(CachingMetadataFetcher.class);

    private boolean httpRetrievalEnabled = false;

    /**
     * when restrictHttp is true, remote metadata is accepted only through https otherwise, metadata retrieved using
     * http protocol is also accepted
     */
    private boolean restrictHttp = false;

    /**
     * SSL/TLS enabled protocols
     */
    private String tlsEnabledProtocols;

    /**
     * SSL/TLS enabled ciphers suites
     */
    private String tlsEnabledCipherSuites;

    /**
     * whether to enable the signature validation for EntityDescriptors
     */
    private boolean validateEntityDescriptorSignature = true;

    /**
     * initialized with a list of urls corresponding to EntityDescriptor not needing signature validation
     */
    private String trustedEntityDescriptors = "";

    private MetadataLoaderPlugin metadataLoaderPlugin;

    private IMetadataCachingService cache;

    private RollOverMetadataConfig rollOverMetadataConfig;

    public IMetadataCachingService getCache(MetadataSignerI metadataSigner, MetadataClockI metadataClock) {
        return new ValidityCheckingMetadataCachingService(metadataClock,
                new LocallyStoredMetadataCachingService(metadataSigner, metadataLoaderPlugin,
                        new TrustMatchingAndCacheClearingMetadataCachingService(metadataSigner, cache))
                        .setTrustedEntityDescriptors(trustedEntityDescriptors)
                        .setValidateEntityDescriptorSignature(validateEntityDescriptorSignature)
                        .setMetadataLoaderPluginEnabled(!httpRetrievalEnabled));
    }

    @Override
    public boolean isHttpRetrievalEnabled() {
        return httpRetrievalEnabled;
    }

    public boolean isValidateEidasMetadataSignature() {
        return validateEntityDescriptorSignature;
    }

    @Override
    protected boolean mustUseHttps() {
        return restrictHttp;
    }

    @Override
    protected String[] getTlsEnabledProtocols() {
        return super.getTlsEnabledProtocols(tlsEnabledProtocols);
    }

    @Override
    public String[] getTlsEnabledCiphers() {
        return super.getTlsEnabledCiphers(tlsEnabledCipherSuites);
    }

    @Override
    protected boolean mustValidateSignature(@Nonnull String url) {
        return isValidateEidasMetadataSignature() && !trustedEntityDescriptors.contains(url);
    }

    public RollOverMetadataConfig getRollOverMetadataConfig() {
        return rollOverMetadataConfig;
    }

    public void setCache(IMetadataCachingService cache) {
        this.cache = cache;
    }

    public void setRollOverMetadataConfig(RollOverMetadataConfig rollOverMetadataConfig) {
        this.rollOverMetadataConfig = rollOverMetadataConfig;
    }

    public void setMetadataLoaderPlugin(MetadataLoaderPlugin metadataLoaderPlugin) {
        this.metadataLoaderPlugin = metadataLoaderPlugin;
    }

    public void setHttpRetrievalEnabled(boolean httpRetrievalEnabled) {
        this.httpRetrievalEnabled = httpRetrievalEnabled;
    }

    public void setRestrictHttp(boolean restrictHttp) {
        this.restrictHttp = restrictHttp;
    }

    public void setTlsEnabledProtocols(String tlsEnabledProtocols) {
        this.tlsEnabledProtocols = tlsEnabledProtocols;
    }

    public void setTlsEnabledCiphers(String tlsEnabledCiphers) {
        this.tlsEnabledCipherSuites = tlsEnabledCiphers;
    }

    public void setTrustedEidasMetadataUrls(String trustedEidasMetadataUrls) {
        this.trustedEntityDescriptors = trustedEidasMetadataUrls;
    }

    public void setValidateEidasMetadataSignature(boolean validateEidasMetadataSignature) {
        this.validateEntityDescriptorSignature = validateEidasMetadataSignature;
    }
}
