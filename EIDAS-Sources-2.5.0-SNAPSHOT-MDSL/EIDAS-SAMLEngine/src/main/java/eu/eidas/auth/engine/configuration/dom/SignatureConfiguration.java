/*
 * Copyright (c) 2020 by European Commission
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

import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import eu.eidas.util.Preconditions;

/**
 * SignatureConfiguration
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
public final class SignatureConfiguration {

    private final boolean checkedValidityPeriod;

    private final boolean disallowedSelfSignedCertificate;

    private final boolean responseSignAssertions;

    private final boolean requestSignWithKey;
    
    private final boolean responseSignWithKey;
    
    @Nonnull
    private final KeyStore.PrivateKeyEntry signatureKeyAndCertificate;

    @Nonnull
    private final ImmutableSet<X509Certificate> trustedCertificates;

    @Nullable
    private final String signatureAlgorithm;

    @Nullable
    private final String signatureAlgorithmWhiteList;

    @Nullable
    private final String digestMethodAlgorithm;

    @Nullable
    private final ImmutableSet<String> digestMethodAlgorithmWhiteList;

    @Nullable
    private final KeyStore.PrivateKeyEntry metadataSigningKeyAndCertificate;

    @Nullable
    private final ImmutableSet<X509Certificate> metadataKeystoreCertificates;

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
        this.metadataSigningKeyAndCertificate = metadataSigningKeyAndCertificate;
        this.metadataKeystoreCertificates = metadataKeystoreCertificates;
        this.digestMethodAlgorithm = digestMethodAlgorithm;
        this.digestMethodAlgorithmWhiteList = digestMethodAlgorithmWhiteList;
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
                ", metadataSigningKeyAndCertificate=" + metadataSigningKeyAndCertificate +
                ", metadataKeystoreCertificates=" + metadataKeystoreCertificates +
                '}';
    }
}
