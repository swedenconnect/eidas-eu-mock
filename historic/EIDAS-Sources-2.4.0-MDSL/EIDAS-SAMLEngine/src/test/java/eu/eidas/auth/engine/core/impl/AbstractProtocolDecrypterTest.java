/*
 * Copyright (c) 2019 by European Commission
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
import eu.eidas.auth.engine.configuration.dom.KeyStoreConfigurator;
import eu.eidas.auth.engine.configuration.dom.KeyStoreContent;
import eu.eidas.auth.engine.configuration.dom.KeyStoreKey;
import eu.eidas.encryption.SAMLAuthnResponseDecrypter;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for the {@link AbstractProtocolDecrypter}
 */
public class AbstractProtocolDecrypterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProtocolDecrypterTest.class);
    private static final String RETRIEVE_DECRYPTION_CREDENTIALS_METHOD_NAME = "retrieveDecryptionCredentials";
    private static final Method RETRIEVE_DECRYPTION_CREDENTIALS_METHOD = ReflectionUtils.findMethod(
            AbstractProtocolDecrypter.class, RETRIEVE_DECRYPTION_CREDENTIALS_METHOD_NAME);
    static {
        RETRIEVE_DECRYPTION_CREDENTIALS_METHOD.setAccessible(true);
    }


    SAMLAuthnResponseDecrypter dummySAMLAuthnResponseDecrypter =
            new SAMLAuthnResponseDecrypter(BouncyCastleBootstrap.BOUNCY_CASTLE_PROVIDER.getName());

    AbstractProtocolDecrypter decrypter;

    KeyStoreContent keyStoreContent;

    @Before
    public void setup() throws Exception {
        setupKeyStore();

        decrypter = new AbstractSamlEngineEncryption.BaseProtocolDecrypter(
                isCheckedValidityPeriod(),
                isDisallowedSelfSignedCertificate(),
                isResponseEncryptionMandatory(),
                isAssertionEncryptWithKey(),
                getDecryptionKeyAndCertificates(),
                getSamlAuthnResponseDecrypter(),
                getEncryptionAlgorithmWhiteList()
        );
    }

    protected void setupKeyStore() throws Exception {
        Map<String, String> testProperties = new HashMap<>();
        testProperties.put(KeyStoreKey.KEYSTORE_PATH.getKey(), getKeyStorePath());
        testProperties.put(KeyStoreKey.KEYSTORE_TYPE.getKey(), "JKS");
        testProperties.put(KeyStoreKey.KEYSTORE_PASSWORD.getKey(), "local-demo");
        testProperties.put(KeyStoreKey.KEY_PASSWORD.getKey(), "local-demo");

        KeyStoreConfigurator keyStoreConfigurator = new KeyStoreConfigurator(testProperties, "src/test/resources");
        keyStoreContent = keyStoreConfigurator.loadKeyStoreContent();
    }

    protected String getKeyStorePath() {
        return "/keyStoreWithMultiplePrivateKeyEntries.jks";
    }

    protected boolean isCheckedValidityPeriod() {
        return false;
    }

    protected boolean isDisallowedSelfSignedCertificate() {
        return false;
    }

    protected boolean isResponseEncryptionMandatory() {
        return true;
    }

    protected boolean isAssertionEncryptWithKey() {
        return true;
    }

    protected ImmutableSet<KeyStore.PrivateKeyEntry> getDecryptionKeyAndCertificates() {
        if (keyStoreContent != null) {
            return keyStoreContent.getPrivateKeyEntries();
        }
        return ImmutableSet.<KeyStore.PrivateKeyEntry>builder().build();
    }

    protected SAMLAuthnResponseDecrypter getSamlAuthnResponseDecrypter() {
        return dummySAMLAuthnResponseDecrypter;
    }

    protected ImmutableSet<String> getEncryptionAlgorithmWhiteList() {
        return ImmutableSet.<String>builder().build();
    }

    /**
     * Subclass with tests for the private method retrieveDecryptionCredentials()
     * in the {@link AbstractProtocolDecrypter}
     * when the keystore contains multiple private key entries.
     */
    public static class DecryptionCredentialTest extends AbstractProtocolDecrypterTest {

        /**
         * Test method that verifies that both private key entries are correctly return by the method.
         * @throws Exception
         *      should not throw any exception
         */
        @Test
        public void testRetrieveDecryptionCredentials() {
            Credential[] credentials = (Credential[])
                    ReflectionUtils.invokeMethod(RETRIEVE_DECRYPTION_CREDENTIALS_METHOD, decrypter);

            Assert.assertEquals(2, credentials.length);
            List<String> validSerialNumber = Arrays.asList(
                    "4CD836D017315376DAF6EAEAB44AA1950A7268BF".toLowerCase(),
                    "13F9B7951CAC5C38A772801403DD87680A0EC2EF".toLowerCase());

            String certificateSerialNumberOfFirstCredential = getCertificateSerialNumberOfCredential(credentials[0]);
            Assert.assertTrue(validSerialNumber.contains(certificateSerialNumberOfFirstCredential));

            String certificateSerialNumberOfSecondCredential = getCertificateSerialNumberOfCredential(credentials[1]);
            Assert.assertTrue(validSerialNumber.contains(certificateSerialNumberOfSecondCredential));

            List<String> validSubject = Arrays.asList(
                    "CN=test-EC, OU=DIGIT, O=EC, L=Brussels, ST=Belgium, C=BE",
                    "CN=test-RSA, OU=DIGIT, O=EC, L=Brussels, ST=Belgium, C=BE");

            String certificateSubjectDNOfFirstCredential = getCertificateSubjectDNOfCredential(credentials[0]);
            Assert.assertTrue(validSubject.contains(certificateSubjectDNOfFirstCredential));

            String certificateSubjectDNOfSecondCredential = getCertificateSubjectDNOfCredential(credentials[1]);
            Assert.assertTrue(validSubject.contains(certificateSubjectDNOfSecondCredential));

        }

        private String getCertificateSerialNumberOfCredential(Credential credential) {
            int hexa_radix = 16;
            X509Certificate certificate = ((X509Credential) credential).getEntityCertificate();
            return certificate.getSerialNumber().toString(hexa_radix);
        }

        private String getCertificateSubjectDNOfCredential(Credential credential) {
            X509Certificate certificate = ((X509Credential) credential).getEntityCertificate();
            return certificate.getSubjectDN().toString();
        }
    }

    /**
     * Subclass with tests for the private method retrieveDecryptionCredentials()
     * in the {@link AbstractProtocolDecrypter}
     * when the keystore has only invalid private key entries and that we check the validity period
     * of the certificates.
     */
    public static class EmptyKeystoreDecryptionCredentialTest extends AbstractProtocolDecrypterTest {

        @Override
        public String getKeyStorePath() {
            return "/keyStoreMetadata.jks";
        }

        @Override
        protected boolean isCheckedValidityPeriod() {
            return true;
        }

        /**
         * Test method that verifies that an exception is correctly thrown is no valid private key entries
         * could be found in the configured keystore.
         * @throws Throwable
         *      Should throw an EIDASSAMLEngineException in the end of the test.
         */
        @Test(expected= EIDASSAMLEngineException.class)
        public void testRetrieveDecryptionCredentialWithInvalidPrivateKeys() throws Throwable {

            try {
                ReflectionUtils.invokeMethod(RETRIEVE_DECRYPTION_CREDENTIALS_METHOD, decrypter);
            } catch (Exception e) {
                Assert.assertTrue(e.getCause() instanceof EIDASSAMLEngineException);
                EIDASSAMLEngineException eidasException = (EIDASSAMLEngineException) e.getCause();
                Assert.assertEquals(EidasErrorKey.SAML_ENGINE_INVALID_CERTIFICATE.errorCode(), eidasException.getErrorCode());
                Assert.assertEquals(EidasErrorKey.SAML_ENGINE_INVALID_CERTIFICATE.errorMessage(), eidasException.getErrorMessage());
                throw e.getCause();
            }
            Assert.fail("This execution should have thrown an error");
        }
    }
}
