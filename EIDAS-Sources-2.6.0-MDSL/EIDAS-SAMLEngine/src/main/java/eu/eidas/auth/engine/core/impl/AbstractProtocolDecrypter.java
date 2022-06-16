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
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.EncryptionConfiguration;
import eu.eidas.auth.engine.core.ProtocolDecrypterI;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.encryption.SAMLAuthnResponseDecrypter;
import eu.eidas.encryption.exception.DecryptionException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.util.Preconditions;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * The base abstract class for implementations of {@link ProtocolDecrypterI}.
 *
 * @since 1.1
 */
public abstract class AbstractProtocolDecrypter extends AbstractProtocolCipher implements ProtocolDecrypterI {

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractProtocolDecrypter.class);

    private final ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates;

    private final SAMLAuthnResponseDecrypter samlAuthnResponseDecrypter;

    protected AbstractProtocolDecrypter(@Nonnull EncryptionConfiguration encryptionConfiguration)
            throws ProtocolEngineConfigurationException {
        super(encryptionConfiguration);
        Preconditions.checkNotEmpty(encryptionConfiguration.getDecryptionKeyAndCertificates(), "decryptionKeyAndCertificates");
        Preconditions.checkNotNull(encryptionConfiguration.getEncryptionAlgorithmWhiteList(), "encryptionAlgorithmWhiteList");

        this.decryptionKeyAndCertificates = encryptionConfiguration.getDecryptionKeyAndCertificates();

        String jcaProviderName = getJcaProviderNameWithDefault(encryptionConfiguration.getJcaProviderName());
        samlAuthnResponseDecrypter = new SAMLAuthnResponseDecrypter(jcaProviderName);

        LOG.trace("AbstractProtocolDecrypter loaded.");
    }

    /**
     * @deprecated since 2.6
     * Use {@link AbstractProtocolDecrypter#AbstractProtocolDecrypter(EncryptionConfiguration)} instead.
     */
    @Deprecated
    protected AbstractProtocolDecrypter(boolean checkedValidityPeriod,
                                        boolean disallowedSelfSignedCertificate,
                                        boolean responseEncryptionMandatory,
                                        boolean isAssertionEncryptWithKey,
                                        @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates,
                                        @Nonnull SAMLAuthnResponseDecrypter samlAuthnResponseDecrypter,
                                        @Nonnull ImmutableSet<String> encryptionAlgorithmWhiteList) {
        super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,isAssertionEncryptWithKey,
              encryptionAlgorithmWhiteList);

        Preconditions.checkNotEmpty(decryptionKeyAndCertificates, "decryptionKeyAndCertificates");
        Preconditions.checkNotNull(samlAuthnResponseDecrypter, "samlAuthnResponseDecrypter");

        this.decryptionKeyAndCertificates = decryptionKeyAndCertificates;
        this.samlAuthnResponseDecrypter = samlAuthnResponseDecrypter;
    }

    /**
     * @deprecated since 2.6
     * Use {@link AbstractProtocolDecrypter#AbstractProtocolDecrypter(EncryptionConfiguration)} instead.
     */
    @Deprecated
    protected AbstractProtocolDecrypter(boolean checkedValidityPeriod,
                                        boolean disallowedSelfSignedCertificate,
                                        boolean responseEncryptionMandatory,
                                        boolean isAssertionEncryptWithKey,
                                        @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates,
                                        @Nullable String jcaProviderName,
                                        @Nullable String encryptionAlgorithmWhiteList)
            throws ProtocolEngineConfigurationException {
        super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
        		isAssertionEncryptWithKey,
              encryptionAlgorithmWhiteList);

        Preconditions.checkNotEmpty(decryptionKeyAndCertificates, "decryptionKeyAndCertificates");
        Preconditions.checkNotNull(encryptionAlgorithmWhiteList, "encryptionAlgorithmWhiteList");

        this.decryptionKeyAndCertificates = decryptionKeyAndCertificates;

        jcaProviderName = getJcaProviderNameWithDefault(jcaProviderName);

        samlAuthnResponseDecrypter = new SAMLAuthnResponseDecrypter(jcaProviderName);

        LOG.trace("AbstractProtocolDecrypter loaded.");
    }

    @Override
    @Nonnull
    public Response decryptSamlResponse(@Nonnull Response authResponse) throws EIDASSAMLEngineException {
        LOG.debug("Decryption enabled, proceeding...");
        // Decryption is always made with private key. Only own certificate needed
        try {
            // Acquire Private Key of current point as a SAMLResponse target
            // e.g.: the targeted ProxyService acquires its own PrivateKey from its own KeyStore
            // Acquire PublicKey of SAMLResponse Point
            // e.g.: SAMLAdapter acquires PublicKey of the targeted ProxyService from the SAMLAdapter's KeyStore

            List<EncryptedAssertion> encryptedAssertions = authResponse.getEncryptedAssertions();
            if (null == encryptedAssertions || encryptedAssertions.isEmpty()) {
                LOG.error("Response is not encrypted: " + authResponse);
                throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_UNENCRYPTED_RESPONSE.errorCode(),
                                                           EidasErrorKey.SAML_ENGINE_UNENCRYPTED_RESPONSE.errorMessage());
            }
            EncryptedAssertion encAssertion = encryptedAssertions.get(0);

            validateResponseEncryptionAlgorithm(encAssertion);

            Credential[] credentials = retrieveDecryptionCredentials();

            Response response = samlAuthnResponseDecrypter.decryptSAMLResponse(authResponse, credentials);

            LOG.debug("Decryption of SAML Response done.");
            return response;
        } catch (DecryptionException e) {
            LOG.error("Error decrypting SAML Response: " + e.getMessage(), e);
            throw new EIDASSAMLEngineException(e);
        }
    }

    @Override
    @Nonnull
    public X509Certificate getDecryptionCertificate() throws EIDASSAMLEngineException {
        // This only returns the first certificate in the Set but it may not be the right one!
        return (X509Certificate) decryptionKeyAndCertificates.iterator().next().getCertificate();
    }

    protected ImmutableSet<KeyStore.PrivateKeyEntry> getDecryptionKeyAndCertificates() {
        return decryptionKeyAndCertificates;
    }

    protected SAMLAuthnResponseDecrypter getSamlAuthnResponseDecrypter() {
        return samlAuthnResponseDecrypter;
    }

    private Credential[] retrieveDecryptionCredentials() throws EIDASSAMLEngineException {
        List<Credential> credentialList = new ArrayList<>();
        for (final KeyStore.PrivateKeyEntry privateKeyEntry : decryptionKeyAndCertificates) {
            if (isValidPrivateKeyCertificate(privateKeyEntry)) {
                credentialList.add(CertificateUtil.createCredential(privateKeyEntry));
            }
        }
        if (credentialList.isEmpty()) {
            LOG.error("No valid credential for decryption");
            throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_INVALID_CERTIFICATE.errorCode(),
                    EidasErrorKey.SAML_ENGINE_INVALID_CERTIFICATE.errorMessage());
        }
        return credentialList.toArray(new Credential[credentialList.size()]);
    }

    private boolean isValidPrivateKeyCertificate(KeyStore.PrivateKeyEntry privateKeyEntry) {
        try {
            if (isCheckedValidityPeriod()) {
                Certificate certificate = privateKeyEntry.getCertificate();
                if (certificate instanceof X509Certificate) {
                    X509Certificate x509Certificate = (X509Certificate) certificate;
                    CertificateValidator.checkCertificateValidityPeriod(x509Certificate);
                }
            }
        } catch (EIDASSAMLEngineException e) {
            return false;
        }
        return true;
    }

    private void validateResponseEncryptionAlgorithm(EncryptedAssertion encAssertion) throws EIDASSAMLEngineException {
        String responseAlgorithm = encAssertion.getEncryptedData().getEncryptionMethod().getAlgorithm();

        validateEncryptionAlgorithm(responseAlgorithm);
    }

}
