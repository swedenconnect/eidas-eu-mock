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

package eu.eidas.encryption;

import eu.eidas.encryption.exception.EncryptionException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.encryption.AgreementMethod;
import org.opensaml.xmlsec.encryption.EncryptedData;
import org.opensaml.xmlsec.encryption.EncryptedKey;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.signature.KeyInfo;
import se.swedenconnect.opensaml.xmlsec.encryption.support.EcEncryptionConstants;

/**
 * Tests for the {@link SAMLAuthnResponseEncrypter} with KeyAgreement
 */
@RunWith(Enclosed.class)
public class SAMLAuthnResponseKeyAgreementEncrypterTest {

    private static final String CERTIFICATE_PATH = "src/test/resources/certificates/keyAgreement.crt";

    abstract static class KeyAgreementEncrypterTestConfig extends SAMLAuthnResponseEncrypterTestConfig {

        @Override
        protected String getCertificateFilePath() {
            return CERTIFICATE_PATH;
        }

        /**
         * Explicitly use the BouncyCastleProvider for the ECC certificate.
         */
        @Override
        protected String getSecurityProviderName() {
            return BouncyCastleProvider.PROVIDER_NAME;
        }

        @Override
        protected String getDefaultKeyEncryptionAlgorithm() {
            return DefaultEncryptionAlgorithm.DEFAULT_KEY_ENCRYPTION_ALGORITHM_FOR_KEY_AGREEMENT.getValue();
        }

        /**
         * Test method for {@link SAMLAuthnResponseEncrypter#encryptSAMLResponse(Response, Credential, boolean)}
         * with key agreement credentials
         */
        @Test
        public void testEncryptSAMLResponse() throws EncryptionException {

            Response result = encrypter.encryptSAMLResponse(getDefaultResponse(), getCredential(), isKeyInfoDisplayedAsKeyValue());

            verifyResponseAssertions(result);

            EncryptedData actualEncryptedData = result.getEncryptedAssertions().get(0).getEncryptedData();
            verifyEncryptedData(actualEncryptedData);

            EncryptedKey encryptedKey = actualEncryptedData.getKeyInfo().getEncryptedKeys().get(0);
            verifyAgreementMethod(EcEncryptionConstants.ALGO_ID_KEYAGREEMENT_ECDH_ES, encryptedKey);
        }

        protected void verifyKeyInfoFromEncryptedKey(KeyInfo keyInfo) {
            Assert.assertTrue(keyInfo.getX509Datas().isEmpty());
            Assert.assertTrue(keyInfo.getKeyValues().isEmpty());
            Assert.assertFalse(keyInfo.getAgreementMethods().isEmpty());
        }

        protected void verifyAgreementMethod(String agreementMethodAlgorithm, EncryptedKey encryptedKey) {
            AgreementMethod actualAgreementMethod = encryptedKey.getKeyInfo().getAgreementMethods().get(0);
            Assert.assertNotNull(actualAgreementMethod);
            Assert.assertEquals(agreementMethodAlgorithm, actualAgreementMethod.getAlgorithm());
        }

    }

    /**
     * Tests for the {@link SAMLAuthnResponseEncrypter} with Key Agreement
     * with default parameters
     */
    public static class SAMLAuthnResponseKeyAgreementEncrypterWithDefaultParametersTest
            extends KeyAgreementEncrypterTestConfig {
        // No configuration change - use default configuration for test
    }

    /**
     * Tests for the {@link SAMLAuthnResponseEncrypter} with Key Agreement
     * Modifying keyEncryptionAlgorithm doesn't impact algorithm used for keyAgreement
     */
    public static class SAMLAuthnResponseKeyAgreementEncrypterWithNoAlgorithmForKeyAgreementTest
            extends KeyAgreementEncrypterTestConfig {

        @Override
        protected String getKeyEncryptionAlgorithm() {
            return EncryptionConstants.ALGO_ID_KEYWRAP_AES128;
        }

    }

    /**
     * Tests for the {@link SAMLAuthnResponseEncrypter} with Key Agreement
     * with kw-aes128 key encryption algorithm
     */
    public static class SAMLAuthnResponseKeyAgreementEncrypterWithOtherKeyEncryptionAlgorithmTest
            extends KeyAgreementEncrypterTestConfig {

        @Override
        protected String getKeyEncryptionAlgorithmForKeyAgreement() {
            return EncryptionConstants.ALGO_ID_KEYWRAP_AES128;
        }

    }

    /**
     * Tests for the {@link SAMLAuthnResponseEncrypter} with Key Agreement
     * with an incompatible key encryption algorithm.
     */
    public static class SAMLAuthnResponseKeyAgreementEncrypterWithIncompatibleKeyEncryptionAlgorithmTest
            extends KeyAgreementEncrypterTestConfig {

        /**
         * This key encryption algorithm is not compatible with key Agreement
         * hence the test should fail.
         */
        @Override
        protected String getKeyEncryptionAlgorithmForKeyAgreement() {
            return EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP11;
        }

        @Rule
        public ExpectedException thrown = ExpectedException.none();

        /**
         * Test method for {@link SAMLAuthnResponseEncrypter#encryptSAMLResponse(Response, Credential, boolean)}
         * with key agreement credentials
         * but with incompatible encryption algorithm
         */
        @Test
        public void testEncryptSAMLResponse() throws EncryptionException {
            thrown.expect(EncryptionException.class);
            encrypter.encryptSAMLResponse(getDefaultResponse(), getCredential(), false);

            Assert.fail("Should have thrown an error");
        }
    }
}
