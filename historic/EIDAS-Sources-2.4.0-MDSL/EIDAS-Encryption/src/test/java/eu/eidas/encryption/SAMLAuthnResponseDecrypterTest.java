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

import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.encryption.exception.DecryptionException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Abstract Test class helping for the setup of tests for {@link SAMLAuthnResponseDecrypter}
 */
public abstract class SAMLAuthnResponseDecrypterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SAMLAuthnResponseDecrypterTest.class);
    protected static final String EC_KEY_SERIAL_NUMBER = "4cd836d017315376daf6eaeab44aa1950a7268bf";
    protected static final String RSA_KEY_SERIAL_NUMBER = "13f9b7951cac5c38a772801403dd87680a0ec2ef";


    protected SAMLAuthnResponseDecrypter decrypter;
    protected List<Credential> credentials = new ArrayList<>();

    private String keyStorePassword = "local-demo";

    @BeforeClass
    public static void setupClass() {
        if (Security.getProvider("BC") == null) {
            LOGGER.info("Add BC Security Provider in first position in the JVM");
            Security.insertProviderAt(new BouncyCastleProvider(), 1);
        }
        OpenSamlHelper.initialize();
    }

    @Before
    public void setup() throws Exception {
        setupCredentials();
        decrypter = new SAMLAuthnResponseDecrypter(getJcaProviderName());
    }

    public void setupCredentials() throws Exception {
        KeyStore keystore = KeyStore.getInstance("jks");
        keystore.load(new FileInputStream("src/test/resources" + getKeyStorePath()), getKeyStorePassword());
        LOGGER.debug("Keystore " + getKeyStorePath() + " is loaded.");
        KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(getKeyPassword());
        try {
            Enumeration<String> keyAliases = keystore.aliases();
            while (keyAliases.hasMoreElements()) {
                String keyAlias = keyAliases.nextElement();
                if (keystore.isKeyEntry(keyAlias)) {
                    LOGGER.debug("Extract key with alias {} from keystore", keyAlias);
                    KeyStore.Entry keyStoreEntry = keystore.getEntry(keyAlias, passwordProtection);
                    if (keyStoreEntry instanceof KeyStore.PrivateKeyEntry) {
                        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStoreEntry;
                        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
                        Credential credential = new BasicX509Credential(certificate, privateKeyEntry.getPrivateKey());
                        credentials.add(credential);
                    }
                }
            }
        } finally {
            passwordProtection.destroy();
        }
    }

    @Nullable
    public String getJcaProviderName() {
        return null;
    }

    protected String getKeyStorePath() {
        return "/keystores/test.jks";
    }

    protected char[] getKeyStorePassword() {
        return keyStorePassword.toCharArray();
    }

    protected char[] getKeyPassword() {
        return keyStorePassword.toCharArray();
    }

    protected abstract String getResponseXMLFilePath();

    protected X509Credential getSpecificDecrypterCredentials() {
        for (Credential credential : getDecryptionCredentials()) {
            X509Credential x509Credential = (X509Credential) credential;
            String credentialSerialNumber = x509Credential.getEntityCertificate().getSerialNumber().toString(16);
            if (getSpecificKeySerialNumber().equalsIgnoreCase(credentialSerialNumber)) {
                return x509Credential;
            }
        }
        return null;
    }

    protected abstract String getSpecificKeySerialNumber();

    protected Response createMockResponse(String responseFilePath) {
        try {
            InputStream responseInputStream = new FileInputStream((responseFilePath));
            Document responseDocument = DocumentBuilderFactoryUtil.parse(responseInputStream);
            return (Response) OpenSamlHelper.unmarshallFromDom(responseDocument);
        } catch (Exception e) {
            LOGGER.error("Mock response could not be loaded!");
            throw new RuntimeException(e);
        }
    }

    public List<Credential> getDecryptionCredentials() {
        return credentials;
    }

    /**
     * Test method for {@link SAMLAuthnResponseDecrypter#decryptSAMLResponse(Response, Credential...)}
     * with an array of credentials from the configured test KeyStore
     */
    @Test
    public void decryptSamlResponse() throws DecryptionException {
        String samlResponse = getResponseXMLFilePath();
        List<Credential> credentials = getDecryptionCredentials();
        Credential[] decrytpCredentialArray = credentials.toArray(new Credential[credentials.size()]);

        Response mockResponse = createMockResponse(samlResponse);
        Response actualResponse = decrypter.decryptSAMLResponse(mockResponse, decrytpCredentialArray);

        assertValidResponse(actualResponse);
    }

    /**
     * Test method for {@link SAMLAuthnResponseDecrypter#decryptSAMLResponse(Response, X509Credential)}
     * with one specific credential from the configured test KeyStore
     */
    @Test
    public void decryptSamlResponseEncryptWithSingleCredentials() throws DecryptionException {
        String samlResponse = getResponseXMLFilePath();
        X509Credential decryptCredential = getSpecificDecrypterCredentials();

        Response actualResponse = decrypter.decryptSAMLResponse(createMockResponse(samlResponse), decryptCredential);

        assertValidResponse(actualResponse);
    }

    private void assertValidResponse(Response actualResponse) {
        Assert.assertTrue(actualResponse.getEncryptedAssertions().isEmpty());
        Assert.assertFalse(actualResponse.getAssertions().isEmpty());

        Assert.assertEquals(actualResponse.getAssertions().size(), 1);

        Assertion actualAssertion = actualResponse.getAssertions().get(0);

        String expectedNameId = "0123456";
        verifyNameIdValue(expectedNameId, actualAssertion);
        verifyAmountOfAttributeStatements(1, actualAssertion);

        AttributeStatement actualAttributeStatement = actualAssertion.getAttributeStatements().get(0);
        int expectedAmount = 6;
        verifyAmountOfAttributes(expectedAmount, actualAttributeStatement);
    }

    private void verifyNameIdValue(String expectedNameId, Assertion assertion) {
        String actualNameIdValue = assertion.getSubject().getNameID().getValue();
        Assert.assertEquals(expectedNameId, actualNameIdValue);
    }

    private void verifyAmountOfAttributeStatements(long expectedAmount, Assertion assertion) {
        long actualAmount = assertion.getAttributeStatements().size();
        Assert.assertEquals(expectedAmount, actualAmount);
    }

    private void verifyAmountOfAttributes(long expectedAmount, AttributeStatement attributeStatement) {
        long actualAmountOfAttributes = attributeStatement.getAttributes().size();
        Assert.assertEquals(expectedAmount, actualAmountOfAttributes);
    }

}
