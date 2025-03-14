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
import eu.eidas.auth.engine.core.ProtocolEncrypterI;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.encryption.SAMLAuthnResponseEncrypter;
import eu.eidas.encryption.exception.EncryptionException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.security.cert.X509Certificate;
import java.util.Set;

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

    private final Set<X509Certificate> encryptionCertificates;

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

    @Override
    @Nonnull
    public Response encryptSamlResponse(@Nonnull Response authResponse, @Nonnull X509Certificate destinationCertificate,
    		boolean encryptAssertionWithKey)
            throws EIDASSAMLEngineException {
        if (null == destinationCertificate) {
            throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_UNENCRYPTED_RESPONSE,
                    "Encryption Certificate is not available for destination");
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
            throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_UNENCRYPTED_RESPONSE,
                    "ERROR : Error encrypting SAML Response: "+ e.getMessage(), e);
        }
    }

    protected Set<X509Certificate> getEncryptionCertificates() {
        return encryptionCertificates;
    }

    protected SAMLAuthnResponseEncrypter getSamlAuthnResponseEncrypter() {
        return samlAuthnResponseEncrypter;
    }
}
