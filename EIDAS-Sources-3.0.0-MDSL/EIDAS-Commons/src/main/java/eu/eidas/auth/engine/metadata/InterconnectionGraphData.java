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

package eu.eidas.auth.engine.metadata;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

public class InterconnectionGraphData implements Serializable {

    private final Collection<String> metadataUrlHashes;
    private final Collection<String> trustedCertificateIdentifiers;
    private final Collection<String> supportedEncryptionAlgorithms;
    private final boolean whitelistEnabled;
    private final boolean displayTrustStoreAttribute;

    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder {

        private Collection<String> metadataUrlHashes;
        private Collection<String> trustedCertificateIdentifiers;
        private Collection<String> supportedEncryptionAlgorithms;
        private boolean whitelistEnabled;
        private boolean displayTrustStoreAttribute;

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            metadataUrlHashes = copy.metadataUrlHashes;
            trustedCertificateIdentifiers = copy.trustedCertificateIdentifiers;
            supportedEncryptionAlgorithms = copy.supportedEncryptionAlgorithms;
        }

        public Builder(@Nonnull InterconnectionGraphData copy) {
            metadataUrlHashes = copy.metadataUrlHashes;
            trustedCertificateIdentifiers = copy.trustedCertificateIdentifiers;
            supportedEncryptionAlgorithms = copy.supportedEncryptionAlgorithms;
        }

        @Nonnull
        public Builder metadataUrlHashes(final Collection<String> metadataUrlHashes) {
            this.metadataUrlHashes = metadataUrlHashes;
            return this;
        }

        @Nonnull
        public Builder trustedCertificateIdentifiers(final Collection<String> trustedCertificateIdentifiers) {
            this.trustedCertificateIdentifiers = trustedCertificateIdentifiers;
            return this;
        }

        @Nonnull
        public Builder supportedEncryptionAlgorithms(final Collection<String> supportedEncryptionAlgorithms) {
            this.supportedEncryptionAlgorithms = supportedEncryptionAlgorithms;
            return this;
        }

        @Nonnull
        public Builder whitelistEnabled(final boolean whitelistEnabled) {
            this.whitelistEnabled = whitelistEnabled;
            return this;
        }

        @Nonnull
        public Builder displayTrustStoreAttribute(final boolean displayTrustStoreAttribute) {
            this.displayTrustStoreAttribute = displayTrustStoreAttribute;
            return this;
        }

        @Nonnull
        public InterconnectionGraphData build() {
            return new InterconnectionGraphData(this);
        }

    }

    private InterconnectionGraphData(@Nonnull Builder builder) {
        metadataUrlHashes = builder.metadataUrlHashes == null ? null : new HashSet<>(builder.metadataUrlHashes);
        trustedCertificateIdentifiers = builder.trustedCertificateIdentifiers == null ? null : new HashSet<>(builder.trustedCertificateIdentifiers);
        supportedEncryptionAlgorithms = builder.supportedEncryptionAlgorithms == null ? null : new HashSet<>(builder.supportedEncryptionAlgorithms);
        whitelistEnabled = builder.whitelistEnabled;
        displayTrustStoreAttribute = builder.displayTrustStoreAttribute;
    }

    InterconnectionGraphData(@Nonnull InterconnectionGraphData copy) {
        metadataUrlHashes = copy.metadataUrlHashes == null ? null : new HashSet<>(copy.metadataUrlHashes);
        trustedCertificateIdentifiers = copy.trustedCertificateIdentifiers == null ? null : new HashSet<>(copy.trustedCertificateIdentifiers);
        supportedEncryptionAlgorithms = copy.supportedEncryptionAlgorithms == null ? null : new HashSet<>(copy.supportedEncryptionAlgorithms);
        whitelistEnabled = copy.whitelistEnabled;
        displayTrustStoreAttribute = copy.displayTrustStoreAttribute;
    }

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public static Builder builder(@Nonnull Builder copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static Builder builder(@Nonnull InterconnectionGraphData copy) {
        return new Builder(copy);
    }

    public Collection<String> getMetadataUrlHashes() {
        return this.metadataUrlHashes;
    }

    public Collection<String> getTrustedCertificateIdentifiers() {
        return this.trustedCertificateIdentifiers;
    }

    public Collection<String> getSupportedEncryptionAlgorithms() {
        return this.supportedEncryptionAlgorithms;
    }

    public boolean isWhitelistEnabled() {
        return this.whitelistEnabled;
    }

    public boolean isDisplayTrustStoreAttribute() {
        return this.displayTrustStoreAttribute;
    }
}
