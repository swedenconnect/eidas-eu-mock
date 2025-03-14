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
package eu.eidas.engine.test.simple;

import eu.eidas.RecommendedSecurityProviders;
import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.spec.NaturalPersonSpec;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.eidas.EidasProtocolProcessor;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static eu.eidas.auth.engine.core.eidas.TestMetadataFetcherToken.CONNECTOR_METADATA_URL;
import static eu.eidas.auth.engine.core.eidas.TestMetadataFetcherToken.PROXY_METADATA_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * The Class AuthRequestTest.
 */
public class AuthResponseTest {

    /**
     * The engine.
     */
    private static ProtocolEngineI engine = null;

    static {
        engine = ProtocolEngineFactory.getDefaultProtocolEngine("SkewTest");
    }

    /**
     * Gets the engine.
     *
     * @return the engine
     */
    public static ProtocolEngineI getEngine() {
        return engine;
    }

    /**
     * Sets the engine.
     *
     * @param newEngine the new engine
     */
    public static void setEngine(final ProtocolEngineI newEngine) {
        AuthResponseTest.engine = newEngine;
    }

    /**
     * The destination.
     */
    private static String destination;

    /**
     * The service provider name.
     */
    private static String spName;

    /**
     * The service provider sector.
     */
    private static String spSector;

    /**
     * The service provider institution.
     */
    private static String spInstitution;

    /**
     * The service provider application.
     */
    private static String spApplication;

    /**
     * The service provider country.
     */
    private static String spCountry;

    /**
     * The service provider id.
     */
    private static String spId;

    /**
     * The state.
     */
    private static String state = "ES";

    /**
     * The town.
     */
    private static String town = "Madrid";

    /**
     * The municipality code.
     */
    private static String municipalityCode = "MA001";

    /**
     * The postal code.
     */
    private static String postalCode = "28038";

    /**
     * The street name.
     */
    private static String streetName = "Marchamalo";

    /**
     * The street number.
     */
    private static String streetNumber = "3";

    /**
     * The apartament number.
     */
    private static String apartamentNumber = "5\u00BA E";

    /**
     * The Map of Personal Attributes.
     */
    private static ImmutableAttributeMap attributeMap;

    /**
     * The assertion consumer URL.
     */
    private static String assertConsumerUrl;

    /**
     * The authentication request.
     */
    private static byte[] authRequest;

    /**
     * The authentication response.
     */
    private static byte[] authResponse;

    /**
     * The authentication request.
     */
    private static IEidasAuthenticationRequest authenticationRequest;

    /**
     * The authentication response.
     */
    private static IAuthenticationResponse authnResponse;

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AuthResponseTest.class.getName());

    /**
     * The IP address.
     */
    private static String ipAddress;

    /**
     * The ERROR text.
     */
    private static final String ERROR_TXT = "generateAuthnResponse(...) should've thrown an EIDASSAMLEngineException!";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void setupClass() {
        RecommendedSecurityProviders.setupRecommendedSecurityProviders();
    }



    @Before
    public void setup() throws EIDASSAMLEngineException {

        String destination = "http://proxyservice.gov.xx/EidasNode/ColleagueRequest";
        String assertConsumerUrl = "http://connector.gov.xx/EidasNode/ColleagueResponse";

        String spName = "University of Oxford";
        String spCountry = "EN";

        EidasAuthenticationRequest request = EidasAuthenticationRequest.builder()
                .id("QDS2QFD") // Common part
                .assertionConsumerServiceURL(assertConsumerUrl)
                .destination(destination)
                .issuer(CONNECTOR_METADATA_URL)
                .providerName(spName)
                .serviceProviderCountryCode(spCountry)
                .citizenCountryCode("ES")
                .requestedAttributes(newResponseImmutableAttributeMap())
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH)
                .build();

        try {
            byte[] rawAuthRequest = engine.generateRequestMessage(request, PROXY_METADATA_URL).getMessageBytes();
            authenticationRequest = (IEidasAuthenticationRequest) engine.unmarshallRequestAndValidate(rawAuthRequest, "ES");

        } catch (EIDASSAMLEngineException e) {
            fail("Error create EidasAuthenticationRequest");
        }

        String ipAddress = "111.222.333.444";


        ImmutableAttributeMap.Builder builder = ImmutableAttributeMap.builder();

        builder.put(NaturalPersonSpec.Definitions.PERSON_IDENTIFIER, "john");
        builder.put(NaturalPersonSpec.Definitions.CURRENT_FAMILY_NAME, "john2");
        builder.put(NaturalPersonSpec.Definitions.CURRENT_GIVEN_NAME, "john3");
        builder.put(NaturalPersonSpec.Definitions.DATE_OF_BIRTH);
        builder.put(NaturalPersonSpec.Definitions.PLACE_OF_BIRTH, "Brussels");
        builder.put(NaturalPersonSpec.Definitions.BIRTH_NAME, "john4");
        builder.put(NaturalPersonSpec.Definitions.CURRENT_ADDRESS, getAddressValue());
        builder.put(NaturalPersonSpec.Definitions.GENDER, "Male");

        attributeMap = builder.build();

        // default value for authResponse to keep tests seperate
        // override authResponse where needed
        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(attributeMap)
                .id("963158")
                .inResponseTo(authenticationRequest.getId())
                .issuer(PROXY_METADATA_URL)
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .ipAddress("123.123.123.123")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();

        IResponseMessage responseMessage = getEngine().generateResponseMessage(authenticationRequest, response, ipAddress);

        authResponse = responseMessage.getMessageBytes();

    }

    @SuppressWarnings({"PublicField"})

    @Test
    public final void testGenerateAuthnResponse() throws EIDASSAMLEngineException {
        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(attributeMap)
                .id("963158")
                .inResponseTo(authenticationRequest.getId())
                .issuer(PROXY_METADATA_URL)
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .ipAddress("123.123.123.123")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();

        IResponseMessage responseMessage = getEngine().generateResponseMessage(authenticationRequest, response, ipAddress);

        authResponse = responseMessage.getMessageBytes();
        String result = EidasStringUtil.toString(authResponse);
        LOG.info("RESPONSE: " + SSETestUtils.encodeSAMLToken(authResponse));
        LOG.info("RESPONSE as string: " + result);
    }

    /**
     * Test validation id parameter mandatory.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testResponseMandatoryId() throws Exception {
        IEidasAuthenticationRequest requestWithoutSamlId =
                EidasAuthenticationRequest.builder(authenticationRequest).id(null).build();
        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(attributeMap)
                .id("963158")
                .inResponseTo(authenticationRequest.getId())
                .issuer(PROXY_METADATA_URL)
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .ipAddress("123.123.123.123")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();
        getEngine().generateResponseMessage(requestWithoutSamlId, response, ipAddress);
    }

    /**
     * Test generate authentication response in response to err1.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testResponseMandatoryIssuer() throws Exception {
        IEidasAuthenticationRequest requestWithoutIssuer =
                EidasAuthenticationRequest.builder(authenticationRequest).issuer(null).build();
        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(attributeMap)
                .id("963158")
                .inResponseTo(authenticationRequest.getId())
                .issuer(PROXY_METADATA_URL)
                .ipAddress("123.123.123.123")
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();
        getEngine().generateResponseMessage(requestWithoutIssuer, response, ipAddress);
    }

    /**
     * Test generate authentication response assertion consumer null.
     */
    @Test
    public final void testResponseMandatoryAssertionConsumerServiceURL() throws Exception {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        IEidasAuthenticationRequest request =
                EidasAuthenticationRequest.builder(authenticationRequest).assertionConsumerServiceURL(null).build();

        assertNull(request.getAssertionConsumerServiceURL());

        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(attributeMap)
                .id("963158")
                .inResponseTo(authenticationRequest.getId())
                .issuer(PROXY_METADATA_URL)
                .ipAddress("123.123.123.123")
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();

        getEngine().generateResponseMessage(request, response, ipAddress);
    }

    /**
     * Test generate authentication response IP address null.
     */
    @Test
    @Ignore
    public final void testResponseValidationIP() {
        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(attributeMap)
                .id("963158")
                .inResponseTo(authenticationRequest.getId())
                .issuer(PROXY_METADATA_URL)
                .ipAddress("123.123.123.123")
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();

        try {
            getEngine().generateResponseMessage(authenticationRequest, response, null);
            fail("generateAuthnResponse(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error: " + e, e);
        }
    }

    /**
     * Test generate authentication response with personal attribute list null.
     */
    @Test
    public final void testResponseMandatoryPersonalAttributeList() {
        AuthenticationResponse response = AuthenticationResponse.builder()
                .id("789")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .inResponseTo("456")
                .issuer(PROXY_METADATA_URL)
                .ipAddress("123.123.123.123")
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .levelOfAssurance("high")
                .build();

        try {
            getEngine().generateResponseMessage(authenticationRequest, response, ipAddress);
            fail("generateAuthnResponse(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error: " + e, e);
        }
    }

    /**
     * Test validate authentication response token null.
     */
    @Test
    public final void testResponseInvalidParametersToken() {
        try {
            getEngine().unmarshallResponseAndValidate(null, ipAddress, 0, 0, null);
            fail(ERROR_TXT);
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error: " + e, e);
        }
    }

    /**
     * Test validate authentication response IP null.
     */
    @Test
    public final void testResponseInvalidParametersIP() {
        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(attributeMap)
                .id("963158")
                .inResponseTo(authenticationRequest.getId())
                .issuer(PROXY_METADATA_URL)
                .ipAddress("123.123.123.123")
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();
        try {
            authResponse = getEngine().generateResponseMessage(authenticationRequest, response, ipAddress).getMessageBytes();
            // In Conf1 ipValidate is false
            getEngine().unmarshallResponseAndValidate(authResponse, null, 0, 0, null);
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error: " + e, e);
        }
    }

    /**
     * Test validate authentication response parameter name wrong.
     */
    @Test
    public final void testResponseInvalidParametersAttr() {
        ImmutableAttributeMap attributeMap = ImmutableAttributeMap.builder()
                .put(new AttributeDefinition.Builder<String>().nameUri("urn:example.com/AttrWrong")
                        .friendlyName("AttrWrong")
                        .personType(PersonType.NATURAL_PERSON)
                        .xmlType("urn:example.com", "AttrWrongType", "wrong")
                        .attributeValueMarshaller(new StringAttributeValueMarshaller())
                        .build())
                .build();

        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(attributeMap)
                .id("963158")
                .inResponseTo(authenticationRequest.getId())
                .issuer(PROXY_METADATA_URL)
                .ipAddress("123.123.123.123")
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();

        try {
            authResponse = getEngine().generateResponseMessage(authenticationRequest, response, ipAddress).getMessageBytes();
            // In Conf1 ipValidate is false
            getEngine().unmarshallResponseAndValidate(authResponse, null, 0, 0, null);
            fail("generateResponseMessage(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException expected) {
            // expected
        }
    }

    /**
     * Test validate authentication response set null value into attribute.
     */
    @Test
    public final void testResponseInvalidParametersAttrSimpleValue() {
        ImmutableAttributeMap.Builder wrongList = ImmutableAttributeMap.builder();

        EidasProtocolProcessor.INSTANCE.getClass();
        AttributeDefinition birthName = NaturalPersonSpec.Definitions.BIRTH_NAME;

        wrongList.put(birthName, "");

        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(wrongList.build())
                .id("963158")
                .inResponseTo(authenticationRequest.getId())
                .issuer(PROXY_METADATA_URL)
                .ipAddress("123.123.123.123")
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();

        try {
            authResponse = getEngine().generateResponseMessage(authenticationRequest, response, ipAddress).getMessageBytes();
            // In Conf1 ipValidate is false
            getEngine().unmarshallResponseAndValidate(authResponse, null, 0, 0, null);
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error " + e, e);
        }
    }

    /**
     * Test validate authentication response set null value into attribute.
     */
    @Test
    public final void testResponseInvalidParametersAttrNoValue() {
        ImmutableAttributeMap.Builder wrongList = ImmutableAttributeMap.builder();

        EidasProtocolProcessor.INSTANCE.getClass();
        AttributeDefinition gender = NaturalPersonSpec.Definitions.GENDER;

        wrongList.put(gender);

        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(wrongList.build())
                .id("963158")
                .inResponseTo(authenticationRequest.getId())
                .issuer(PROXY_METADATA_URL)
                .ipAddress("123.123.123.123")
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();

        try {
            authResponse = getEngine().generateResponseMessage(authenticationRequest, response, ipAddress).getMessageBytes();
            // In Conf1 ipValidate is false
            getEngine().unmarshallResponseAndValidate(authResponse, null, 0, 0, null);
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error " + e, e);
        }
    }

    /**
     * Test validate authentication response set null value into attribute.
     */
    @Test
    public final void testResponseInvalidParametersAttrNoName() {
        ImmutableAttributeMap.Builder wrongList = ImmutableAttributeMap.builder();

        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(wrongList.build())
                .id("963158")
                .inResponseTo(authenticationRequest.getId())
                .issuer(PROXY_METADATA_URL)
                .ipAddress("123.123.123.123")
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();

        try {
            authResponse = getEngine().generateResponseMessage(authenticationRequest, response, ipAddress).getMessageBytes();
            // In Conf1 ipValidate is false
            getEngine().unmarshallResponseAndValidate(authResponse, null, 0, 0, null);
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error " + e, e);
        }
    }

    /**
     * Test validate authentication response IP distinct and disabled validation IP.
     */
    @Test
    public final void testResponseInvalidParametersIPDistinct() {

        try {
            // ipAddress origin "111.222.33.44"
            // ipAddrValidation = false
            // Subject Confirmation Bearer.

            getEngine().unmarshallResponseAndValidate(authResponse, "127.0.0.1", 0, 0, null);
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error: " + e, e);
            fail("validateAuthenticationResponse(...) should've thrown an EIDASSAMLEngineException!: " + e);
        }
    }

    /**
     * Test response invalid parameters invalid token.
     */
    @Test
    public final void testResponseInvalidParametersTokenMsg() {
        try {
            // ipAddress origin "111.222.333.444"
            // Subject Confirmation Bearer.
            getEngine().unmarshallResponseAndValidate(EidasStringUtil.getBytes("errorMessage"), ipAddress, 0, 0, null);
            fail("validateAuthenticationResponse(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error: " + e, e);
        }
    }

    /**
     * Test validate authentication response is fail.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testValidateAuthenticationResponseIsFail() throws EIDASSAMLEngineException {
        testGenerateAuthnResponse();//prepare valid authnResponse
        authnResponse = getEngine().unmarshallResponseAndValidate(authResponse, ipAddress, 0, 0, null);
        assertFalse("Generate incorrect response: ", authnResponse.isFailure());
    }

    /**
     * Test validate authentication response destination.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testValidateAuthenticationResponseDestination() throws EIDASSAMLEngineException {
        authnResponse = getEngine().unmarshallResponseAndValidate(authResponse, ipAddress, 0, 0, null);

        assertEquals("Destination incorrect: ", authnResponse.getInResponseToId(), authenticationRequest.getId());
    }

    /**
     * Test validate authentication response values.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    public final void testValidateAuthenticationResponseValuesComplex() throws EIDASSAMLEngineException {
        authnResponse = getEngine().unmarshallResponseAndValidate(authResponse, ipAddress, 0, 0, null);

        assertEquals("Country incorrect:", authnResponse.getCountry(), "EN");

        for (final Map.Entry<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> entry : authnResponse.getAttributes()
                .getAttributeMap()
                .entrySet()) {

            AttributeDefinition<?> attributeDefinition = entry.getKey();
            Set<? extends AttributeValue<?>> values = entry.getValue();

            if ("canonicalResidenceAddress".equalsIgnoreCase(attributeDefinition.getFriendlyName())) {
                String value = (String) values.iterator().next().getValue();

                assertEquals("Incorrect eIDAS address: ", getAddressValue(), value);
            }
        }
    }

    /**
     * Test generate authenticate response fail in response to it's null.
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testGenerateAuthnResponseFailInResponseToNull() {
        IEidasAuthenticationRequest request = EidasAuthenticationRequest.builder(authenticationRequest).id(null).build();

        AuthenticationResponse.Builder response = new AuthenticationResponse.Builder();
        response.statusCode(EIDASStatusCode.REQUESTER_URI.toString());
        response.subStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString());
        response.statusMessage("");
        response.id("963158");
        response.inResponseTo(authenticationRequest.getId());
        response.issuer(PROXY_METADATA_URL);
        response.subject("UK/UK/Banksy");
        response.subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
        response.ipAddress("123.123.123.123");
        response.levelOfAssurance("high");
        response.statusCode(EIDASStatusCode.SUCCESS_URI.toString());

        try {
            authResponse =
                    getEngine().generateResponseErrorMessage(request, response.build(), ipAddress).getMessageBytes();
            fail(ERROR_TXT);
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error: " + e, e);
        }
    }

    /**
     * Test generate authenticate response fail assertion consumer URL err1.
     *
     */
    @Test
    public final void testGenerateAuthnResponseFailAssertionConsumerUrlNull() {
        IAuthenticationRequest request =
                EidasAuthenticationRequest.builder(authenticationRequest)
                        .assertionConsumerServiceURL(null).build();

        AuthenticationResponse.Builder response = new AuthenticationResponse.Builder();
        response.statusCode(EIDASStatusCode.REQUESTER_URI.toString());
        response.subStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString());
        response.statusMessage("");
        response.id("963158");
        response.inResponseTo(authenticationRequest.getId());
        response.issuer(PROXY_METADATA_URL);
        response.ipAddress("123.123.123.123");
        response.subject("UK/UK/Banksy");
        response.subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
        response.levelOfAssurance("high");
        response.statusCode(EIDASStatusCode.SUCCESS_URI.toString());

        try {
            authResponse =
                    getEngine().generateResponseErrorMessage(request, response.build(), ipAddress).getMessageBytes();
            fail("generateAuthnResponseFail(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error: " + e, e);
        }
    }

    /**
     * Test generate authentication response fail code error err1.
     */
    @Test
    public final void testGenerateAuthnResponseFailCodeErrorNull() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("statusCode cannot be null, empty or blank");

        AuthenticationResponse.Builder response = new AuthenticationResponse.Builder();
        response.statusCode(null);
        response.subStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString());
        response.statusMessage("Error message");
        response.id("963158");
        response.inResponseTo(authenticationRequest.getId());
        response.issuer(PROXY_METADATA_URL);
        response.ipAddress("123.123.123.123");
        response.subject("UK/UK/Banksy");
        response.subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
        response.levelOfAssurance("high");

        authResponse =
                getEngine().generateResponseErrorMessage(authenticationRequest, response.build(), ipAddress).getMessageBytes();
    }

    /**
     * Test generate authentication request without errors.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testValidateAuthnResponse() throws EIDASSAMLEngineException {
        AuthenticationResponse.Builder response = new AuthenticationResponse.Builder();
        response.attributes(newResponseImmutableAttributeMap());
        response.id("963158");
        response.inResponseTo(authenticationRequest.getId());
        response.issuer(PROXY_METADATA_URL);
        response.ipAddress("123.123.123.123");
        response.levelOfAssurance("high");
        response.subject("UK/UK/Banksy");
        response.subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
        response.statusCode(EIDASStatusCode.SUCCESS_URI.toString());

        IResponseMessage responseMessage =
                getEngine().generateResponseMessage(authenticationRequest, response.build(), ipAddress);

        authResponse = responseMessage.getMessageBytes();
        LOG.info("Request id: " + authenticationRequest.getId());

        LOG.info("RESPONSE: " + SSETestUtils.encodeSAMLToken(authResponse));

        authnResponse = getEngine().unmarshallResponseAndValidate(authResponse, ipAddress, 0, 0, null);

        LOG.info("RESPONSE ID: " + authnResponse.getId());
        LOG.info("RESPONSE IN_RESPONSE_TO: " + authnResponse.getInResponseToId());
        LOG.info("RESPONSE COUNTRY: " + authnResponse.getCountry());
    }

    /**
     * Test validate authentication response fail is fail.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testValidateAuthenticationResponseFailIsFail() throws EIDASSAMLEngineException {
        AuthenticationResponse.Builder response = new AuthenticationResponse.Builder();
        response.statusCode(EIDASStatusCode.REQUESTER_URI.toString());
        response.failure(true);
        response.subStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString());
        response.statusMessage("message");
        //response.subject("UK/UK/Banksy");
        //response.subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
        response.id("963158");
        response.inResponseTo(authenticationRequest.getId());
        response.issuer(PROXY_METADATA_URL);
        response.ipAddress("123.123.123.123");
        response.levelOfAssurance("high");
        authResponse =
                getEngine().generateResponseErrorMessage(authenticationRequest, response.build(), ipAddress).getMessageBytes();

        LOG.error("ERROR_FAIL: " + EidasStringUtil.encodeToBase64(authResponse));

        authnResponse = getEngine().unmarshallResponseAndValidate(authResponse, ipAddress, 0, 0, null);

        LOG.info("COUNTRY: " + authnResponse.getCountry());
        assertTrue("Generate incorrect response: ", authnResponse.isFailure());
    }

    private static ImmutableAttributeMap newResponseImmutableAttributeMap() {
        ImmutableAttributeMap.Builder builder = ImmutableAttributeMap.builder();

        builder.put(NaturalPersonSpec.Definitions.PERSON_IDENTIFIER, "john");
        builder.put(NaturalPersonSpec.Definitions.CURRENT_FAMILY_NAME, "john2");
        builder.put(NaturalPersonSpec.Definitions.CURRENT_GIVEN_NAME, "john3");
        builder.put(NaturalPersonSpec.Definitions.DATE_OF_BIRTH);
        builder.put(NaturalPersonSpec.Definitions.PLACE_OF_BIRTH, "Brussels");
        builder.put(NaturalPersonSpec.Definitions.BIRTH_NAME, "john4");
        builder.put(NaturalPersonSpec.Definitions.CURRENT_ADDRESS, "john");
        builder.put(NaturalPersonSpec.Definitions.GENDER, "Male");

        return builder.build();
    }

    private static String getAddressValue() {
        Map<String, String> address = new LinkedHashMap<String, String>();
        address.put("state", state);
        address.put("municipalityCode", municipalityCode);
        address.put("town", town);
        address.put("postalCode", postalCode);
        address.put("streetName", streetName);
        address.put("streetNumber", streetNumber);
        address.put("apartamentNumber", apartamentNumber);
        return EidasStringUtil.encodeToBase64(address.toString());
    }
}
