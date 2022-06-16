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

package eu.eidas.engine.test.simple.eidas;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.SamlNameIdFormat;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.eidas.EidasProtocolProcessor;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.engine.test.simple.SSETestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import static eu.eidas.engine.EidasAttributeTestUtil.newAttributeDefinition;
import static eu.eidas.engine.EidasAttributeTestUtil.newEidasAttributeDefinition;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * The Class EidasAuthRequestTest performs unit test for EIDAS format requests
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EidasAuthRequestTest {

    @SuppressWarnings({"PublicField"})
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String TEST_ATTRIBUTE_FULL_NAME =
            "http://eidas.europa.eu/attributes/naturalperson/EidasAdditionalAttribute";

    /**
     * The engines.
     */
    ProtocolEngineI getEngine() {
        return getEngine("CONF1");
    }

    ProtocolEngineI getEngine2() {
        return getEngine("CONF2");
    }

    ProtocolEngineI getEngine3() {
        return getEngine("CONF3");
    }

    ProtocolEngineI getEngine4() {
        return getEngine("CONF4");
    }

    ProtocolEngineI getEngine(String conf) {
        ProtocolEngineI engine = null;
        try {
            engine = ProtocolEngineFactory.createProtocolEngine(conf, new EidasProtocolProcessor(
                    "saml-engine-eidas-attributes-" + conf + ".xml",
                    "saml-engine-additional-attributes-" + conf + ".xml", null, null, null, null));
        } catch (EIDASSAMLEngineException exc) {
            fail("Failed to initialize SAMLEngines");
        }
        return engine;
    }

    /**
     * Instantiates a new authentication request test.
     */
    public EidasAuthRequestTest() {
        Map<AttributeDefinition, ? extends Iterable<String>> attributeDefinitionMap = new HashMap<>();

        AttributeDefinition eIDNumber =
                newEidasAttributeDefinition("PersonIdentifier", "PersonIdentifier", true, true, false);
        AttributeDefinition dateOfBirth = newEidasAttributeDefinition("DateOfBirth", "DateOfBirth", true, false, false);
        AttributeDefinition familyName =
                newEidasAttributeDefinition("CurrentFamilyName", "FamilyName", true, false, true);
        AttributeDefinition currentAddress =
                newEidasAttributeDefinition("CurrentAddress", "CurrentAddress", false, false, false);

        immutableAttributeMap = new ImmutableAttributeMap.Builder().put(eIDNumber)
                .put(dateOfBirth)
                .put(familyName)
                .put(currentAddress)
                .build();

        destination = "http://proxyservice.gov.xx/EidasNode/ColleagueRequest";
        assertConsumerUrl = "http://connector.gov.xx/EidasNode/ColleagueResponse";

        spName = "University of Oxford";

        spType = "public";
    }

    /**
     * Dummy samlId
     */
    private static String DUMMY_SAML_ID = "0";

    private static String DUMMY_ISSUER_URI = "http://issuer.com";

    private static String LEVEL_OF_ASSURANCE = NotifiedLevelOfAssurance.LOW.getValue();

    /**
     * The destination.
     */
    private String destination;

    /**
     * The service provider name.
     */
    private String spName;

    /**
     * The service provider type.
     */
    private String spType;

    /**
     * The Map of Attributes.
     */
    private ImmutableAttributeMap immutableAttributeMap;

    /**
     * The assertion consumer URL.
     */
    private String assertConsumerUrl;

    /**
     * The authentication request.
     */
    private static byte[] authRequest;

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EidasAuthRequestTest.class.getName());

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

    /**
     * Test generate authentication request.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public void testGenerateAuthnRequest() throws EIDASSAMLEngineException {

        AttributeDefinition additionalAttributeDefinition =
                newAttributeDefinition(TEST_ATTRIBUTE_FULL_NAME, "EidasAdditionalAttribute", true);
        ImmutableAttributeMap additionalAttributeMap =
                new ImmutableAttributeMap.Builder().put(additionalAttributeDefinition).build();

        IAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .providerName(spName)
                .requestedAttributes(additionalAttributeMap)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .spType("public")
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .id(DUMMY_SAML_ID)
                .issuer(DUMMY_ISSUER_URI)
                .citizenCountryCode("ES")
                .nameIdFormat(SamlNameIdFormat.PERSISTENT.toString())
                .build();

        byte[] samlToken = getEngine().generateRequestMessage(request, null).getMessageBytes();
        LOG.info("EidasAuthenticationRequest 1: " + SSETestUtils.encodeSAMLToken(samlToken));
        IAuthenticationRequest parsedRequest = getEngine().unmarshallRequestAndValidate(samlToken, "ES");
        assertNotNull(parsedRequest);
        assertFalse(parsedRequest.getRequestedAttributes().isEmpty());

        LOG.info("EidasAuthenticationRequest 2: " + SSETestUtils.encodeSAMLToken(
                getEngine().generateRequestMessage(request, null).getMessageBytes()));
    }

    /**
     * Test generate authentication request empty personal attribute name error.
     */
    @Test
    public void testGenerateAuthnRequestPALsErr1() throws EIDASSAMLEngineException {
        thrown.expect(EIDASSAMLEngineException.class);
        thrown.expectMessage(containsString("Error (no. 202005) processing request"));
        thrown.expectMessage(containsString("No requested attribute (request issuer:"));

        ImmutableAttributeMap emptyAttributeMap = new ImmutableAttributeMap.Builder().build();
        IAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .providerName(spName)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .spType(spType)
                .requestedAttributes(emptyAttributeMap)
                .id(DUMMY_SAML_ID)
                .issuer(DUMMY_ISSUER_URI)
                .citizenCountryCode("ES")
                .nameIdFormat(SamlNameIdFormat.PERSISTENT.toString())
                .build();

        getEngine().generateRequestMessage(request, null);
    }

    /**
     * Test generate authentication request error personal attribute value error.
     */
    @Test
    public void testGenerateAuthnRequestPALsErr2() throws Exception {

        AttributeDefinition wrongAttributeDefinition =
                newEidasAttributeDefinition("attrNotValid", "attrNotValid", true);
        ImmutableAttributeMap wrongAttributeMap =
                new ImmutableAttributeMap.Builder().put(wrongAttributeDefinition).build();

        IAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .providerName(spName)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .spType(spType)
                .requestedAttributes(wrongAttributeMap)
                .id(DUMMY_SAML_ID)
                .issuer(DUMMY_ISSUER_URI)
                .citizenCountryCode("ES")
                .nameIdFormat(SamlNameIdFormat.PERSISTENT.toString())
                .build();

        IRequestMessage authenticationRequest = getEngine().generateRequestMessage(request, null);
        assertNotNull(authenticationRequest);
    }

    /**
     * Test generate authentication request service provider country null.
     */
    @Test
    public void testGenerateAuthnRequestCountryErr() throws Exception {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("citizenCountryCode cannot be null, empty or blank");

        IAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .providerName(spName)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .spType(spType)
                .requestedAttributes(immutableAttributeMap)
                .id(DUMMY_SAML_ID)
                .issuer(DUMMY_ISSUER_URI)
                .citizenCountryCode(null)
                .nameIdFormat(SamlNameIdFormat.PERSISTENT.toString())
                .build();
        getEngine().generateRequestMessage(request, null);
    }

    /**
     * Test generate authentication request error with quality authentication assurance level wrong.
     */
    @Test
    public void testGenerateAuthnRequestLoAErr() throws EIDASSAMLEngineException, NoSuchFieldException, IllegalAccessException {
        thrown.expect(EIDASSAMLEngineException.class);
        thrown.expectMessage(EidasErrors.get(EidasErrorKey.INVALID_LOA_VALUE.errorMessage()));

        IAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .providerName(spName)
                .levelOfAssurance("http://non.eidas.eu/NotNotified/LoA/4")
                .assertionConsumerServiceURL(assertConsumerUrl)
                .spType(spType)
                .requestedAttributes(immutableAttributeMap)
                .id(DUMMY_SAML_ID)
                .issuer(DUMMY_ISSUER_URI)
                .citizenCountryCode("ES")
                .nameIdFormat(SamlNameIdFormat.PERSISTENT.toString())
                .build();

        Field comparison = EidasAuthenticationRequest.class.getSuperclass().getDeclaredField("levelOfAssuranceComparison");
        comparison.setAccessible(true);
        comparison.set(request, LevelOfAssuranceComparison.MINIMUM);

        getEngine().generateRequestMessage(request, null);
    }

    /**
     * Test generate authentication request personal attribute list null value.
     */
    @Test
    public void testGenerateAuthnRequestPALErr1() throws EIDASSAMLEngineException {
        thrown.expect(EIDASSAMLEngineException.class);
        thrown.expectMessage(containsString("Error (no. 202005) processing request"));

        IAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .providerName(spName)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .spType(spType)
                .id(DUMMY_SAML_ID)
                .issuer(DUMMY_ISSUER_URI)
                .citizenCountryCode("ES")
                .nameIdFormat(SamlNameIdFormat.PERSISTENT.toString())
                .build();

        getEngine().generateRequestMessage(request, null);
    }

    /**
     * Test validate authentication request null parameter.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public void testValidateAuthnRequestNullParam() throws EIDASSAMLEngineException {
        thrown.expect(EIDASSAMLEngineException.class);
        thrown.expectMessage(containsString("Saml authentication request is null"));

        getEngine().unmarshallRequestAndValidate(null, "ES"/*,null*/);
    }

    /**
     * Test validate authentication request error bytes encode.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public void testValidateAuthnRequestErrorEncode() throws EIDASSAMLEngineException {
        thrown.expect(EIDASSAMLEngineException.class);
        thrown.expectMessage(containsString("Error (no. 203007) processing request"));

        getEngine().unmarshallRequestAndValidate(EidasStringUtil.getBytes("messageError"), "ES"/*,null*/);
    }

    /**
     * Test validate authentication request.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public void testValidateAuthnRequest() throws EIDASSAMLEngineException {
        IEidasAuthenticationRequest validatedRequest =
                (IEidasAuthenticationRequest) getEngine().unmarshallRequestAndValidate(
                        getDefaultTestEidasAuthnRequestTokenSaml(), "ES"/*,Arrays.asList(DUMMY_ISSUER_URI)*/);
        assertNotNull(validatedRequest.getSpType());
    }

    /**
     * Test validate data authenticate request. Verified parameters after validation.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public void testValidateDataAuthnRequest() throws EIDASSAMLEngineException {

        IAuthenticationRequest request =
                getEngine().unmarshallRequestAndValidate(getDefaultTestEidasAuthnRequestTokenSaml(), "ES"/*,Arrays.asList(DUMMY_ISSUER_URI)*/);

        assertEquals("Sestination incorrect: ", request.getDestination(), destination);

//        assertEquals("CrossBorderShare incorrect: ", request.isEIDCrossBorderShare(), false);
//        assertEquals("CrossSectorShare incorrect: ", request.isEIDCrossSectorShare(), false);
//        assertEquals("SectorShare incorrect: ", request.isEIDSectorShare(), false);

        assertEquals("Service provider incorrect: ", request.getProviderName(), spName);
//        assertEquals("SPInstitution incorrect: ", request.getSpInstitution(), null);
//        assertEquals("SPApplication incorrect: ", request.getSpApplication(), spApplication);
        assertEquals("Asserition consumer URL incorrect: ", request.getAssertionConsumerServiceURL(),
                     assertConsumerUrl);

//        assertEquals("SP Country incorrect: ", request.getSpCountry(), spCountry);
//        assertEquals("SP Id incorrect: ", request.getSPID(), spId);
//        assertEquals("CitizenCountryCode incorrect: ", request.getCitizenCountryCode(), "ES");

    }

    /**
     * Test validate authentication request not trusted token.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public void testValidateAuthnRequestNotTrustedErr1() throws EIDASSAMLEngineException {

        ProtocolEngineI engineNotTrusted =
                ProtocolEngineFactory.createProtocolEngine("CONF2", EidasProtocolProcessor.INSTANCE);

        IAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .providerName(spName)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .spType(spType)
                .requestedAttributes(immutableAttributeMap)
                .id(DUMMY_SAML_ID)
                .issuer(DUMMY_ISSUER_URI)
                .citizenCountryCode("ES")
                .nameIdFormat(SamlNameIdFormat.PERSISTENT.toString())
                .build();
        byte[] authReqNotTrust = engineNotTrusted.generateRequestMessage(request, null).getMessageBytes();

        getEngine().unmarshallRequestAndValidate(authReqNotTrust, "ES"/*,Arrays.asList(DUMMY_ISSUER_URI)*/);

    }

    /**
     * Test validate authentication request trusted.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public void testValidateAuthnRequestTrusted() throws EIDASSAMLEngineException {

        ProtocolEngineI engineTrusted =
                ProtocolEngineFactory.createProtocolEngine("CONF3", EidasProtocolProcessor.INSTANCE);

        IAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .providerName(spName)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .spType(spType)
                .requestedAttributes(immutableAttributeMap)
                .id(DUMMY_SAML_ID)
                .issuer(DUMMY_ISSUER_URI)
                .citizenCountryCode("ES")
                .nameIdFormat(SamlNameIdFormat.PERSISTENT.toString())
                .build();

        byte[] authReqTrust = engineTrusted.generateRequestMessage(request, null).getMessageBytes();

        // engine ("CONF1")  have trust certificate from "CONF3"
        getEngine().unmarshallRequestAndValidate(authReqTrust, "ES"/*,Arrays.asList(DUMMY_ISSUER_URI)*/);

    }

    /**
     * Test generate authentication request service provider application not null.
     */
    @Test
    public void testGenerateAuthnRequestNADA() throws EIDASSAMLEngineException {
        IEidasAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .providerName(spName)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .spType("public")
                .requestedAttributes(immutableAttributeMap)
                .id(DUMMY_SAML_ID)
                .issuer(DUMMY_ISSUER_URI)
                .citizenCountryCode("ES")
                .nameIdFormat(SamlNameIdFormat.PERSISTENT.toString())
                .build();

        authRequest = getEngine().generateRequestMessage(request, null).getMessageBytes();
        getEngine().unmarshallRequestAndValidate(authRequest, "ES"/*,null*/);
        assertNotNull(request.getSpType());
    }

    /**
     * Test generate authentication request service provider application not null.
     */
    @Test
    public void testGenerateAuthnRequestWithVIDPAuthenticationBlockAbsent() throws EIDASSAMLEngineException {
        IEidasAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .providerName(spName)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .requestedAttributes(immutableAttributeMap)
                .id(DUMMY_SAML_ID)
                .issuer(DUMMY_ISSUER_URI)
                .citizenCountryCode("ES")
                .nameIdFormat(SamlNameIdFormat.PERSISTENT.toString())
                .spType("public")
                .build();

        authRequest = getEngine().generateRequestMessage(request, null).getMessageBytes();
        IAuthenticationRequest authenticationRequest = getEngine().unmarshallRequestAndValidate(authRequest, "ES");
        assertEquals("public", authenticationRequest.getSpType());
    }

    /**
     * Test validate authentication request with unknown elements.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public void testValidateAuthnRequestWithUnknownElements() throws EIDASSAMLEngineException {

        AttributeDefinition unknownAttributeDefinition = newEidasAttributeDefinition("unknown", "unknown", true);

        AttributeDefinition eIdentifier =
                newEidasAttributeDefinition("PersonIdentifier", "PersonIdentifier", true, true, false);

        ImmutableAttributeMap attributeMapWithUnknown =
                new ImmutableAttributeMap.Builder().put(unknownAttributeDefinition).put(eIdentifier).build();

        IAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .providerName(spName)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .spType(spType)
                .requestedAttributes(attributeMapWithUnknown)
                .id(DUMMY_SAML_ID)
                .issuer(DUMMY_ISSUER_URI)
                .citizenCountryCode("ES")
                .nameIdFormat(SamlNameIdFormat.PERSISTENT.toString())
                .build();

        IRequestMessage req = getEngine3().generateRequestMessage(request, null);
        byte[] messageBytes = req.getMessageBytes();
        String saml = EidasStringUtil.toString(messageBytes);
        assertFalse(saml.isEmpty());

        IAuthenticationRequest authenticationRequest = getEngine().unmarshallRequestAndValidate(messageBytes, "ES"/*,Arrays.asList(DUMMY_ISSUER_URI)*/);

        assertNull("The value shouldn't exist",
                   authenticationRequest.getRequestedAttributes().getAttributeValuesByNameUri("unknown"));
        assertNotNull("The value should exist", authenticationRequest.getRequestedAttributes()
                .getAttributeValuesByNameUri("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier"));

    }

    /**
     * Test generate Request with required elements by default
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public void testGenerateAuthnRequestWithIsRequiredElementsByDefault() throws EIDASSAMLEngineException {
        AttributeDefinition eIdentifierRequired =
                newEidasAttributeDefinition("PersonIdentifier", "PersonIdentifier", true, true, false);
        AttributeDefinition eIdentifierOptional =
                newEidasAttributeDefinition("PersonIdentifier", "PersonIdentifier", false, true, false);

        ImmutableAttributeMap requestedAttributesWithRequired =
                new ImmutableAttributeMap.Builder().put(eIdentifierRequired).build();

        ImmutableAttributeMap requestedAttributesWithOptional =
                new ImmutableAttributeMap.Builder().put(eIdentifierOptional).build();

        IEidasAuthenticationRequest requestWithRequired =
                new EidasAuthenticationRequest.Builder().destination(destination)
                        .providerName(spName)
                        .levelOfAssurance(LEVEL_OF_ASSURANCE)
                        .assertionConsumerServiceURL(assertConsumerUrl)
                        .spType(spType)
                        .requestedAttributes(requestedAttributesWithRequired)
                        .id(DUMMY_SAML_ID)
                        .issuer(DUMMY_ISSUER_URI)
                        .citizenCountryCode("ES")
                        .nameIdFormat(SamlNameIdFormat.PERSISTENT.toString())
                        .build();

        IAuthenticationRequest requestWithOptional =
                new EidasAuthenticationRequest.Builder(requestWithRequired).requestedAttributes(
                        requestedAttributesWithOptional).build();

        IRequestMessage reqTrue = getEngine().generateRequestMessage(requestWithRequired, null);
        IRequestMessage reqFalse = getEngine2().generateRequestMessage(requestWithOptional, null);
        IRequestMessage req = getEngine3().generateRequestMessage(requestWithRequired, null);

        String token = EidasStringUtil.toString(req.getMessageBytes());
        String reqTrueToken = EidasStringUtil.toString(reqTrue.getMessageBytes());
        String reqFalseToken = EidasStringUtil.toString(reqFalse.getMessageBytes());

        System.out.println();
        System.out.println("token = " + token);
        System.out.println();
        System.out.println("reqTrueToken = " + reqTrueToken);
        System.out.println();
        System.out.println("reqFalseToken = " + reqFalseToken);
        System.out.println();

        assertTrue("The token must contain the chain 'isRequired=\"true\"'", token.contains("isRequired=\"true\""));
        assertTrue("The token must contain the chain 'isRequired=\"true\"'",
                   reqTrueToken.contains("isRequired=\"true\""));
        assertTrue("The token must contain the chain 'isRequired=\"false\"'",
                   reqFalseToken.contains("isRequired=\"false\""));
    }

    /**
     * Return the default authRequest token used in the tests.
     *
     * @return default authRequest token
     * @throws EIDASSAMLEngineException
     */
    private byte[] getDefaultTestEidasAuthnRequestTokenSaml() throws EIDASSAMLEngineException {
        IAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .providerName(spName)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .spType(spType)
                .requestedAttributes(immutableAttributeMap)
                .id(DUMMY_SAML_ID)
                .issuer(DUMMY_ISSUER_URI)
                .citizenCountryCode("ES")
                .nameIdFormat(SamlNameIdFormat.PERSISTENT.toString())
                .build();

        byte saml[] = getEngine().generateRequestMessage(request, null).getMessageBytes();
        String base64SamlXml = EidasStringUtil.toString(saml);
        assertFalse(base64SamlXml.isEmpty());
        return saml;
    }
}
