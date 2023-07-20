/*
 * Copyright (c) 2022 by European Commission
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
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.KeyStoreConfigurator;
import eu.eidas.auth.engine.configuration.dom.KeyStoreContent;
import eu.eidas.auth.engine.configuration.dom.KeyStoreKey;
import eu.eidas.encryption.DefaultEncryptionAlgorithm;
import eu.eidas.encryption.SAMLAuthnResponseEncrypter;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.crypto.JCAConstants;
import org.opensaml.xmlsec.encryption.EncryptionMethod;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.signature.DigestMethod;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractProtocolEncrypterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProtocolEncrypterTest.class);

    private static final String BOUNCY_CASTLE_PROVIDER_NAME = "BC";
    private static final String MOCK_RESPONSE_FILE_PATH = "src/test/resources/responses/saml_response.xml";

    private final ImmutableSet<X509Certificate> certificates = generateKeyStoreContent("/keyStoreWithMultiplePrivateKeyEntries.p12").getCertificates();

    private final SAMLAuthnResponseEncrypter samlEncrypter = SAMLAuthnResponseEncrypter.builder()
            .dataEncryptionAlgorithm(DefaultEncryptionAlgorithm.DEFAULT_DATA_ENCRYPTION_ALGORITHM.getValue())
            .jcaProviderName("BC")
            .keyEncryptionAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP11)
            .keyEncryptionAlgorithmForKeyAgreement(EncryptionConstants.ALGO_ID_KEYAGREEMENT_ECDH_ES)
            .build();

    private AbstractProtocolEncrypter encrypter;

    @BeforeClass
    public static void setUpClass() {
        if (Security.getProvider(BOUNCY_CASTLE_PROVIDER_NAME) == null) {
            LOGGER.info("Add BC Security Provider in the JVM");
            Security.addProvider(new BouncyCastleProvider());
        }
        OpenSamlHelper.initialize();
    }

    @AfterClass
    public static void tearDownClass() {
        Provider bouncyCastleProvider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (bouncyCastleProvider != null) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @Before
    public void setUp(){
        encrypter = new TestEncrypter();
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for
     * {@link AbstractProtocolEncrypter#encryptSamlResponse(Response, X509Certificate, boolean)}
     * when destination certificate is null
     * <p>
     * Must fail.
     */
    @Test
    public void testEncryptSamlResponseDestinationCertificateNull() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        final Response authResponse = Mockito.mock(Response.class);
        encrypter.encryptSamlResponse(authResponse, null, true);
    }

    /**
     * Test method for
     * {@link AbstractProtocolEncrypter#encryptSamlResponse(Response, X509Certificate, boolean)}
     * when the response contains invalid assertions
     * <p>
     * Must fail.
     */
    @Test
    public void testEncryptSamlResponseInvalidAssertions() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        final Response authResponse = Mockito.mock(Response.class);
        encrypter.encryptSamlResponse(authResponse, certificates.asList().get(1), true);
    }

    /**
     * Test method for
     * {@link AbstractProtocolEncrypter#encryptSamlResponse(Response, X509Certificate, boolean)}
     * when happy flow
     * <p>
     * Must succeed.
     */
    @Test
    public void testEncryptSamlResponse() throws EIDASSAMLEngineException {
        final Response authResponse = createMockResponse();
        X509Certificate signingCert = getRSACertificate();
        Response encryptedResponse = encrypter.encryptSamlResponse(authResponse, signingCert, true);

        Assert.assertFalse(encryptedResponse.getEncryptedAssertions().isEmpty());
        Assert.assertEquals(1, encryptedResponse.getEncryptedAssertions().size());

        EncryptedAssertion encryptedAssertion = encryptedResponse.getEncryptedAssertions().get(0);
        verifyDigestMethodForRSAOAEP(encryptedAssertion.getEncryptedData().getEncryptionMethod(),
                SignatureConstants.ALGO_ID_DIGEST_SHA1);
    }

    private void verifyDigestMethodForRSAOAEP(EncryptionMethod encryptionMethod, String digestMethod) {
        if (EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP.equals(encryptionMethod.getAlgorithm())) {
            List<XMLObject> digestMethods = encryptionMethod.getUnknownXMLObjects(DigestMethod.DEFAULT_ELEMENT_NAME);
            Assert.assertEquals(1, digestMethods.size());
            Assert.assertTrue(((DigestMethod) digestMethods.get(0)).getAlgorithm()
                    .contains(digestMethod));
        }
    }

    private Response createMockResponse() {
        try {
            InputStream mockResponseXML = new FileInputStream(MOCK_RESPONSE_FILE_PATH);
            Document mockResponseDocument = DocumentBuilderFactoryUtil.parse(mockResponseXML);
            XMLObject mockResponseXmlObject = OpenSamlHelper.unmarshallFromDom(mockResponseDocument);
            return (Response) mockResponseXmlObject;
        } catch (Exception e) {
            LOGGER.error("Mock response could not be loaded!");
            throw new RuntimeException(e);
        }
    }

    private KeyStoreContent generateKeyStoreContent(String keystorepath) {
       final Map<String, String> testProperties = new HashMap<>();
        testProperties.put(KeyStoreKey.KEYSTORE_PATH.getKey(), keystorepath);
        testProperties.put(KeyStoreKey.KEYSTORE_TYPE.getKey(), "PKCS12");
        testProperties.put(KeyStoreKey.KEYSTORE_PASSWORD.getKey(), "local-demo");
        testProperties.put(KeyStoreKey.KEY_PASSWORD.getKey(), "local-demo");

        try {
           final KeyStoreConfigurator keyStoreConfigurator = new KeyStoreConfigurator(testProperties, "src/test/resources");
            return keyStoreConfigurator.loadKeyStoreContent();
        } catch (ProtocolEngineConfigurationException e) {
            e.printStackTrace();
            Assert.fail("KeyStoreContent has not been initialized correctly");
            return null;
        }
    }

    private X509Certificate getRSACertificate() {
        for (X509Certificate certificate: certificates) {
            if (JCAConstants.KEY_ALGO_RSA.equalsIgnoreCase(certificate.getPublicKey().getAlgorithm())) {
                return certificate;
            }
        }
        return certificates.asList().get(0);
    }

    private class TestEncrypter extends AbstractProtocolEncrypter {
        public TestEncrypter() {
            super(
                    false,
                    false,
                    true,
                    true,
                    AbstractProtocolEncrypterTest.this.certificates,
                    AbstractProtocolEncrypterTest.this.samlEncrypter,
                    ImmutableSet.<String>builder().add("sha512-rsa-MGF1").build());
        }

        /**
         * Returns the encryption certificate to be used to encrypt a response for the given country
         *
         * @param destinationCountryCode the given country code
         * @return the encryption certificate to be used to encrypt a response for the given country
         */
        @Nullable
        @Override
        public X509Certificate getEncryptionCertificate(@Nullable String destinationCountryCode) {
            return null;
        }

        /**
         * Returns whether encryption is enabled for the given country.
         *
         * @param countryCode the 2-letter country code as defined in ISO 3166 of the country the response is being sent
         *                    to.
         * @return whether encryption is enabled for the given country.
         */
        @Override
        public boolean isEncryptionEnabled(@Nonnull String countryCode) {
            return false;
        }
    }
}