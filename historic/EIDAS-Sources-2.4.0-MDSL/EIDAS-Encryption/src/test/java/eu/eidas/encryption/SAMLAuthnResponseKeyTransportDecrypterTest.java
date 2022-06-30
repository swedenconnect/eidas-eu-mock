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

import eu.eidas.encryption.exception.DecryptionException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.opensaml.security.x509.X509Credential;

/**
 * Tests for the {@link SAMLAuthnResponseDecrypter} used for responses encrypted with Key Transport
 */
@RunWith(Enclosed.class)
public class SAMLAuthnResponseKeyTransportDecrypterTest {

    abstract static class KeyTransportDecrypterTestConfig extends SAMLAuthnResponseDecrypterTest {

        private static final String MOCK_RESPONSE_FILE_PATH = "src/test/resources/responses/saml_rsa_rsa-oaep-mgf1p_encrypted_response.xml";

        @Override
        public String getResponseXMLFilePath() {
            return MOCK_RESPONSE_FILE_PATH;
        }

        @Override
        public String getSpecificKeySerialNumber() {
            return RSA_KEY_SERIAL_NUMBER;
        }
    }

    /**
     * Tests for the {@link SAMLAuthnResponseDecrypter} with Key Transport credentials
     * with default parameters
     *
     * All must succeed
     */
    public static class SAMLAuthnResponseKeyTransportDecrypterWithDefaultParametersTest
            extends KeyTransportDecrypterTestConfig {
        // No configuration change - use default configuration for test
    }

    /**
     * Tests for the {@link SAMLAuthnResponseDecrypter} with Key Transport credentials
     * with RSAOAEP key encryption algorithm
     *
     * All must succeed
     */
    public static class SAMLAuthnResponseKeyTransportDecrypterWithOtherKeyEncryptionAlgorithmTest
            extends KeyTransportDecrypterTestConfig {

        private static final String MOCK_RESPONSE_FILE_PATH = "src/test/resources/responses/saml_rsa_rsa-oaep_encrypted_response.xml";

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
    public static class SAMLAuthnResponseKeyTransportDecrypterWithIncorrectCredentialTest
            extends KeyTransportDecrypterTestConfig {

        @Override
        public String getSpecificKeySerialNumber() {
            return EC_KEY_SERIAL_NUMBER;
        }

        @Rule
        public ExpectedException thrown = ExpectedException.none();

        @Test
        public void decryptSamlResponseEncryptWithSingleCredentials() throws DecryptionException {
            thrown.expect(DecryptionException.class);
            String samlResponse = getResponseXMLFilePath();
            X509Credential credential = getSpecificDecrypterCredentials();

            decrypter.decryptSAMLResponse(createMockResponse(samlResponse), credential);

            Assert.fail("Should have thrown an error");
        }
    }

}
