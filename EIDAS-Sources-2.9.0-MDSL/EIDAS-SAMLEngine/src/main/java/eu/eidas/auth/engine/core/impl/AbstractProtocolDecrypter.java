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

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.configuration.dom.EncryptionConfiguration;
import eu.eidas.auth.engine.core.ProtocolDecrypterI;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.encryption.SAMLAuthnResponseDecrypter;
import eu.eidas.encryption.exception.DecryptionException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    private final Set<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates;

    private final SAMLAuthnResponseDecrypter samlAuthnResponseDecrypter;

    protected AbstractProtocolDecrypter(@Nonnull EncryptionConfiguration encryptionConfiguration) {
        super(encryptionConfiguration);

        this.decryptionKeyAndCertificates = encryptionConfiguration.getDecryptionKeyAndCertificates();

        String jcaProviderName = getJcaProviderNameWithDefault(encryptionConfiguration.getJcaProviderName());
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
                throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_UNENCRYPTED_RESPONSE, "Response is not encrypted: ");
            }
            EncryptedAssertion encAssertion = encryptedAssertions.get(0);

            validateResponseEncryptionAlgorithm(encAssertion);

            Credential[] credentials = retrieveDecryptionCredentials();

            Response response = samlAuthnResponseDecrypter.decryptSAMLResponse(authResponse, credentials);

            LOG.debug("Decryption of SAML Response done.");
            return response;
        } catch (DecryptionException e) {
            LOG.error("Error decrypting SAML Response: " + e.getMessage(), e);
            throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_DECRYPTING_RESPONSE, "Error decrypting SAML Response: " + e.getMessage(), e);
        }
    }

    @Override
    @Nonnull
    public List<X509Certificate> getDecryptionCertificates() throws EIDASSAMLEngineException {
        List<X509Certificate> certificates = new ArrayList<>();
        for(KeyStore.PrivateKeyEntry privateKeyEntry : decryptionKeyAndCertificates) {
            certificates.add((X509Certificate) privateKeyEntry.getCertificate());
            decryptionKeyAndCertificates.remove(privateKeyEntry);
        }
        return certificates;
    }

    protected Set<KeyStore.PrivateKeyEntry> getDecryptionKeyAndCertificates() {
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
            throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_INVALID_CERTIFICATE, "No valid credential for decryption");
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
