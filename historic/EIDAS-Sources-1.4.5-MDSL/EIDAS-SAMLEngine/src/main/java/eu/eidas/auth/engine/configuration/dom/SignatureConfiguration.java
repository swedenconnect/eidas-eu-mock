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

    @Nonnull
    private final KeyStore.PrivateKeyEntry signatureKeyAndCertificate;

    @Nonnull
    private final ImmutableSet<X509Certificate> trustedCertificates;

    @Nullable
    private final String signatureAlgorithm;

    @Nullable
    private final String signatureAlgorithmWhiteList;

    @Nullable
    private final KeyStore.PrivateKeyEntry metadataSigningKeyAndCertificate;

    public SignatureConfiguration(boolean checkedValidityPeriod,
                                  boolean disallowedSelfSignedCertificate,
                                  boolean responseSignAssertions,
                                  @Nonnull KeyStore.PrivateKeyEntry signatureKeyAndCertificate,
                                  @Nonnull ImmutableSet<X509Certificate> trustedCertificates,
                                  @Nullable String signatureAlgorithm,
                                  @Nullable String signatureAlgorithmWhiteList,
                                  @Nullable KeyStore.PrivateKeyEntry metadataSigningKeyAndCertificate) {
        Preconditions.checkNotNull(signatureKeyAndCertificate, "signatureKeyAndCertificate");
        Preconditions.checkNotNull(trustedCertificates, "trustedCertificates");

        this.checkedValidityPeriod = checkedValidityPeriod;
        this.disallowedSelfSignedCertificate = disallowedSelfSignedCertificate;
        this.responseSignAssertions = responseSignAssertions;
        this.signatureKeyAndCertificate = signatureKeyAndCertificate;
        this.trustedCertificates = trustedCertificates;
        this.signatureAlgorithm = signatureAlgorithm;
        this.signatureAlgorithmWhiteList = signatureAlgorithmWhiteList;
        this.metadataSigningKeyAndCertificate = metadataSigningKeyAndCertificate;
    }

    @Nullable
    public KeyStore.PrivateKeyEntry getMetadataSigningKeyAndCertificate() {
        return metadataSigningKeyAndCertificate;
    }

    @Nullable
    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    @Nullable
    public String getSignatureAlgorithmWhiteList() {
        return signatureAlgorithmWhiteList;
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
        result = 31 * result + (metadataSigningKeyAndCertificate != null ? metadataSigningKeyAndCertificate.hashCode()
                                                                         : 0);
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
                ", metadataSigningKeyAndCertificate=" + metadataSigningKeyAndCertificate +
                '}';
    }
}
