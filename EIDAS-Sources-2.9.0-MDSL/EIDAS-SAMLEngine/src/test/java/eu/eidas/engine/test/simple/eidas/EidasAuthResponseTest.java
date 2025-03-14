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
package eu.eidas.engine.test.simple.eidas;

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
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.PostalAddress;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.protocol.impl.SamlNameIdFormat;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.eidas.EidasProtocolProcessor;
import eu.eidas.auth.engine.metadata.FakeMetadata;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.engine.test.simple.SSETestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static eu.eidas.engine.EidasAttributeTestUtil.newEidasAttributeDefinition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;

/**
 * The Class EidasResponseTest.
 */
public class EidasAuthResponseTest {

    private static final String EIDAS_ADDRESS = getEidasAddress();
    final static String RESPONSE_ISSUER = "http://Responder".toLowerCase();

    final MetadataFetcherI metadataFetcher = Mockito.mock(MetadataFetcherI.class);
    /**
     * The engine.
     */
    private ProtocolEngineI engine = getEngine("CONF1");

    ProtocolEngineI getEngine(String conf) {
        ProtocolEngineI engine = null;
        try {
            engine = ProtocolEngineFactory.createProtocolEngine(conf, new EidasProtocolProcessor(
                    "saml-engine-eidas-attributes-" + conf + ".xml",
                    "saml-engine-additional-attributes-" + conf + ".xml",
                    null,
                    metadataFetcher,
                    null,
                    null
            ));
        } catch (EIDASSAMLEngineException exc) {
            fail("Failed to initialize SAMLEngines");
        }
        return engine;
    }

    /**
     * Gets the engine.
     *
     * @return the engine
     */
    public ProtocolEngineI getEngine() {
        return engine;
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
    private static String apartamentNumber = "5º E";

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
    private static final Logger LOG = LoggerFactory.getLogger(EidasAuthResponseTest.class.getName());

    /**
     * The IP address.
     */
    private static String ipAddress;

    /**
     * The ERROR text.
     */
    private static final String ERROR_TXT = "generateAuthnResponse(...) should've thrown an EIDASSAMLEngineException!";

    private static ImmutableAttributeMap newResponseImmutableAttributeMap() {

        ImmutableAttributeMap.Builder builder = ImmutableAttributeMap.builder();

        builder.put(EidasSpec.Definitions.DATE_OF_BIRTH, "2008-12-16");

        builder.put(EidasSpec.Definitions.PERSON_IDENTIFIER, "123456789P\u00D1");

        builder.put(EidasSpec.Definitions.CURRENT_FAMILY_NAME, "\u03A9\u03BD\u03AC\u03C3\u03B7\u03C2");

        builder.put(EidasSpec.Definitions.CURRENT_ADDRESS, new AttributeValue<PostalAddress>() {
            @Override
            public PostalAddress getValue() {
                return getAddressValue();
            }

            @Override
            public boolean isNonLatinScriptAlternateVersion() {
                return false;
            }
        });

        return builder.build();
    }

    private static PostalAddress getAddressValue() {
        return PostalAddress.builder().
                cvAddressArea("AddressAreaContentTest").
                adminUnitFirstLine("AdminUnitFirstLineContentTest").
                poBox("1000").
                locatorDesignator("LocatorDesignatorTest").
                locatorName("locatorNameTest").
                thoroughfare("thoroughfareTest").
                postName("postNameTest").
                adminUnitFirstLine("adminUnitFirstLine").
                adminUnitSecondLine("adminUnitSecondLine").
                postCode("postCodeTest").build();
    }

    private static String getEidasAddress() {
        StringBuilder builder = new StringBuilder(150);

        builder.append("<eidas:PoBox>");
        builder.append("SamplePoBox");
        builder.append("</eidas:PoBox>");

        builder.append("<eidas:LocatorDesignator>");
        builder.append("SampleLocatorDesignator");
        builder.append("</eidas:LocatorDesignator>");

        builder.append("<eidas:LocatorName>");
        builder.append("SampleLocatorName");
        builder.append("</eidas:LocatorName>");

        builder.append("<eidas:CvaddressArea>");
        builder.append("SampleCvaddressArea");
        builder.append("</eidas:CvaddressArea>");

        builder.append("<eidas:Thoroughfare>");
        builder.append("SampleThoroughfare");
        builder.append("</eidas:Thoroughfare>");

        builder.append("<eidas:PostName>");
        builder.append("SamplePostName");
        builder.append("</eidas:PostName>");

        builder.append("<eidas:AdminunitFirstline>");
        builder.append("SampleAdminunitFirstline");
        builder.append("</eidas:AdminunitFirstline>");

        builder.append("<eidas:AdminunitSecondline>");
        builder.append("SampleAdminunitSecondline");
        builder.append("</eidas:AdminunitSecondline>");

        builder.append("<eidas:PostCode>");
        builder.append("SamplePostCode");
        builder.append("</eidas:PostCode>");

        return EidasStringUtil.encodeToBase64(builder.toString());
    }

    private void initStaticVariables() {
        attributeMap =
                new ImmutableAttributeMap.Builder().put(newEidasAttributeDefinition("DateOfBirth", "DateOfBirth", true))
                        .put(newEidasAttributeDefinition("PersonIdentifier", "PersonIdentifier", true, true, false))
                        .put(newEidasAttributeDefinition("CurrentGivenName", "FirstName", true, false, true))
                        .put(newEidasAttributeDefinition("CurrentAddress", "CurrentAddress", false))
                        .build();

        destination = "http://proxyservice.gov.xx/EidasNode/ColleagueRequest";
        assertConsumerUrl = "http://connector.gov.xx/EidasNode/ColleagueResponse";
        spName = "University Oxford";

        spName = "University of Oxford";
        spSector = "EDU001";
        spInstitution = "OXF001";
        spApplication = "APP001";
        spCountry = "EN";

        spId = "EDU001-APP001-APP001";

        final String REQUEST_ISSUER = "http://localhost:7001/SP/metadata".toLowerCase();
        IEidasAuthenticationRequest request = new EidasAuthenticationRequest.Builder().destination(destination)
                .id("f5e7e0f5-b9b8-4256-a7d0-4090141b326d")
                .issuer(REQUEST_ISSUER)
                .providerName(spName)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .serviceProviderCountryCode(spCountry)
                .spType("public")
                .levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue())
                .nameIdFormat(SamlNameIdFormat.TRANSIENT.getNameIdFormat())
                .requestedAttributes(attributeMap)
                .citizenCountryCode("ES")
                .build();

        try {
            authRequest = getEngine().generateRequestMessage(request, null).getMessageBytes();

            authenRequest = (IEidasAuthenticationRequest) getEngine().unmarshallRequestAndValidate(authRequest, "EN");

        } catch (EIDASSAMLEngineException e) {
            LOG.error("EIDASSAMLEngineException {}", e);
            fail("Error create EidasAuthenticationRequest: " + e);
        }

        ipAddress = "111.222.333.444";

        attributeMap = newResponseImmutableAttributeMap();
    }

    @SuppressWarnings({"PublicField"})
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void setupClass() {
        RecommendedSecurityProviders.setupRecommendedSecurityProviders();
    }

    @Before
    public void setup() throws EIDASMetadataException {
        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any(), any())).thenReturn(FakeMetadata.connector());
        initStaticVariables();
    }



    /**
     * Test generate authentication request without errors.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testGenerateAuthnResponse() throws EIDASSAMLEngineException {

        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(attributeMap)
                .id("963158")
                .inResponseTo(authenRequest.getId())
                .issuer(RESPONSE_ISSUER)
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .failure(false)
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:transient")
                .levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue())
                .build();

        IResponseMessage responseMessage =
                getEngine().generateResponseMessage(authenRequest, response, ipAddress);

        authResponse = responseMessage.getMessageBytes();
        String result = EidasStringUtil.toString(authResponse);
        LOG.info("RESPONSE: " + SSETestUtils.encodeSAMLToken(authResponse));
        LOG.info("RESPONSE as string: " + result);
    }

    /**
     * Test validation id parameter mandatory.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testResponseMandatoryId() throws EIDASSAMLEngineException {
        IEidasAuthenticationRequest authnRequestCopyButNullId =
                new EidasAuthenticationRequest.Builder(authenRequest).id(null).build();

        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(attributeMap)
                .id("963158")
                .inResponseTo(authenRequest.getId())
                .issuer("http://Responder")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();

        getEngine().generateResponseMessage(authnRequestCopyButNullId, response, ipAddress);
        fail(ERROR_TXT);
    }

    /**
     * Test generate authentication response in response to err1.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testResponseMandatoryIssuer() {

        final String issuer = authenRequest.getIssuer();
        IEidasAuthenticationRequest authnRequestCopyButNullIssuer =
                new EidasAuthenticationRequest.Builder(authenRequest).issuer(null).build();

        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(attributeMap)
                .id("963158")
                .inResponseTo(authenRequest.getId())
                .issuer("http://Responder")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();
    }

    /**
     * Test generate authentication response assertion consumer null.
     */
    @Test
    public final void testResponseMandatoryAssertionConsumerServiceURL() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        IEidasAuthenticationRequest authnRequestCopyButNullAssertionConsumerUrl =
                new EidasAuthenticationRequest.Builder(authenRequest).assertionConsumerServiceURL(null).build();

        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(attributeMap)
                .id("963158")
                .inResponseTo(authenRequest.getId())
                .issuer("http://Responder")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:transient")
                .build();

        getEngine().generateResponseMessage(authnRequestCopyButNullAssertionConsumerUrl, response,
                ipAddress);
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
                .issuer("http://Responder")
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
    public final void testResponseMandatoryPersonalAttributeList() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        AuthenticationResponse response = new AuthenticationResponse.Builder().id("963158")
                .inResponseTo(authenRequest.getId())
                .issuer("http://Responder")
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:transient")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .build();

        getEngine().generateResponseMessage(authenRequest, response, ipAddress);
    }

    /**
     * Test validate authentication response token null.
     */
    @Test
    public final void testResponseInvalidParametersToken() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        getEngine().unmarshallResponseAndValidate(null, ipAddress, 0L, 0L, null);
    }

    /**
     * Test validate authentication response IP null.
     */
    @Test
    public final void testResponseInvalidParametersIP() throws EIDASSAMLEngineException, EIDASMetadataException {
        AuthenticationResponse response = new AuthenticationResponse.Builder().attributes(attributeMap)
                .id("963158")
                .inResponseTo(authenRequest.getId())
                .issuer("http://Responder")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:transient")
                .levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue())
                .build();
        authResponse = getEngine().generateResponseMessage(authenRequest, response, ipAddress).getMessageBytes();
        // In Conf1 ipValidate is false
        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any(), any())).thenReturn(FakeMetadata.proxyService());
        getEngine().unmarshallResponseAndValidate(authResponse, null, 0L, 0L, null);
    }

    /**
     * Test validate authentication response parameter name wrong.
     */
    @Test
    public final void testResponseInvalidParametersAttr() throws EIDASSAMLEngineException, EIDASMetadataException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.INTERNAL_ERROR.errorMessage());

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
                .issuer("http://Responder")
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .subject("UK/UK/Banksy")
                .subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:transient")
                .levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue())
                .build();

        authResponse = getEngine().generateResponseMessage(authenRequest, response, ipAddress).getMessageBytes();
        // In Conf1 ipValidate is false
        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any(), any())).thenReturn(FakeMetadata.proxyService());
        getEngine().unmarshallResponseAndValidate(authResponse, null, 0L, 0L, null);
    }

    /**
     * Test validate authentication response IP distinct and disabled validation IP.
     */
    @Test
    public final void testResponseInvalidParametersIPDistinct() throws EIDASSAMLEngineException, EIDASMetadataException {

        AuthenticationResponse.Builder responseBuilder = new AuthenticationResponse.Builder();
        responseBuilder.attributes(attributeMap);
        responseBuilder.id("963158");
        responseBuilder.inResponseTo(authenRequest.getId());
        responseBuilder.issuer(RESPONSE_ISSUER);
        responseBuilder.statusCode(EIDASStatusCode.SUCCESS_URI.toString());
        responseBuilder.subject("UK/UK/Banksy");
        responseBuilder.subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:transient");
        responseBuilder.levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue());

        IResponseMessage responseMessage = getEngine().generateResponseMessage(authenRequest, responseBuilder.build(), ipAddress);

        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any(), any())).thenReturn(FakeMetadata.proxyService());
        authResponse = responseMessage.getMessageBytes();
        getEngine().unmarshallResponseAndValidate(authResponse, "127.0.0.1", 0, 0, null);
    }

    /**
     * Test response invalid parameters invalid token.
     */
    @Test
    public final void testResponseInvalidParametersTokenMsg() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        // ipAddress origin "111.222.333.444"
        // Subject Confirmation Bearer.
        getEngine().unmarshallResponseAndValidate(EidasStringUtil.getBytes("errorMessage"), ipAddress, 0, 0, null);
    }

    /**
     * Test validate authentication response is fail.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testValidateAuthenticationResponseIsFail() throws EIDASSAMLEngineException, EIDASMetadataException {
        testGenerateAuthnResponse();//prepare valid authnResponse
        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any(), any())).thenReturn(FakeMetadata.proxyService());
        authnResponse = getEngine().unmarshallResponseAndValidate(authResponse, ipAddress, 0, 0, null);
        assertFalse("Generate incorrect response: ", authnResponse.isFailure());
    }

    /**
     * Test validate authentication response destination.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testValidateAuthenticationResponseDestination() throws EIDASSAMLEngineException, EIDASMetadataException {
        testGenerateAuthnResponse();
        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any(), any())).thenReturn(FakeMetadata.proxyService());
        authnResponse = getEngine().unmarshallResponseAndValidate(authResponse, ipAddress, 0, 0, null);

        assertEquals("Destination incorrect: ", authnResponse.getInResponseToId(), authenRequest.getId());
    }

    /**
     * Test validate authentication response values.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    @Ignore
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

                assertEquals("Incorrect eIDAS address: ", EIDAS_ADDRESS, value);
            }
        }
    }

    /**
     * Test generate authenticate response fail in response to it's null.
     *
     * @throws EIDASSAMLEngineException
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testGenerateAuthnResponseFailInResponseToNull() throws EIDASSAMLEngineException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("id cannot be null, empty or blank");

        IEidasAuthenticationRequest authnRequestCopyButNullId =
                new EidasAuthenticationRequest.Builder(authenRequest).id(null).build();

        AuthenticationResponse.Builder response = new AuthenticationResponse.Builder();
        response.statusCode(EIDASStatusCode.REQUESTER_URI.toString());
        response.subStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString());
        response.statusMessage("");
        response.id("963158");
        response.inResponseTo(authenRequest.getId());
        response.issuer("http://Responder");

        authResponse =
                getEngine().generateResponseErrorMessage(authnRequestCopyButNullId, response.build(), ipAddress)
                        .getMessageBytes();
    }

    /**
     * Test generate authenticate response fail assertion consumer URL err1.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testGenerateAuthnResponseFailAssertionConsumerUrlNull() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        IEidasAuthenticationRequest authnRequestCopyButNullAssertionConsumerUrl =
                new EidasAuthenticationRequest.Builder(authenRequest).assertionConsumerServiceURL(null).build();

        AuthenticationResponse.Builder response = new AuthenticationResponse.Builder();
        response.statusCode(EIDASStatusCode.REQUESTER_URI.toString());
        response.subStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString());
        response.statusMessage("");
        response.failure(true);
        response.id("963158");
        response.inResponseTo(authenRequest.getId());
        response.issuer("http://Responder");

        authResponse = getEngine().generateResponseErrorMessage(authnRequestCopyButNullAssertionConsumerUrl,
                response.build(), ipAddress).getMessageBytes();
    }

    /**
     * Test generate authentication response fail code error err1.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testGenerateAuthnResponseFailCodeErrorNull() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("statusCode cannot be null, empty or blank");

        AuthenticationResponse.Builder response = new AuthenticationResponse.Builder();
        response.statusCode(null);
        response.subStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString());
        response.statusMessage("");
        response.id("963158");
        response.inResponseTo(authenRequest.getId());
        response.issuer("http://Responder");

        authResponse =
                getEngine().generateResponseErrorMessage(authenRequest, response.build(), ipAddress).getMessageBytes();
    }

    /**
     * Test generate authentication request without errors.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testValidateAuthnResponse() throws EIDASSAMLEngineException, EIDASMetadataException {
        AuthenticationResponse.Builder responseBuilder = new AuthenticationResponse.Builder();
        responseBuilder.attributes(newResponseImmutableAttributeMap());
        responseBuilder.id("963158");
        responseBuilder.inResponseTo(authenRequest.getId());
        responseBuilder.issuer("http://Responder");
        responseBuilder.statusCode(EIDASStatusCode.SUCCESS_URI.toString());
        responseBuilder.subject("UK/UK/Banksy");
        responseBuilder.subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:transient");
        responseBuilder.levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue());

        IResponseMessage responseMessage =
                getEngine().generateResponseMessage(authenRequest, responseBuilder.build(), ipAddress);

        authResponse = responseMessage.getMessageBytes();
        LOG.info("Request id: " + authenRequest.getId());

        LOG.info("RESPONSE: " + SSETestUtils.encodeSAMLToken(authResponse));

        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any(), any())).thenReturn(FakeMetadata.proxyService());
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
    public final void testValidateAuthenticationResponseFailIsFail() throws EIDASSAMLEngineException, EIDASMetadataException {
        final AuthenticationResponse.Builder responseBuilder = new AuthenticationResponse.Builder();
        responseBuilder.statusCode(EIDASStatusCode.REQUESTER_URI.toString());
        responseBuilder.subStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString());
        responseBuilder.statusMessage("message");
        responseBuilder.id("963158");
        responseBuilder.inResponseTo(authenRequest.getId());
        responseBuilder.issuer(RESPONSE_ISSUER);
        responseBuilder.failure(true);
        responseBuilder.levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue());

        authResponse =
                getEngine().generateResponseErrorMessage(authenRequest, responseBuilder.build(), ipAddress).getMessageBytes();

        LOG.error("ERROR_FAIL: " + EidasStringUtil.encodeToBase64(authResponse));

        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any(), any())).thenReturn(FakeMetadata.proxyService());
        authnResponse = getEngine().unmarshallResponseAndValidate(authResponse, ipAddress, 0, 0, null);

        LOG.info("COUNTRY: " + authnResponse.getCountry());
        assertTrue("Generate incorrect response: ", authnResponse.isFailure());
    }

    /**
     * tests support for level of assurance
     */
    @Test
    public final void testGenerateAuthnResponseLoA() throws EIDASSAMLEngineException {
        AuthenticationResponse.Builder response = new AuthenticationResponse.Builder();
        response.attributes(attributeMap);
        response.levelOfAssurance("http://eidas.europa.eu/LoA/low");
        response.id("963158");
        response.inResponseTo(authenRequest.getId());
        response.issuer("http://Responder");
        response.statusCode(EIDASStatusCode.SUCCESS_URI.toString());
        response.subject("UK/UK/Banksy");
        response.subjectNameIdFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:transient");

        IResponseMessage responseMessage =
                getEngine().generateResponseMessage(authenRequest, response.build(), ipAddress);

        authResponse = responseMessage.getMessageBytes();

        LOG.info("RESPONSE: " + SSETestUtils.encodeSAMLToken(authResponse));
    }
}
