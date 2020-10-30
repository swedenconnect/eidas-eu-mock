/*
 * Copyright (c) 2020 by European Commission
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
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.KeyStoreConfigurator;
import eu.eidas.auth.engine.configuration.dom.KeyStoreContent;
import eu.eidas.auth.engine.configuration.dom.KeyStoreKey;
import eu.eidas.encryption.SAMLAuthnResponseDecrypter;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.apache.xml.security.signature.XMLSignature;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.isA;

/**
 * Tests for the {@link AbstractProtocolSigner}
 */
public class AbstractProtocolSignerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProtocolSignerTest.class);

    private static final String GET_SIGNATURE_CERTIFICATE_METHOD_NAME = "getSignatureCertificate";
    private static final Method GET_SIGNATURE_CERTIFICATE_METHOD = ReflectionUtils.findMethod(
            AbstractProtocolSigner.class, GET_SIGNATURE_CERTIFICATE_METHOD_NAME, Signature.class);
    static {
        GET_SIGNATURE_CERTIFICATE_METHOD.setAccessible(true);
    }

    private static final String GET_TRUSTED_CERTIFICATE_METHOD_NAME = "getTrustedCertificate";
    private static final Method GET_TRUSTED_CERTIFICATE_METHOD = ReflectionUtils.findMethod(
            AbstractProtocolSigner.class, GET_TRUSTED_CERTIFICATE_METHOD_NAME, Signature.class, List.class);
    static {
        GET_TRUSTED_CERTIFICATE_METHOD.setAccessible(true);
    }

    private static final String IS_ALGORITHM_ALLOWED_FOR_VERIFYING_METHOD_NAME = "isAlgorithmAllowedForVerifying";
    private static final Method IS_ALGORITHM_ALLOWED_FOR_VERIFYING_METHOD = ReflectionUtils.findMethod(
            AbstractProtocolSigner.class, IS_ALGORITHM_ALLOWED_FOR_VERIFYING_METHOD_NAME, String.class);
    static {
        IS_ALGORITHM_ALLOWED_FOR_VERIFYING_METHOD.setAccessible(true);
    }

    private static final String ADD_ALL_SIGNATURE_CERTIFICATES_TO_CREDENTIAL_METHOD_NAME = "addAllSignatureCertificatesToCredential";
    private static final Method ADD_ALL_SIGNATURE_CERTIFICATES_TO_CREDENTIAL_METHOD = ReflectionUtils.findMethod(
            AbstractProtocolSigner.class, ADD_ALL_SIGNATURE_CERTIFICATES_TO_CREDENTIAL_METHOD_NAME, Signature.class, X509Credential.class);
    static {
        ADD_ALL_SIGNATURE_CERTIFICATES_TO_CREDENTIAL_METHOD.setAccessible(true);
    }

    private static final String GET_HASH_ALGORITHM_BITS_LENGTH_METHOD_NAME = "getHashAlgorithmBitsLength";
    private static final Method GET_HASH_ALGORITHM_BITS_LENGTH_METHOD = ReflectionUtils.findMethod(
            AbstractProtocolSigner.class, GET_HASH_ALGORITHM_BITS_LENGTH_METHOD_NAME, String.class);
    static {
        GET_HASH_ALGORITHM_BITS_LENGTH_METHOD.setAccessible(true);
    }

    private static final String VALIDATE_CERTIFICATE_SIGNATURE_HASH_METHOD_NAME = "validateCertificateSignatureHash";
    private static final Method VALIDATE_CERTIFICATE_SIGNATURE_HASH_METHOD = ReflectionUtils.findMethod(
            AbstractProtocolSigner.class, VALIDATE_CERTIFICATE_SIGNATURE_HASH_METHOD_NAME, X509Certificate.class);
    static {
        VALIDATE_CERTIFICATE_SIGNATURE_HASH_METHOD.setAccessible(true);
    }

    private static final AttributeDefinition<String> PERSON_IDENTIFIER =
            new AttributeDefinition.Builder<String>().nameUri("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier")
                    .friendlyName("PersonIdentifier")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .uniqueIdentifier(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "PersonIdentifierType", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static final String SIGNATURE_CERT_SUBJECT = "C=EU, O=European Commission, OU=DIGIT, CN=rootCAMetadata";
    private static final String SIGNATURE_CERTIFICATE =
            "MIIFjjCCA3agAwIBAgIJAKmSfYIA+TEAMA0GCSqGSIb3DQEBBQUAMFQxCzAJBgNV\n" +
                    "BAYTAkVVMRwwGgYDVQQKDBNFdXJvcGVhbiBDb21taXNzaW9uMQ4wDAYDVQQLDAVE\n" +
                    "SUdJVDEXMBUGA1UEAwwOcm9vdENBTWV0YWRhdGEwHhcNMTkwNzI5MDcyODA3WhcN\n" +
                    "MzAxMjMxMjM1OTU5WjBUMQswCQYDVQQGEwJFVTEcMBoGA1UECgwTRXVyb3BlYW4g\n" +
                    "Q29tbWlzc2lvbjEOMAwGA1UECwwFRElHSVQxFzAVBgNVBAMMDnJvb3RDQU1ldGFk\n" +
                    "YXRhMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAyRFNpe2quaNhiyrV\n" +
                    "nngPkTb5hIScZQ+Dxq+7Gl9wS8c2hRCwew28sbFeLcv/hwwxfxwIt6+04Xh/kaTk\n" +
                    "8CQlRxcsV89dDMWOq4j+n7VvlaCPOhu/5FI2d0Fe02Z63u6EZHYszSmqBxPn05eb\n" +
                    "UJ0rG0uYNCOpaVoQAwvYyQZPRcbKGGgltnM+fBIXzK0eLtXXvJJuzK8Jl36xyTEs\n" +
                    "A/D2UcWfT7PaUQI+tvpKwdtPy9qKOauytZSM0ZN1ITmSA1pzksvbgynk7yaIDCN7\n" +
                    "vA6/NLCSrL9pmSn00+gnxFuXsamDnjenIfLe5VD21OpwP70Akv6PtJ0XKuTEPJxt\n" +
                    "3LCK/L6pfKAnJA0RZFlKfv76d99ogmWCjM/dNabH5AglJ/NTTTSgxh8rWcSzYTGg\n" +
                    "LRs9l69IqHR7XlpIHIePod9fr1sw/HFfwbNAzG0d/ZTsx5OD5A2QQYfibXfzCSJ8\n" +
                    "nAz1/jS7IU9A329B4hmKK4nJbygylLVotEHnXdwjv/369NdQuSnd95XISJQVSBTS\n" +
                    "aQghvsQM6zVC9zbMUYTAQjUqAuR1kw98xvA+GrCz6o0zQMtWnAK+JUhgtVkgS1VM\n" +
                    "UdYfesYA4zh0n4UOj+QH5GmY8Rv5kOHueOYworPw9y9IQrpbp675zGxAEO9T8bwC\n" +
                    "imuGHSrMs2HXJGZcmbFm2NC0hQMCAwEAAaNjMGEwDgYDVR0PAQH/BAQDAgEGMA8G\n" +
                    "A1UdEwEB/wQFMAMBAf8wHQYDVR0OBBYEFHMK47MFhe0Dabpe2XPDob2jbkprMB8G\n" +
                    "A1UdIwQYMBaAFHMK47MFhe0Dabpe2XPDob2jbkprMA0GCSqGSIb3DQEBBQUAA4IC\n" +
                    "AQCBIdVGckDSdY+D5vloOp/0Gk9e2NFf/VOzun470aVNUv9Ye1tSWt1lkBu+koiT\n" +
                    "SYTmxgUeesaeHYq78fXsxEFZ6oQWEik8v/WaSvJAyQEaIe6R1ksbs7SnSKXDZaPw\n" +
                    "DHIVxW7P44XroBn60JzjxOQ3FPSKDEAsmCfAIaLjvZm7szkSE8eV8Iw8j8Tr1Kud\n" +
                    "s7OzNsGRS61VwaoqMAHY0ldspfTGN75BKdwOvasDsRHXxjTuGjtN3fioLXoTHXuS\n" +
                    "F9bukbBfxUROOa/e9YRxDZ30T52Yl7CuDRrhbNd5hLswnzD3YErHgzujGvPMTOFI\n" +
                    "zN9HOsQleacYsrWAjCG69sAuegBUYoEbjMMBApndyf7sTANQB/z+QGDKm6+FpzuX\n" +
                    "hyMr5GBaEgfvIjtMf/OxorlqOk2np1rHeXw7wOM1lUapW2AxBk6bHBHIJZsXwN9G\n" +
                    "SrVtjQi7PX+RJaxCTQiI4xsXBXqZ+7MImxkGMA/62pnOwIEsH5KKP/AkY05q0Tf9\n" +
                    "VFeF1pumPxPXTRLIv+QTC2CQQ/bLkh87J1KNCCyz94VDkotlbxnt1k7LAme15gVv\n" +
                    "2pkroOChq6bJO0k+1karFCWvyok8ZsDnV6Q7HKJ4eisMunipRbxhNKCMQ4GVMrWX\n" +
                    "+a30q1OIQN8CKPPeEgra3pWMGvwAc04Ru3gvnNkXFPFOew==";

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

    private Signature mockSignature(String signCertificateValue) {
        Signature signature = Mockito.mock(Signature.class);

        KeyInfo mockKeyInfo = Mockito.mock(KeyInfo.class);
        Mockito.when(signature.getKeyInfo()).thenReturn(mockKeyInfo);
        X509Data x509Data = Mockito.mock(X509Data.class);
        if (signCertificateValue != null && signCertificateValue.equals("empty")){
            Mockito.when(mockKeyInfo.getX509Datas()).thenReturn(Collections.emptyList());
        }
        else {
            Mockito.when(mockKeyInfo.getX509Datas()).thenReturn(Collections.singletonList(x509Data));
        }
        org.opensaml.xmlsec.signature.X509Certificate x509Certificate = Mockito.mock(org.opensaml.xmlsec.signature.X509Certificate.class);
        if (signCertificateValue == null) {
            Mockito.when(x509Certificate.getValue()).thenReturn("wrongvalue");
        }

        else {
            Mockito.when(x509Certificate.getValue()).thenReturn(signCertificateValue);
        }
        Mockito.when(x509Data.getX509Certificates()).thenReturn(Collections.singletonList(x509Certificate));
        return signature;
    }

    private ImmutableSet<String> getAllowedAlgorithms() {
        return ImmutableSet.of(
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1,
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384_MGF1,
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1,
                SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA256,
                SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA384,
                SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA512
        );
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#getSignatureCertificate(Signature)}
     *
     * Must succeed
     */
    @Test
    public void testGetSignatureCertificate(){
        Signature mockSignature = mockSignature(SIGNATURE_CERTIFICATE);
        X509Certificate certificate = (X509Certificate)ReflectionUtils.invokeMethod(GET_SIGNATURE_CERTIFICATE_METHOD, null, mockSignature);
        Assert.assertNotNull(certificate);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#getSignatureCertificate(Signature)}
     * when Signature is null
     *
     * Must throw expected exception
     */
    @Test
    public void testGetSignatureCertificateNull(){
        thrown.expectCause(isA(EIDASSAMLEngineException.class));
        Signature nullSignature = mockSignature(null);

        X509Certificate certificate = (X509Certificate)ReflectionUtils.invokeMethod(GET_SIGNATURE_CERTIFICATE_METHOD, null, nullSignature);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#validateDigestAlgorithm(String)}
     * when digestMethodAlgorithmName is valid
     *
     * Must succeed
     */
    @Test
    public void testValidateDigestAlgorithm() throws ProtocolEngineConfigurationException {
        AbstractProtocolSigner.validateDigestAlgorithm(SignatureConstants.ALGO_ID_DIGEST_SHA512);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#validateDigestAlgorithm(String)}
     * when digestMethodAlgorithmName is invalid
     *
     * Must throw expected exception
     */
    @Test
    public void testValidateDigestAlgorithmInvalidAlgorithm() throws ProtocolEngineConfigurationException {
        thrown.expect(ProtocolEngineConfigurationException.class);
        AbstractProtocolSigner.validateDigestAlgorithm(SignatureConstants.ALGO_ID_DIGEST_SHA1);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#validateDigestAlgorithm(String)}
     * when digestMethodAlgorithmName is empty
     *
     * Returned Algorithm must equal {@link SignatureConstants#ALGO_ID_DIGEST_SHA512}
     * Must succeed
     */
    @Test
    public void testValidateDigestAlgorithmBlank() throws ProtocolEngineConfigurationException {
        String returnedAlgorithm = AbstractProtocolSigner.validateDigestAlgorithm("");
        Assert.assertEquals(returnedAlgorithm, SignatureConstants.ALGO_ID_DIGEST_SHA512);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#validateSigningAlgorithm(String)}
     * when signatureAlgorithmName is valid
     *
     * Must succeed
     */
    @Test
    public void testValidateSigningAlgorithm() throws ProtocolEngineConfigurationException {
        AbstractProtocolSigner.validateSigningAlgorithm("http://www.w3.org/2007/05/xmldsig-more#sha512-rsa-MGF1");
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#validateSigningAlgorithm(String)}
     * when signatureAlgorithmName is invalid
     *
     * Must throw expected exception
     */
    @Test
    public void testValidateSigningAlgorithmInvalidAlgorithm() throws ProtocolEngineConfigurationException {
        thrown.expect(ProtocolEngineConfigurationException.class);
        AbstractProtocolSigner.validateSigningAlgorithm("http://www.w3.org/2007/05/xmldsig-more#sha1-rsa-MGF1");
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#validateSigningAlgorithm(String)}
     * when signatureAlgorithmName is empty
     *
     * Returned Algorithm must equal {@link XMLSignature#ALGO_ID_SIGNATURE_RSA_SHA512_MGF1}
     * Must succeed
     */
    @Test
    public void testValidateSigningAlgorithmBlank() throws ProtocolEngineConfigurationException {
        String returnedAlgorithm = AbstractProtocolSigner.validateSigningAlgorithm("");
        Assert.assertEquals(returnedAlgorithm, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#validateSigningAlgorithm(String)}
     * when signatureAlgorithmName is null
     *
     * Returned Algorithm must equal {@link XMLSignature#ALGO_ID_SIGNATURE_RSA_SHA512_MGF1}
     * Must succeed
     */
    @Test
    public void testValidateSigningAlgorithmNull() throws ProtocolEngineConfigurationException {
        String returnedAlgorithm = AbstractProtocolSigner.validateSigningAlgorithm(null);
        Assert.assertEquals(returnedAlgorithm, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#getTrustedCertificate(Signature, List)}
     * when List is empty
     *
     * Must throw expected exception
     */
    @Test
    public void testGetTrustedCertificateInvalidList(){
        thrown.expectCause(isA(EIDASSAMLEngineException.class));
        Signature mockSignature = mockSignature(SIGNATURE_CERTIFICATE);
        AbstractProtocolSigner signer = Mockito.mock(AbstractProtocolSigner.class);
        X509Certificate certificate = (X509Certificate)ReflectionUtils.invokeMethod(GET_TRUSTED_CERTIFICATE_METHOD, signer, mockSignature, Collections.EMPTY_LIST);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#getTrustedCertificate(Signature, List)}
     * when Signature is empty
     *
     * Must throw expected exception
     */
    @Test
    public void testGetTrustedCertificateInvalidSignature(){
        thrown.expectCause(isA(EIDASSAMLEngineException.class));
        Signature mockSignature = mockSignature("empty");
        AbstractProtocolSigner signer = Mockito.mock(AbstractProtocolSigner.class);
        X509Certificate certificate = (X509Certificate)ReflectionUtils.invokeMethod(GET_TRUSTED_CERTIFICATE_METHOD, signer, mockSignature, Collections.EMPTY_LIST);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#isAlgorithmAllowedForVerifying(String)}
     * when algorithm is allowed
     *
     * Must return True
     * Must succeed
     */
    @Test
    public void testIsAlgorithmAllowedForVerifyingMethodAllowed(){
        String algorithm = "http://www.w3.org/2007/05/xmldsig-more#sha512-rsa-MGF1";
        AbstractProtocolSigner signer = Mockito.mock(AbstractProtocolSigner.class);
        Mockito.when(signer.getSignatureAlgorithmWhiteList()).thenReturn(getAllowedAlgorithms());
        Boolean result = (Boolean) ReflectionUtils.invokeMethod(IS_ALGORITHM_ALLOWED_FOR_VERIFYING_METHOD, signer, algorithm);
        Assert.assertTrue(result);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#isAlgorithmAllowedForVerifying(String)}
     * when algorithm is not allowed
     *
     * Must return False
     * Must succeed
     */
    @Test
    public void testIsAlgorithmAllowedForVerifyingMethodDisallowed(){
        String algorithm = "http://www.w3.org/2007/05/xmldsig-more#sha1-rsa-MGF1";
        AbstractProtocolSigner signer = Mockito.mock(AbstractProtocolSigner.class);
        Mockito.when(signer.getSignatureAlgorithmWhiteList()).thenReturn(getAllowedAlgorithms());
        Boolean result = (Boolean) ReflectionUtils.invokeMethod(IS_ALGORITHM_ALLOWED_FOR_VERIFYING_METHOD, signer, algorithm);
        Assert.assertFalse(result);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#isAlgorithmAllowedForVerifying(String)}
     * when algorithm is empty
     *
     * Must return False
     * Must succeed
     */
    @Test
    public void testIsAlgorithmAllowedForVerifyingMethodBlank(){
        String algorithm = "";
        AbstractProtocolSigner signer = Mockito.mock(AbstractProtocolSigner.class);
        Mockito.when(signer.getSignatureAlgorithmWhiteList()).thenReturn(getAllowedAlgorithms());
        Boolean result = (Boolean) ReflectionUtils.invokeMethod(IS_ALGORITHM_ALLOWED_FOR_VERIFYING_METHOD, signer, algorithm);
        Assert.assertFalse(result);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#addAllSignatureCertificatesToCredential(Signature, X509Credential)}
     *
     * Must succeed
     */
    @Test
    public void testAddAllSignatureCertificatesToCredential(){
        Signature mockSignature = mockSignature(SIGNATURE_CERTIFICATE);
        X509Credential credential = Mockito.mock(X509Credential.class);
        AbstractProtocolSigner signer = Mockito.mock(AbstractProtocolSigner.class);
        ReflectionUtils.invokeMethod(ADD_ALL_SIGNATURE_CERTIFICATES_TO_CREDENTIAL_METHOD, signer, mockSignature, credential);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#getHashAlgorithmBitsLength(String)}
     *
     * Must succeed
     */
    @Test
    public void testGetHashAlgorithmBitsLength(){
/*
        testAlgorithmHashLength("SHA1", 160);
        testAlgorithmHashLength("SHA224", 224);
        testAlgorithmHashLength("SHA256", 256);
        testAlgorithmHashLength("SHA384", 384);
        testAlgorithmHashLength("SHA512", 512);

        testAlgorithmHashLength("MD2", 128);
        testAlgorithmHashLength("MD4", 128);
        testAlgorithmHashLength("MD5", 128);

        testAlgorithmHashLength("TIGER", 192);
        testAlgorithmHashLength("WHIRLPOOL", 512);
*/
    }

    private void testAlgorithmHashLength(String algorithmName, int expectedHashLength) {
        AbstractProtocolSigner signer = Mockito.mock(AbstractProtocolSigner.class);
        int actualHashLength = (int) ReflectionUtils.invokeMethod(GET_HASH_ALGORITHM_BITS_LENGTH_METHOD, signer, algorithmName);

        Assert.assertEquals(expectedHashLength, actualHashLength);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#validateCertificateSignatureHash(X509Certificate)}
     * when the parameter X509Certificate#getSigAlgName returns an unknown algorithm.
     *
     * Must fail.
     */
    @Test
    public void testValidateCertificateSignatureHashInvalidSignatureAlgorithmName() {
        thrown.expect(UndeclaredThrowableException.class);
        thrown.expectCause(Matchers.isA(EIDASSAMLEngineException.class));

        testValidateCertificateSignatureHash("invalidAlgorithmName");
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#validateCertificateSignatureHash(X509Certificate)}
     * when the parameter method call X509Certificate#getSigAlgName returns a valid algorithm
     * which has a valid hash algorithm and valid hash length.
     *
     * Must succeed.
     */
    @Test
    public void testValidateCertificateSignatureHashValidHashLength() {
/*
        testValidateCertificateSignatureHash("SHA256WITHRSAANDMGF1");
        testValidateCertificateSignatureHash("SHA384WITHRSAANDMGF1");
        testValidateCertificateSignatureHash("SHA512WITHRSAANDMGF1");

        testValidateCertificateSignatureHash("SHA256WITHECDSA");
        testValidateCertificateSignatureHash("SHA384WITHECDSA");
        testValidateCertificateSignatureHash("SHA512WITHECDSA");

        testValidateCertificateSignatureHash("SHA256withRSA");
        testValidateCertificateSignatureHash("SHA384withRSA");
        testValidateCertificateSignatureHash("SHA512withRSA");
*/
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#validateCertificateSignatureHash(X509Certificate)}
     * when the parameter method call X509Certificate#getSigAlgName returns a valid algorithm
     * which has an valid hash algorithm
     * but an invalid hash length.
     *
     * Must fail.
     */
    @Test
    public void testValidateCertificateSignatureHashInValidHashLength() {
        thrown.expect(UndeclaredThrowableException.class);
        thrown.expectCause(Matchers.isA(EIDASSAMLEngineException.class));

        testValidateCertificateSignatureHash("SHA1WITHRSAANDMGF1");
        testValidateCertificateSignatureHash("SHA224WITHRSAANDMGF1");

        testValidateCertificateSignatureHash("SHA1WITHECDSA");
        testValidateCertificateSignatureHash("SHA224WITHECDSA");

        testValidateCertificateSignatureHash("SHA1withRSA");
        testValidateCertificateSignatureHash("SHA224withRSA");
    }

    private void testValidateCertificateSignatureHash(String sha512withRSA) {
        X509Certificate mockX509Certificate = Mockito.mock(X509Certificate.class);
        Mockito.when(mockX509Certificate.getSigAlgName()).thenReturn(sha512withRSA);

        AbstractProtocolSigner signer = Mockito.mock(AbstractProtocolSigner.class);
        ReflectionUtils.invokeMethod(VALIDATE_CERTIFICATE_SIGNATURE_HASH_METHOD, signer, mockX509Certificate);
    }

}
