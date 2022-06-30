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
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.encryption.EncryptedData;
import org.opensaml.xmlsec.encryption.EncryptedKey;
import org.opensaml.xmlsec.encryption.EncryptionMethod;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;

/**
 * Abstract Test class helping for the setup of tests for {@link SAMLAuthnResponseEncrypter}
 */
public abstract class SAMLAuthnResponseEncrypterTestConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SAMLAuthnResponseEncrypterTestConfig.class);
    private static final String MOCK_RESPONSE_FILE_PATH = "src/test/resources/responses/saml_response.xml";

    protected SAMLAuthnResponseEncrypter encrypter;

    private Response mockResponse;
    private Credential credential;

    @BeforeClass
    public static void setupClass() {
        OpenSamlHelper.initialize();
    }

    @Before
    public void setup() throws Exception {
        setupCredentials();
        encrypter = setupEncrypter(getDataEncryptionAlgorithm(),
                getJCAProviderName(),
                getKeyEncryptionAlgorithm(),
                getKeyEncryptionAlgorithmForKeyAgreement());
        mockResponse = createMockResponse();
    }

    protected SAMLAuthnResponseEncrypter setupEncrypter(String dataEncryptionAlgorithm, String jcaProviderName,
            String keyEncryptionAlgorithm, String keyEncryptionAlgorithmForKeyAgreement) {
        SAMLAuthnResponseEncrypter.Builder samlAuthnResponseEncrypterBuilder = SAMLAuthnResponseEncrypter.builder()
                .dataEncryptionAlgorithm(dataEncryptionAlgorithm)
                .jcaProviderName(jcaProviderName)
                .keyEncryptionAlgorithm(keyEncryptionAlgorithm)
                .keyEncryptionAlgorithmForKeyAgreement(keyEncryptionAlgorithmForKeyAgreement);

        return samlAuthnResponseEncrypterBuilder.build();
    }

    protected void setupCredentials() throws CertificateException, NoSuchProviderException, FileNotFoundException {
        CertificateFactory certificateFactory;
        if (null == getSecurityProviderName()) {
            certificateFactory = CertificateFactory.getInstance("X.509");
        } else {
            certificateFactory = CertificateFactory.getInstance("X.509", getSecurityProviderName());
        }
        FileInputStream certificateFileInputStream = new FileInputStream(getCertificateFilePath());
        Certificate certificate = certificateFactory.generateCertificate(certificateFileInputStream);
        credential = CertificateUtil.toCredential((X509Certificate) certificate);
    }

    protected abstract String getCertificateFilePath();

    protected String getSecurityProviderName() {
        return null;
    }

    protected Response createMockResponse() {
        try {
            InputStream mockResponseXML = getMockResponseXML();
            Document mockResponseDocument = DocumentBuilderFactoryUtil.parse(mockResponseXML);
            XMLObject mockResponseXmlObject = OpenSamlHelper.unmarshallFromDom(mockResponseDocument);
            return (Response) mockResponseXmlObject;
        } catch (Exception e) {
            LOGGER.error("Mock response could not be loaded!");
            throw new RuntimeException(e);
        }
    }

    protected String getDataEncryptionAlgorithm() {
        return null;
    }

    protected String getDefaultDataEncryptionAlgorithm() {
        return DefaultEncryptionAlgorithm.DEFAULT_DATA_ENCRYPTION_ALGORITHM.getValue();
    }

    protected String getJCAProviderName() {
        return null;
    }

    protected String getKeyEncryptionAlgorithm() {
        return null;
    }

    protected String getKeyEncryptionAlgorithmForKeyAgreement() {
        return null;
    }

    protected abstract String getDefaultKeyEncryptionAlgorithm();

    protected InputStream getMockResponseXML() throws Exception {
        return new FileInputStream(MOCK_RESPONSE_FILE_PATH);
    }

    protected Response getDefaultResponse() {
        return mockResponse;
    }

    protected Credential getCredential() {
        return credential;
    }

    protected boolean isKeyInfoDisplayedAsKeyValue() {
        return false;
    }

    protected void verifyResponseAssertions(Response encryptedResponse) {
        Assert.assertTrue(encryptedResponse.getAssertions().isEmpty());

        Assert.assertFalse(encryptedResponse.getEncryptedAssertions().isEmpty());
        Assert.assertEquals(1, encryptedResponse.getEncryptedAssertions().size());
    }

    protected void verifyEncryptedData(EncryptedData encryptedData) {
        verifyDataEncryptionAlgorithm(encryptedData);

        long expectedAmountOfEncryptedKeys = 1;
        Assert.assertEquals(expectedAmountOfEncryptedKeys, encryptedData.getKeyInfo().getEncryptedKeys().size());

        EncryptedKey encryptedKey = encryptedData.getKeyInfo().getEncryptedKeys().get(0);
        verifyEncryptedKey(encryptedKey);
    }

    protected void verifyDataEncryptionAlgorithm(EncryptedData encryptedData) {
        if (null == getDataEncryptionAlgorithm()) {
            String expectedDataEncryptionAlgorithm = getDefaultDataEncryptionAlgorithm();
            verifyEncryptionMethod(expectedDataEncryptionAlgorithm, encryptedData.getEncryptionMethod());
        } else {
            verifyEncryptionMethod(getDataEncryptionAlgorithm(), encryptedData.getEncryptionMethod());
        }
    }

    protected void verifyEncryptedKey(EncryptedKey encryptedKey) {
        verifyKeyEncryptionAlgorithm(encryptedKey);
        verifyKeyInfoFromEncryptedKey(encryptedKey.getKeyInfo());
    }

    protected void verifyKeyEncryptionAlgorithm(EncryptedKey encryptedKey) {
        String expectedKeyEncryptionAlgorithm = getExpectedKeyEncryptionAlgorithm();
        verifyEncryptionMethod(expectedKeyEncryptionAlgorithm, encryptedKey.getEncryptionMethod());
    }

    /**
     * This method gets the default key encryption algorithm or configured key encryption algorithm
     * depending on the type of credentials used by the test class. This match the key encryption
     * algorithm that we expect to be used by the encryption.
     * @return the expected key encryption algorithm to should have been used.
     */
    protected String getExpectedKeyEncryptionAlgorithm() {
        if (ECPublicKey.class.isInstance(credential.getPublicKey())) {
            if (null == getKeyEncryptionAlgorithmForKeyAgreement()){
                return getDefaultKeyEncryptionAlgorithm();
            } else {
                return getKeyEncryptionAlgorithmForKeyAgreement();
            }
        } else {
            if (null == getKeyEncryptionAlgorithm()){
                return getDefaultKeyEncryptionAlgorithm();
            } else {
                return getKeyEncryptionAlgorithm();
            }
        }
    }

    protected abstract void verifyKeyInfoFromEncryptedKey(KeyInfo keyInfo);

    protected void verifyEncryptionMethod(String expectedMethodAlgorithm, EncryptionMethod encryptionMethod) {
        Assert.assertEquals(expectedMethodAlgorithm, encryptionMethod.getAlgorithm());
    }

}
