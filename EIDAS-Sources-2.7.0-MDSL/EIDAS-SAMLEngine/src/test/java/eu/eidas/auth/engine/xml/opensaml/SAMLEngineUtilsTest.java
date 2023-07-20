/*
 * Copyright (c) 2023 by European Commission
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

package eu.eidas.auth.engine.xml.opensaml;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.configuration.dom.KeyStoreConfigurator;
import eu.eidas.auth.engine.configuration.dom.KeyStoreContent;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.core.impl.SignSW;
import eu.eidas.auth.engine.util.tests.TestingConstants;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.apache.xml.security.signature.XMLSignature;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SAMLObjectContentReference;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.SecurityException;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.SecurityConfigurationSupport;
import org.opensaml.xmlsec.SignatureSigningConfiguration;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.keyinfo.KeyInfoGeneratorManager;
import org.opensaml.xmlsec.keyinfo.NamedKeyInfoGeneratorManager;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.ContentReference;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.opensaml.core.xml.util.XMLObjectSupport.buildXMLObject;

public class SAMLEngineUtilsTest {

    private static final String PROTOCOL_ENGINE_CONF = "CONF1";
    private static final String KEYSTORE_PATH = "src/test/resources/signatureTestKeystore.p12";
    private static final String ISSUER = "CN=testCert, OU=DIGIT, O=EC, L=EU, ST=EU, C=EU";
    private static final String RSA_KEY_3072_SERIAL = "5CB93CF26B3687D6F6A65BD5C900B17F5BC85B7D";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private ProtocolEngineI protocolEngine;

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

    private static final ImmutableAttributeMap getRequestedAttributes() {
        return new ImmutableAttributeMap.Builder()
                .put(EidasSpec.Definitions.PERSON_IDENTIFIER)
                .build();
    }

    public static KeyInfo createKeyInfo(X509Credential credential) throws EIDASSAMLEngineException {
        final SignatureSigningConfiguration secConfiguration = SecurityConfigurationSupport.getGlobalSignatureSigningConfiguration();
        final NamedKeyInfoGeneratorManager keyInfoManager = secConfiguration.getKeyInfoGeneratorManager();
        final KeyInfoGeneratorManager keyInfoGenManager = keyInfoManager.getDefaultManager();
        final KeyInfoGeneratorFactory keyInfoGenFac = keyInfoGenManager.getFactory(credential);
        final KeyInfo keyInfo = createKeyInfo(keyInfoGenFac, credential);
        return keyInfo;
    }

    public static KeyInfo createKeyInfo(KeyInfoGeneratorFactory keyInfoGenFac, X509Credential credential) throws EIDASSAMLEngineException
    {
        KeyInfoGenerator keyInfoGenerator = keyInfoGenFac.newInstance();
        KeyInfo keyInfo;
        try {
            keyInfo = keyInfoGenerator.generate(credential);
            return keyInfo;
        } catch (SecurityException e) {
            throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_UNTRUSTED_CERTIFICATE.errorCode(),e);
        }
    }

    @Before
    public void setup() {
        protocolEngine = getProtocolEngine(PROTOCOL_ENGINE_CONF);
    }

    /**
     * Test method for
     * {@link SAMLEngineUtils#getDigestMethodAlgorithm(Signature)}
     * when a request is signed with {@link XMLSignature#ALGO_ID_SIGNATURE_RSA_SHA512_MGF1}
     * getDigestMethodAlgorithm must return default digest method {@link SignatureConstants#ALGO_ID_DIGEST_SHA512}
     * <p>
     * Must succeed.
     */
    @Test
    public void getDigestMethodAlgorithm() throws EIDASSAMLEngineException {
        String expectedDigestAlgorithm = SignatureConstants.ALGO_ID_DIGEST_SHA512;
        ProtocolSignerI signSW = getRSASigner(RSA_KEY_3072_SERIAL);
        AuthnRequest requestToSign = getRequestToSign();
        AuthnRequest signedRequest = signSW.sign(requestToSign);
        Signature signature = signedRequest.getSignature();

        String determinedDigestAlgorithm = SAMLEngineUtils.getDigestMethodAlgorithm(signature);

        Assert.assertEquals(expectedDigestAlgorithm, determinedDigestAlgorithm);
    }

    /**
     * Test method for
     * {@link SAMLEngineUtils#getDigestMethodAlgorithm(Signature)}
     * when signature is missing XmlSignature getDigestMethodAlgorithm throws EIDASSAMLEngineException
     * <p>
     * Must fail.
     */

    @Test
    public void getDigestMethodAlgorithmMissingXmlSignature() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.INVALID_SIGNATURE_ALGORITHM.errorCode());

        Signature signature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);

        SAMLEngineUtils.getDigestMethodAlgorithm(signature);
    }

    /**
     * Test method for
     * {@link SAMLObjectContentReference#getDigestAlgorithm()}
     * when our created test object is run against getDigestAlgorithm() it must succeed at misleading it.
     * this is behavior observed in opensaml:opensaml-core:3.4.3
     * when upgrading to a version of opensaml-core where this is fixed: must fail
     * <p>
     * Must succeed.
     */
    @Test
    public void getDigestAlgorithmMethodIncorrectlyCheckedAfterUnmarshalling() throws EIDASSAMLEngineException, MarshallingException, SignatureException, UnmarshallException {
        final String expectedDigestAlgorithm = SignatureConstants.ALGO_ID_DIGEST_SHA1;
        final String incorrectDigestAlgorithm = SignatureConstants.ALGO_ID_DIGEST_SHA256;
        AuthnRequest signedRequest = createSignedRequestThatIsMisleading(expectedDigestAlgorithm);
        Signature signature = signedRequest.getSignature();

        SAMLObjectContentReference samlObjContentRef = (SAMLObjectContentReference) signature.getContentReferences().get(0);
        String wrongDigestAlgorithm = samlObjContentRef.getDigestAlgorithm();

        Assert.assertEquals(incorrectDigestAlgorithm, wrongDigestAlgorithm);
        Assert.assertNotEquals(expectedDigestAlgorithm, wrongDigestAlgorithm);
    }

    /**
     * Test method for
     * {@link SAMLEngineUtils#getDigestMethodAlgorithm(Signature)}
     * when our created test object is run against getDigestAlgorithm() it must fail at misleading it.
     * <p>
     * Must succeed.
     */
    @Test
    public void getDigestAlgorithmMethodCorrectlyCheckedAfterUnmarshalling() throws EIDASSAMLEngineException, MarshallingException, SignatureException, UnmarshallException {
        final String expectedDigestAlgorithm = SignatureConstants.ALGO_ID_DIGEST_SHA1;
        final String incorrectDigestAlgorithm = SignatureConstants.ALGO_ID_DIGEST_SHA256;
        AuthnRequest signedRequest = createSignedRequestThatIsMisleading(expectedDigestAlgorithm);
        Signature signature = signedRequest.getSignature();

        String determinedDigestAlgorithm = SAMLEngineUtils.getDigestMethodAlgorithm(signature);

        Assert.assertEquals(expectedDigestAlgorithm, determinedDigestAlgorithm);
        Assert.assertNotEquals(incorrectDigestAlgorithm, determinedDigestAlgorithm);
    }

    /**
     * Test method for
     * {@link SAMLEngineUtils#getValidIssuerValue(String)}
     * when value is an allowed one
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetValidIssuerValueWithValueValidScheme() throws EIDASSAMLEngineException {
        SAMLEngineUtils.getValidIssuerValue("https://allowedMetadataScheme");
    }

    /**
     * Test method for
     * {@link SAMLEngineUtils#getValidIssuerValue(String)}
     * when value is null and the method throws EIDASSAMLEngineException
     * <p>
     * Must fail.
     */
    @Test
    public void testGetValidIssuerValueWithValueNull() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        SAMLEngineUtils.getValidIssuerValue(null);
    }

    private ProtocolEngineI getProtocolEngine(String configName) {
        return ProtocolEngineFactory.getDefaultProtocolEngine(configName);
    }

    private ProtocolSignerI getRSASigner(String keySerialNumber) throws EIDASSAMLEngineException {
        Map<String, String> signingProperties = getSigningProperties(keySerialNumber);
        signingProperties.put("signature.algorithm", "http://www.w3.org/2007/05/xmldsig-more#sha512-rsa-MGF1");
        return getSigner(signingProperties);
    }

    private ProtocolSignerI getSigner(Map<String, String> signingProperties) throws EIDASSAMLEngineException {
        ProtocolSignerI signer = new SignSW(signingProperties, null);

        return signer;
    }

    private Map<String, String> getSigningProperties(String keySerialNumber) {
        Map<String, String> signingProperties = new HashMap<>();
        signingProperties.put("check.certificate.validity.period", "false");
        signingProperties.put("disallow.self.signed.certificate", "false");
        signingProperties.put("keyStorePath", KEYSTORE_PATH);
        signingProperties.put("keyStorePassword", "local-demo");
        signingProperties.put("keyPassword", "local-demo");
        signingProperties.put("keyStoreType", "PKCS12");
        signingProperties.put("keyStorePurpose", "TRUSTSTORE");
        signingProperties.put("issuer", ISSUER);
        signingProperties.put("serialNumber", keySerialNumber);
        return signingProperties;
    }

    private AuthnRequest getRequestToSign() throws EIDASSAMLEngineException {
        IEidasAuthenticationRequest testRequest = getTestRequest();

        AuthnRequest samlRequest = protocolEngine.getProtocolProcessor().marshallRequest(
                testRequest,
                TestingConstants.SERVICE_METADATA_URL_CONS.toString(),
                protocolEngine.getCoreProperties(),
                protocolEngine.getClock().getCurrentTime());

        return samlRequest;
    }

    private IEidasAuthenticationRequest getTestRequest() {
        IEidasAuthenticationRequest request = EidasAuthenticationRequest.builder()
                .id(TestingConstants.REQUEST_ID_CONS.toString())
                .issuer(TestingConstants.ISSUER_CONS.toString())
                .assertionConsumerServiceURL(TestingConstants.ASSERTION_URL_CONS.toString())
                .destination(TestingConstants.DESTINATION_CONS.toString())
                .serviceProviderCountryCode(TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())
                .citizenCountryCode(TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())
                .requestedAttributes(getRequestedAttributes())
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH.getValue())
                .build();

        return request;
    }

    private AuthnRequest createSignedRequestThatIsMisleading(String actualSignedDigestAlgorithm) throws MarshallingException, SignatureException, EIDASSAMLEngineException, UnmarshallException {
        final Map<String, String> properties = getSigningProperties(RSA_KEY_3072_SERIAL);
        final AuthnRequest signableObject = getRequestToSign();
        final KeyStoreContent keyStoreContent = new KeyStoreConfigurator(properties, "").loadKeyStoreContent();
        final KeyStore.PrivateKeyEntry signatureKeyAndCertificate = keyStoreContent.getMatchingPrivateKeyEntry(properties.get("serialNumber"), properties.get("issuer"));
        final X509Credential privateSigningCredential = CertificateUtil.createCredential(signatureKeyAndCertificate);

        Signature signature = createSignature(privateSigningCredential);
        signableObject.setSignature(signature);
        List<ContentReference> contentReferences = signature.getContentReferences();

        ((SAMLObjectContentReference)contentReferences.get(0)).setDigestAlgorithm(actualSignedDigestAlgorithm);
        Element marshalledObject = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(signableObject).marshall(signableObject);

        Signer.signObject(signature);

        final AuthnRequest signedRequest = (AuthnRequest) OpenSamlHelper.unmarshallFromDom(marshalledObject.getOwnerDocument());

        return signedRequest;
    }

    private Signature createSignature(@Nonnull X509Credential credential) throws EIDASSAMLEngineException {
        final KeyInfo keyInfo = createKeyInfo(credential);
        final Signature signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
                .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
                .buildObject(Signature.DEFAULT_ELEMENT_NAME);

        signature.setSigningCredential(credential);
        signature.setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1);
        signature.setKeyInfo(keyInfo);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        return signature;
    }
}