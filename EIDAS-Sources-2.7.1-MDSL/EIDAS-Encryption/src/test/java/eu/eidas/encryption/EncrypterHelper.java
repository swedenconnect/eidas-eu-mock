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

package eu.eidas.encryption;

import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import org.junit.Assert;
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
 * Test class helping for the setup of tests that use an Encrypter
 */
public class EncrypterHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncrypterHelper.class);
    private static final String MOCK_RESPONSE_FILE_PATH = "src/test/resources/responses/saml_response.xml";


    public SAMLAuthnResponseEncrypter encrypter;

    private Response mockResponse;
    public Credential credential;

    public EncrypterHelper() throws FileNotFoundException, CertificateException, NoSuchProviderException {
        setupCredentials();
        encrypter = setupEncrypter(getDataEncryptionAlgorithm(),
                getJCAProviderName(),
                getKeyEncryptionAlgorithm(),
                getKeyEncryptionAlgorithmForKeyAgreement());
        mockResponse = createMockResponse();
    }

    private SAMLAuthnResponseEncrypter setupEncrypter(String dataEncryptionAlgorithm, String jcaProviderName,
            String keyEncryptionAlgorithm, String keyEncryptionAlgorithmForKeyAgreement) {
        SAMLAuthnResponseEncrypter.Builder samlAuthnResponseEncrypterBuilder = SAMLAuthnResponseEncrypter.builder()
                .dataEncryptionAlgorithm(dataEncryptionAlgorithm)
                .jcaProviderName(jcaProviderName)
                .keyEncryptionAlgorithm(keyEncryptionAlgorithm)
                .keyEncryptionAlgorithmForKeyAgreement(keyEncryptionAlgorithmForKeyAgreement);

        return samlAuthnResponseEncrypterBuilder.build();
    }

    private void setupCredentials() throws CertificateException, NoSuchProviderException, FileNotFoundException {
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

    public String getCertificateFilePath() {
        return "src/test/resources/certificates/keyTransport.crt";
    }

    public String getSecurityProviderName() {
        return null;
    }

    public Response createMockResponse() {
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

    public String getDataEncryptionAlgorithm() {
        return null;
    }

    public String getDefaultDataEncryptionAlgorithm() {
        return DefaultEncryptionAlgorithm.DEFAULT_DATA_ENCRYPTION_ALGORITHM.getValue();
    }

    public String getJCAProviderName() {
        return null;
    }

    public String getKeyEncryptionAlgorithm() {
        return null;
    }

    public String getKeyEncryptionAlgorithmForKeyAgreement() {
        return null;
    }

    public String getDefaultKeyEncryptionAlgorithm(){
        return DefaultEncryptionAlgorithm.DEFAULT_KEY_ENCRYPTION_ALGORITHM_FOR_KEY_TRANSPORT.getValue();
    }

    public InputStream getMockResponseXML() throws Exception {
        return new FileInputStream(MOCK_RESPONSE_FILE_PATH);
    }

    public Response getDefaultResponse() {
        return mockResponse;
    }

    public Credential getCredential() {
        return credential;
    }

    public boolean isKeyInfoDisplayedAsKeyValue() {
        return false;
    }

    public void verifyResponseAssertions(Response encryptedResponse) {
        Assert.assertTrue(encryptedResponse.getAssertions().isEmpty());

        Assert.assertFalse(encryptedResponse.getEncryptedAssertions().isEmpty());
        Assert.assertEquals(1, encryptedResponse.getEncryptedAssertions().size());
    }

    public void verifyEncryptedData(EncryptedData encryptedData) {
        verifyDataEncryptionAlgorithm(encryptedData);

        long expectedAmountOfEncryptedKeys = 1;
        Assert.assertEquals(expectedAmountOfEncryptedKeys, encryptedData.getKeyInfo().getEncryptedKeys().size());

        EncryptedKey encryptedKey = encryptedData.getKeyInfo().getEncryptedKeys().get(0);
        verifyEncryptedKey(encryptedKey);
    }

    public void verifyDataEncryptionAlgorithm(EncryptedData encryptedData) {
        if (null == getDataEncryptionAlgorithm()) {
            String expectedDataEncryptionAlgorithm = getDefaultDataEncryptionAlgorithm();
            verifyEncryptionMethod(expectedDataEncryptionAlgorithm, encryptedData.getEncryptionMethod());
        } else {
            verifyEncryptionMethod(getDataEncryptionAlgorithm(), encryptedData.getEncryptionMethod());
        }
    }

    public void verifyEncryptedKey(EncryptedKey encryptedKey) {
        verifyKeyEncryptionAlgorithm(encryptedKey);
        verifyKeyInfoFromEncryptedKey(encryptedKey.getKeyInfo());
    }

    public void verifyKeyEncryptionAlgorithm(EncryptedKey encryptedKey) {
        String expectedKeyEncryptionAlgorithm = getExpectedKeyEncryptionAlgorithm();
        verifyEncryptionMethod(expectedKeyEncryptionAlgorithm, encryptedKey.getEncryptionMethod());
    }

    /**
     * This method gets the default key encryption algorithm or configured key encryption algorithm
     * depending on the type of credentials used by the test class. This match the key encryption
     * algorithm that we expect to be used by the encryption.
     * @return the expected key encryption algorithm to should have been used.
     */
    public String getExpectedKeyEncryptionAlgorithm() {
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

    public void verifyKeyInfoFromEncryptedKey(KeyInfo keyInfo) {

    }

    public void verifyEncryptionMethod(String expectedMethodAlgorithm, EncryptionMethod encryptionMethod) {
        Assert.assertEquals(expectedMethodAlgorithm, encryptionMethod.getAlgorithm());
    }

}
