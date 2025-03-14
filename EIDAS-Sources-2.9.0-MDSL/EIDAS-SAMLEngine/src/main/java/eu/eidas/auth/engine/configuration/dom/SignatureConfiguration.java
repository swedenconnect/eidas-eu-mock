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
package eu.eidas.auth.engine.configuration.dom;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    private boolean enableCertificateRevocationChecking;

    private boolean enableCertificateRevocationSoftFail;
    
    @Nonnull
    private List<KeyStore.PrivateKeyEntry> signatureKeyAndCertificates;

    @Nonnull
    private Set<X509Certificate> trustedCertificates;

    @Nullable
    private String signatureAlgorithm;

    @Nullable
    private String signatureKeyProvider;

    @Nullable
    private String signatureAlgorithmWhiteList;

    @Nullable
    private Set<String> signatureAlgorithmWhitelistSet;

    @Nullable
    private String digestMethodAlgorithm;

    @Nullable
    private Set<String> digestMethodAlgorithmWhiteList;

    @Nullable
    private String metadataSignatureAlgorithm;

    @Nullable
    private String metadataSignatureKeyProvider;

    @Nullable
    private KeyStore.PrivateKeyEntry metadataSigningKeyAndCertificate;

    @Nullable
    private Set<X509Certificate> metadataKeystoreCertificates;

    private SignatureConfiguration() {}

    public String getMetadataSignatureAlgorithm() {
        return this.metadataSignatureAlgorithm;
    }

    @Nullable
    public String getMetadataSignatureKeyProvider() {
        return metadataSignatureKeyProvider;
    }

    @Nullable
    public KeyStore.PrivateKeyEntry getMetadataSigningKeyAndCertificate() {
        return metadataSigningKeyAndCertificate;
    }

    @Nullable
    public Set<X509Certificate> getMetadataKeystoreCertificates() {
        return metadataKeystoreCertificates;
    }

    @Nullable
    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    @Nullable
    public String getSignatureKeyProvider() {
        return signatureKeyProvider;
    }

    @Nullable
    public String getSignatureAlgorithmWhiteList() {
        return signatureAlgorithmWhiteList;
    }

    @Nullable
    public Set<String> getSignatureAlgorithmWhitelist() {
        return signatureAlgorithmWhitelistSet;
    }

    @Nullable
    public String getDigestAlgorithm() {
        return digestMethodAlgorithm;
    }

    @Nullable
    public Set<String> getDigestMethodAlgorithmWhiteList() {
        return digestMethodAlgorithmWhiteList;
    }

    @Deprecated
    @Nonnull
    public KeyStore.PrivateKeyEntry getSignatureKeyAndCertificate() {
        return signatureKeyAndCertificates.get(0);
    }
    @Nonnull
    public List<KeyStore.PrivateKeyEntry> getSignatureKeyAndCertificates() {
        return signatureKeyAndCertificates;
    }

    @Nonnull
    public Set<X509Certificate> getTrustedCertificates() {
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

    public boolean isEnableCertificateRevocationChecking() {
        return this.enableCertificateRevocationChecking;
    }

    public boolean isEnableCertificateRevocationSoftFail() {
        return this.enableCertificateRevocationSoftFail;
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
        if (enableCertificateRevocationSoftFail != that.enableCertificateRevocationSoftFail) {
            return false;
        }
        if (enableCertificateRevocationChecking != that.enableCertificateRevocationChecking) {
            return false;
        }
        if (signatureKeyAndCertificates != null ? !signatureKeyAndCertificates.equals(that.signatureKeyAndCertificates)
                                               : that.signatureKeyAndCertificates != null) {
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
        if (signatureKeyProvider != null ? !signatureKeyProvider.equals(that.signatureKeyProvider)
                                       : that.signatureKeyProvider != null) {
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
        if (metadataSignatureKeyProvider != null ? ! metadataSignatureKeyProvider.equals(that.metadataSignatureKeyProvider)
                : that.metadataSignatureKeyProvider != null) {
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
        result = 31 * result + (enableCertificateRevocationChecking ? 1 : 0);
        result = 31 * result + (enableCertificateRevocationSoftFail ? 1 : 0);
        result = 31 * result + (signatureKeyAndCertificates != null ? signatureKeyAndCertificates.hashCode() : 0);
        result = 31 * result + (trustedCertificates != null ? trustedCertificates.hashCode() : 0);
        result = 31 * result + (signatureAlgorithm != null ? signatureAlgorithm.hashCode() : 0);
        result = 31 * result + (signatureAlgorithmWhiteList != null ? signatureAlgorithmWhiteList.hashCode() : 0);
        result = 31 * result + (digestMethodAlgorithm != null ? digestMethodAlgorithm.hashCode() : 0);
        result = 31 * result + (digestMethodAlgorithmWhiteList != null ? digestMethodAlgorithmWhiteList.hashCode() : 0);
        result = 31 * result + (metadataSignatureAlgorithm != null ? metadataSignatureAlgorithm.hashCode() : 0);
        result = 31 * result + (metadataSigningKeyAndCertificate != null ? metadataSigningKeyAndCertificate.hashCode() : 0);
        result = 31 * result + (metadataKeystoreCertificates != null ? metadataKeystoreCertificates.hashCode() : 0);
        result = 31 * result + (signatureKeyProvider != null ? signatureKeyProvider.hashCode() : 0);
        result = 31 * result + (metadataSignatureKeyProvider != null ? metadataSignatureKeyProvider.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SignatureConfiguration{" +
                "checkedValidityPeriod=" + checkedValidityPeriod +
                ", disallowedSelfSignedCertificate=" + disallowedSelfSignedCertificate +
                ", responseSignAssertions=" + responseSignAssertions +
                ", signatureKeyAndCertificate=" + signatureKeyAndCertificates +
                ", trustedCertificates=" + trustedCertificates +
                ", enableCertificateRevocationChecking=" + enableCertificateRevocationChecking +
                ", enableCertificateRevocationSoftFail=" + enableCertificateRevocationSoftFail +
                ", signatureAlgorithm='" + signatureAlgorithm + '\'' +
                ", signatureKeyProvider='" + signatureKeyProvider + '\'' +
                ", signatureAlgorithmWhiteList='" + signatureAlgorithmWhiteList + '\'' +
                ", digestMethodAlgorithm='" + digestMethodAlgorithm + '\'' +
                ", digestMethodAlgorithmWhiteList='" + digestMethodAlgorithmWhiteList + '\'' +
                ", metadataSignatureAlgorithm='" + metadataSignatureAlgorithm + '\'' +
                ", metadataSignatureKeyProvider='" + metadataSignatureKeyProvider + '\'' +
                ", metadataSigningKeyAndCertificate=" + metadataSigningKeyAndCertificate +
                ", metadataKeystoreCertificates=" + metadataKeystoreCertificates +
                '}';
    }

    public static final class Builder {
        private SignatureConfiguration signatureConfiguration;

        public Builder() {
            this.signatureConfiguration = new SignatureConfiguration();
        }
        public Builder(SignatureConfiguration signatureConfiguration) {
            this.signatureConfiguration = signatureConfiguration;
        }

        public Builder setMetadataSignatureAlgorithm(String metadataSignatureAlgorithm) {
            signatureConfiguration.metadataSignatureAlgorithm = metadataSignatureAlgorithm;
            return this;
        }

        public Builder setMetadataSignatureKeyProvider(String metadataSignatureKeyProvider) {
            signatureConfiguration.metadataSignatureKeyProvider = metadataSignatureKeyProvider;
            return this;
        }

        public Builder setMetadataSigningKeyAndCertificate(KeyStore.PrivateKeyEntry metadataSigningKeyAndCertificate) {
            signatureConfiguration.metadataSigningKeyAndCertificate = metadataSigningKeyAndCertificate;
            return this;
        }

        public Builder setMetadataKeystoreCertificates(Set<X509Certificate> metadataKeystoreCertificates) {
            signatureConfiguration.metadataKeystoreCertificates = metadataKeystoreCertificates;
            return this;
        }

        public Builder setSignatureAlgorithm(String signatureAlgorithm) {
            signatureConfiguration.signatureAlgorithm = signatureAlgorithm;
            return this;
        }

        public Builder setSignatureKeyProvider(String signatureKeyProvider) {
            signatureConfiguration.signatureKeyProvider = signatureKeyProvider;
            return this;
        }

        public Builder setSignatureAlgorithmWhiteList(String signatureAlgorithmWhiteList) {
            signatureConfiguration.signatureAlgorithmWhiteList = signatureAlgorithmWhiteList;
            signatureConfiguration.signatureAlgorithmWhitelistSet = new LinkedHashSet<>(EidasStringUtil.getDistinctValues(signatureAlgorithmWhiteList));
            return this;
        }

        public Builder setSignatureAlgorithmWhitelist(Set<String> signatureAlgorithmWhitelist) {
            signatureConfiguration.signatureAlgorithmWhitelistSet = signatureAlgorithmWhitelist;
            signatureConfiguration.signatureAlgorithmWhiteList = String.join(EIDASValues.SEMICOLON.toString(), signatureAlgorithmWhitelist);
            return this;
        }

        public Builder setDigestAlgorithm(String digestMethodAlgorithm) {
            signatureConfiguration.digestMethodAlgorithm = digestMethodAlgorithm;
            return this;
        }

        public Builder setDigestMethodAlgorithmWhiteList(Set<String> digestMethodAlgorithmWhiteList) {
            signatureConfiguration.digestMethodAlgorithmWhiteList = digestMethodAlgorithmWhiteList;
            return this;
        }

        public Builder setSignatureKeyAndCertificate(List<KeyStore.PrivateKeyEntry> signatureKeyAndCertificates) {
            signatureConfiguration.signatureKeyAndCertificates = signatureKeyAndCertificates;
            return this;
        }

        public Builder setTrustedCertificates(Set<X509Certificate> trustedCertificates) {
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

        public Builder setEnableCertificateRevocationChecking(boolean enableCertificateRevocationChecking) {
            signatureConfiguration.enableCertificateRevocationChecking = enableCertificateRevocationChecking;
            return this;
        }

        public Builder setEnableCertificateRevocationSoftFail(boolean enableCertificateRevocationSoftFail) {
            signatureConfiguration.enableCertificateRevocationSoftFail = enableCertificateRevocationSoftFail;
            return this;
        }

        public SignatureConfiguration build() {
            Preconditions.checkNotNull(signatureConfiguration.signatureKeyAndCertificates, "signatureKeyAndCertificate");
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
            copy.signatureKeyAndCertificates = this.signatureConfiguration.signatureKeyAndCertificates;
            copy.trustedCertificates = this.signatureConfiguration.trustedCertificates;
            copy.signatureAlgorithm = this.signatureConfiguration.signatureAlgorithm;
            copy.signatureKeyProvider = this.signatureConfiguration.signatureKeyProvider;
            copy.signatureAlgorithmWhiteList = this.signatureConfiguration.signatureAlgorithmWhiteList;
            copy.signatureAlgorithmWhitelistSet = this.signatureConfiguration.signatureAlgorithmWhitelistSet;
            copy.metadataSignatureAlgorithm = this.signatureConfiguration.metadataSignatureAlgorithm;
            copy.metadataSignatureKeyProvider = this.signatureConfiguration.metadataSignatureKeyProvider;
            copy.metadataSigningKeyAndCertificate = this.signatureConfiguration.metadataSigningKeyAndCertificate;
            copy.metadataKeystoreCertificates = this.signatureConfiguration.metadataKeystoreCertificates;
            copy.digestMethodAlgorithm = this.signatureConfiguration.digestMethodAlgorithm;
            copy.digestMethodAlgorithmWhiteList = this.signatureConfiguration.digestMethodAlgorithmWhiteList;
            copy.enableCertificateRevocationChecking = this.signatureConfiguration.enableCertificateRevocationChecking;
            copy.enableCertificateRevocationSoftFail = this.signatureConfiguration.enableCertificateRevocationSoftFail;
            return copy;
        }
    }
}
