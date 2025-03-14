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

package eu.eidas.auth.engine.core.impl;

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
import java.util.List;
import java.util.Set;

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
    }

    final class BaseProtocolEncrypter extends AbstractProtocolEncrypter {

        BaseProtocolEncrypter(EncryptionConfiguration encryptionConfiguration)
                throws ProtocolEngineConfigurationException {
            super(encryptionConfiguration);
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
    public List<X509Certificate> getDecryptionCertificates() throws EIDASSAMLEngineException {
        return decrypter.getDecryptionCertificates();
    }

    protected Set<KeyStore.PrivateKeyEntry> getDecryptionKeyAndCertificates() {
        return decrypter.getDecryptionKeyAndCertificates();
    }

    @Nonnull
    protected Set<String> getEncryptionAlgorithmWhiteList() {
        return decrypter.getEncryptionAlgorithmWhiteList();
    }

    protected Set<X509Certificate> getEncryptionCertificates() {
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
