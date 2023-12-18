/*
 * Copyright (c) 2023 by European Commission
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
import eu.eidas.auth.engine.core.ProtocolEncrypterI;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.encryption.SAMLAuthnResponseEncrypter;
import eu.eidas.encryption.exception.EncryptionException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.util.Preconditions;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.cert.X509Certificate;

/**
 * The base abstract class for implementations of {@link ProtocolEncrypterI}.
 *
 * @since 1.1
 */
public abstract class AbstractProtocolEncrypter extends AbstractProtocolCipher implements ProtocolEncrypterI {

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractProtocolEncrypter.class);

    private final ImmutableSet<X509Certificate> encryptionCertificates;

    private final SAMLAuthnResponseEncrypter samlAuthnResponseEncrypter;

    protected AbstractProtocolEncrypter(@Nonnull EncryptionConfiguration encryptionConfiguration) {
        super(encryptionConfiguration);

        this.encryptionCertificates = encryptionConfiguration.getEncryptionCertificates();

        String jcaProviderName = getJcaProviderNameWithDefault(encryptionConfiguration.getJcaProviderName());

        samlAuthnResponseEncrypter = SAMLAuthnResponseEncrypter.builder()
                .dataEncryptionAlgorithm(encryptionConfiguration.getDataEncryptionAlgorithm())
                .jcaProviderName(jcaProviderName)
                .keyEncryptionAlgorithm(encryptionConfiguration.getKeyEncryptionAlgorithm())
                .messageDigestKeyTransport(encryptionConfiguration.getMessageDigestKeyTransport())
                .maskGenerationFunctionKeyTransport(encryptionConfiguration.getMaskGenerationFunctionKeyTransport())
                .keyEncryptionAlgorithmForKeyAgreement(encryptionConfiguration.getKeyEncryptionAlgorithmForKeyAgreement())
                .keyEncryptionAgreementMethodAlgorithm(encryptionConfiguration.getKeyEncryptionAgreementMethodAlgorithm())
                .build();

        LOG.debug("AbstractSamlEngineEncryption loaded.");
    }

    /**
     * @deprecated since 2.6
     * Use {@link AbstractProtocolEncrypter#AbstractProtocolEncrypter(EncryptionConfiguration)} instead.
     */
    @Deprecated
    protected AbstractProtocolEncrypter(boolean checkedValidityPeriod,
                                        boolean disallowedSelfSignedCertificate,
                                        boolean responseEncryptionMandatory,
                                        boolean isAssertionEncryptWithKey,
                                        @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                                        @Nonnull SAMLAuthnResponseEncrypter samlAuthnResponseEncrypter,
                                        @Nonnull ImmutableSet<String> encryptionAlgorithmWhiteList) {
        super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
        		isAssertionEncryptWithKey,
              encryptionAlgorithmWhiteList);

        Preconditions.checkNotEmpty(encryptionCertificates, "encryptionCertificates");
        Preconditions.checkNotNull(samlAuthnResponseEncrypter, "samlAuthnResponseEncrypter");

        this.encryptionCertificates = encryptionCertificates;
        this.samlAuthnResponseEncrypter = samlAuthnResponseEncrypter;
    }

    /**
     * @deprecated Use {@link AbstractProtocolEncrypter#AbstractProtocolEncrypter(EncryptionConfiguration)} instead.
     * @param checkedValidityPeriod flag to indicate if the certificate's validity period should be verified
     * @param disallowedSelfSignedCertificate the flag to verify when using self-signed X.509 certificate is not allowed
     * @param responseEncryptionMandatory the flag to verify if encryption is mandatory regardless of the country
     * @param isAssertionEncryptWithKey the flag to verify if assertion is encrypted with key
     * @param encryptionCertificates a set with certificates of the trusted peers
     * @param dataEncryptionAlgorithm the algorithm to be use for data (saml assertion) encryption
     * @param keyEncryptionAlgorithm the algorithm key value for encryption
     * @param jcaProviderName the provider name value
     * @param encryptionAlgorithmWhiteList the algorithm whiteList for encryption
     * @throws ProtocolEngineConfigurationException the SAML engine configuration exception
     */
    @Deprecated
    protected AbstractProtocolEncrypter(boolean checkedValidityPeriod,
                                        boolean disallowedSelfSignedCertificate,
                                        boolean responseEncryptionMandatory,
                                        boolean isAssertionEncryptWithKey,
                                        @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                                        @Nullable String dataEncryptionAlgorithm,
                                        @Nullable String keyEncryptionAlgorithm,
                                        @Nullable String jcaProviderName,
                                        @Nullable String encryptionAlgorithmWhiteList)
            throws ProtocolEngineConfigurationException {
        this(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
                isAssertionEncryptWithKey, encryptionCertificates, dataEncryptionAlgorithm, keyEncryptionAlgorithm,
                null, null, jcaProviderName, encryptionAlgorithmWhiteList);
    }

    /**
     * @deprecated since 2.6
     * Use {@link AbstractProtocolEncrypter#AbstractProtocolEncrypter(EncryptionConfiguration)} instead.
     */
    @Deprecated
    protected AbstractProtocolEncrypter(boolean checkedValidityPeriod,
                                        boolean disallowedSelfSignedCertificate,
                                        boolean responseEncryptionMandatory,
                                        boolean isAssertionEncryptWithKey,
                                        @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                                        @Nullable String dataEncryptionAlgorithm,
                                        @Nullable String keyEncryptionAlgorithm,
                                        @Nullable String keyEncryptionAlgorithmForKeyAgreement,
                                        @Nullable String jcaProviderName,
                                        @Nullable String encryptionAlgorithmWhiteList)
            throws ProtocolEngineConfigurationException {
        this(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
                isAssertionEncryptWithKey, encryptionCertificates, dataEncryptionAlgorithm, keyEncryptionAlgorithm,
                null, null, jcaProviderName, encryptionAlgorithmWhiteList);
    }

    /**
     * @deprecated since 2.6
     * Use {@link AbstractProtocolEncrypter#AbstractProtocolEncrypter(EncryptionConfiguration)} instead.
     *
     * @param checkedValidityPeriod flag to indicate if the certificate's validity period should be verified
     * @param disallowedSelfSignedCertificate the flag to verify when using self-signed X.509 certificate is not allowed
     * @param responseEncryptionMandatory the flag to verify if encryption is mandatory regardless of the country
     * @param isAssertionEncryptWithKey the flag to verify if assertion is encrypted with key
     * @param encryptionCertificates a set with certificates of the trusted peers
     * @param dataEncryptionAlgorithm the algorithm to be use for data (saml assertion) encryption
     * @param keyEncryptionAlgorithm the algorithm key value for key transport encryption
     * @param keyEncryptionAlgorithmForKeyAgreement the key wrapping algorithm for Key Agreement encryption
     * @param keyEncryptionAgreementMethodAlgorithm the Agreement method algorithm for Key Agreement encryption
     * @param jcaProviderName the provider name value
     * @param encryptionAlgorithmWhiteList the algorithm whiteList for encryption
     * @throws ProtocolEngineConfigurationException the SAML engine configuration exception
     */
    @Deprecated
    protected AbstractProtocolEncrypter(boolean checkedValidityPeriod,
                                        boolean disallowedSelfSignedCertificate,
                                        boolean responseEncryptionMandatory,
                                        boolean isAssertionEncryptWithKey,
                                        @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                                        @Nullable String dataEncryptionAlgorithm,
                                        @Nullable String keyEncryptionAlgorithm,
                                        @Nullable String keyEncryptionAlgorithmForKeyAgreement,
                                        @Nullable String keyEncryptionAgreementMethodAlgorithm,
                                        @Nullable String jcaProviderName,
                                        @Nullable String encryptionAlgorithmWhiteList)
            throws ProtocolEngineConfigurationException {
        super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,isAssertionEncryptWithKey,
                encryptionAlgorithmWhiteList);

        Preconditions.checkNotEmpty(encryptionCertificates, "encryptionCertificates");

        this.encryptionCertificates = encryptionCertificates;

        jcaProviderName = getJcaProviderNameWithDefault(jcaProviderName);

        samlAuthnResponseEncrypter = SAMLAuthnResponseEncrypter.builder()
                .dataEncryptionAlgorithm(dataEncryptionAlgorithm)
                .jcaProviderName(jcaProviderName)
                .keyEncryptionAlgorithm(keyEncryptionAlgorithm)
                .keyEncryptionAlgorithmForKeyAgreement(keyEncryptionAlgorithmForKeyAgreement)
                .keyEncryptionAgreementMethodAlgorithm(keyEncryptionAgreementMethodAlgorithm)
                .build();

        LOG.debug("AbstractSamlEngineEncryption loaded.");
    }

    @Override
    @Nonnull
    public Response encryptSamlResponse(@Nonnull Response authResponse, @Nonnull X509Certificate destinationCertificate,
    		boolean encryptAssertionWithKey)
            throws EIDASSAMLEngineException {
        if (null == destinationCertificate) {
            throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_UNENCRYPTED_RESPONSE.errorCode(),
                                               EidasErrorKey.SAML_ENGINE_UNENCRYPTED_RESPONSE.errorMessage());
        }

        X509Credential credential = CertificateUtil.toCredential(destinationCertificate);

        try {
            // Execute encryption
            Response response = samlAuthnResponseEncrypter.encryptSAMLResponse(authResponse, credential,encryptAssertionWithKey);

            LOG.debug("Encryption of SAML Response performed with certificate of: " + credential.getEntityCertificate()
                    .getIssuerDN());

            return response;
        } catch (EncryptionException e) {
            LOG.error("ERROR : Error encrypting SAML Response: " + e.getMessage(), e);
            throw new EIDASSAMLEngineException(e);
        }
    }

    protected ImmutableSet<X509Certificate> getEncryptionCertificates() {
        return encryptionCertificates;
    }

    protected SAMLAuthnResponseEncrypter getSamlAuthnResponseEncrypter() {
        return samlAuthnResponseEncrypter;
    }
}
