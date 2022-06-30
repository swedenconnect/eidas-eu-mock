package eu.eidas.auth.engine.configuration.dom;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import eu.eidas.util.Preconditions;

/**
 * EncryptionConfiguration
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
public final class EncryptionConfiguration {

    private final boolean responseEncryptionMandatory;

    private final boolean checkedValidityPeriod;

    private final boolean disallowedSelfSignedCertificate;

    /**
     * There can be more than one decryption private key and associated certificate because of overlaps.
     */
    @Nonnull
    private final ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates;

    /**
     * The certificates of the trusted peers, which can be used to encrypt a response to be sent to them.
     */
    private final ImmutableSet<X509Certificate> encryptionCertificates;

    @Nullable
    private final String dataEncryptionAlgorithm;

    @Nullable
    private final String keyEncryptionAlgorithm;

    @Nullable
    private final String jcaProviderName;

    @Nullable
    private final String encryptionAlgorithmWhiteList;

    public EncryptionConfiguration(boolean checkedValidityPeriod,
                                   boolean disallowedSelfSignedCertificate,
                                   boolean responseEncryptionMandatory,
                                   @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates,
                                   @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                                   @Nullable String dataEncryptionAlgorithm,
                                   @Nullable String keyEncryptionAlgorithm,
                                   @Nullable String jcaProviderName,
                                   @Nullable String encryptionAlgorithmWhiteList) {
        this.checkedValidityPeriod = checkedValidityPeriod;
        this.disallowedSelfSignedCertificate = disallowedSelfSignedCertificate;
        this.responseEncryptionMandatory = responseEncryptionMandatory;
        this.decryptionKeyAndCertificates = decryptionKeyAndCertificates;
        this.encryptionCertificates = encryptionCertificates;
        this.dataEncryptionAlgorithm = dataEncryptionAlgorithm;
        this.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
        this.jcaProviderName = jcaProviderName;
        this.encryptionAlgorithmWhiteList = encryptionAlgorithmWhiteList;
    }

    @Nullable
    public String getDataEncryptionAlgorithm() {
        return dataEncryptionAlgorithm;
    }

    /**
     * There can be more than one decryption private key and associated certificate because of time-validity overlaps.
     */
    @Nonnull
    public ImmutableSet<KeyStore.PrivateKeyEntry> getDecryptionKeyAndCertificates() {
        return decryptionKeyAndCertificates;
    }

    @Nullable
    public String getEncryptionAlgorithmWhiteList() {
        return encryptionAlgorithmWhiteList;
    }

    @Nonnull
    public ImmutableSet<X509Certificate> getEncryptionCertificates() {
        return encryptionCertificates;
    }

    @Nullable
    public String getJcaProviderName() {
        return jcaProviderName;
    }

    @Nullable
    public String getKeyEncryptionAlgorithm() {
        return keyEncryptionAlgorithm;
    }

    public boolean isCheckedValidityPeriod() {
        return checkedValidityPeriod;
    }

    public boolean isDisallowedSelfSignedCertificate() {
        return disallowedSelfSignedCertificate;
    }

    public boolean isResponseEncryptionMandatory() {
        return responseEncryptionMandatory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EncryptionConfiguration that = (EncryptionConfiguration) o;

        if (responseEncryptionMandatory != that.responseEncryptionMandatory) {
            return false;
        }
        if (checkedValidityPeriod != that.checkedValidityPeriod) {
            return false;
        }
        if (disallowedSelfSignedCertificate != that.disallowedSelfSignedCertificate) {
            return false;
        }
        if (decryptionKeyAndCertificates != null ? !decryptionKeyAndCertificates.equals(
                that.decryptionKeyAndCertificates) : that.decryptionKeyAndCertificates != null) {
            return false;
        }
        if (encryptionCertificates != null ? !encryptionCertificates.equals(that.encryptionCertificates)
                                           : that.encryptionCertificates != null) {
            return false;
        }
        if (dataEncryptionAlgorithm != null ? !dataEncryptionAlgorithm.equals(that.dataEncryptionAlgorithm)
                                            : that.dataEncryptionAlgorithm != null) {
            return false;
        }
        if (keyEncryptionAlgorithm != null ? !keyEncryptionAlgorithm.equals(that.keyEncryptionAlgorithm)
                                           : that.keyEncryptionAlgorithm != null) {
            return false;
        }
        if (jcaProviderName != null ? !jcaProviderName.equals(that.jcaProviderName) : that.jcaProviderName != null) {
            return false;
        }
        return encryptionAlgorithmWhiteList != null ? encryptionAlgorithmWhiteList.equals(
                that.encryptionAlgorithmWhiteList) : that.encryptionAlgorithmWhiteList == null;

    }

    @Override
    public int hashCode() {
        int result = (responseEncryptionMandatory ? 1 : 0);
        result = 31 * result + (checkedValidityPeriod ? 1 : 0);
        result = 31 * result + (disallowedSelfSignedCertificate ? 1 : 0);
        result = 31 * result + (decryptionKeyAndCertificates != null ? decryptionKeyAndCertificates.hashCode() : 0);
        result = 31 * result + (encryptionCertificates != null ? encryptionCertificates.hashCode() : 0);
        result = 31 * result + (dataEncryptionAlgorithm != null ? dataEncryptionAlgorithm.hashCode() : 0);
        result = 31 * result + (keyEncryptionAlgorithm != null ? keyEncryptionAlgorithm.hashCode() : 0);
        result = 31 * result + (jcaProviderName != null ? jcaProviderName.hashCode() : 0);
        result = 31 * result + (encryptionAlgorithmWhiteList != null ? encryptionAlgorithmWhiteList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EncryptionConfiguration{" +
                "responseEncryptionMandatory=" + responseEncryptionMandatory +
                ", checkedValidityPeriod=" + checkedValidityPeriod +
                ", disallowedSelfSignedCertificate=" + disallowedSelfSignedCertificate +
                ", decryptionKeyAndCertificates=" + decryptionKeyAndCertificates +
                ", encryptionCertificates=" + encryptionCertificates +
                ", dataEncryptionAlgorithm='" + dataEncryptionAlgorithm + '\'' +
                ", keyEncryptionAlgorithm='" + keyEncryptionAlgorithm + '\'' +
                ", jcaProviderName='" + jcaProviderName + '\'' +
                ", encryptionAlgorithmWhiteList='" + encryptionAlgorithmWhiteList + '\'' +
                '}';
    }
}
