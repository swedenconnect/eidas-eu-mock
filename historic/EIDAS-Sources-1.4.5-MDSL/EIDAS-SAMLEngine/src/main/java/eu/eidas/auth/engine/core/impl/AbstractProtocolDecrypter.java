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
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.encryption.EncryptedKey;
import org.opensaml.xml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.EncryptionConfiguration;
import eu.eidas.auth.engine.core.ProtocolDecrypterI;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.encryption.SAMLAuthnResponseDecrypter;
import eu.eidas.encryption.exception.DecryptionException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.util.Preconditions;

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
            throws SamlEngineConfigurationException {
        this(encryptionConfiguration.isCheckedValidityPeriod(),
             encryptionConfiguration.isDisallowedSelfSignedCertificate(),
             encryptionConfiguration.isResponseEncryptionMandatory(),
             encryptionConfiguration.getDecryptionKeyAndCertificates(), encryptionConfiguration.getJcaProviderName(),
             encryptionConfiguration.getEncryptionAlgorithmWhiteList());
    }

    protected AbstractProtocolDecrypter(boolean checkedValidityPeriod,
                                        boolean disallowedSelfSignedCertificate,
                                        boolean responseEncryptionMandatory,
                                        @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates,
                                        @Nonnull SAMLAuthnResponseDecrypter samlAuthnResponseDecrypter,
                                        @Nonnull ImmutableSet<String> encryptionAlgorithmWhiteList) {
        super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
              encryptionAlgorithmWhiteList);

        Preconditions.checkNotEmpty(decryptionKeyAndCertificates, "decryptionKeyAndCertificates");
        Preconditions.checkNotNull(samlAuthnResponseDecrypter, "samlAuthnResponseEncrypter");

        this.decryptionKeyAndCertificates = decryptionKeyAndCertificates;
        this.samlAuthnResponseDecrypter = samlAuthnResponseDecrypter;
    }

    protected AbstractProtocolDecrypter(boolean checkedValidityPeriod,
                                        boolean disallowedSelfSignedCertificate,
                                        boolean responseEncryptionMandatory,
                                        @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates,
                                        @Nullable String jcaProviderName,
                                        @Nullable String encryptionAlgorithmWhiteList)
            throws SamlEngineConfigurationException {
        super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
              encryptionAlgorithmWhiteList);

        Preconditions.checkNotEmpty(decryptionKeyAndCertificates, "decryptionKeyAndCertificates");
        Preconditions.checkNotNull(encryptionAlgorithmWhiteList, "encryptionAlgorithmWhiteList");

        try {
            this.decryptionKeyAndCertificates = decryptionKeyAndCertificates;

            jcaProviderName = getJcaProviderNameWithDefault(jcaProviderName);

            samlAuthnResponseDecrypter = new SAMLAuthnResponseDecrypter(jcaProviderName);

            LOG.trace("AbstractProtocolDecrypter loaded.");
        } catch (Exception e) {
            LOG.error("AbstractProtocolDecrypter init: " + e, e);
            throw new SamlEngineConfigurationException(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorCode(),
                                                       EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorMessage(), e);
        }
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
                throw new SamlEngineConfigurationException(EidasErrorKey.SAML_ENGINE_UNENCRYPTED_RESPONSE.errorCode(),
                                                           EidasErrorKey.SAML_ENGINE_UNENCRYPTED_RESPONSE.errorMessage());
            }
            EncryptedAssertion encAssertion = encryptedAssertions.get(0);

            validateResponseEncryptionAlgorithm(encAssertion);

            X509Credential credential = retrieveDecryptionCredential(encAssertion);

            Response response = samlAuthnResponseDecrypter.decryptSAMLResponse(authResponse, credential);

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

    private X509Credential retrieveDecryptionCredential(EncryptedAssertion encAssertion)
            throws EIDASSAMLEngineException {
        EncryptedKey encryptedSymmetricKey = encAssertion.getEncryptedData().getKeyInfo().getEncryptedKeys().get(0);
        X509Certificate keyInfoCert = CertificateUtil.toCertificate(encryptedSymmetricKey.getKeyInfo());

        PrivateKey privateKey = null;
        for (final KeyStore.PrivateKeyEntry privateKeyEntry : decryptionKeyAndCertificates) {
            X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
            if (certificate.equals(keyInfoCert)) {
                privateKey = privateKeyEntry.getPrivateKey();
                break;
            }
        }

        if (null == privateKey) {
            LOG.error("Response encrypted with unknown certificate: " + keyInfoCert);
            throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_INVALID_CERTIFICATE.errorCode(),
                                               EidasErrorKey.SAML_ENGINE_INVALID_CERTIFICATE.errorMessage());
        }

        if (isCheckedValidityPeriod()) {
            CertificateValidator.checkCertificateValidityPeriod(keyInfoCert);
        }

        return CertificateUtil.createCredential(keyInfoCert, privateKey);
    }

    private void validateResponseEncryptionAlgorithm(EncryptedAssertion encAssertion) throws EIDASSAMLEngineException {
        String responseAlgorithm = encAssertion.getEncryptedData().getEncryptionMethod().getAlgorithm();

        validateEncryptionAlgorithm(responseAlgorithm);
    }
}
