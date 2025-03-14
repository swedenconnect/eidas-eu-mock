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
import java.util.Objects;
import java.util.Set;

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
    private Set<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates;

    /**
     * The certificates of the trusted peers, which can be used to encrypt a response to be sent to them.
     */
    @Nonnull
    private Set<X509Certificate> encryptionCertificates;

    @Nullable
    private String dataEncryptionAlgorithm;

    @Nullable
    private String keyEncryptionAlgorithm;

    @Nullable
    private String messageDigestKeyTransport;

    @Nullable
    private String maskGenerationFunctionKeyTransport;

    @Nullable
    private String keyEncryptionAlgorithmForKeyAgreement;

    @Nullable
    private String keyEncryptionAgreementMethodAlgorithm;

    @Nullable
    private String jcaProviderName;

    @Nullable
    private String encryptionAlgorithmWhiteList;

    @Nonnull
    private Set<String> encryptionAlgorithmWhitelist;

    private EncryptionConfiguration() {}

    @Nullable
    public String getDataEncryptionAlgorithm() {
        return dataEncryptionAlgorithm;
    }

    /**
     * There can be more than one decryption private key and associated certificate because of time-validity overlaps.
     *
     * @return a {@link Set} collection that contains {@link KeyStore.PrivateKeyEntry}.
     */
    @Nonnull
    public Set<KeyStore.PrivateKeyEntry> getDecryptionKeyAndCertificates() {
        return decryptionKeyAndCertificates;
    }

    @Nullable
    public String getEncryptionAlgorithmWhiteList() {
        return encryptionAlgorithmWhiteList;
    }

    @Nonnull
    public Set<String> getEncryptionAlgorithmWhitelist() {
        return encryptionAlgorithmWhitelist;
    }

    @Nonnull
    public Set<X509Certificate> getEncryptionCertificates() {
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
    public String getMessageDigestKeyTransport() {
        return messageDigestKeyTransport;
    }

    @Nullable
    public String getMaskGenerationFunctionKeyTransport() {
        return maskGenerationFunctionKeyTransport;
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
        public EncryptionConfiguration.Builder setDecryptionKeyAndCertificates(Set<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates) {
            encryptionConfiguration.decryptionKeyAndCertificates = decryptionKeyAndCertificates;
            return this;
        }

        /**
         * The certificates of the trusted peers, which can be used to encrypt a response to be sent to them.         *
         * @param encryptionCertificates encryption certificates
         * @return this builder instance
         */
        public EncryptionConfiguration.Builder setEncryptionCertificates(Set<X509Certificate> encryptionCertificates) {
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

        public EncryptionConfiguration.Builder setMessageDigestKeyTransport(String messageDigestKeyTransport) {
            encryptionConfiguration.messageDigestKeyTransport = messageDigestKeyTransport;
            return this;
        }

        public EncryptionConfiguration.Builder setMaskGenerationFunctionKeyTransport(String maskGenerationFunctionKeyTransport) {
            encryptionConfiguration.maskGenerationFunctionKeyTransport = maskGenerationFunctionKeyTransport;
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
            encryptionConfiguration.encryptionAlgorithmWhitelist = new LinkedHashSet<>(EidasStringUtil.getDistinctValues(encryptionAlgorithmWhiteList));
            return this;
        }

        public EncryptionConfiguration.Builder setEncryptionAlgorithmWhitelist(Set<String> encryptionAlgorithmWhitelistSet) {
            encryptionConfiguration.encryptionAlgorithmWhitelist = encryptionAlgorithmWhitelistSet;
            encryptionConfiguration.encryptionAlgorithmWhiteList = String.join(EIDASValues.SEMICOLON.toString(), encryptionAlgorithmWhitelistSet);
            return this;
        }

        public EncryptionConfiguration build() {
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
            copy.messageDigestKeyTransport = this.encryptionConfiguration.messageDigestKeyTransport;
            copy.maskGenerationFunctionKeyTransport = this.encryptionConfiguration.maskGenerationFunctionKeyTransport;
            copy.keyEncryptionAlgorithmForKeyAgreement = this.encryptionConfiguration.keyEncryptionAlgorithmForKeyAgreement;
            copy.keyEncryptionAgreementMethodAlgorithm = this.encryptionConfiguration.keyEncryptionAgreementMethodAlgorithm;
            copy.jcaProviderName = this.encryptionConfiguration.jcaProviderName;
            copy.encryptionAlgorithmWhiteList = this.encryptionConfiguration.encryptionAlgorithmWhiteList;
            copy.encryptionAlgorithmWhitelist = this.encryptionConfiguration.encryptionAlgorithmWhitelist;
            return copy;
        }
    }
}
