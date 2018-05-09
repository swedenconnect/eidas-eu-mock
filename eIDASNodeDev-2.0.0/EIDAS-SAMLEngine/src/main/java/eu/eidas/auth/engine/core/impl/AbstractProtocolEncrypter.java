/*
 * Copyright (c) 2017 by European Commission
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

    protected AbstractProtocolEncrypter(@Nonnull EncryptionConfiguration encryptionConfiguration)
            throws ProtocolEngineConfigurationException {
        this(encryptionConfiguration.isCheckedValidityPeriod(),
             encryptionConfiguration.isDisallowedSelfSignedCertificate(),
             encryptionConfiguration.isResponseEncryptionMandatory(),
             encryptionConfiguration.getEncryptionCertificates(), encryptionConfiguration.getDataEncryptionAlgorithm(),
             encryptionConfiguration.getKeyEncryptionAlgorithm(), encryptionConfiguration.getJcaProviderName(),
             encryptionConfiguration.getEncryptionAlgorithmWhiteList());
    }

    protected AbstractProtocolEncrypter(boolean checkedValidityPeriod,
                                        boolean disallowedSelfSignedCertificate,
                                        boolean responseEncryptionMandatory,
                                        @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                                        @Nonnull SAMLAuthnResponseEncrypter samlAuthnResponseEncrypter,
                                        @Nonnull ImmutableSet<String> encryptionAlgorithmWhiteList) {
        super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
              encryptionAlgorithmWhiteList);

        Preconditions.checkNotEmpty(encryptionCertificates, "encryptionCertificates");
        Preconditions.checkNotNull(samlAuthnResponseEncrypter, "samlAuthnResponseEncrypter");

        this.encryptionCertificates = encryptionCertificates;
        this.samlAuthnResponseEncrypter = samlAuthnResponseEncrypter;
    }

    protected AbstractProtocolEncrypter(boolean checkedValidityPeriod,
                                        boolean disallowedSelfSignedCertificate,
                                        boolean responseEncryptionMandatory,
                                        @Nonnull ImmutableSet<X509Certificate> encryptionCertificates,
                                        @Nullable String dataEncryptionAlgorithm,
                                        @Nullable String keyEncryptionAlgorithm,
                                        @Nullable String jcaProviderName,
                                        @Nullable String encryptionAlgorithmWhiteList)
            throws ProtocolEngineConfigurationException {
        super(checkedValidityPeriod, disallowedSelfSignedCertificate, responseEncryptionMandatory,
              encryptionAlgorithmWhiteList);

        Preconditions.checkNotEmpty(encryptionCertificates, "encryptionCertificates");
        try {
            this.encryptionCertificates = encryptionCertificates;

            jcaProviderName = getJcaProviderNameWithDefault(jcaProviderName);

            samlAuthnResponseEncrypter = SAMLAuthnResponseEncrypter.builder()
                    .dataEncryptionAlgorithm(dataEncryptionAlgorithm)
                    .jcaProviderName(jcaProviderName)
                    .keyEncryptionAlgorithm(keyEncryptionAlgorithm)
                    .build();

            LOG.debug("AbstractSamlEngineEncryption loaded.");
        } catch (Exception e) {
            LOG.error("AbstractSamlEngineEncryption init method: " + e, e);
            throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorCode(),
                                                       EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorMessage(), e);
        }
    }

    @Override
    @Nonnull
    public Response encryptSamlResponse(@Nonnull Response authResponse, @Nonnull X509Certificate destinationCertificate)
            throws EIDASSAMLEngineException {
        if (null == destinationCertificate) {
            throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_UNENCRYPTED_RESPONSE.errorCode(),
                                               EidasErrorKey.SAML_ENGINE_UNENCRYPTED_RESPONSE.errorMessage());
        }

        X509Credential credential = CertificateUtil.toCredential(destinationCertificate);

        try {
            // Execute encryption
            Response response = samlAuthnResponseEncrypter.encryptSAMLResponse(authResponse, credential);

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
