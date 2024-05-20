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

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.EncryptionConfiguration;
import eu.eidas.auth.engine.configuration.dom.KeyStoreConfigurator;
import eu.eidas.auth.engine.configuration.dom.KeyStoreContent;
import eu.eidas.auth.engine.configuration.dom.KeyStoreKey;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.impl.EncryptedAssertionImpl;
import org.opensaml.saml.saml2.core.impl.ResponseImpl;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.encryption.EncryptedData;
import org.opensaml.xmlsec.encryption.EncryptionMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for the {@link AbstractProtocolDecrypter}
 */
public class AbstractProtocolDecrypterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProtocolDecrypterTest.class);
    private static final String RETRIEVE_DECRYPTION_CREDENTIALS_METHOD_NAME = "retrieveDecryptionCredentials";
    private static final Method RETRIEVE_DECRYPTION_CREDENTIALS_METHOD = ReflectionUtils.findMethod(
            AbstractProtocolDecrypter.class, RETRIEVE_DECRYPTION_CREDENTIALS_METHOD_NAME);

    static {
        RETRIEVE_DECRYPTION_CREDENTIALS_METHOD.setAccessible(true);
    }

    @BeforeClass
    public static void setupClass() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        Provider bouncyCastleProvider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (bouncyCastleProvider != null) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     *  Test method for
     *  {@link AbstractProtocolDecrypter#retrieveDecryptionCredentials()}
     *  when the keystore contains multiple private key entries.
     *  should return private key entries correctly and not throw any exception.
     *  <p>
     *  Must succeed.
     */
    @Test
    public void testRetrieveDecryptionCredentials() throws ProtocolEngineConfigurationException {
        AbstractProtocolDecrypter decrypter = generateAbstractProtocolDecrypter("/keyStoreWithMultiplePrivateKeyEntries.p12", false);
        Credential[] credentials = (Credential[])
                ReflectionUtils.invokeMethod(RETRIEVE_DECRYPTION_CREDENTIALS_METHOD, decrypter);

        Assert.assertEquals(2, credentials.length);
        List<String> validSerialNumber = Arrays.asList(
                "4CD836D017315376DAF6EAEAB44AA1950A7268BF".toLowerCase(),
                "13F9B7951CAC5C38A772801403DD87680A0EC2EF".toLowerCase());

        String certificateSerialNumberOfFirstCredential = getCertificateSerialNumberOfCredential(credentials[0]);
        Assert.assertTrue(validSerialNumber.contains(certificateSerialNumberOfFirstCredential));

        String certificateSerialNumberOfSecondCredential = getCertificateSerialNumberOfCredential(credentials[1]);
        Assert.assertTrue(validSerialNumber.contains(certificateSerialNumberOfSecondCredential));

        List<String> validSubject = Arrays.asList(
                "CN=test-EC, OU=DIGIT, O=EC, L=Brussels, ST=Belgium, C=BE",
                "CN=test-RSA, OU=DIGIT, O=EC, L=Brussels, ST=Belgium, C=BE");

        String certificateSubjectDNOfFirstCredential = getCertificateSubjectDNOfCredential(credentials[0]);
        Assert.assertTrue(validSubject.contains(certificateSubjectDNOfFirstCredential));

        String certificateSubjectDNOfSecondCredential = getCertificateSubjectDNOfCredential(credentials[1]);
        Assert.assertTrue(validSubject.contains(certificateSubjectDNOfSecondCredential));
    }

    /**
     * Test method for
     * {@link AbstractProtocolDecrypter#retrieveDecryptionCredentials()}
     * when the keystore has only invalid private key entries and that we check the validity period of the certificates.
     * Should throw an {@link EIDASSAMLEngineException}.
     * <p>
     * Must fail.
     */
    @Test(expected = EIDASSAMLEngineException.class)
    public void testRetrieveDecryptionCredentialWithInvalidPrivateKeys() throws Throwable {
        AbstractProtocolDecrypter decrypter = generateAbstractProtocolDecrypter("/keyStoreMetadata.p12", true);

        try {
            ReflectionUtils.invokeMethod(RETRIEVE_DECRYPTION_CREDENTIALS_METHOD, decrypter);
        } catch (Exception e) {
            Assert.assertTrue(e.getCause() instanceof EIDASSAMLEngineException);
            EIDASSAMLEngineException eidasException = (EIDASSAMLEngineException) e.getCause();
            Assert.assertEquals(EidasErrorKey.SAML_ENGINE_INVALID_CERTIFICATE, eidasException.getEidasErrorKey());
            throw e.getCause();
        }
        Assert.fail("This execution should have thrown an error");
    }

    /**
     * Test method for
     * {@link AbstractProtocolDecrypter#decryptSamlResponse(Response)}
     * when the response is not encrypted
     * should throw {@link EIDASSAMLEngineException}.
     * <p>
     * Must fail.
     */
    @Test
    public void testDecryptResponseNotEncrypted() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        AbstractProtocolDecrypter decrypter = generateAbstractProtocolDecrypter("/keyStoreWithMultiplePrivateKeyEntries.p12", false);
        Response authResponse = Mockito.mock(ResponseImpl.class);
        decrypter.decryptSamlResponse(authResponse);
    }

    /**
     * Test method for
     * {@link AbstractProtocolDecrypter#decryptSamlResponse(Response)}
     * when the response contains undecryptable assertions
     * should throw {@link EIDASSAMLEngineException}.
     * <p>
     * Must fail.
     */
    @Test
    public void testDecryptResponseIncorrectAssertions() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        AbstractProtocolDecrypter decrypter = generateAbstractProtocolDecrypter("/keyStoreWithMultiplePrivateKeyEntries.p12", false);

        OpenSamlHelper.initialize();

        Response authResponse = Mockito.mock(ResponseImpl.class);
        EncryptedAssertion assertion = Mockito.mock(EncryptedAssertionImpl.class);
        List<EncryptedAssertion> assertions = new ArrayList<>();
        assertions.add(assertion);
        EncryptedData encryptedData = Mockito.mock(EncryptedData.class);
        EncryptionMethod encryptionMethod = Mockito.mock(EncryptionMethod.class);

        Mockito.when(authResponse.getEncryptedAssertions()).thenReturn(assertions);
        Mockito.when(assertion.getEncryptedData()).thenReturn(encryptedData);
        Mockito.when(encryptedData.getEncryptionMethod()).thenReturn(encryptionMethod);
        Mockito.when(encryptionMethod.getAlgorithm()).thenReturn("sha512-rsa-MGF1");

        decrypter.decryptSamlResponse(authResponse);
    }


    private String getCertificateSerialNumberOfCredential(Credential credential) {
        int hexa_radix = 16;
        X509Certificate certificate = ((X509Credential) credential).getEntityCertificate();
        return certificate.getSerialNumber().toString(hexa_radix);
    }

    private String getCertificateSubjectDNOfCredential(Credential credential) {
        X509Certificate certificate = ((X509Credential) credential).getEntityCertificate();
        return certificate.getSubjectDN().toString();
    }

    private AbstractProtocolDecrypter generateAbstractProtocolDecrypter(String keystoreFileLocation, Boolean checkedValidityPeriod) throws ProtocolEngineConfigurationException {
        final KeyStoreContent keyStoreContent = generateKeyStoreContent(keystoreFileLocation);
        Assert.assertNotNull(keyStoreContent);
        ImmutableSet<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates = keyStoreContent.getPrivateKeyEntries();

        EncryptionConfiguration encryptionConfiguration = new EncryptionConfiguration.Builder()
                .setCheckedValidityPeriod(checkedValidityPeriod)
                .setDisallowedSelfSignedCertificate(false)
                .setResponseEncryptionMandatory(true)
                .setAssertionEncryptWithKey(true)
                .setDecryptionKeyAndCertificates(decryptionKeyAndCertificates)
                .setEncryptionAlgorithmWhitelist(ImmutableSet.<String>builder().add("sha512-rsa-MGF1").build())
                .build();
        return new AbstractSamlEngineEncryption.BaseProtocolDecrypter(encryptionConfiguration);
    }

    private KeyStoreContent generateKeyStoreContent(String keystorepath) {
        Map<String, String> testProperties = new HashMap<>();
        testProperties.put(KeyStoreKey.KEYSTORE_PATH.getKey(), keystorepath);
        testProperties.put(KeyStoreKey.KEYSTORE_TYPE.getKey(), "PKCS12");
        testProperties.put(KeyStoreKey.KEYSTORE_PASSWORD.getKey(), "local-demo");
        testProperties.put(KeyStoreKey.KEY_PASSWORD.getKey(), "local-demo");

        try {
            KeyStoreConfigurator keyStoreConfigurator = new KeyStoreConfigurator(testProperties, "src/test/resources");
            return keyStoreConfigurator.loadKeyStoreContent();
        } catch (ProtocolEngineConfigurationException e) {
            e.printStackTrace();
            Assert.fail("KeyStoreContent has not been initialized correctly");
            return null;
        }
    }
}
