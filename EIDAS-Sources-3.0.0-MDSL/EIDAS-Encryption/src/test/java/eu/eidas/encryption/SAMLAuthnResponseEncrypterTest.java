/*
 * Copyright (c) 2025 by European Commission
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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.encryption.exception.EncryptionException;
import org.junit.Assert;
import org.junit.Test;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.slf4j.LoggerFactory;

/**
 * Test class for {@link SAMLAuthnResponseEncrypter}
 */
public class SAMLAuthnResponseEncrypterTest extends SAMLAuthnResponseEncrypterTestConfig {

    /**
     * Test method for {@link SAMLAuthnResponseEncrypter#encryptSAMLResponse(Response, Credential, boolean)}
     * Ensures that releasing the DOM does not change the logical contents of the SAML response.
     * <p>
     * Must succeed.
     */
    @Test
    public void testReleaseDomKeepsResponseIntact() throws Exception {
        final Response original = getDefaultResponse();
        final String before = EidasStringUtil.toString(OpenSamlHelper.marshall(XMLObjectSupport.cloneXMLObject(original)));

        original.releaseDOM();
        original.releaseChildrenDOM(true);

        final String after = EidasStringUtil.toString(OpenSamlHelper.marshall(XMLObjectSupport.cloneXMLObject(original)));

        Assert.assertEquals("Released DOM should not change logical content", before, after);
    }

    /**
     * Test method for {@link SAMLAuthnResponseEncrypter#encryptSAMLResponse(Response, Credential, boolean)}
     * Simulates TRACE logging enabled and verifies that encryption still succeeds.
     * <p>
     * Must succeed.
     */
    @Test
    public void testEncryptSamlResponseWithTraceLogging() throws EncryptionException {
        final Logger logger = (Logger) LoggerFactory.getLogger(SAMLAuthnResponseEncrypter.class);
        final Level originalLevel = logger.getLevel();
        logger.setLevel(Level.TRACE);

        try {
            final Response samlResponse = getDefaultResponse();
            final Credential credential = getCredential();

            final Response encrypted = encrypter.encryptSAMLResponse(samlResponse, credential, true);

            Assert.assertNotNull("Encrypted response must not be null", encrypted);
            Assert.assertNotSame("Encrypted response should be different from input", samlResponse, encrypted);

            verifyResponseAssertions(encrypted);
            verifyEncryptedData(encrypted.getEncryptedAssertions().get(0).getEncryptedData());
        } finally {
            logger.setLevel(originalLevel);
        }
    }

    @Override
    protected String getCertificateFilePath() {
        return "src/test/resources/certificates/keyAgreement.crt";
    }

    @Override
    protected String getDefaultKeyEncryptionAlgorithm() {
        return DefaultEncryptionAlgorithm.DEFAULT_KEY_ENCRYPTION_ALGORITHM_FOR_KEY_AGREEMENT.getValue();
    }

    @Override
    protected void verifyKeyInfoFromEncryptedKey(KeyInfo keyInfo) {

    }

}