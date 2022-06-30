/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.eidas.auth.engine.core.impl;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import org.opensaml.saml2.core.Response;

import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.EncryptionConfiguration;
import eu.eidas.auth.engine.core.ProtocolDecrypterI;
import eu.eidas.auth.engine.core.ProtocolEncrypterI;
import eu.eidas.encryption.SAMLAuthnResponseDecrypter;
import eu.eidas.encryption.SAMLAuthnResponseEncrypter;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * The base abstract class for implementations of {@link eu.eidas.auth.engine.core.ProtocolDecrypterI} and {@link
 * ProtocolEncrypterI}.
 *
 * @since 1.1
 */
public abstract class AbstractSamlEngineEncryption implements ProtocolDecrypterI, ProtocolEncrypterI {

    static final class BaseProtocolDecrypter extends AbstractProtocolDecrypter {

        BaseProtocolDecrypter(boolean checkedValidityPeriod,
                              boolean disallowedSelfSignedCertificate,
                              boolean responseEncryptionMandatory,
                              @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates,
                              @Nonnull SAMLAuthnResponseDecrypter samlAuthnResponseDecrypter,
                              @Nonnull ImmutableSet<String> encryptionAlgorithmWhiteList) {
            super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
                  decryptionKeyAndCertificates, samlAuthnResponseDecrypter, encryptionAlgorithmWhiteList);
        }

        BaseProtocolDecrypter(boolean checkedValidityPeriod,
                              boolean disallowedSelfSignedCertificate,
                              boolean responseEncryptionMandatory,
                              @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates,
                              @Nullable String jcaProviderName,
                              @Nullable String encryptionAlgorithmWhiteList) throws SamlEngineConfigurationException {
            super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
                  decryptionKeyAndCertificates, jcaProviderName, encryptionAlgorithmWhiteList);
        }
    }

    final class BaseProtocolEncrypter extends AbstractProtocolEncrypter {

        BaseProtocolEncrypter(boolean checkedValidityPeriod,
                              boolean disallowedSelfSignedCertificate,
                              boolean responseEncryptionMandatory,
                              @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                              @Nonnull SAMLAuthnResponseEncrypter samlAuthnResponseEncrypter,
                              @Nonnull ImmutableSet<String> encryptionAlgorithmWhiteList) {
            super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
                  encryptionCertificates, samlAuthnResponseEncrypter, encryptionAlgorithmWhiteList);
        }

        BaseProtocolEncrypter(boolean checkedValidityPeriod,
                              boolean disallowedSelfSignedCertificate,
                              boolean responseEncryptionMandatory,
                              @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                              @Nullable String dataEncryptionAlgorithm,
                              @Nullable String keyEncryptionAlgorithm,
                              @Nullable String jcaProviderName,
                              @Nullable String encryptionAlgorithmWhiteList) throws SamlEngineConfigurationException {
            super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
                  encryptionCertificates, dataEncryptionAlgorithm, keyEncryptionAlgorithm, jcaProviderName,
                  encryptionAlgorithmWhiteList);
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

    private final BaseProtocolDecrypter decrypter;

    private final BaseProtocolEncrypter encrypter;

    protected AbstractSamlEngineEncryption(@Nonnull EncryptionConfiguration encryptionConfiguration)
            throws SamlEngineConfigurationException {
        this(encryptionConfiguration.isCheckedValidityPeriod(),
             encryptionConfiguration.isDisallowedSelfSignedCertificate(),
             encryptionConfiguration.isResponseEncryptionMandatory(),
             encryptionConfiguration.getDecryptionKeyAndCertificates(),
             encryptionConfiguration.getEncryptionCertificates(), encryptionConfiguration.getDataEncryptionAlgorithm(),
             encryptionConfiguration.getKeyEncryptionAlgorithm(), encryptionConfiguration.getJcaProviderName(),
             encryptionConfiguration.getEncryptionAlgorithmWhiteList());
    }

    protected AbstractSamlEngineEncryption(boolean checkedValidityPeriod,
                                           boolean disallowedSelfSignedCertificate,
                                           boolean responseEncryptionMandatory,
                                           @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates,
                                           @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                                           @Nonnull SAMLAuthnResponseEncrypter samlAuthnResponseEncrypter,
                                           @Nonnull SAMLAuthnResponseDecrypter samlAuthnResponseDecrypter,
                                           @Nonnull ImmutableSet<String> encryptionAlgorithmWhiteList) {
        decrypter = new BaseProtocolDecrypter(checkedValidityPeriod, disallowedSelfSignedCertificate,
                                              responseEncryptionMandatory, decryptionKeyAndCertificates,
                                              samlAuthnResponseDecrypter, encryptionAlgorithmWhiteList);

        encrypter = new BaseProtocolEncrypter(checkedValidityPeriod, disallowedSelfSignedCertificate,
                                              responseEncryptionMandatory, encryptionCertificates,
                                              samlAuthnResponseEncrypter, encryptionAlgorithmWhiteList);
    }

    protected AbstractSamlEngineEncryption(boolean checkedValidityPeriod,
                                           boolean disallowedSelfSignedCertificate,
                                           boolean responseEncryptionMandatory,
                                           @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates,
                                           @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                                           @Nullable String dataEncryptionAlgorithm,
                                           @Nullable String keyEncryptionAlgorithm,
                                           @Nullable String jcaProviderName,
                                           @Nullable String encryptionAlgorithmWhiteList)
            throws SamlEngineConfigurationException {
        decrypter = new BaseProtocolDecrypter(checkedValidityPeriod, disallowedSelfSignedCertificate,
                                              responseEncryptionMandatory, decryptionKeyAndCertificates,
                                              jcaProviderName, encryptionAlgorithmWhiteList);

        encrypter = new BaseProtocolEncrypter(checkedValidityPeriod, disallowedSelfSignedCertificate,
                                              responseEncryptionMandatory, encryptionCertificates,
                                              dataEncryptionAlgorithm, keyEncryptionAlgorithm, jcaProviderName,
                                              encryptionAlgorithmWhiteList);
    }

    @Override
    @Nonnull
    public Response decryptSamlResponse(@Nonnull Response authResponse) throws EIDASSAMLEngineException {
        return decrypter.decryptSamlResponse(authResponse);
    }

    @Override
    @Nonnull
    public Response encryptSamlResponse(@Nonnull Response authResponse, @Nonnull X509Certificate destinationCertificate)
            throws EIDASSAMLEngineException {
        return encrypter.encryptSamlResponse(authResponse, destinationCertificate);
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
    public boolean isCheckedValidityPeriod() {
        return decrypter.isCheckedValidityPeriod();
    }

    @Override
    public boolean isDisallowedSelfSignedCertificate() {
        return decrypter.isDisallowedSelfSignedCertificate();
    }

    @Override
    public boolean isResponseEncryptionMandatory() {
        return decrypter.isResponseEncryptionMandatory();
    }
}
