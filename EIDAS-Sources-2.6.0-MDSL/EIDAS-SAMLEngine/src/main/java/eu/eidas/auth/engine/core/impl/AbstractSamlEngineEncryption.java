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

package eu.eidas.auth.engine.core.impl;

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.EncryptionConfiguration;
import eu.eidas.auth.engine.core.ProtocolDecrypterI;
import eu.eidas.auth.engine.core.ProtocolEncrypterI;
import eu.eidas.encryption.SAMLAuthnResponseDecrypter;
import eu.eidas.encryption.SAMLAuthnResponseEncrypter;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.opensaml.saml.saml2.core.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * The base abstract class for implementations of {@link eu.eidas.auth.engine.core.ProtocolDecrypterI} and {@link
 * ProtocolEncrypterI}.
 *
 * @since 1.1
 */
public abstract class AbstractSamlEngineEncryption implements ProtocolDecrypterI, ProtocolEncrypterI {

    static final class BaseProtocolDecrypter extends AbstractProtocolDecrypter {

        BaseProtocolDecrypter(@Nonnull EncryptionConfiguration encryptionConfiguration)
                throws ProtocolEngineConfigurationException {
            super(encryptionConfiguration);
        }

        /**
         * @deprecated since 2.6
         * Use {@link BaseProtocolDecrypter#BaseProtocolDecrypter(EncryptionConfiguration)} instead.
         */
        @Deprecated
        BaseProtocolDecrypter(boolean checkedValidityPeriod,
                              boolean disallowedSelfSignedCertificate,
                              boolean responseEncryptionMandatory,
                              boolean isAssertionEncryptWithKey,
                              @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates,
                              @Nonnull SAMLAuthnResponseDecrypter samlAuthnResponseDecrypter,
                              @Nonnull ImmutableSet<String> encryptionAlgorithmWhiteList) {
            super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
            		isAssertionEncryptWithKey,
                  decryptionKeyAndCertificates, samlAuthnResponseDecrypter, encryptionAlgorithmWhiteList);
        }

        /**
         * @deprecated since 2.6
         * Use {@link BaseProtocolDecrypter#BaseProtocolDecrypter(EncryptionConfiguration)} instead.
         */
        @Deprecated
        BaseProtocolDecrypter(boolean checkedValidityPeriod,
                              boolean disallowedSelfSignedCertificate,
                              boolean responseEncryptionMandatory,
                              boolean isAssertionEncryptWithKey,
                              @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates,
                              @Nullable String jcaProviderName,
                              @Nullable String encryptionAlgorithmWhiteList) throws ProtocolEngineConfigurationException {
            super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
            		isAssertionEncryptWithKey,
                  decryptionKeyAndCertificates, jcaProviderName, encryptionAlgorithmWhiteList);
        }
    }

    final class BaseProtocolEncrypter extends AbstractProtocolEncrypter {

        BaseProtocolEncrypter(EncryptionConfiguration encryptionConfiguration)
                throws ProtocolEngineConfigurationException {
            super(encryptionConfiguration);
        }

        /**
         * @deprecated since 2.6
         * Use {@link BaseProtocolEncrypter#BaseProtocolEncrypter(EncryptionConfiguration)} instead.
         */
        @Deprecated
        BaseProtocolEncrypter(boolean checkedValidityPeriod,
                              boolean disallowedSelfSignedCertificate,
                              boolean responseEncryptionMandatory,
                              boolean isAssertionEncryptWithKey,
                              @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                              @Nonnull SAMLAuthnResponseEncrypter samlAuthnResponseEncrypter,
                              @Nonnull ImmutableSet<String> encryptionAlgorithmWhiteList) {
            super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
            		isAssertionEncryptWithKey,
                  encryptionCertificates, samlAuthnResponseEncrypter, encryptionAlgorithmWhiteList);
        }

        /**
         * @deprecated Use {@link BaseProtocolEncrypter#BaseProtocolEncrypter(EncryptionConfiguration)} instead.
         */
        @Deprecated
        BaseProtocolEncrypter(boolean checkedValidityPeriod,
                              boolean disallowedSelfSignedCertificate,
                              boolean responseEncryptionMandatory,
                              boolean isAssertionEncryptWithKey,
                              @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                              @Nullable String dataEncryptionAlgorithm,
                              @Nullable String keyEncryptionAlgorithm,
                              @Nullable String jcaProviderName,
                              @Nullable String encryptionAlgorithmWhiteList) throws ProtocolEngineConfigurationException {
            super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
            			isAssertionEncryptWithKey,
                  encryptionCertificates, dataEncryptionAlgorithm, keyEncryptionAlgorithm, jcaProviderName,
                  encryptionAlgorithmWhiteList);
        }

        /**
         * @deprecated since 2.6
         * Use {@link BaseProtocolEncrypter#BaseProtocolEncrypter(EncryptionConfiguration)} instead.
         */
        @Deprecated
        BaseProtocolEncrypter(boolean checkedValidityPeriod,
                              boolean disallowedSelfSignedCertificate,
                              boolean responseEncryptionMandatory,
                              boolean isAssertionEncryptWithKey,
                              @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                              @Nullable String dataEncryptionAlgorithm,
                              @Nullable String keyEncryptionAlgorithm,
                              @Nullable String keyEncryptionAlgorithmForKeyAgreement,
                              @Nullable String jcaProviderName,
                              @Nullable String encryptionAlgorithmWhiteList) throws ProtocolEngineConfigurationException {
            super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
                    isAssertionEncryptWithKey,
                    encryptionCertificates, dataEncryptionAlgorithm, keyEncryptionAlgorithm,
                    keyEncryptionAlgorithmForKeyAgreement, jcaProviderName, encryptionAlgorithmWhiteList);
        }

        @Nullable
        @Override
        public X509Certificate getEncryptionCertificate(@Nullable String destinationCountryCode)
                throws EIDASSAMLEngineException {
            return AbstractSamlEngineEncryption.this.getEncryptionCertificate(destinationCountryCode);
        }

        @Override
        public boolean isEncryptionEnabled(@Nonnull String countryCode) {
            return AbstractSamlEngineEncryption.this.isEncryptionEnabled(countryCode);
        }
    }

    private final EncryptionConfiguration encryptionConfiguration;

    private final BaseProtocolDecrypter decrypter;

    private final BaseProtocolEncrypter encrypter;

    protected AbstractSamlEngineEncryption(@Nonnull EncryptionConfiguration encryptionConfiguration)
            throws ProtocolEngineConfigurationException {
        this.encryptionConfiguration = encryptionConfiguration;
        decrypter = new BaseProtocolDecrypter(encryptionConfiguration);

        encrypter = new BaseProtocolEncrypter(encryptionConfiguration);
    }

    /**
     * @deprecated since 2.6
     * Use {@link AbstractSamlEngineEncryption#AbstractSamlEngineEncryption(EncryptionConfiguration)} instead.
     */
    @Deprecated
    protected AbstractSamlEngineEncryption(boolean checkedValidityPeriod,
                                           boolean disallowedSelfSignedCertificate,
                                           boolean responseEncryptionMandatory,
                                           boolean isAssertionEncryptWithKey,
                                           @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates,
                                           @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                                           @Nonnull SAMLAuthnResponseEncrypter samlAuthnResponseEncrypter,
                                           @Nonnull SAMLAuthnResponseDecrypter samlAuthnResponseDecrypter,
                                           @Nonnull ImmutableSet<String> encryptionAlgorithmWhiteList) {
        decrypter = new BaseProtocolDecrypter(checkedValidityPeriod, disallowedSelfSignedCertificate,
                responseEncryptionMandatory,
                isAssertionEncryptWithKey,
                decryptionKeyAndCertificates,
                samlAuthnResponseDecrypter, encryptionAlgorithmWhiteList);

        encrypter = new BaseProtocolEncrypter(checkedValidityPeriod, disallowedSelfSignedCertificate,
                responseEncryptionMandatory,
                isAssertionEncryptWithKey,
                encryptionCertificates,
                samlAuthnResponseEncrypter, encryptionAlgorithmWhiteList);

        this.encryptionConfiguration = decrypter.getEncryptionConfiguration();
    }

    /**
     * @deprecated Use {@link AbstractSamlEngineEncryption#AbstractSamlEngineEncryption(EncryptionConfiguration)} instead.
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
     * @throws ProtocolEngineConfigurationException the SAML engine configuration exception
     */
    @Deprecated
    protected AbstractSamlEngineEncryption(boolean checkedValidityPeriod,
                                           boolean disallowedSelfSignedCertificate,
                                           boolean responseEncryptionMandatory,
                                           boolean isAssertionEncryptWithKey,
                                           @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates,
                                           @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                                           @Nullable String dataEncryptionAlgorithm,
                                           @Nullable String keyEncryptionAlgorithm,
                                           @Nullable String jcaProviderName,
                                           @Nullable String encryptionAlgorithmWhiteList)
            throws ProtocolEngineConfigurationException {
        this(new EncryptionConfiguration(checkedValidityPeriod,
                disallowedSelfSignedCertificate,
                responseEncryptionMandatory,
                isAssertionEncryptWithKey,
                decryptionKeyAndCertificates,
                encryptionCertificates,
                dataEncryptionAlgorithm,
                keyEncryptionAlgorithm,
                jcaProviderName,
                encryptionAlgorithmWhiteList)
        );
    }

    /**
     /**
     * @deprecated since 2.6
     * Use {@link AbstractSamlEngineEncryption#AbstractSamlEngineEncryption(EncryptionConfiguration)} instead.
     */
    @Deprecated
    protected AbstractSamlEngineEncryption(boolean checkedValidityPeriod,
                                           boolean disallowedSelfSignedCertificate,
                                           boolean responseEncryptionMandatory,
                                           boolean isAssertionEncryptWithKey,
                                           @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates,
                                           @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                                           @Nullable String dataEncryptionAlgorithm,
                                           @Nullable String keyEncryptionAlgorithm,
                                           @Nullable String keyEncryptionAlgorithmForKeyAgreement,
                                           @Nullable String jcaProviderName,
                                           @Nullable String encryptionAlgorithmWhiteList)
            throws ProtocolEngineConfigurationException {
        this(new EncryptionConfiguration(checkedValidityPeriod,
                disallowedSelfSignedCertificate,
                responseEncryptionMandatory,
                isAssertionEncryptWithKey,
                decryptionKeyAndCertificates,
                encryptionCertificates,
                dataEncryptionAlgorithm,
                keyEncryptionAlgorithm,
                keyEncryptionAlgorithmForKeyAgreement,
                jcaProviderName,
                encryptionAlgorithmWhiteList)
        );
    }

    @Override
    @Nonnull
    public Response decryptSamlResponse(@Nonnull Response authResponse) throws EIDASSAMLEngineException {
        return decrypter.decryptSamlResponse(authResponse);
    }

    @Override
    @Nonnull
    public Response encryptSamlResponse(@Nonnull Response authResponse, @Nonnull X509Certificate destinationCertificate, boolean encryptAssertionWithKey)
            throws EIDASSAMLEngineException {
        return encrypter.encryptSamlResponse(authResponse, destinationCertificate, 
        		encryptAssertionWithKey);
    }

    @Override
    @Nonnull
    public X509Certificate getDecryptionCertificate() throws EIDASSAMLEngineException {
        return decrypter.getDecryptionCertificate();
    }

    protected ImmutableSet<KeyStore.PrivateKeyEntry> getDecryptionKeyAndCertificates() {
        return decrypter.getDecryptionKeyAndCertificates();
    }

    @Nonnull
    protected ImmutableSet<String> getEncryptionAlgorithmWhiteList() {
        return decrypter.getEncryptionAlgorithmWhiteList();
    }

    protected ImmutableSet<X509Certificate> getEncryptionCertificates() {
        return encrypter.getEncryptionCertificates();
    }

    protected SAMLAuthnResponseDecrypter getSamlAuthnResponseDecrypter() {
        return decrypter.getSamlAuthnResponseDecrypter();
    }

    protected SAMLAuthnResponseEncrypter getSamlAuthnResponseEncrypter() {
        return encrypter.getSamlAuthnResponseEncrypter();
    }

    @Override
    public EncryptionConfiguration getEncryptionConfiguration() {
        return encryptionConfiguration;
    }

    @Override
    public boolean isCheckedValidityPeriod() {
        return encryptionConfiguration.isCheckedValidityPeriod();
    }

    @Override
    public boolean isDisallowedSelfSignedCertificate() {
        return encryptionConfiguration.isDisallowedSelfSignedCertificate();
    }

    @Override
    public boolean isResponseEncryptionMandatory() {
        return encryptionConfiguration.isResponseEncryptionMandatory();
    }
    
    @Override
    public boolean isAssertionEncryptWithKey() {
        return encryptionConfiguration.isAssertionEncryptWithKey();
    }
}
