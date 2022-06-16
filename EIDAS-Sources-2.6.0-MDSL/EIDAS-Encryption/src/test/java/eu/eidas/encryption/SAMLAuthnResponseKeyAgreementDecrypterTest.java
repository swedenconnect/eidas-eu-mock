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

package eu.eidas.encryption;

import eu.eidas.encryption.exception.DecryptionException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.opensaml.security.x509.X509Credential;

/**
 * Tests for the {@link SAMLAuthnResponseDecrypter} used for responses encrypted with Key Agreement
 */
@RunWith(Enclosed.class)
public class SAMLAuthnResponseKeyAgreementDecrypterTest {

    /**
     * Key Agreement decryption test configuration with Brainpool algorithm
     */
    abstract static class KeyAgreementBrainpoolDecrypterTestConfig extends SAMLAuthnResponseDecrypterTest {
        private static final String MOCK_RESPONSE_FILE_PATH = "src/test/resources/responses/saml_ec_Brainpool_kw-aes256_encrypted_response.xml";

        @Override
        public String getResponseXMLFilePath() {
            return MOCK_RESPONSE_FILE_PATH;
        }

        @Override
        public String getSpecificKeySerialNumber() {
            return EC_KEY_SERIAL_NUMBER;
        }
    }

    /**
     * Tests for the {@link SAMLAuthnResponseDecrypter} with Key Agreement Brainpool credentials
     * with default parameters
     *
     * All must succeed
     */
    public static class SAMLAuthnResponseKeyAgreementDecrypterWithBrainpoolDefaultTest
            extends KeyAgreementBrainpoolDecrypterTestConfig {
        // No configuration change - use default configuration for test
    }

    /**
     * Tests for the {@link SAMLAuthnResponseDecrypter} with Key Agreement credentials
     * with kw-aes128 key encryption algorithm
     *
     * All must succeed
     */
    public static class SAMLAuthnResponseKeyAgreementDecrypterWithBrainpoolAndOtherKeyEncryptionAlgorithmTest
            extends KeyAgreementBrainpoolDecrypterTestConfig {

        private static final String MOCK_RESPONSE_FILE_PATH = "src/test/resources/responses/saml_ec_Brainpool_kw-aes128_encrypted_response.xml";

        @Override
        public String getResponseXMLFilePath() {
            return MOCK_RESPONSE_FILE_PATH;
        }
    }

    /**
     * Tests for the {@link SAMLAuthnResponseDecrypter}
     * with incorrect credentials
     *
     * Test with both credentials must succeed
     * Test with SingleCredentials must fail
     */
    public static class SAMLAuthnResponseKeyAgreementDecrypterWithBrainpoolAndIncorrectCredentialTest
            extends KeyAgreementBrainpoolDecrypterTestConfig {

        @Override
        public String getSpecificKeySerialNumber() {
            return RSA_KEY_SERIAL_NUMBER;
        }

        @Rule
        public ExpectedException thrown = ExpectedException.none();

        @Test
        public void decryptSamlResponseEncryptWithSingleCredentials() throws DecryptionException {
            thrown.expect(DecryptionException.class);
            String samlResponse = getResponseXMLFilePath();
            X509Credential credential = getSpecificDecrypterCredentials();

            decrypter.decryptSAMLResponse(createResponse(samlResponse), credential);

            Assert.fail("Should have thrown an error");
        }
    }

    /**
     * Key Agreement decryption test configuration with NIST Curve algorithm
     */
    abstract static class KeyAgreementNistCurveDecrypterTestConfig extends SAMLAuthnResponseDecrypterTest {
        private static final String MOCK_RESPONSE_FILE_PATH = "src/test/resources/responses/saml_ec_NistCurve_kw-aes256_encrypted_response.xml";

        @Override
        public String getResponseXMLFilePath() {
            return MOCK_RESPONSE_FILE_PATH;
        }

        @Override
        public String getSpecificKeySerialNumber() {
            return EC_NIST_KEY_SERIAL_NUMBER;
        }
    }

    /**
     * Tests for the {@link SAMLAuthnResponseDecrypter} with Key Agreement NIST Curve credentials
     * with default parameters
     *
     * All must succeed
     */
    public static class SAMLAuthnResponseKeyAgreementDecrypterWithNISTCurveDefaultTest
            extends KeyAgreementNistCurveDecrypterTestConfig {
        // No configuration change - use default configuration for test
    }

    /**
     * Tests for the {@link SAMLAuthnResponseDecrypter} with Key Agreement credentials
     * with NistCurve key and a NistCurve Encrypted response
     *
     * All must succeed
     */
    public static class SAMLAuthnResponseKeyAgreementDecrypterWithNistCurveAndOtherKeyEncryptionAlgorithmTest
            extends KeyAgreementNistCurveDecrypterTestConfig {

        private static final String MOCK_RESPONSE_FILE_PATH = "src/test/resources/responses/saml_ec_NistCurve_kw-aes128_encrypted_response.xml";

        @Override
        public String getResponseXMLFilePath() {
            return MOCK_RESPONSE_FILE_PATH;
        }

    }

    /**
     * Tests for the {@link SAMLAuthnResponseDecrypter}
     * with incorrect credentials
     *
     * Test with both credentials must succeed
     * Test with SingleCredentials must fail
     */
    public static class SAMLAuthnResponseKeyAgreementDecrypterWithNistCurveAndIncorrectCredentialTest
            extends KeyAgreementNistCurveDecrypterTestConfig {

        @Override
        public String getSpecificKeySerialNumber() {
            return RSA_KEY_SERIAL_NUMBER;
        }

        @Rule
        public ExpectedException thrown = ExpectedException.none();

        @Test
        public void decryptSamlResponseEncryptWithSingleCredentials() throws DecryptionException {
            thrown.expect(DecryptionException.class);
            String samlResponse = getResponseXMLFilePath();
            X509Credential credential = getSpecificDecrypterCredentials();

            decrypter.decryptSAMLResponse(createResponse(samlResponse), credential);

            Assert.fail("Should have thrown an error");
        }
    }

}
