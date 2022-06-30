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
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.encryption.EncryptedData;
import org.opensaml.xmlsec.encryption.EncryptedKey;
import org.opensaml.xmlsec.encryption.EncryptionMethod;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.signature.DigestMethod;
import org.opensaml.xmlsec.signature.KeyInfo;

import java.util.List;

/**
 * Tests for the {@link SAMLAuthnResponseEncrypter} with KeyTransport
 */
@RunWith(Enclosed.class)
public class SAMLAuthnResponseKeyTransportEncrypterTest {

    private static final String CERTIFICATE_PATH = "src/test/resources/certificates/keyTransport.crt";

    abstract static class KeyTransportEncrypterTestConfig extends SAMLAuthnResponseEncrypterTestConfig {

        @Override
        protected String getCertificateFilePath() {
            return CERTIFICATE_PATH;
        }

        @Override
        protected String getDefaultKeyEncryptionAlgorithm() {
            return DefaultEncryptionAlgorithm.DEFAULT_KEY_ENCRYPTION_ALGORITHM.getValue();
        }

        /**
         * Test method for {@link SAMLAuthnResponseEncrypter#encryptSAMLResponse(Response, Credential, boolean)}
         * with key transport credentials
         */
        @Test
        public void testEncryptSAMLResponse() throws EncryptionException {

            Response result = encrypter.encryptSAMLResponse(getDefaultResponse(), getCredential(), isKeyInfoDisplayedAsKeyValue());

            verifyResponseAssertions(result);

            EncryptedData actualEncryptedData = result.getEncryptedAssertions().get(0).getEncryptedData();
            verifyEncryptedData(actualEncryptedData);

            EncryptedKey encryptedKey = actualEncryptedData.getKeyInfo().getEncryptedKeys().get(0);
            verifyDigestMethodForRSAOAEP(encryptedKey.getEncryptionMethod());
        }

        protected void verifyKeyInfoFromEncryptedKey(KeyInfo keyInfo) {
            if (isKeyInfoDisplayedAsKeyValue()) {
                Assert.assertTrue(keyInfo.getX509Datas().isEmpty());
                Assert.assertFalse(keyInfo.getKeyValues().isEmpty());
            } else {
                Assert.assertFalse(keyInfo.getX509Datas().isEmpty());
                Assert.assertTrue(keyInfo.getKeyValues().isEmpty());
            }
        }

        protected void verifyDigestMethodForRSAOAEP(EncryptionMethod encryptionMethod) {
            if (EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP.equals(encryptionMethod.getAlgorithm())) {
                List<XMLObject> digestMethods = encryptionMethod.getUnknownXMLObjects(DigestMethod.DEFAULT_ELEMENT_NAME);
                Assert.assertEquals(1, digestMethods.size());
                Assert.assertTrue(((DigestMethod) digestMethods.get(0)).getAlgorithm()
                        .contains(DigestMethod.DEFAULT_ELEMENT_NAME.getNamespaceURI()));
            }
        }
    }

    /**
     * Tests for the {@link SAMLAuthnResponseEncrypter} with Key Transport
     * with default parameters
     */
    public static class SAMLAuthnResponseKeyTransportEncrypterWithDefaultParametersTest
            extends KeyTransportEncrypterTestConfig {
        // No configuration change - use default configuration for test
    }

    /**
     * Tests for the {@link SAMLAuthnResponseEncrypter} with Key Transport
     * Modifying keyEncryptionAlgorithmForKeyAgreement doesn't impact algorithm used for keyTransport
     */
    public static class SAMLAuthnResponseKeyTransportEncrypterWithNoAlgorithmForKeyTransportTest
            extends KeyTransportEncrypterTestConfig {

        @Override
        protected String getKeyEncryptionAlgorithmForKeyAgreement() {
            return EncryptionConstants.ALGO_ID_KEYWRAP_AES128;
        }
    }

    /**
     * Tests for the {@link SAMLAuthnResponseEncrypter} with Key Transport
     * with default parameters but showing RSA key instead of certificate in the encrypted key.
     */
    public static class SAMLAuthnResponseKeyTransportEncrypterWithKeyInfoDisplayedAsRSAKeyTest
            extends KeyTransportEncrypterTestConfig {

        @Override
        protected boolean isKeyInfoDisplayedAsKeyValue() {
            return true;
        }
    }

    /**
     * Tests for the {@link SAMLAuthnResponseEncrypter} with Key Transport
     * with RSAOAEP 11 as key encryption algorithm
     */
    public static class SAMLAuthnResponseKeyTransportEncrypterWithOtherKeyEncryptionAlgorithmTest
            extends KeyTransportEncrypterTestConfig {

        @Override
        protected String getKeyEncryptionAlgorithm() {
            return EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP11;
        }

    }

    /**
     * Tests for the {@link SAMLAuthnResponseEncrypter} with Key Transport
     * with AES192-GCM instead of the default AES256-GCM as data encryption algorithm
     */
    public static class SAMLAuthnResponseKeyTransportEncrypterWithOtherDataEncryptionAlgorithmTest
            extends KeyTransportEncrypterTestConfig {

        @Override
        protected String getDataEncryptionAlgorithm() {
            return EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES192_GCM;
        }

    }


    /**
     * Tests for the {@link SAMLAuthnResponseEncrypter} with Key Transport
     * with key encryption algorithm not compatible with Key Transport
     */
    public static class SAMLAuthnResponseKeyTransportEncrypterWithIncompatibleKeyEncryptionAlgorithmTest
            extends KeyTransportEncrypterTestConfig {

        /**
         * This key encryption algorithm is not compatible with key Transport
         * hence the test should fail.
         */
        @Override
        protected String getKeyEncryptionAlgorithm() {
            return EncryptionConstants.ALGO_ID_KEYAGREEMENT_DH;
        }

        @Rule
        public ExpectedException thrown = ExpectedException.none();

        /**
         * Test method for {@link SAMLAuthnResponseEncrypter#encryptSAMLResponse(Response, Credential, boolean)}
         * with key transport credentials
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
