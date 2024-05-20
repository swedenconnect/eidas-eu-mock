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

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.util.tests.TestingConstants;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.xmlsec.signature.SignableXMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Provider;
import java.security.Security;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
  * Tests for the {@link ProtocolSignerI}'s implementations
  */
public class ProtocolSignerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final Logger LOG = LoggerFactory.getLogger(ProtocolSignerTest.class);

    private static final String PROTOCOL_ENGINE_CONF = "CONF1";

    private static final String KEYSTORE_PATH = "src/test/resources/signatureTestKeystore.p12";
    private static final String ISSUER = "CN=testCert, OU=DIGIT, O=EC, L=EU, ST=EU, C=EU";

    private static final String RSA_KEY_2048_SERIAL = "6FF71A957A6813F9DDD393C0A9C5B64537860D21";
    private static final String RSA_KEY_3072_SERIAL = "5CB93CF26B3687D6F6A65BD5C900B17F5BC85B7D";

    private static final String EC_KEY_224_SERIAL = "21CED2ACF220E7DD0E4DA32C910C61198B5FFA22";
    private static final String EC_KEY_256_SERIAL = "2C589B8D66EB03A6E7E8C6F5E481291B18F4B54B";

    private ProtocolEngineI protocolEngine;

    @BeforeClass
    public static void setupClass() {
        OpenSamlHelper.initialize();
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.insertProviderAt(new BouncyCastleProvider(), 1);
        }
    }

    @AfterClass
    public static void tearDownClass() {
        Provider bouncyCastleProvider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (bouncyCastleProvider != null) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @Before
    public void setup() {
        protocolEngine = getProtocolEngine(PROTOCOL_ENGINE_CONF);
    }

    /**
      * Test method for
      * {@link AbstractProtocolSigner#validateSignature(SignableXMLObject, Collection)}
      * of an AuthnRequest signed with a RSA key of 3072 bits long
      *
      * <p>
      * Must succeed.
      */
    @Test
    public void testSignatureValidationForRequestSignedWithRSA3072() throws EIDASSAMLEngineException {
        LOG.info("RSA signature with keys of 3072 bits.");
        ProtocolSignerI signSW = getRSASigner(RSA_KEY_3072_SERIAL);

        AuthnRequest requestToSign = getRequestToSign();
        AuthnRequest signedRequest = signSW.sign(requestToSign);

        signSW.validateSignature(signedRequest, null);
    }

    /**
      * Test method for
      * {@link AbstractProtocolSigner#validateSignature(SignableXMLObject, Collection)}
      * of an AuthnRequest signed with a RSA key of 2048 bits long
      *
      * <p>
      * Must throw an EIDASSAMLEngineException.
      */
    @Test
    public void testSignatureValidationForRequestSignedWithRSA2048() throws EIDASSAMLEngineException {
        thrown.expect(EIDASSAMLEngineException.class);
        thrown.expectMessage(EidasErrorKey.INVALID_SIGNATURE_ALGORITHM.errorMessage());

        LOG.info("RSA signature with keys of 2048 bits.");

        ProtocolSignerI signSW = getRSASigner(RSA_KEY_2048_SERIAL);

        AuthnRequest requestToSign = getRequestToSign();
        AuthnRequest signedRequest = signSW.sign(requestToSign);

        signSW.validateSignature(signedRequest, null);
    }

    /**
      * Test method for
      * {@link AbstractProtocolSigner#validateSignature(SignableXMLObject, Collection)}
      * of an AuthnRequest signed with a EC key of 224 bits long
      *
      * <p>
      * Must throw an EIDASSAMLEngineException.
      */
    @Test
    public void testSignatureValidationForRequestSignedWithEC224() throws EIDASSAMLEngineException {
        thrown.expect(EIDASSAMLEngineException.class);
        thrown.expectMessage(EidasErrorKey.INVALID_SIGNATURE_ALGORITHM.errorMessage());

        LOG.info("EC signature with keys of 224 bits.");

        ProtocolSignerI signSW = getECSigner(EC_KEY_224_SERIAL);

        AuthnRequest requestToSign = getRequestToSign();
        AuthnRequest signedRequest = signSW.sign(requestToSign);

        signSW.validateSignature(signedRequest, null);
    }

    /**
      * Test method for
      * {@link AbstractProtocolSigner#validateSignature(SignableXMLObject, Collection)}
      * of an AuthnRequest signed with a EC key of 256 bits long
      *
      * <p>
      * Must succeed.
      */
    @Test
    public void testSignatureValidationForRequestSignedWithEC256() throws EIDASSAMLEngineException {
        LOG.info("EC signature with keys of 256 bits.");

        ProtocolSignerI signSW = getECSigner(EC_KEY_256_SERIAL);

        AuthnRequest requestToSign = getRequestToSign();
        AuthnRequest signedRequest = signSW.sign(requestToSign);

        signSW.validateSignature(signedRequest, null);
    }

    private ProtocolEngineI getProtocolEngine(String configName) {
        return ProtocolEngineFactory.getDefaultProtocolEngine(configName);
    }

    private ProtocolSignerI getRSASigner(String keySerialNumber) throws EIDASSAMLEngineException {
        Map<String, String> signingProperties = getSigningProperties(keySerialNumber);
        signingProperties.put("signature.algorithm", "http://www.w3.org/2007/05/xmldsig-more#sha512-rsa-MGF1");
        return getSigner(signingProperties);
    }

    private ProtocolSignerI getECSigner(String keySerialNumber) throws EIDASSAMLEngineException {
        Map<String, String> signingProperties = getSigningProperties(keySerialNumber);
        signingProperties.put("signature.algorithm", "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256");
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
        signingProperties.put("1.keyStorePath", KEYSTORE_PATH);
        signingProperties.put("1.keyStorePassword", "local-demo");
        signingProperties.put("1.keyPassword", "local-demo");
        signingProperties.put("1.keyStoreType", "PKCS12");
        signingProperties.put("1.keyStorePurpose", "TRUSTSTORE");
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

    private static final ImmutableAttributeMap getRequestedAttributes() {
        return new ImmutableAttributeMap.Builder()
                .put(EidasSpec.Definitions.PERSON_IDENTIFIER)
                .build();
    }

}
