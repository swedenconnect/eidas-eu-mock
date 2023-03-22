/*
 * Copyright (c) 2021 by European Commission
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
package eu.eidas.auth.engine.configuration.dom;

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * SignatureConfiguration
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
public final class SignatureConfiguration {

    private boolean checkedValidityPeriod;

    private boolean disallowedSelfSignedCertificate;

    private boolean responseSignAssertions;

    private boolean requestSignWithKey;
    
    private boolean responseSignWithKey;
    
    @Nonnull
    private KeyStore.PrivateKeyEntry signatureKeyAndCertificate;

    @Nonnull
    private ImmutableSet<X509Certificate> trustedCertificates;

    @Nullable
    private String signatureAlgorithm;

    @Nullable
    private String signatureAlgorithmWhiteList;

    @Nullable
    private ImmutableSet<String> signatureAlgorithmWhitelistSet;

    @Nullable
    private String digestMethodAlgorithm;

    @Nullable
    private ImmutableSet<String> digestMethodAlgorithmWhiteList;

    @Nullable
    private String metadataSignatureAlgorithm;

    @Nullable
    private KeyStore.PrivateKeyEntry metadataSigningKeyAndCertificate;

    @Nullable
    private ImmutableSet<X509Certificate> metadataKeystoreCertificates;

    private SignatureConfiguration() {}

    /**
     * Constructor of SignatureConfiguration
     * @param checkedValidityPeriod boolean to indicated if validity period needs to be check
     * @param disallowedSelfSignedCertificate boolean to indicate if self signed certificate should be disallowed
     * @param responseSignAssertions boolean to indicate if response's assertions should be signed
     * @param requestSignWithKey boolean to indicate if the signature of the request should present the key instead of
     *                           the certificate in the KeyInfo
     * @param responseSignWithKey boolean to indicate if the signature of the response should present the key instead of
     *      *                           the certificate in the KeyInfo
     * @param signatureKeyAndCertificate the PrivateKeyEntry for the signature of requests and responses
     * @param trustedCertificates the set of certificates that should be trusted
     * @param signatureAlgorithm the signature algorithm to be used to sign request and response
     * @param signatureAlgorithmWhiteList the whitelist of signature algorithms
     * @param metadataSigningKeyAndCertificate the PrivateKeyEntry for the signature of metadata
     * @param metadataKeystoreCertificates the certificates chain of the metadata PrivateKeyEntry.
     *
     * @deprecated Shoud use the {@link SignatureConfiguration.Builder} instead
     */
    @Deprecated
    public SignatureConfiguration(boolean checkedValidityPeriod,
                                  boolean disallowedSelfSignedCertificate,
                                  boolean responseSignAssertions,
                                  boolean requestSignWithKey,
                                  boolean responseSignWithKey,
                                  @Nonnull KeyStore.PrivateKeyEntry signatureKeyAndCertificate,
                                  @Nonnull ImmutableSet<X509Certificate> trustedCertificates,
                                  @Nullable String signatureAlgorithm,
                                  @Nullable String signatureAlgorithmWhiteList,
                                  @Nullable KeyStore.PrivateKeyEntry metadataSigningKeyAndCertificate,
                                  ImmutableSet<X509Certificate> metadataKeystoreCertificates) {
        this(checkedValidityPeriod,
                disallowedSelfSignedCertificate,
                responseSignAssertions,
                requestSignWithKey,
                responseSignWithKey,
                signatureKeyAndCertificate,
                trustedCertificates,
                signatureAlgorithm,
                signatureAlgorithmWhiteList,
                null,
                null,
                metadataSigningKeyAndCertificate,
                metadataKeystoreCertificates);

    }

    /**
     * @deprecated Shoud use the {@link SignatureConfiguration.Builder} instead
     */
    @Deprecated
    public SignatureConfiguration(boolean checkedValidityPeriod,
                                boolean disallowedSelfSignedCertificate,
                                boolean responseSignAssertions,
                                boolean requestSignWithKey,
                                boolean responseSignWithKey,
                                @Nonnull KeyStore.PrivateKeyEntry signatureKeyAndCertificate,
                                @Nonnull ImmutableSet<X509Certificate> trustedCertificates,
                                @Nullable String signatureAlgorithm,
                                @Nullable String signatureAlgorithmWhiteList,
                                @Nullable String digestMethodAlgorithm,
                                @Nullable ImmutableSet<String> digestMethodAlgorithmWhiteList,
                                @Nullable KeyStore.PrivateKeyEntry metadataSigningKeyAndCertificate,
                                ImmutableSet<X509Certificate> metadataKeystoreCertificates) {
        Preconditions.checkNotNull(signatureKeyAndCertificate, "signatureKeyAndCertificate");
        Preconditions.checkNotNull(trustedCertificates, "trustedCertificates");

        this.checkedValidityPeriod = checkedValidityPeriod;
        this.disallowedSelfSignedCertificate = disallowedSelfSignedCertificate;
        this.responseSignAssertions = responseSignAssertions;
        this.requestSignWithKey=requestSignWithKey;
        this.responseSignWithKey=responseSignWithKey;
        this.signatureKeyAndCertificate = signatureKeyAndCertificate;
        this.trustedCertificates = trustedCertificates;
        this.signatureAlgorithm = signatureAlgorithm;
        this.signatureAlgorithmWhiteList = signatureAlgorithmWhiteList;
        this.signatureAlgorithmWhitelistSet = ImmutableSet.copyOf(EidasStringUtil.getDistinctValues(signatureAlgorithmWhiteList));
        this.metadataSignatureAlgorithm = signatureAlgorithm;
        this.metadataSigningKeyAndCertificate = metadataSigningKeyAndCertificate;
        this.metadataKeystoreCertificates = metadataKeystoreCertificates;
        this.digestMethodAlgorithm = digestMethodAlgorithm;
        this.digestMethodAlgorithmWhiteList = digestMethodAlgorithmWhiteList;
    }

    public String getMetadataSignatureAlgorithm() {
        return this.metadataSignatureAlgorithm;
    }

    @Nullable
    public KeyStore.PrivateKeyEntry getMetadataSigningKeyAndCertificate() {
        return metadataSigningKeyAndCertificate;
    }

    @Nullable
    public ImmutableSet<X509Certificate> getMetadataKeystoreCertificates() {
        return metadataKeystoreCertificates;
    }

    @Nullable
    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    @Nullable
    public String getSignatureAlgorithmWhiteList() {
        return signatureAlgorithmWhiteList;
    }

    @Nullable
    public ImmutableSet<String> getSignatureAlgorithmWhitelist() {
        return signatureAlgorithmWhitelistSet;
    }

    @Nullable
    public String getDigestAlgorithm() {
        return digestMethodAlgorithm;
    }

    @Nullable
    public ImmutableSet<String> getDigestMethodAlgorithmWhiteList() {
        return digestMethodAlgorithmWhiteList;
    }

    @Nonnull
    public KeyStore.PrivateKeyEntry getSignatureKeyAndCertificate() {
        return signatureKeyAndCertificate;
    }

    @Nonnull
    public ImmutableSet<X509Certificate> getTrustedCertificates() {
        return trustedCertificates;
    }

    public boolean isCheckedValidityPeriod() {
        return checkedValidityPeriod;
    }

    public boolean isDisallowedSelfSignedCertificate() {
        return disallowedSelfSignedCertificate;
    }

    public boolean isResponseSignAssertions() {
        return responseSignAssertions;
    }

    public boolean isRequestSignWithKey() {
        return requestSignWithKey;
    }

    public boolean isResponseSignWithKey() {
        return responseSignWithKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SignatureConfiguration that = (SignatureConfiguration) o;

        if (checkedValidityPeriod != that.checkedValidityPeriod) {
            return false;
        }
        if (disallowedSelfSignedCertificate != that.disallowedSelfSignedCertificate) {
            return false;
        }
        if (responseSignAssertions != that.responseSignAssertions) {
            return false;
        }
        if (signatureKeyAndCertificate != null ? !signatureKeyAndCertificate.equals(that.signatureKeyAndCertificate)
                                               : that.signatureKeyAndCertificate != null) {
            return false;
        }
        if (trustedCertificates != null ? !trustedCertificates.equals(that.trustedCertificates)
                                        : that.trustedCertificates != null) {
            return false;
        }
        if (signatureAlgorithm != null ? !signatureAlgorithm.equals(that.signatureAlgorithm)
                                       : that.signatureAlgorithm != null) {
            return false;
        }
        if (signatureAlgorithmWhiteList != null ? !signatureAlgorithmWhiteList.equals(that.signatureAlgorithmWhiteList)
                                                : that.signatureAlgorithmWhiteList != null) {
            return false;
        }
        if (digestMethodAlgorithm != null ? !digestMethodAlgorithm.equals(that.digestMethodAlgorithm)
                : that.digestMethodAlgorithm != null) {
            return false;
        }
        if (digestMethodAlgorithmWhiteList != null ? !digestMethodAlgorithmWhiteList.equals(that.digestMethodAlgorithmWhiteList)
                : that.digestMethodAlgorithmWhiteList != null) {
            return false;
        }
        if (metadataSignatureAlgorithm != null ? ! metadataSignatureAlgorithm.equals(that.metadataSignatureAlgorithm)
                : that.metadataSignatureAlgorithm != null) {
            return false;
        }
        return metadataSigningKeyAndCertificate != null ? metadataSigningKeyAndCertificate.equals(
                that.metadataSigningKeyAndCertificate) : that.metadataSigningKeyAndCertificate == null;
    }

    @Override
    public int hashCode() {
        int result = (checkedValidityPeriod ? 1 : 0);
        result = 31 * result + (disallowedSelfSignedCertificate ? 1 : 0);
        result = 31 * result + (responseSignAssertions ? 1 : 0);
        result = 31 * result + (signatureKeyAndCertificate != null ? signatureKeyAndCertificate.hashCode() : 0);
        result = 31 * result + (trustedCertificates != null ? trustedCertificates.hashCode() : 0);
        result = 31 * result + (signatureAlgorithm != null ? signatureAlgorithm.hashCode() : 0);
        result = 31 * result + (signatureAlgorithmWhiteList != null ? signatureAlgorithmWhiteList.hashCode() : 0);
        result = 31 * result + (digestMethodAlgorithm != null ? digestMethodAlgorithm.hashCode() : 0);
        result = 31 * result + (digestMethodAlgorithmWhiteList != null ? digestMethodAlgorithmWhiteList.hashCode() : 0);
        result = 31 * result + (metadataSignatureAlgorithm != null ? metadataSignatureAlgorithm.hashCode() : 0);
        result = 31 * result + (metadataSigningKeyAndCertificate != null ? metadataSigningKeyAndCertificate.hashCode() : 0);
        result = 31 * result + (metadataKeystoreCertificates != null ? metadataKeystoreCertificates.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SignatureConfiguration{" +
                "checkedValidityPeriod=" + checkedValidityPeriod +
                ", disallowedSelfSignedCertificate=" + disallowedSelfSignedCertificate +
                ", responseSignAssertions=" + responseSignAssertions +
                ", signatureKeyAndCertificate=" + signatureKeyAndCertificate +
                ", trustedCertificates=" + trustedCertificates +
                ", signatureAlgorithm='" + signatureAlgorithm + '\'' +
                ", signatureAlgorithmWhiteList='" + signatureAlgorithmWhiteList + '\'' +
                ", digestMethodAlgorithm='" + digestMethodAlgorithm + '\'' +
                ", digestMethodAlgorithmWhiteList='" + digestMethodAlgorithmWhiteList + '\'' +
                ", metadataSignatureAlgorithm='" + metadataSignatureAlgorithm + '\'' +
                ", metadataSigningKeyAndCertificate=" + metadataSigningKeyAndCertificate +
                ", metadataKeystoreCertificates=" + metadataKeystoreCertificates +
                '}';
    }

    public static final class Builder {
        private SignatureConfiguration signatureConfiguration;

        public Builder() {
            this.signatureConfiguration = new SignatureConfiguration();
        }

        public Builder setMetadataSignatureAlgorithm(String metadataSignatureAlgorithm) {
            signatureConfiguration.metadataSignatureAlgorithm = metadataSignatureAlgorithm;
            return this;
        }

        @Nullable
        public Builder setMetadataSigningKeyAndCertificate(KeyStore.PrivateKeyEntry metadataSigningKeyAndCertificate) {
            signatureConfiguration.metadataSigningKeyAndCertificate = metadataSigningKeyAndCertificate;
            return this;
        }

        @Nullable
        public Builder setMetadataKeystoreCertificates(ImmutableSet<X509Certificate> metadataKeystoreCertificates) {
            signatureConfiguration.metadataKeystoreCertificates = metadataKeystoreCertificates;
            return this;
        }

        @Nullable
        public Builder setSignatureAlgorithm(String signatureAlgorithm) {
            signatureConfiguration.signatureAlgorithm = signatureAlgorithm;
            return this;
        }

        @Nullable
        public Builder setSignatureAlgorithmWhiteList(String signatureAlgorithmWhiteList) {
            signatureConfiguration.signatureAlgorithmWhiteList = signatureAlgorithmWhiteList;
            signatureConfiguration.signatureAlgorithmWhitelistSet = ImmutableSet.copyOf(EidasStringUtil.getDistinctValues(signatureAlgorithmWhiteList));
            return this;
        }

        @Nullable
        public Builder setSignatureAlgorithmWhitelist(ImmutableSet<String> signatureAlgorithmWhitelist) {
            signatureConfiguration.signatureAlgorithmWhitelistSet = signatureAlgorithmWhitelist;
            signatureConfiguration.signatureAlgorithmWhiteList = String.join(EIDASValues.SEMICOLON.toString(), signatureAlgorithmWhitelist);
            return this;
        }

        @Nullable
        public Builder setDigestAlgorithm(String digestMethodAlgorithm) {
            signatureConfiguration.digestMethodAlgorithm = digestMethodAlgorithm;
            return this;
        }

        @Nullable
        public Builder setDigestMethodAlgorithmWhiteList(ImmutableSet<String> digestMethodAlgorithmWhiteList) {
            signatureConfiguration.digestMethodAlgorithmWhiteList = digestMethodAlgorithmWhiteList;
            return this;
        }

        @Nonnull
        public Builder setSignatureKeyAndCertificate(KeyStore.PrivateKeyEntry signatureKeyAndCertificate) {
            signatureConfiguration.signatureKeyAndCertificate = signatureKeyAndCertificate;
            return this;
        }

        @Nonnull
        public Builder setTrustedCertificates(ImmutableSet<X509Certificate> trustedCertificates) {
            signatureConfiguration.trustedCertificates = trustedCertificates;
            return this;
        }

        public Builder setCheckedValidityPeriod(boolean isCheckedValidityPeriod) {
            signatureConfiguration.checkedValidityPeriod = isCheckedValidityPeriod;
            return this;
        }

        public Builder setDisallowedSelfSignedCertificate(boolean isDisallowedSelfSignedCertificate) {
            signatureConfiguration.disallowedSelfSignedCertificate = isDisallowedSelfSignedCertificate;
            return this;
        }

        public Builder setResponseSignAssertions(boolean isResponseSignAssertions) {
            signatureConfiguration.responseSignAssertions = isResponseSignAssertions;
            return this;
        }

        public Builder setRequestSignWithKey(boolean isRequestSignWithKey) {
            signatureConfiguration.requestSignWithKey = isRequestSignWithKey;
            return this;
        }

        public Builder setResponseSignWithKey(boolean isResponseSignWithKey) {
            signatureConfiguration.responseSignWithKey = isResponseSignWithKey;
            return this;
        }

        public SignatureConfiguration build() {
            Preconditions.checkNotNull(signatureConfiguration.signatureKeyAndCertificate, "signatureKeyAndCertificate");
            Preconditions.checkNotNull(signatureConfiguration.trustedCertificates, "trustedCertificates");

            if (signatureConfiguration.metadataSignatureAlgorithm == null) {
                signatureConfiguration.metadataSignatureAlgorithm = signatureConfiguration.signatureAlgorithm;
            }

            return buildInternal();
        }

        /**
         * Private build method used to not return the same instance when builder is used multiple times.
         * @return a soft copy of the SignatureConfiguration instance variable.
         */
        private SignatureConfiguration buildInternal() {
            SignatureConfiguration copy = new SignatureConfiguration();
            copy.checkedValidityPeriod = this.signatureConfiguration.checkedValidityPeriod;
            copy.disallowedSelfSignedCertificate = this.signatureConfiguration.disallowedSelfSignedCertificate;
            copy.responseSignAssertions = this.signatureConfiguration.responseSignAssertions;
            copy.requestSignWithKey= this.signatureConfiguration.requestSignWithKey;
            copy.responseSignWithKey= this.signatureConfiguration.responseSignWithKey;
            copy.signatureKeyAndCertificate = this.signatureConfiguration.signatureKeyAndCertificate;
            copy.trustedCertificates = this.signatureConfiguration.trustedCertificates;
            copy.signatureAlgorithm = this.signatureConfiguration.signatureAlgorithm;
            copy.signatureAlgorithmWhiteList = this.signatureConfiguration.signatureAlgorithmWhiteList;
            copy.signatureAlgorithmWhitelistSet = this.signatureConfiguration.signatureAlgorithmWhitelistSet;
            copy.metadataSignatureAlgorithm = this.signatureConfiguration.metadataSignatureAlgorithm;
            copy.metadataSigningKeyAndCertificate = this.signatureConfiguration.metadataSigningKeyAndCertificate;
            copy.metadataKeystoreCertificates = this.signatureConfiguration.metadataKeystoreCertificates;
            copy.digestMethodAlgorithm = this.signatureConfiguration.digestMethodAlgorithm;
            copy.digestMethodAlgorithmWhiteList = this.signatureConfiguration.digestMethodAlgorithmWhiteList;
            return copy;
        }
    }
}
