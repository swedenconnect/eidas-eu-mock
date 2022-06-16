/*
 * Copyright (c) 2021 by European Commission
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

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
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
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.eidas.EidasProtocolProcessor;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Provider;
import java.security.Security;
import java.util.LinkedHashMap;
import java.util.Map;

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
    private static IEidasAuthenticationRequest authenRequest;

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

    static String ISSUER = "https://testIssuer".toLowerCase();

    /**
     * Test generate authentication request without errors.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    final static String RESPONSE_ISSUER = "http://response.issuer".toLowerCase();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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

    @Before
    public void setup() throws EIDASSAMLEngineException {

        String destination = "http://proxyservice.gov.xx/EidasNode/ColleagueRequest";
        String assertConsumerUrl = "http://connector.gov.xx/EidasNode/ColleagueResponse";

        String spName = "University of Oxford";
        String spSector = "EDU001";
        String spInstitution = "OXF001";
        String spApplication = "APP001";
        String spCountry = "EN";

        String spId = "EDU001-APP001-APP001";

        EidasAuthenticationRequest request = EidasAuthenticationRequest.builder()
                .id("QDS2QFD") // Common part
                .assertionConsumerServiceURL(assertConsumerUrl)
                .destination(destination)
                .issuer(ISSUER)
                .providerName(spName)
                .serviceProviderCountryCode(spCountry)
                .citizenCountryCode("ES")
                .requestedAttributes(newResponseImmutableAttributeMap())
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH)
                .build();

        byte[] authRequest;
        //IAuthenticationRequest authenRequest = null;

        try {
            authRequest = engine.generateRequestMessage(request, null).getMessageBytes();

            authenRequest = (IEidasAuthenticationRequest) engine.unmarshallRequestAndValidate(authRequest, "ES");

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
                .inResponseTo(authenRequest.getId())
                .issuer(RESPONSE_ISSUER)
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .ipAddress("123.123.123.123")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();

        IResponseMessage responseMessage = getEngine().generateResponseMessage(authenRequest, response, ipAddress);

        authResponse = responseMessage.getMessageBytes();

    }

    @SuppressWarnings({"PublicField"})

    @Test
    public final void testGenerateAuthnResponse() throws EIDASSAMLEngineException {

        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(attributeMap)
                .id("963158")
                .inResponseTo(authenRequest.getId())
                .issuer(RESPONSE_ISSUER)
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .ipAddress("123.123.123.123")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();

        IResponseMessage responseMessage = getEngine().generateResponseMessage(authenRequest, response, ipAddress);

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
                EidasAuthenticationRequest.builder(authenRequest).id(null).build();
        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(attributeMap)
                .id("963158")
                .inResponseTo(authenRequest.getId())
                .issuer("http://response.issuer")
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
                EidasAuthenticationRequest.builder(authenRequest).issuer(null).build();
        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(attributeMap)
                .id("963158")
                .inResponseTo(authenRequest.getId())
                .issuer("http://response.issuer")
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

        thrown.expect(EIDASSAMLEngineException.class);
        thrown.expectMessage(
                "Error (no. message.validation.error.code) processing request : message.validation.error.code - Request AssertionConsumerServiceURL must not be blank.");

        IEidasAuthenticationRequest request =
                EidasAuthenticationRequest.builder(authenRequest).assertionConsumerServiceURL(null).build();

        assertNull(request.getAssertionConsumerServiceURL());

        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(attributeMap)
                .id("963158")
                .inResponseTo(authenRequest.getId())
                .issuer("http://response.issuer")
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
                .inResponseTo(authenRequest.getId())
                .issuer("http://response.issuer")
                .ipAddress("123.123.123.123")
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();

        try {
            getEngine().generateResponseMessage(authenRequest, response, null);
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
                .issuer("http://response.issuer")
                .ipAddress("123.123.123.123")
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .levelOfAssurance("high")
                .build();

        try {
            getEngine().generateResponseMessage(authenRequest, response, ipAddress);
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
                .inResponseTo(authenRequest.getId())
                .issuer("http://response.issuer")
                .ipAddress("123.123.123.123")
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();
        try {
            authResponse = getEngine().generateResponseMessage(authenRequest, response, ipAddress).getMessageBytes();
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
                .inResponseTo(authenRequest.getId())
                .issuer("http://response.issuer")
                .ipAddress("123.123.123.123")
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();

        try {
            authResponse = getEngine().generateResponseMessage(authenRequest, response, ipAddress).getMessageBytes();
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
    public final void testResponseInvalidParametersAttrSimpleValue() throws Exception {
        ImmutableAttributeMap.Builder wrongList = ImmutableAttributeMap.builder();

        EidasProtocolProcessor.INSTANCE.getClass();
        AttributeDefinition birthName =  NaturalPersonSpec.Definitions.BIRTH_NAME;

        wrongList.put(birthName, "");

        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(wrongList.build())
                .id("963158")
                .inResponseTo(authenRequest.getId())
                .issuer("http://response.issuer")
                .ipAddress("123.123.123.123")
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();

        try {
            authResponse = getEngine().generateResponseMessage(authenRequest, response, ipAddress).getMessageBytes();
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
    public final void testResponseInvalidParametersAttrNoValue() throws Exception {
        ImmutableAttributeMap.Builder wrongList = ImmutableAttributeMap.builder();

        EidasProtocolProcessor.INSTANCE.getClass();
        AttributeDefinition gender =  NaturalPersonSpec.Definitions.GENDER;

        wrongList.put(gender);

        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(wrongList.build())
                .id("963158")
                .inResponseTo(authenRequest.getId())
                .issuer("http://response.issuer")
                .ipAddress("123.123.123.123")
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();

        try {
            authResponse = getEngine().generateResponseMessage(authenRequest, response, ipAddress).getMessageBytes();
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
    public final void testResponseInvalidParametersAttrNoName() throws Exception {
        ImmutableAttributeMap.Builder wrongList = ImmutableAttributeMap.builder();

        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(wrongList.build())
                .id("963158")
                .inResponseTo(authenRequest.getId())
                .issuer("http://response.issuer")
                .ipAddress("123.123.123.123")
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent")
                .levelOfAssurance("high")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();

        try {
            authResponse = getEngine().generateResponseMessage(authenRequest, response, ipAddress).getMessageBytes();
            // In Conf1 ipValidate is false
            getEngine().unmarshallResponseAndValidate(authResponse, null, 0, 0,null);
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

        assertEquals("Destination incorrect: ", authnResponse.getInResponseToId(), authenRequest.getId());
    }

    /**
     * Test validate authentication response values.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    public final void testValidateAuthenticationResponseValuesComplex() throws EIDASSAMLEngineException {
        authnResponse = getEngine().unmarshallResponseAndValidate(authResponse, ipAddress, 0, 0, null);

        assertEquals("Country incorrect:", authnResponse.getCountry(), "EN");

        for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> entry : authnResponse.getAttributes()
                .getAttributeMap()
                .entrySet()) {

            AttributeDefinition<?> attributeDefinition = entry.getKey();
            ImmutableSet<? extends AttributeValue<?>> values = entry.getValue();

            if ("canonicalResidenceAddress".equalsIgnoreCase(attributeDefinition.getFriendlyName())) {
                String value = (String) values.iterator().next().getValue();

                assertEquals("Incorrect eIDAS address: ", getAddressValue(), value);
            }
        }
    }

    /**
     * Test generate authenticate response fail in response to it's null.
     *
     * @throws EIDASSAMLEngineException
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testGenerateAuthnResponseFailInResponseToNull() throws EIDASSAMLEngineException {
        IEidasAuthenticationRequest request = EidasAuthenticationRequest.builder(authenRequest).id(null).build();

        AuthenticationResponse.Builder response = new AuthenticationResponse.Builder();
        response.statusCode(EIDASStatusCode.REQUESTER_URI.toString());
        response.subStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString());
        response.statusMessage("");
        response.id("963158");
        response.inResponseTo(authenRequest.getId());
        response.issuer("http://response.issuer");
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
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testGenerateAuthnResponseFailAssertionConsumerUrlNull() throws EIDASSAMLEngineException {

        IAuthenticationRequest request =
                EidasAuthenticationRequest.builder(authenRequest)
                        .assertionConsumerServiceURL(null).build();

        AuthenticationResponse.Builder response = new AuthenticationResponse.Builder();
        response.statusCode(EIDASStatusCode.REQUESTER_URI.toString());
        response.subStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString());
        response.statusMessage("");
        response.id("963158");
        response.inResponseTo(authenRequest.getId());
        response.issuer("http://response.issuer");
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

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("statusCode cannot be null, empty or blank");

        AuthenticationResponse.Builder response = new AuthenticationResponse.Builder();
        response.statusCode(null);
        response.subStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString());
        response.statusMessage("Error message");
        response.id("963158");
        response.inResponseTo(authenRequest.getId());
        response.issuer("http://response.issuer");
        response.ipAddress("123.123.123.123");
        response.subject("UK/UK/Banksy");
        response.subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
        response.levelOfAssurance("high");

        authResponse =
                getEngine().generateResponseErrorMessage(authenRequest, response.build(), ipAddress).getMessageBytes();
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
        response.inResponseTo(authenRequest.getId());
        response.issuer("http://response.issuer");
        response.ipAddress("123.123.123.123");
        response.levelOfAssurance("high");
        response.subject("UK/UK/Banksy");
        response.subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
        response.statusCode(EIDASStatusCode.SUCCESS_URI.toString());

        IResponseMessage responseMessage =
                getEngine().generateResponseMessage(authenRequest, response.build(), ipAddress);

        authResponse = responseMessage.getMessageBytes();
        LOG.info("Request id: " + authenRequest.getId());

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
        response.inResponseTo(authenRequest.getId());
        response.issuer("http://response.issuer");
        response.ipAddress("123.123.123.123");
        response.levelOfAssurance("high");
        authResponse =
                getEngine().generateResponseErrorMessage(authenRequest, response.build(), ipAddress).getMessageBytes();

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
