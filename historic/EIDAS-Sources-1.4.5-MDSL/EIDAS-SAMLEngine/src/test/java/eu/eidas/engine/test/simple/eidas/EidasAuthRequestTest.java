/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.eidas.engine.test.simple.eidas;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.SamlNameIdFormat;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.eidas.EidasProtocolProcessor;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.engine.test.simple.SSETestUtils;

import static eu.eidas.engine.EidasAttributeTestUtil.newAttributeDefinition;
import static eu.eidas.engine.EidasAttributeTestUtil.newEidasAttributeDefinition;
import static eu.eidas.engine.EidasAttributeTestUtil.newStorkAttributeDefinition;
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
                    "saml-engine-additional-attributes-" + conf + ".xml", null, null, null));
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

    private static String LEVEL_OF_ASSURANCE = LevelOfAssurance.LOW.getValue();

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
        IAuthenticationRequest parsedRequest = getEngine().unmarshallRequestAndValidate(samlToken, "ES",Arrays.asList(DUMMY_ISSUER_URI));
        assertNotNull(parsedRequest);
        assertFalse(parsedRequest.getRequestedAttributes().isEmpty());

        LOG.info("EidasAuthenticationRequest 2: " + SSETestUtils.encodeSAMLToken(
                getEngine().generateRequestMessage(request, null).getMessageBytes()));
    }

    /**
     * Test generate authentication request empty personal attribute name error.
     */
    @Test
    public void testGenerateAuthnRequestPALsErr1() {
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

        try {
            getEngine().generateRequestMessage(request, null);
            fail("generateRequestMessage(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error");
        }
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
     * Test generate authentication request error provider name null.
     */
    @Test
    public void testGenerateAuthnRequestSPNAmeErr1() {

        IAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .spType(spType)
                .requestedAttributes(immutableAttributeMap)
                .id(DUMMY_SAML_ID)
                .issuer(DUMMY_ISSUER_URI)
                .citizenCountryCode("ES")
                .nameIdFormat(SamlNameIdFormat.PERSISTENT.toString())
                .build();

        try {
            getEngine().generateRequestMessage(request, null);
            fail("generateRequestMessage(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error");
        }
    }

    /**
     * Test generate authentication request service provider application null.
     */
    @Test
    public void testGenerateAuthnRequestApplicationErr() {

        IAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .providerName(null)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .spType(spType)
                .requestedAttributes(immutableAttributeMap)
                .id(DUMMY_SAML_ID)
                .issuer(DUMMY_ISSUER_URI)
                .citizenCountryCode("ES")
                .nameIdFormat(SamlNameIdFormat.PERSISTENT.toString())
                .build();

        try {
            getEngine().generateRequestMessage(request, null);
            fail("generateRequestMessage(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error: " + e, e);
            e.printStackTrace();
        }
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
    public void testGenerateAuthnRequestLoAErr() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid levelOfAssurance: \"incorrectValue\"");

        IAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .providerName(spName)
                .levelOfAssurance("incorrectValue")
                .assertionConsumerServiceURL(assertConsumerUrl)
                .spType(spType)
                .requestedAttributes(immutableAttributeMap)
                .id(DUMMY_SAML_ID)
                .issuer(DUMMY_ISSUER_URI)
                .citizenCountryCode("ES")
                .nameIdFormat(SamlNameIdFormat.PERSISTENT.toString())
                .build();

        try {
            getEngine().generateRequestMessage(request, null);
            fail("generateRequestMessage(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error");
        }
    }

    /**
     * Test generate authentication request personal attribute list null value.
     */
    @Test
    public void testGenerateAuthnRequestPALErr1() {
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

        try {
            getEngine().generateRequestMessage(request, null);
            fail("generateRequestMessage(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error");
        }
    }

    /**
     * Test validate authentication request null parameter.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public void testValidateAuthnRequestNullParam() throws EIDASSAMLEngineException {
        try {
            getEngine().unmarshallRequestAndValidate(null, "ES",null);
            fail("processValidateRequestToken(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error");
        }
    }

    /**
     * Test validate authentication request error bytes encode.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public void testValidateAuthnRequestErrorEncode() throws EIDASSAMLEngineException {
        try {
            getEngine().unmarshallRequestAndValidate(EidasStringUtil.getBytes("messageError"), "ES",null);
            fail("processValidateRequestToken(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error");
        }
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
                        getDefaultTestEidasAuthnRequestTokenSaml(), "ES",Arrays.asList(DUMMY_ISSUER_URI));
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
                getEngine().unmarshallRequestAndValidate(getDefaultTestEidasAuthnRequestTokenSaml(), "ES",Arrays.asList(DUMMY_ISSUER_URI));

        assertEquals("Sestination incorrect: ", request.getDestination(), destination);

//        assertEquals("CrossBorderShare incorrect: ", request.isEIDCrossBorderShare(), false);
//        assertEquals("CrossSectorShare incorrect: ", request.isEIDCrossSectorShare(), false);
//        assertEquals("SectorShare incorrect: ", request.isEIDSectorShare(), false);

        assertEquals("Service provider incorrect: ", request.getProviderName(), spName);
//        assertEquals("QAAL incorrect: ", request.getQaa(), QAAL);
//        assertEquals("SPInstitution incorrect: ", request.getSpInstitution(), null);
//        assertEquals("SPApplication incorrect: ", request.getSpApplication(), spApplication);
        assertEquals("Asserition consumer URL incorrect: ", request.getAssertionConsumerServiceURL(),
                     assertConsumerUrl);

//        assertEquals("SP Country incorrect: ", request.getSpCountry(), spCountry);
//        assertEquals("SP Id incorrect: ", request.getSPID(), spId);
//        assertEquals("CitizenCountryCode incorrect: ", request.getCitizenCountryCode(), "ES");

    }

    /**
     * Test validate file authentication request. Validate from XML file.
     *
     * @throws Exception the exception
     */
    @Test
    public void testValidateFileAuthnRequest() throws Exception {

        byte[] bytes = SSETestUtils.readSamlFromFile("/data/eu/eidas/EIDASSAMLEngine/AuthnRequest.xml");

        try {
            getEngine().unmarshallRequestAndValidate(bytes, "ES",null);
            fail("testValidateFileAuthnRequest(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error(e.getMessage());
        }
    }

    /**
     * Test validate file authentication request tag delete.
     *
     * @throws Exception the exception
     */
    @Test
    public void testValidateFileAuthnRequestTagDelete() throws Exception {

        byte[] bytes = SSETestUtils.readSamlFromFile("/data/eu/eidas/EIDASSAMLEngine/AuthnRequestTagDelete.xml");

        try {
            getEngine().unmarshallRequestAndValidate(bytes, "ES",null);
            fail("processValidateRequestToken(...) should have thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error(e.getMessage());

        }
    }

    /**
     * Test validate authentication request not trusted token.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public void testValidateAuthnRequestNotTrustedErr1() throws EIDASSAMLEngineException {

        try {
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

            getEngine().unmarshallRequestAndValidate(authReqNotTrust, "ES",Arrays.asList(DUMMY_ISSUER_URI));
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error");
            fail("validateEIDASAuthnRequestNotTrusted(...) should not have thrown an EIDASSAMLEngineException!");
        }
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
        getEngine().unmarshallRequestAndValidate(authReqTrust, "ES",Arrays.asList(DUMMY_ISSUER_URI));

    }

    /**
     * Test generate authentication request service provider application not null.
     */
    @Test
    public void testGenerateAuthnRequestNADA() {
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

        try {
            authRequest = getEngine().generateRequestMessage(request, null).getMessageBytes();
            getEngine().unmarshallRequestAndValidate(authRequest, "ES",null);
            assertNotNull(request.getSpType());
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error");
        }
    }

    /**
     * Test generate authentication request service provider application not null.
     */
    @Test
    public void testGenerateAuthnRequestWithVIDPAuthenticationBlockAbsent() {
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

        try {
            authRequest = getEngine().generateRequestMessage(request, null).getMessageBytes();
            IAuthenticationRequest authenticationRequest = getEngine().unmarshallRequestAndValidate(authRequest, "ES",null);
            assertEquals("public", ((IEidasAuthenticationRequest) authenticationRequest).getSpType());
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error");
        }
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

        IAuthenticationRequest authenticationRequest = getEngine().unmarshallRequestAndValidate(messageBytes, "ES",Arrays.asList(DUMMY_ISSUER_URI));

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
     * Test cross validation: a request in EIDAS format validated against an eidas engine
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public void testCrossValidation() throws Exception {

        AttributeDefinition<String> eIdentifier = newStorkAttributeDefinition("eIdentifier", true);
        AttributeDefinition<String> isAgeOver = newStorkAttributeDefinition("isAgeOver", true);

        ImmutableAttributeMap attributeMapWithMandatoryDataset = new ImmutableAttributeMap.Builder().put(eIdentifier)
                .put(isAgeOver, new StringAttributeValue("16", false), new StringAttributeValue("18", false))
                .build();
        IAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .providerName(spName)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .spType(spType)
                .requestedAttributes(attributeMapWithMandatoryDataset)
                .id(DUMMY_SAML_ID)
                .issuer(DUMMY_ISSUER_URI)
                .citizenCountryCode("ES")
                .nameIdFormat(SamlNameIdFormat.PERSISTENT.toString())
                .build();
        //prepare request in STORK format
        ProtocolEngineI storkEngine = getEngine4();
        IRequestMessage req = storkEngine.generateRequestMessage(request, null);

        //validate request in a EIDAS enabled samlengine
        IAuthenticationRequest authenticationRequest =
                getEngine().unmarshallRequestAndValidate(req.getMessageBytes(), "ES",Arrays.asList(DUMMY_ISSUER_URI));
        assertNotNull(authenticationRequest);
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
