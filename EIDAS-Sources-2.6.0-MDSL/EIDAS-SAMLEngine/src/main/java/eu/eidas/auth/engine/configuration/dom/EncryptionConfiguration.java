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
import java.util.Objects;

/**
 * EncryptionConfiguration
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
public final class EncryptionConfiguration {

    private boolean responseEncryptionMandatory;

    private boolean checkedValidityPeriod;

    private boolean disallowedSelfSignedCertificate;

    private boolean isAssertionEncryptWithKey;
    
    /**
     * There can be more than one decryption private key and associated certificate because of overlaps.
     */
    @Nonnull
    private ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates;

    /**
     * The certificates of the trusted peers, which can be used to encrypt a response to be sent to them.
     */
    @Nonnull
    private ImmutableSet<X509Certificate> encryptionCertificates;

    @Nullable
    private String dataEncryptionAlgorithm;

    @Nullable
    private String keyEncryptionAlgorithm;

    @Nullable
    private String keyEncryptionAlgorithmForKeyAgreement;

    @Nullable
    private String keyEncryptionAgreementMethodAlgorithm;

    @Nullable
    private String jcaProviderName;

    @Nullable
    private String encryptionAlgorithmWhiteList;

    @Nonnull
    private ImmutableSet<String> encryptionAlgorithmWhitelist;

    private EncryptionConfiguration() {}

    /**
     * @deprecated Use {@link EncryptionConfiguration.Builder#build()} instead.
     * @param checkedValidityPeriod flag to indicate if the certificate's validity period should be verified
     * @param disallowedSelfSignedCertificate the flag to verify when using self-signed X.509 certificate is not allowed
     * @param responseEncryptionMandatory the flag to verify if encryption is mandatory regardless of the country
     * @param isAssertionEncryptWithKey the flag to verify if assertion is encrypted with key
     * @param decryptionKeyAndCertificates a set with more than one decryption private key and associated certificate
     * @param encryptionCertificates a set with certificates of the trusted peers
     * @param dataEncryptionAlgorithm the algorithm to be use for data (saml assertion) encryption
     * @param keyEncryptionAlgorithm the algorithm key value for encryption
     * @param jcaProviderName the provider name value
     * @param encryptionAlgorithmWhiteList the algorithm whiteList for encryption
     */
    @Deprecated
    public EncryptionConfiguration(boolean checkedValidityPeriod,
                                   boolean disallowedSelfSignedCertificate,
                                   boolean responseEncryptionMandatory,
                                   boolean isAssertionEncryptWithKey,
                                   @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates,
                                   @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                                   @Nullable String dataEncryptionAlgorithm,
                                   @Nullable String keyEncryptionAlgorithm,
                                   @Nullable String jcaProviderName,
                                   @Nullable String encryptionAlgorithmWhiteList) {
        this(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
                isAssertionEncryptWithKey, decryptionKeyAndCertificates, encryptionCertificates,
                dataEncryptionAlgorithm, keyEncryptionAlgorithm, null, jcaProviderName,
                encryptionAlgorithmWhiteList);
    }

    /**
     * @deprecated since 2.6
     * use {@link EncryptionConfiguration.Builder#build()} instead
     */
    @Deprecated
    public EncryptionConfiguration(boolean checkedValidityPeriod,
                                   boolean disallowedSelfSignedCertificate,
                                   boolean responseEncryptionMandatory,
                                   boolean isAssertionEncryptWithKey,
                                   @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates,
                                   @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                                   @Nullable String dataEncryptionAlgorithm,
                                   @Nullable String keyEncryptionAlgorithm,
                                   @Nullable String keyEncryptionAlgorithmForKeyAgreement,
                                   @Nullable String jcaProviderName,
                                   @Nullable String encryptionAlgorithmWhiteList) {
        this.checkedValidityPeriod = checkedValidityPeriod;
        this.disallowedSelfSignedCertificate = disallowedSelfSignedCertificate;
        this.responseEncryptionMandatory = responseEncryptionMandatory;
        this.isAssertionEncryptWithKey = isAssertionEncryptWithKey;
        this.decryptionKeyAndCertificates = decryptionKeyAndCertificates;
        this.encryptionCertificates = encryptionCertificates;
        this.dataEncryptionAlgorithm = dataEncryptionAlgorithm;
        this.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
        this.keyEncryptionAlgorithmForKeyAgreement = keyEncryptionAlgorithmForKeyAgreement;
        this.jcaProviderName = jcaProviderName;
        this.encryptionAlgorithmWhiteList = encryptionAlgorithmWhiteList;
        this.encryptionAlgorithmWhitelist = ImmutableSet.copyOf(EidasStringUtil.getDistinctValues(encryptionAlgorithmWhiteList));
    }

    @Nullable
    public String getDataEncryptionAlgorithm() {
        return dataEncryptionAlgorithm;
    }

    /**
     * There can be more than one decryption private key and associated certificate because of time-validity overlaps.
     *
     * @return a {@link ImmutableSet} collection that contains {@link KeyStore.PrivateKeyEntry}.
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
    public ImmutableSet<String> getEncryptionAlgorithmWhitelist() {
        return encryptionAlgorithmWhitelist;
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

    @Nullable
    public String getKeyEncryptionAlgorithmForKeyAgreement() {
        return keyEncryptionAlgorithmForKeyAgreement;
    }

    @Nullable
    public String getKeyEncryptionAgreementMethodAlgorithm() {
        return keyEncryptionAgreementMethodAlgorithm;
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
        if (!Objects.equals(keyEncryptionAlgorithmForKeyAgreement, that.keyEncryptionAlgorithmForKeyAgreement)) {
            return false;
        }
        if (!Objects.equals(keyEncryptionAgreementMethodAlgorithm, that.keyEncryptionAgreementMethodAlgorithm)) {
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
        result = 31 * result + (keyEncryptionAlgorithmForKeyAgreement != null ? keyEncryptionAlgorithmForKeyAgreement.hashCode() : 0);
        result = 31 * result + (keyEncryptionAgreementMethodAlgorithm != null ? keyEncryptionAgreementMethodAlgorithm.hashCode() : 0);
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
                ", keyEncryptionAlgorithmForKeyAgreement='" + keyEncryptionAlgorithmForKeyAgreement + '\'' +
                ", keyEncryptionAgreementMethodAlgorithm='" + keyEncryptionAgreementMethodAlgorithm + '\'' +
                ", jcaProviderName='" + jcaProviderName + '\'' +
                ", encryptionAlgorithmWhiteList='" + encryptionAlgorithmWhiteList + '\'' +
                '}';
    }

	public boolean isAssertionEncryptWithKey() {
		return isAssertionEncryptWithKey;
	}

    /**
     * Builder class
     */
    public static final class Builder {
        private final EncryptionConfiguration encryptionConfiguration;

        public Builder() {
            this.encryptionConfiguration = new EncryptionConfiguration();
        }

        public EncryptionConfiguration.Builder setResponseEncryptionMandatory(boolean responseEncryptionMandatory) {
            encryptionConfiguration.responseEncryptionMandatory = responseEncryptionMandatory;
            return this;
        }

        public EncryptionConfiguration.Builder setCheckedValidityPeriod(boolean checkedValidityPeriod) {
            encryptionConfiguration.checkedValidityPeriod = checkedValidityPeriod;
            return this;
        }

        public EncryptionConfiguration.Builder setDisallowedSelfSignedCertificate(boolean disallowedSelfSignedCertificate) {
            encryptionConfiguration.disallowedSelfSignedCertificate = disallowedSelfSignedCertificate;
            return this;
        }

        public EncryptionConfiguration.Builder setAssertionEncryptWithKey(boolean isAssertionEncryptWithKey) {
            encryptionConfiguration.isAssertionEncryptWithKey = isAssertionEncryptWithKey;
            return this;
        }

        /**
         * There can be more than one decryption private key and associated certificate because of overlaps.
         * @param decryptionKeyAndCertificates set of decryption keys and certificates.
         * @return this builder instance
         */
        public EncryptionConfiguration.Builder setDecryptionKeyAndCertificates(ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates) {
            encryptionConfiguration.decryptionKeyAndCertificates = decryptionKeyAndCertificates;
            return this;
        }

        /**
         * The certificates of the trusted peers, which can be used to encrypt a response to be sent to them.         *
         * @param encryptionCertificates encryption certificates
         * @return this builder instance
         */
        public EncryptionConfiguration.Builder setEncryptionCertificates(ImmutableSet<X509Certificate> encryptionCertificates) {
            encryptionConfiguration.encryptionCertificates = encryptionCertificates;
            return this;
        }

        public EncryptionConfiguration.Builder setDataEncryptionAlgorithm(String dataEncryptionAlgorithm) {
            encryptionConfiguration.dataEncryptionAlgorithm = dataEncryptionAlgorithm;
            return this;
        }

        public EncryptionConfiguration.Builder setKeyEncryptionAlgorithm(String keyEncryptionAlgorithm) {
            encryptionConfiguration.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
            return this;
        }

        public EncryptionConfiguration.Builder setKeyEncryptionAlgorithmForKeyAgreement(String keyEncryptionAlgorithmForKeyAgreement) {
            encryptionConfiguration.keyEncryptionAlgorithmForKeyAgreement = keyEncryptionAlgorithmForKeyAgreement;
            return this;
        }

        public EncryptionConfiguration.Builder setKeyEncryptionAgreementMethodAlgorithm(String keyEncryptionAgreementMethodAlgorithm) {
            encryptionConfiguration.keyEncryptionAgreementMethodAlgorithm = keyEncryptionAgreementMethodAlgorithm;
            return this;
        }

        public EncryptionConfiguration.Builder setJcaProviderName(String jcaProviderName) {
            encryptionConfiguration.jcaProviderName = jcaProviderName;
            return this;
        }

        public EncryptionConfiguration.Builder setEncryptionAlgorithmWhiteList(String encryptionAlgorithmWhiteList) {
            encryptionConfiguration.encryptionAlgorithmWhiteList = encryptionAlgorithmWhiteList;
            encryptionConfiguration.encryptionAlgorithmWhitelist = ImmutableSet.copyOf(EidasStringUtil.getDistinctValues(encryptionAlgorithmWhiteList));
            return this;
        }

        public EncryptionConfiguration.Builder setEncryptionAlgorithmWhitelist(ImmutableSet<String> encryptionAlgorithmWhitelistSet) {
            encryptionConfiguration.encryptionAlgorithmWhitelist = encryptionAlgorithmWhitelistSet;
            encryptionConfiguration.encryptionAlgorithmWhiteList = String.join(EIDASValues.SEMICOLON.toString(), encryptionAlgorithmWhitelistSet);
            return this;
        }

        public EncryptionConfiguration build() {
            Preconditions.checkNotNull(encryptionConfiguration.decryptionKeyAndCertificates, "decryptionKeyAndCertificates");
            Preconditions.checkNotNull(encryptionConfiguration.encryptionCertificates, "encryptionCertificates");
            Preconditions.checkNotNull(encryptionConfiguration.encryptionAlgorithmWhitelist, "encryptionAlgorithmWhitelist");

            return buildInternal();
        }

        /**
         * Private build method used to not return the same instance when builder is used multiple times.
         * @return a soft copy of the SignatureConfiguration instance variable.
         */
        private EncryptionConfiguration buildInternal() {
            EncryptionConfiguration copy = new EncryptionConfiguration();
            copy.responseEncryptionMandatory = this.encryptionConfiguration.responseEncryptionMandatory;
            copy.checkedValidityPeriod = this.encryptionConfiguration.checkedValidityPeriod;
            copy.disallowedSelfSignedCertificate = this.encryptionConfiguration.disallowedSelfSignedCertificate;
            copy.isAssertionEncryptWithKey = this.encryptionConfiguration.isAssertionEncryptWithKey;
            copy.decryptionKeyAndCertificates = this.encryptionConfiguration.decryptionKeyAndCertificates;
            copy.encryptionCertificates = this.encryptionConfiguration.encryptionCertificates;
            copy.dataEncryptionAlgorithm = this.encryptionConfiguration.dataEncryptionAlgorithm;
            copy.keyEncryptionAlgorithm = this.encryptionConfiguration.keyEncryptionAlgorithm;
            copy.keyEncryptionAlgorithmForKeyAgreement = this.encryptionConfiguration.keyEncryptionAlgorithmForKeyAgreement;
            copy.keyEncryptionAgreementMethodAlgorithm = this.encryptionConfiguration.keyEncryptionAgreementMethodAlgorithm;
            copy.jcaProviderName = this.encryptionConfiguration.jcaProviderName;
            copy.encryptionAlgorithmWhiteList = this.encryptionConfiguration.encryptionAlgorithmWhiteList;
            copy.encryptionAlgorithmWhitelist = this.encryptionConfiguration.encryptionAlgorithmWhitelist;
            return copy;
        }
    }
}
