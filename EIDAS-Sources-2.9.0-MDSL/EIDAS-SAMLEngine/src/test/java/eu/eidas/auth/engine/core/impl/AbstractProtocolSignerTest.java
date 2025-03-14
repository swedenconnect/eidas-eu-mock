/*
 * Copyright (c) 2024 by European Commission
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

import eu.eidas.RecommendedSecurityProviders;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.EncryptionConfiguration;
import eu.eidas.auth.engine.configuration.dom.KeyStoreConfigurator;
import eu.eidas.auth.engine.configuration.dom.KeyStoreContent;
import eu.eidas.auth.engine.configuration.dom.KeyStoreKey;
import eu.eidas.auth.engine.configuration.dom.SignatureKey;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.core.SigningContext;
import eu.eidas.auth.engine.core.eidas.EidasExtensionConfiguration;
import eu.eidas.auth.engine.core.eidas.EidasProtocolProcessor;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.xml.opensaml.CertificateVerifierParams;
import eu.eidas.encryption.SAMLAuthnResponseDecrypter;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.apache.xml.security.signature.XMLSignature;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.core.config.Configuration;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.impl.ResponseBuilder;
import org.opensaml.saml.saml2.core.impl.ResponseMarshaller;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.Exponent;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.KeyValue;
import org.opensaml.xmlsec.signature.Modulus;
import org.opensaml.xmlsec.signature.RSAKeyValue;
import org.opensaml.xmlsec.signature.SignableXMLObject;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.impl.SignatureImpl;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;

import javax.security.auth.x500.X500Principal;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.isA;

/**
 * Test class for {@link AbstractProtocolSigner}
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractProtocolSignerTest {

    private static final String ISSUER = "CN=testCert, OU=DIGIT, O=EC, L=EU, ST=EU, C=EU";
    private static final String TEST_ISSUER = "CN=trustedCert,OU=DIGIT,O=EC,L=Iasi,ST=Iasi,C=RO";
    private static final String KEYSTORE_PATH = "src/test/resources/signatureTestKeystore.p12";
    private static final String TEST_KEYSTORE_PATH = "src/test/resources/keystoreTestCertificates.p12";
    private static final String RSA_KEY_3072_SERIAL = "5CB93CF26B3687D6F6A65BD5C900B17F5BC85B7D";

    private Configuration defaultConfiguration;
    private Configuration spyConfiguration;
    private MetadataSignerI metadataSigner;

    @Mock
    private AbstractProtocolSigner mockSigner;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String SHA1_WITH_RSA_AND_MGF1_CERT_PATH = "src/test/resources/certificates/SHA1withRSAandMGF1.crt";
    private static final String SHA256_WITH_RSA_AND_MGF1_CERT_PATH = "src/test/resources/certificates/SHA256withRSAandMGF1.crt";
    private static final String SHA384_WITH_RSA_AND_MGF1_CERT_PATH = "src/test/resources/certificates/SHA384withRSAandMGF1.crt";
    private static final String SHA512_WITH_RSA_AND_MGF1_CERT_PATH = "src/test/resources/certificates/SHA512withRSAandMGF1.crt";

    private static final String SHA1_WITH_ECDSA_CERT_PATH = "src/test/resources/certificates/SHA1withECDSA.crt";
    private static final String SHA256_WITH_ECDSA_CERT_PATH = "src/test/resources/certificates/SHA256withECDSA.crt";
    private static final String SHA384_WITH_ECDSA_CERT_PATH = "src/test/resources/certificates/SHA384withECDSA.crt";
    private static final String SHA512_WITH_ECDSA_CERT_PATH = "src/test/resources/certificates/SHA512withECDSA.crt";

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

    @BeforeClass
    public static void setupClass() {
        RecommendedSecurityProviders.setupRecommendedSecurityProviders();
        OpenSamlHelper.initialize();
        EidasExtensionConfiguration.configureExtension(new EidasProtocolProcessor(null, null, null), Mockito.mock(AbstractProtocolSigner.class)); {
        };
    }

    SAMLAuthnResponseDecrypter dummySAMLAuthnResponseDecrypter =
            new SAMLAuthnResponseDecrypter(BouncyCastleProvider.PROVIDER_NAME);

    AbstractProtocolDecrypter decrypter;

    KeyStoreContent keyStoreContent;

    @Before
    public void setup() throws Exception {
        setupKeyStore();

        EncryptionConfiguration encryptionConfiguration = new EncryptionConfiguration.Builder()
                .setCheckedValidityPeriod(isCheckedValidityPeriod())
                .setDisallowedSelfSignedCertificate(isDisallowedSelfSignedCertificate())
                .setResponseEncryptionMandatory(isResponseEncryptionMandatory())
                .setAssertionEncryptWithKey(isAssertionEncryptWithKey())
                .setDecryptionKeyAndCertificates(getDecryptionKeyAndCertificates())
                .setEncryptionAlgorithmWhitelist(getEncryptionAlgorithmWhiteList())
                .build();
        decrypter = new AbstractSamlEngineEncryption.BaseProtocolDecrypter(encryptionConfiguration);

        OpenSamlHelper.getSchema();
        final Method getConfiguration = ConfigurationService.class.getDeclaredMethod("getConfiguration");
        getConfiguration.setAccessible(true);
        defaultConfiguration = (Configuration) getConfiguration.invoke(null);
        spyConfiguration = Mockito.spy(defaultConfiguration);
        ConfigurationService.setConfiguration(spyConfiguration);

        final Map<String, String> signingProperties = getSigningProperties(TEST_ISSUER, "67483454", TEST_KEYSTORE_PATH);
        metadataSigner = new SignSW(signingProperties, null);
    }

    @After
    public void setConfiguration() {
        if (null != defaultConfiguration) {
            ConfigurationService.setConfiguration(defaultConfiguration);
        }
    }

    protected void setupKeyStore() throws Exception {
        Map<String, String> testProperties = new HashMap<>();
        testProperties.put(KeyStoreKey.KEYSTORE_PATH.getKey(), getKeyStorePath());
        testProperties.put(KeyStoreKey.KEYSTORE_TYPE.getKey(), "PKCS12");
        testProperties.put(KeyStoreKey.KEYSTORE_PASSWORD.getKey(), "local-demo");
        testProperties.put(KeyStoreKey.KEY_PASSWORD.getKey(), "local-demo");

        KeyStoreConfigurator keyStoreConfigurator = new KeyStoreConfigurator(testProperties, "src/test/resources");
        keyStoreContent = keyStoreConfigurator.loadKeyStoreContent();
    }

    protected String getKeyStorePath() {
        return "/keyStoreWithMultiplePrivateKeyEntries.p12";
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

    protected Set<KeyStore.PrivateKeyEntry> getDecryptionKeyAndCertificates() {
        if (keyStoreContent != null) {
            return keyStoreContent.getPrivateKeyEntries();
        }
        return Collections.unmodifiableSet(new HashSet<>());
    }

    protected SAMLAuthnResponseDecrypter getSamlAuthnResponseDecrypter() {
        return dummySAMLAuthnResponseDecrypter;
    }

    protected Set<String> getEncryptionAlgorithmWhiteList() {
        return Collections.unmodifiableSet(new HashSet<>());
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

    private Set<String> getAllowedAlgorithms() {
        return Set.of(
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
    public void testGetSignatureCertificate() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method getSignatureCertificateMethod = getDeclaredMethod("getSignatureCertificate", Signature.class);
        Signature mockSignature = mockSignature(SIGNATURE_CERTIFICATE);
        X509Certificate certificate = (X509Certificate) getSignatureCertificateMethod.invoke(null, mockSignature);
        Assert.assertNotNull(certificate);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#getSignatureCertificate(Signature)}
     * when Signature is null
     *
     * Must fail
     */
    @Test
    public void testGetSignatureCertificateNull() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        final Method getSignatureCertificateMethod = getDeclaredMethod("getSignatureCertificate", Signature.class);
        Signature nullSignature = mockSignature(null);

        getSignatureCertificateMethod.invoke(null, nullSignature);
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
     * Must fail
     */
    @Test
    public void testValidateDigestAlgorithmInvalidAlgorithm() throws ProtocolEngineConfigurationException {
        expectedException.expect(ProtocolEngineConfigurationException.class);
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
     * {@link AbstractProtocolSigner#getTrustedCertificate(Signature, List)}
     * when List is empty
     *
     * Must fail
     */
    @Test
    public void testGetTrustedCertificateInvalidList() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        final Method getTrustedCertificateMethod = getDeclaredMethod("getTrustedCertificate", Signature.class, List.class, CertificateVerifierParams.class);
        Signature mockSignature = mockSignature(SIGNATURE_CERTIFICATE);

        getTrustedCertificateMethod.invoke(mockSigner, mockSignature, Collections.EMPTY_LIST, new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#getTrustedCertificate(Signature, List)}
     * when Signature is empty
     *
     * Must fail
     */
    @Test
    public void testGetTrustedCertificateInvalidSignature() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        final Method getTrustedCertificateMethod = getDeclaredMethod("getTrustedCertificate", Signature.class, List.class, CertificateVerifierParams.class);
        Signature mockSignature = mockSignature("empty");

        getTrustedCertificateMethod.invoke(mockSigner, mockSignature, Collections.EMPTY_LIST, new CertificateVerifierParams());
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
    public void testIsAlgorithmAllowedForVerifyingMethodAllowed() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method isAlgorithmAllowedForVerifyingMethod = getDeclaredMethod("isAlgorithmAllowedForVerifying", String.class);
        String algorithm = "http://www.w3.org/2007/05/xmldsig-more#sha512-rsa-MGF1";
        Mockito.when(mockSigner.getSignatureAlgorithmWhiteList()).thenReturn(getAllowedAlgorithms());
        final Boolean result = (Boolean) isAlgorithmAllowedForVerifyingMethod.invoke(mockSigner, algorithm);
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
    public void testIsAlgorithmAllowedForVerifyingMethodDisallowed() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method isAlgorithmAllowedForVerifyingMethod = getDeclaredMethod("isAlgorithmAllowedForVerifying", String.class);
        String algorithm = "http://www.w3.org/2007/05/xmldsig-more#sha1-rsa-MGF1";
        Mockito.when(mockSigner.getSignatureAlgorithmWhiteList()).thenReturn(getAllowedAlgorithms());
        final Boolean result = (Boolean) isAlgorithmAllowedForVerifyingMethod.invoke(mockSigner, algorithm);
        Assert.assertFalse(result);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#isAlgorithmAllowedForVerifying(String)}
     * when algorithm is empty
     *
     * Must succeed
     */
    @Test
    public void testIsAlgorithmAllowedForVerifyingMethodBlank() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method isAlgorithmAllowedForVerifyingMethod = getDeclaredMethod("isAlgorithmAllowedForVerifying", String.class);
        String algorithm = "";
        final Boolean result = (Boolean) isAlgorithmAllowedForVerifyingMethod.invoke(mockSigner, algorithm);
        Assert.assertFalse(result);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#addAllSignatureCertificatesToCredential(Signature, X509Credential)}
     *
     * Must succeed
     */
    @Test
    public void testAddAllSignatureCertificatesToCredential() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method addAllSignatureCertificatesToCredentialMethod = getDeclaredMethod("addAllSignatureCertificatesToCredential", Signature.class, X509Credential.class);
        Signature mockSignature = mockSignature(SIGNATURE_CERTIFICATE);
        X509Credential credential = Mockito.mock(X509Credential.class);

        addAllSignatureCertificatesToCredentialMethod.invoke(mockSigner, mockSignature, credential);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#getHashAlgorithmBitsLength(String)}
     *
     * Must succeed
     */
    @Test
    public void testGetHashAlgorithmBitsLength() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
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
    }

    private void testAlgorithmHashLength(String algorithmName, int expectedHashLength) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method getHashAlgorithmBitsLengthMethod = getDeclaredMethod("getHashAlgorithmBitsLength", String.class);
        final int actualHashLength = (int) getHashAlgorithmBitsLengthMethod.invoke(mockSigner, algorithmName);

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
    public void testValidateCertificateSignatureHashInvalidSignatureAlgorithmName() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        testValidateCertificateSignatureHash("invalidAlgorithmName");
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#validateCertificateSignatureHash(X509Certificate)}
     * when the X509Certificate is a RSASSA-PSS certificate or ECDSA with different types of HASH
     *
     * Must succeed.
     */
    @Test
    public void testValidateCertificateSignatureHash() throws CertificateException, FileNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        testInvalidCertificateSignatureHash(getCertificate(SHA1_WITH_RSA_AND_MGF1_CERT_PATH));
        testInvalidCertificateSignatureHash(getCertificate(SHA1_WITH_ECDSA_CERT_PATH));

        testValidCertificateSignatureHash(getCertificate(SHA256_WITH_RSA_AND_MGF1_CERT_PATH));
        testValidCertificateSignatureHash(getCertificate(SHA384_WITH_RSA_AND_MGF1_CERT_PATH));
        testValidCertificateSignatureHash(getCertificate(SHA512_WITH_RSA_AND_MGF1_CERT_PATH));
        testValidCertificateSignatureHash(getCertificate(SHA256_WITH_ECDSA_CERT_PATH));
        testValidCertificateSignatureHash(getCertificate(SHA384_WITH_ECDSA_CERT_PATH));
        testValidCertificateSignatureHash(getCertificate(SHA512_WITH_ECDSA_CERT_PATH));
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
    public void testValidateCertificateSignatureHashValidHashLength() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        testValidateCertificateSignatureHash("SHA256WITHRSAANDMGF1");
        testValidateCertificateSignatureHash("SHA384WITHRSAANDMGF1");
        testValidateCertificateSignatureHash("SHA512WITHRSAANDMGF1");

        testValidateCertificateSignatureHash("SHA256WITHECDSA");
        testValidateCertificateSignatureHash("SHA384WITHECDSA");
        testValidateCertificateSignatureHash("SHA512WITHECDSA");

        testValidateCertificateSignatureHash("SHA256withRSA");
        testValidateCertificateSignatureHash("SHA384withRSA");
        testValidateCertificateSignatureHash("SHA512withRSA");
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
        testInvalidCertificateSignatureHash("SHA1WITHRSAANDMGF1");
        testInvalidCertificateSignatureHash("SHA224WITHRSAANDMGF1");

        testInvalidCertificateSignatureHash("SHA1WITHECDSA");
        testInvalidCertificateSignatureHash("SHA224WITHECDSA");

        testInvalidCertificateSignatureHash("SHA1withRSA");
        testInvalidCertificateSignatureHash("SHA224withRSA");
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#getTrustedCertificate(Signature, List)}
     * when certificate is untrusted
     * <p>
     * Must fail.
     */
    @Test
    public void getTrustedCertificateWhenCertificateIsUntrusted() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        final Method getTrustedCertificateMethod = getDeclaredMethod("getTrustedCertificate", Signature.class, List.class, CertificateVerifierParams.class);

        final List<Credential> credentialList = new ArrayList<>();

        Signature testSignature = mockSignature(SIGNATURE_CERTIFICATE);

        getTrustedCertificateMethod.invoke(mockSigner, testSignature, credentialList, new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#addAllSignatureCertificatesToCredential(Signature, X509Credential)}
     * when {@link eu.eidas.auth.engine.xml.opensaml.CertificateUtil#getAllSignatureCertificates} throws {@link CertificateException}
     * <p>
     * Must fail.
     */
    @Test
    public void testAddAllSignatureCertificatesToCredentialWhenEIDASSAMLEngineExceptionIsThrown() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        final Method addAllSignatureCertificatesToCredentialMethod = getDeclaredMethod("addAllSignatureCertificatesToCredential", Signature.class, X509Credential.class);
        final PrivateKey mockPrivateKey = Mockito.mock(PrivateKey.class);
        final X509Certificate mockX509Certificate = Mockito.mock(X509Certificate.class);

        final Signature mockSignature = mockSignature(null);
        final Credential credential = new BasicX509Credential(mockX509Certificate, mockPrivateKey);

        addAllSignatureCertificatesToCredentialMethod.invoke(mockSigner, mockSignature, credential);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#createKeyInfo(X509Credential, boolean)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testCreateKeyInfoWhenOnlyKeyInfoNoCertIsTrue() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, CertificateException, FileNotFoundException {
        final Method createKeyInfoMethod = getDeclaredMethod("createKeyInfo", X509Credential.class, boolean.class);

        final X509Certificate x509Certificate = getCertificate(SHA256_WITH_RSA_AND_MGF1_CERT_PATH);
        final BasicX509Credential mockBasicX509Credential = Mockito.mock(BasicX509Credential.class);
        Mockito.when(mockBasicX509Credential.getEntityCertificate()).thenReturn(x509Certificate);

        createKeyInfoMethod.invoke(mockSigner, mockBasicX509Credential, true);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#createKeyInfo(KeyInfoGeneratorFactory, X509Credential)}
     * when {@link KeyInfoGenerator#generate(Credential)} throws {@link SecurityException}
     * <p>
     * Must fail.
     */
    @Test
    public void testCreateKeyInfoWhenSecurityExceptionIsCaught() throws SecurityException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        final Method createKeyInfoMethod = getDeclaredMethod("createKeyInfo", KeyInfoGeneratorFactory.class, X509Credential.class);
        final X509Credential mockX509Credential = Mockito.mock(X509Credential.class);
        final KeyInfoGeneratorFactory mockKeyInfoGeneratorFactory = Mockito.mock(KeyInfoGeneratorFactory.class);
        final KeyInfoGenerator mockKeyInfoGenerator = Mockito.mock(KeyInfoGenerator.class);

        Mockito.when(mockKeyInfoGeneratorFactory.newInstance()).thenReturn(mockKeyInfoGenerator);
        Mockito.when(mockKeyInfoGenerator.generate(mockX509Credential)).thenThrow(SecurityException.class);

        createKeyInfoMethod.invoke(mockSigner, mockKeyInfoGeneratorFactory, mockX509Credential);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#sign(SignableXMLObject, SigningContext)}
     * when inside {@link AbstractProtocolSigner#sign(SignableXMLObject, SigningContext)} is thrown {@link MarshallingException}
     * <p>
     * Must fail.
     */
    @Test
    public void testSignWhenMarshallingExceptionIsCaught() throws EIDASSAMLEngineException, MarshallingException {
        expectedException.expect(EIDASSAMLEngineException.class);

        final Response mockResponse = new ResponseBuilder().buildObject();

        ResponseMarshaller mockedMarshaller = Mockito.mock(ResponseMarshaller.class);
        Mockito.when(mockedMarshaller.marshall(mockResponse)).thenThrow(MarshallingException.class);
        Marshaller responseMarshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory()
                .getMarshaller(Response.DEFAULT_ELEMENT_NAME);
        XMLObjectProviderRegistrySupport.getMarshallerFactory().registerMarshaller(Response.DEFAULT_ELEMENT_NAME, mockedMarshaller);

        final Map<String, String> signingProperties = getSigningProperties(ISSUER, RSA_KEY_3072_SERIAL, KEYSTORE_PATH);

        final ProtocolSignerI protocolSigner = new SignSW(signingProperties, null);
        try {
            protocolSigner.sign(mockResponse);
        } finally {
            XMLObjectProviderRegistrySupport.getMarshallerFactory()
                    .registerMarshaller(Response.DEFAULT_ELEMENT_NAME, responseMarshaller);
        }
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#sign(SignableXMLObject, SigningContext)}
     * when inside {@link AbstractProtocolSigner#sign(SignableXMLObject, SigningContext)} is thrown {@link org.opensaml.xmlsec.signature.support.SignatureException}
     *
     * <p>
     * Must fail.
     */
    @Test
    public void testSignWhenSignatureExceptionIsCaught() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage("message.validation.error.message");
        expectedException.expectCause(isA(SignatureException.class));
        expectedException.expectCause(allOf(
                isA(SignatureException.class),
                hasProperty("message", containsString("XMLObject does not have XMLSignature instance, unable to compute signature"))
        ));


        Response mockResponse = new ResponseBuilder().buildObject();
        mockResponse = Mockito.spy(mockResponse);

        Signature dummySignature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
                .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
                .buildObject(Signature.DEFAULT_ELEMENT_NAME);
        dummySignature.setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA512);
        Mockito.when(mockResponse.getSignature()).thenReturn(dummySignature);

        final Map<String, String> signingProperties = getSigningProperties(ISSUER, RSA_KEY_3072_SERIAL, KEYSTORE_PATH);
        final ProtocolSignerI protocolSigner = new SignSW(signingProperties, null);
        protocolSigner.sign(mockResponse);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#validateSamlSignatureStructure(SignableXMLObject)}
     * when inside {@link AbstractProtocolSigner#validateSamlSignatureStructure(SignableXMLObject)} is thrown {@link org.opensaml.xmlsec.signature.support.SignatureException}
     * <p>
     * Must fail.
     */
    @Test
    public void testValidateSamlSignatureStructureWhenSignatureExceptionIsCaught() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        final Method validateSamlSignatureStructureMethod = getDeclaredMethod("validateSamlSignatureStructure", SignableXMLObject.class);
        final SignableXMLObject mockSignableXMLObject = Mockito.mock(SignableXMLObject.class);
        final SignatureImpl mockSignatureImpl = Mockito.mock(SignatureImpl.class);

        Mockito.when(mockSignableXMLObject.getSignature()).thenReturn(mockSignatureImpl);

        validateSamlSignatureStructureMethod.invoke(mockSigner, mockSignableXMLObject);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#validateSignatureAlgorithm(Signature)}
     * when the algorithm used by the signature is null and
     * <p>
     * Must fail.
     */
    @Test
    public void testValidateSignatureAlgorithmWhenEIDASSAMLEngineExceptionIsThrown() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        final Method validateSignatureAlgorithmMethod = getDeclaredMethod("validateSignatureAlgorithm", Signature.class);
        final Signature mockSignature = Mockito.mock(Signature.class);

        validateSignatureAlgorithmMethod.invoke(mockSigner, mockSignature);
    }

    /**
     * Test method for constructor of
     * {@link AbstractProtocolSigner(eu.eidas.auth.engine.configuration.dom.SignatureConfiguration)}
     * when the metadata algorithm used is not part of the signature algorithm whitelist
     * <p>
     * Must fail.
     */
    @Test
    public void testAbstractProtocolSignerWithInvalidMetadataAlgorithm() throws EIDASSAMLEngineException {
        expectedException.expect(isA(ProtocolEngineConfigurationException.class));

        Map<String, String> signingProperties = getSigningProperties(ISSUER, RSA_KEY_3072_SERIAL, KEYSTORE_PATH);

        List<String> signatureAlgorithmWhitelist = Arrays.asList(
          XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1,
          XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384_MGF1,
          XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1
        );
        signingProperties.put(SignatureKey.SIGNATURE_ALGORITHM_WHITE_LIST.toString(), String.join(",", signatureAlgorithmWhitelist));

        signingProperties.put(SignatureKey.METADATA_SIGNATURE_ALGORITHM.toString(), XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA512);

        new SignSW(signingProperties, null);
    }

    /**
     * Test method for constructor of
     * {@link AbstractProtocolSigner(eu.eidas.auth.engine.configuration.dom.SignatureConfiguration)}
     * when the signature algorithm (for request and response) used is not part of the signature algorithm whitelist
     * <p>
     * Must fail.
     */
    @Test
    public void testAbstractProtocolSignerWithInvalidSignatureAlgorithm() throws EIDASSAMLEngineException {
        expectedException.expect(isA(ProtocolEngineConfigurationException.class));

        Map<String, String> signingProperties = getSigningProperties(ISSUER, RSA_KEY_3072_SERIAL, KEYSTORE_PATH);

        List<String> signatureAlgorithmWhitelist = Arrays.asList(
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1,
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384_MGF1
        );
        signingProperties.put(SignatureKey.SIGNATURE_ALGORITHM_WHITE_LIST.toString(), String.join(",", signatureAlgorithmWhitelist));

        new SignSW(signingProperties, null);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#getTrustedCertificateFromRSAKeyValue(RSAKeyValue)}
     * when Exponent and Modulus elements values
     * are not equal with the values from {@link RSAKeyValue}
     * <p>
     * Must fail.
     */
    @Test
    public void testGetTrustedCertificateFromRSAKeyValueWhenEIDASSAMLEngineExceptionIsThrown() throws EIDASSAMLEngineException, SecurityException {
        expectedException.expect(EIDASSAMLEngineException.class);

        final RSAKeyValue mockRSAKeyValue = Mockito.mock(RSAKeyValue.class);
        Exponent mockedExponent = Mockito.mock(Exponent.class);
        Mockito.when(mockedExponent.getValueBigInt()).thenReturn(new BigInteger("1234"));
        Mockito.when(mockRSAKeyValue.getExponent()).thenReturn(mockedExponent);
        Modulus mockedModulus = Mockito.mock(Modulus.class);
        Mockito.when(mockedModulus.getValueBigInt()).thenReturn(new BigInteger("4321"));
        Mockito.when(mockRSAKeyValue.getModulus()).thenReturn(mockedModulus);

        final Map<String, String> signingProperties = getSigningProperties(ISSUER, RSA_KEY_3072_SERIAL, KEYSTORE_PATH);
        final ProtocolSignerI protocolSigner = new SignSW(signingProperties, null);
        protocolSigner.getTrustedCertificateFromRSAKeyValue(mockRSAKeyValue);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#checkMetadataTrustAnchorAgainstTrustStore(List)}
     * when the trust chain contains a certificate present in the trust store.
     * <p>
     * Must succeed.
     */
    @Test
    public void testCheckMetadataTrustAnchor() throws Exception {
        final X509Certificate trustedCert = getCertificateFromKeystore("trustedCert");
        final List<X509Certificate> trustChain = Collections.singletonList(trustedCert);

        final boolean checked = metadataSigner.checkMetadataTrustAnchorAgainstTrustStore(trustChain);

        Assert.assertTrue("Expected trust anchor to be found in trust store", checked);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#checkMetadataTrustAnchorAgainstTrustStore(List)}
     * when the trust chain does not contain a certificate present in the trust store.
     * <p>
     * Must fail.
     */
    @Test
    public void testCheckMetadataTrustAnchorWithNoMatch() throws Exception {
        final X509Certificate untrustedCert = getCertificate(SHA1_WITH_RSA_AND_MGF1_CERT_PATH);
        final List<X509Certificate> trustChain = Collections.singletonList(untrustedCert);

        final boolean checked = metadataSigner.checkMetadataTrustAnchorAgainstTrustStore(trustChain);

        Assert.assertFalse(checked);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#checkMetadataTrustAnchorAgainstTrustStore(List)}
     * when the trust chain is empty.
     * <p>
     * Must fail.
     */
    @Test
    public void testCheckMetadataTrustAnchorWithEmptyTrustChain() throws Exception {
        final List<X509Certificate> trustChain = new ArrayList<>();

        final boolean checked = metadataSigner.checkMetadataTrustAnchorAgainstTrustStore(trustChain);

        Assert.assertFalse(checked);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#checkMetadataTrustAnchorAgainstTrustStore(List)}
     * when the trust chain contains multiple certificates, one of which is present in the trust store.
     * <p>
     * Must succeed.
     */
    @Test
    public void testCheckMetadataTrustAnchorWithMultipleCertificatesOneMatch() throws Exception {
        final X509Certificate trustedCert = getCertificateFromKeystore("trustedCert");
        final X509Certificate untrustedCert = getCertificate(SHA1_WITH_RSA_AND_MGF1_CERT_PATH);

        final List<X509Certificate> trustChain = Arrays.asList(untrustedCert, trustedCert);

        final boolean checked = metadataSigner.checkMetadataTrustAnchorAgainstTrustStore(trustChain);

        Assert.assertTrue("Expected trust anchor to be found in trust store", checked);
    }

    /**
     * Test method for
     * {@link AbstractProtocolSigner#checkMetadataTrustAnchorAgainstTrustStore(List)}
     * when the trust chain contains a self-signed certificate present in the trust store.
     * <p>
     * Must succeed.
     */
    @Test
    public void testCheckMetadataTrustAnchorWithSelfSignedCert() throws Exception {
        final X509Certificate selfSignedCert = getCertificateFromKeystore("selfSignedCert");
        final List<X509Certificate> trustChain = Collections.singletonList(selfSignedCert);

        final boolean checked = metadataSigner.checkMetadataTrustAnchorAgainstTrustStore(trustChain);

        Assert.assertTrue("Expected self-signed trust anchor to be found in trust store", checked);
    }

    /**
     * Creates a map of signing properties dynamically with issuer, serial number, and keystore path as parameters.
     * <p>
     * This method generates a map of signing properties that can be used for configuring
     * signing and trust settings. It allows flexibility to specify the issuer, serial number,
     * and keystore path dynamically while keeping other parameters fixed.
     * </p>
     *
     * @param issuer       the distinguished name (DN) of the certificate issuer
     * @param serialNumber the serial number of the certificate in hexadecimal format
     * @param keystorePath the file path to the keystore
     * @return a map containing the signing properties
     */
    private Map<String, String> getSigningProperties(String issuer, String serialNumber, String keystorePath) {
        final Map<String, String> signingProperties = new HashMap<>();
        signingProperties.put("check.certificate.validity.period", "false");
        signingProperties.put("disallow.self.signed.certificate", "false");
        signingProperties.put("1.keyStorePath", keystorePath);
        signingProperties.put("1.keyStorePassword", "local-demo");
        signingProperties.put("1.keyPassword", "local-demo");
        signingProperties.put("1.keyStoreType", "PKCS12");
        signingProperties.put("1.keyStorePurpose", "TRUSTSTORE");
        signingProperties.put("issuer", issuer);
        signingProperties.put("serialNumber", serialNumber);
        signingProperties.put("signature.algorithm", "http://www.w3.org/2007/05/xmldsig-more#sha512-rsa-MGF1");

        return signingProperties;
    }

    private void interceptSpyConfigurationThenReturnBuilderFactory(XMLObjectBuilderFactory mockXMLObjectBuilderFactory) {
        final XMLObjectProviderRegistry xmlObjectProviderRegistry = defaultConfiguration.get(XMLObjectProviderRegistry.class, "default");
        final XMLObjectProviderRegistry spyXMLObjectProviderRegistry = Mockito.spy(xmlObjectProviderRegistry);
        Mockito.when(spyConfiguration.get(XMLObjectProviderRegistry.class, "default")).thenReturn(spyXMLObjectProviderRegistry);
        Mockito.when(spyXMLObjectProviderRegistry.getBuilderFactory()).thenReturn(mockXMLObjectBuilderFactory);
    }

    private X509Certificate mockX509CertificateWithPrincipal() {
        final X509Certificate mockX509Certificate = Mockito.mock(X509Certificate.class);
        X500Principal testPrincipal = new X500Principal("CN=test");
        Mockito.when(mockX509Certificate.getIssuerDN()).thenReturn(testPrincipal);
        Mockito.when(mockX509Certificate.getSubjectX500Principal()).thenReturn(testPrincipal);
        return mockX509Certificate;
    }

    private XMLObjectBuilder getMockXMLObjectBuilder(Signature mockSignature) {
        final KeyInfo mockKeyInfo = Mockito.mock(KeyInfo.class);
        final XMLObject mockXMLObject = Mockito.mock(XMLObject.class);
        final XMLObjectBuilder mockXMLObjectBuilder = Mockito.mock(XMLObjectBuilder.class);
        final KeyValue mockKeyValue = getMockKeyValue();

        Mockito.when(mockSignature.getKeyInfo()).thenReturn(mockKeyInfo);
        Mockito.when(mockKeyInfo.getKeyValues()).thenReturn(Collections.singletonList(mockKeyValue));
        Mockito.when(mockXMLObjectBuilder.buildObject(KeyInfo.DEFAULT_ELEMENT_NAME)).thenReturn(mockKeyInfo);
        Mockito.when(mockKeyInfo.getOrderedChildren()).thenReturn(Collections.singletonList(mockXMLObject));

        return mockXMLObjectBuilder;
    }

    private KeyValue getMockKeyValue() {
        final KeyValue mockKeyValue = Mockito.mock(KeyValue.class);
        final RSAKeyValue mockRSAKeyValue = Mockito.mock(RSAKeyValue.class);
        final Exponent mockExponent = Mockito.mock(Exponent.class);
        final Modulus mockModulus = Mockito.mock(Modulus.class);
        final BigInteger mockBigInteger = Mockito.mock(BigInteger.class);

        Mockito.when(mockKeyValue.getRSAKeyValue()).thenReturn(mockRSAKeyValue);
        Mockito.when(mockRSAKeyValue.getExponent()).thenReturn(mockExponent);
        Mockito.when(mockRSAKeyValue.getModulus()).thenReturn(mockModulus);
        Mockito.when(mockExponent.getValueBigInt()).thenReturn(mockBigInteger);
        Mockito.when(mockModulus.getValueBigInt()).thenReturn(mockBigInteger);

        return mockKeyValue;
    }

    private Method getDeclaredMethod(String methodName, Class... parameterTypes) throws NoSuchMethodException {
        final Method method = AbstractProtocolSigner.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);

        return method;
    }

    private void testValidateCertificateSignatureHash(String sigAlgName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        X509Certificate mockX509Certificate = Mockito.mock(X509Certificate.class);
        Mockito.when(mockX509Certificate.getSigAlgName()).thenReturn(sigAlgName);

        testValidCertificateSignatureHash(mockX509Certificate);
    }

    private void testInvalidCertificateSignatureHash(String sigAlgName) {
        X509Certificate mockX509Certificate = Mockito.mock(X509Certificate.class);
        Mockito.when(mockX509Certificate.getSigAlgName()).thenReturn(sigAlgName);

        testInvalidCertificateSignatureHash(mockX509Certificate);
    }

    private void testValidCertificateSignatureHash(X509Certificate certificate) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method validateCertificateSignatureHashMethod = getDeclaredMethod("validateCertificateSignatureHash", X509Certificate.class);
        validateCertificateSignatureHashMethod.invoke(mockSigner, certificate);
    }

    private void testInvalidCertificateSignatureHash(X509Certificate certificate) {
        try {
            Method validateCertificateSignatureHashMethod = getDeclaredMethod("validateCertificateSignatureHash", X509Certificate.class);
            validateCertificateSignatureHashMethod.invoke(mockSigner, certificate);
        } catch (UndeclaredThrowableException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            if (e.getCause() instanceof EIDASSAMLEngineException) {
                return;
            }
        }
        Assert.fail("An exception should have been thrown.");
    }


    protected X509Certificate getCertificate(String certificatePath) throws CertificateException, FileNotFoundException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        FileInputStream certificateFileInputStream = new FileInputStream(certificatePath);
        return (X509Certificate) certificateFactory.generateCertificate(certificateFileInputStream);
    }

    /**
     * Retrieves an X509Certificate from the specified keystore based on the provided alias.
     * <p>
     * This method loads the keystore from a predefined file path and attempts to fetch the
     * certificate associated with the given alias.
     * </p>
     *
     * @param alias the alias of the certificate to retrieve from the keystore
     * @return the X509Certificate associated with the provided alias
     * @throws Exception if there is an issue loading the keystore or retrieving the certificate
     */
    private X509Certificate getCertificateFromKeystore(String alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream("src/test/resources/keystoreTestCertificates.p12")) {
            keyStore.load(fis, "local-demo".toCharArray());
        }
        return (X509Certificate) keyStore.getCertificate(alias);
    }

}
