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

package eu.eidas.engine.test.simple;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.stork.IStorkAuthenticationRequest;
import eu.eidas.auth.commons.protocol.stork.impl.StorkAuthenticationRequest;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.stork.StorkProtocolProcessor;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import java.util.Arrays;

/**
 * The Class EidasAuthRequestTest - test support for STORK1 format.
 */
public class AuthRequestTest {

    /**
     * The engines.
     */
    private static final ProtocolEngineI engine1;

    private static final ProtocolEngineI engine2;

    private static final ProtocolEngineI engine3;

    static {
        try {
            engine1 = ProtocolEngineFactory.createProtocolEngine("CONF1", new StorkProtocolProcessor(
                    "saml-engine-stork-attributes-CONF1.xml", "saml-engine-additional-attributes-CONF1.xml", null));
            engine2 = ProtocolEngineFactory.createProtocolEngine("CONF2", new StorkProtocolProcessor(
                    "saml-engine-stork-attributes-CONF2.xml", "saml-engine-additional-attributes-CONF2.xml", null));
            engine3 = ProtocolEngineFactory.createProtocolEngine("CONF3", new StorkProtocolProcessor(
                    "saml-engine-stork-attributes-CONF3.xml", "saml-engine-additional-attributes-CONF3.xml", null));
        } catch (EIDASSAMLEngineException e) {
            fail("Failed to initialize SAMLEngines: " + e);
            e.printStackTrace();
            throw new AssertionError(e);
        }
    }

    private ProtocolEngineI getEngine() {
        return helperGetEngine("CONF1");
    }

    private ProtocolEngineI getEngine2() {
        return helperGetEngine("CONF2");
    }

    private ProtocolEngineI getEngine3() {
        return helperGetEngine("CONF3");
    }

    private ProtocolEngineI helperGetEngine(String name) {
        ProtocolEngineI engine = null;
        try {
            engine = ProtocolEngineFactory.createProtocolEngine(name, new StorkProtocolProcessor(
                    "saml-engine-stork-attributes-" + name + ".xml",
                    "saml-engine-additional-attributes-" + name + ".xml", null));
        } catch (EIDASSAMLEngineException exc) {
            fail("Failed to initialize SAMLEngines");
        }
        return engine;

    }

    private static final AttributeDefinition<String> SIGNED_DOC =
            new AttributeDefinition.Builder<String>().nameUri("http://www.stork.gov.eu/1.0/signedDoc")
                    .friendlyName("signedDoc")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .xmlType("http://www.w3.org/2001/XMLSchema", "string", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static final AttributeDefinition<String> IS_AGE_OVER =
            new AttributeDefinition.Builder<String>().nameUri("http://www.stork.gov.eu/1.0/isAgeOver")
                    .friendlyName("isAgeOver")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(false)
                    .uniqueIdentifier(false)
                    .xmlType("http://www.w3.org/2001/XMLSchema", "string", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static final AttributeDefinition<String> DATE_OF_BIRTH =
            new AttributeDefinition.Builder<String>().nameUri("http://www.stork.gov.eu/1.0/dateOfBirth")
                    .friendlyName("dateOfBirth")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(false)
                    .uniqueIdentifier(false)
                    .xmlType("http://www.w3.org/2001/XMLSchema", "string", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static final AttributeDefinition<String> UNKNWON =
            new AttributeDefinition.Builder<String>().nameUri("http://www.stork.gov.eu/1.0/unknown")
                    .friendlyName("unknown")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(false)
                    .uniqueIdentifier(false)
                    .xmlType("http://www.w3.org/2001/XMLSchema", "string", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static final AttributeDefinition<String> EIDENTIFIER =
            new AttributeDefinition.Builder<String>().nameUri("http://www.stork.gov.eu/1.0/eIdentifier")
                    .friendlyName("eIdentifier")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(false)
                    .uniqueIdentifier(false)
                    .xmlType("http://www.w3.org/2001/XMLSchema", "string", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static final ImmutableAttributeMap REQUESTED_ATTRIBUTES =
            new ImmutableAttributeMap.Builder().put(IS_AGE_OVER, new StringAttributeValue("18", false), new StringAttributeValue("16", true))
                    .put(DATE_OF_BIRTH)
                    .put(EIDENTIFIER)
                    .build();

    private static final String SAMLID = "f5e7e0f5-b9b8-4256-a7d0-4090141b326d";

    private static final String ISSUER = "http://localhost:7001/SP/metadata".toLowerCase();

    private static final String DESTINATION = "http://localhost:7001/EidasNode/ConnectorMetadata";

    /**
     * Instantiates a new authentication request test.
     */
    public AuthRequestTest() {
        destination = "http://proxyservice.gov.xx/EidasNode/ColleagueRequest";
        assertConsumerUrl = "http://connector.gov.xx/EidasNode/ColleagueResponse";

        spName = "University of Oxford";
        spSector = "EDU001";
        spInstitution = "OXF001";
        spApplication = "APP001";
        spCountry = "EN";

        spId = "EDU001-OXF001-APP001";

    }

    /**
     * The destination.
     */
    private String destination;

    /**
     * The service provider name.
     */
    private String spName;

    /**
     * The service provider sector.
     */
    private String spSector;

    /**
     * The service provider institution.
     */
    private String spInstitution;

    /**
     * The service provider application.
     */
    private String spApplication;

    /**
     * The service provider country.
     */
    private String spCountry;

    /**
     * The service provider id.
     */
    private String spId;

    /**
     * The quality authentication assurance level.
     */
    private static final int QAAL = 3;

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
    private static final Logger LOG = LoggerFactory.getLogger(AuthRequestTest.class.getName());

    /**
     * Test generate authentication request.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testGenerateAuthnRequest() throws EIDASSAMLEngineException {

        IStorkAuthenticationRequest storkRequest = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                providerName(spName).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("ES").
                spId(spId).
                qaa(QAAL).
                spSector(spSector).
                spInstitution(spInstitution).
                spApplication(spApplication).
                requestedAttributes(REQUESTED_ATTRIBUTES).
                levelOfAssurance("high").
                build();

        LOG.info("EidasAuthenticationRequest 1: " + SSETestUtils.encodeSAMLToken(
                engine1.generateRequestMessage(storkRequest, null).getMessageBytes()));

        IStorkAuthenticationRequest storkRequest2 =
                new StorkAuthenticationRequest.Builder(storkRequest).citizenCountryCode("ES").build();
        LOG.info("EidasAuthenticationRequest 2: " + SSETestUtils.encodeSAMLToken(
                engine1.generateRequestMessage(storkRequest2, null).getMessageBytes()));
    }

    /**
     * Test generate authentication request error personal attribute name error.
     */
    @Test
    public final void testGenerateAuthnRequestPALsErr1() {

        AttributeDefinition<String> WRONG_ATTRIBUTE_DEFINITION =
                new AttributeDefinition.Builder<String>().nameUri("http://spoofedAttribute/brol")
                        .friendlyName("brol")
                        .personType(PersonType.NATURAL_PERSON)
                        .required(true)
                        .uniqueIdentifier(true)
                        .xmlType("http://eidas.europa.eu/attributes/naturalperson", "brol", "eidas-natural")
                        .attributeValueMarshaller(new StringAttributeValueMarshaller())
                        .build();

        ImmutableAttributeMap MY_WRONG_ATTRIBUTES =
                new ImmutableAttributeMap.Builder().put(WRONG_ATTRIBUTE_DEFINITION).build();

        IStorkAuthenticationRequest IAuthenticationRequest = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                providerName(spName).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("ES").
                spId(spId).
                qaa(QAAL).
                spSector(spSector).
                spInstitution(spInstitution).
                spApplication(spApplication).
                requestedAttributes(MY_WRONG_ATTRIBUTES).
                levelOfAssurance("high").
                build();

        try {
            engine1.generateRequestMessage(IAuthenticationRequest, null);
            fail("generateRequestMessage(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error");
        }
    }

    /**
     * Test generate authentication request error personal attribute value error.
     */
    @Test
    public final void testGenerateAuthnRequestPALsErr2WithStork() throws Exception {

        AttributeDefinition<String> WRONG_ATTRIBUTE_DEFINITION =
                new AttributeDefinition.Builder<String>().nameUri("http://spoofedAttribute/brol")
                        .friendlyName("brol")
                        .personType(PersonType.NATURAL_PERSON)
                        .required(true)
                        .uniqueIdentifier(true)
                        .xmlType("http://eidas.europa.eu/attributes/naturalperson", "brol", "eidas-natural")
                        .attributeValueMarshaller(new StringAttributeValueMarshaller())
                        .build();

        ImmutableAttributeMap MY_WRONG_ATTRIBUTES =
                new ImmutableAttributeMap.Builder().put(WRONG_ATTRIBUTE_DEFINITION, "toto").build();

        IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                providerName(spName).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("ES").
                spId(spId).
                qaa(QAAL).
                spSector(spSector).
                spInstitution(spInstitution).
                spApplication(spApplication).
                requestedAttributes(MY_WRONG_ATTRIBUTES).
                levelOfAssurance("high").
                build();

        try {
            engine1.generateRequestMessage(request, null);
            fail("generateRequestMessage(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error");
        }
    }

    /**
     * Test generate authentication request error provider name null.
     */
    @Test
    public final void testGenerateAuthnRequestSpNameErr1() {

        IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("ES").
                spId(spId).
                qaa(QAAL).
                spSector(spSector).
                spInstitution(spInstitution).
                spApplication(spApplication).
                requestedAttributes(REQUESTED_ATTRIBUTES).
                levelOfAssurance("high").
                build();

        try {
            engine1.generateRequestMessage(request, null);
            fail("generateRequestMessage(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error");
        }
    }

    /**
     * Test generate authentication request authentication assurance level negative value.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testGenerateAuthnRequestQaalErr1() throws EIDASSAMLEngineException {
        IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("ES").
                spId(spId).
                qaa(-1).
                spSector(spSector).
                spInstitution(spInstitution).
                spApplication(spApplication).
                requestedAttributes(REQUESTED_ATTRIBUTES).
                levelOfAssurance("high").
                build();
        engine1.generateRequestMessage(request, null);
    }

    /**
     * Test generate authentication request service provider sector null.
     */
    @Test(expected = EIDASSAMLEngineException.class)
    public final void testGenerateAuthnRequestSectorErr() throws Exception {

        IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("ES").
                spId(spId).
                qaa(QAAL).
                spSector(null).
                spInstitution(spInstitution).
                spApplication(spApplication).
                requestedAttributes(REQUESTED_ATTRIBUTES).
                levelOfAssurance("high").
                build();
        engine1.generateRequestMessage(request, null);

    }

    /**
     * Test generate authentication request service provider institution null.
     */
    @Test(expected = EIDASSAMLEngineException.class)
    public final void testGenerateAuthnRequestInstitutionrErr() throws Exception {

        IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("ES").
                spId(spId).
                qaa(QAAL).
                spSector(spSector).
                spInstitution(null).
                spApplication(spApplication).
                requestedAttributes(REQUESTED_ATTRIBUTES).
                levelOfAssurance("high").
                build();

        engine1.generateRequestMessage(request, null);

    }

    /**
     * Test generate authentication request service provider application null.
     */
    @Test
    public final void testGenerateAuthnRequestApplicationErr() {

        IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                providerName(spName).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("ES").
                spId(spId).
                qaa(QAAL).
                spSector(spSector).
                spInstitution(spInstitution).
                spApplication(null).
                requestedAttributes(REQUESTED_ATTRIBUTES).
                levelOfAssurance("high").
                build();
        try {
            engine1.generateRequestMessage(request, null);

        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error");
            fail("generateRequestMessage(...) should've thrown an EIDASSAMLEngineException!");
        }
    }

    /**
     * Test generate authentication request service provider country null.
     */
    @Test
    public final void testGenerateAuthnRequestCountryErr() {

        IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                providerName(spName).
                serviceProviderCountryCode(null).
                citizenCountryCode("ES").
                spId(spId).
                qaa(QAAL).
                spSector(spSector).
                spInstitution(spInstitution).
                spApplication(spApplication).
                requestedAttributes(REQUESTED_ATTRIBUTES).
                levelOfAssurance("high").
                build();

        try {
            engine1.generateRequestMessage(request, null);

        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error");
            fail("generateRequestMessage(...) should've thrown an EIDASSAMLEngineException!");
        }
    }

    /**
     * Test generate authentication request error with quality authentication assurance level wrong.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testGenerateAuthnRequestQaalErr2() {
        IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                providerName(spName).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("ES").
                spId(spId).
                qaa(0).
                spSector(spSector).
                spInstitution(spInstitution).
                spApplication(spApplication).
                requestedAttributes(REQUESTED_ATTRIBUTES).
                levelOfAssurance("high").
                build();

        try {
            ProtocolEngineI engine = ProtocolEngineFactory.getDefaultProtocolEngine("Qaa0");
            engine.generateRequestMessage(request, null);
            fail("generateRequestMessage(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error", e);
        }
    }

    /**
     * Test generate authentication request personal attribute list null value.
     */
    @Test
    public final void testGenerateAuthnRequestPALErr1() {
        IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                providerName(spName).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("ES").
                spId(spId).
                qaa(QAAL).
                spSector(spSector).
                spInstitution(spInstitution).
                spApplication(spApplication).
                requestedAttributes(null).
                levelOfAssurance("high").
                build();

        try {
            engine1.generateRequestMessage(request, null);
            fail("generateRequestMessage(...) should've thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error");
        }
    }

    /**
     * Test generate authentication request error with assertion consumer URL null.
     */
    @Test
    public final void testGenerateAuthnRequestAssertionConsumerErr1() {
        IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(null).
                destination(destination).
                providerName(spName).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("ES").
                spId(spId).
                qaa(QAAL).
                spSector(spSector).
                spInstitution(spInstitution).
                spApplication(spApplication).
                requestedAttributes(REQUESTED_ATTRIBUTES).
                levelOfAssurance("high").
                build();

            ProtocolEngineI engine = ProtocolEngineFactory.getDefaultProtocolEngine("default");
            assertNull(engine);
    }

    /**
     * Test validate authentication request null parameter.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testValidateAuthnRequestNullParam() throws EIDASSAMLEngineException {
        try {
            engine1.unmarshallRequestAndValidate(null, "ES",null);
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
    public final void testValidateAuthnRequestErrorEncode() throws EIDASSAMLEngineException {
        try {
            engine1.unmarshallRequestAndValidate(EidasStringUtil.getBytes("messageError"), "ES",null);
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
    public final void testValidateAuthnRequest() throws EIDASSAMLEngineException {
        IStorkAuthenticationRequest validatedRequest =
                (IStorkAuthenticationRequest) engine1.unmarshallRequestAndValidate(
                        getDefaultTestStorkAuthnRequestTokenSaml(), "ES",Arrays.asList(ISSUER));

        assertEquals("CrossBorderShare incorrect: ", validatedRequest.isEIDCrossBorderShare(), false);
        assertEquals("CrossSectorShare incorrect: ", validatedRequest.isEIDCrossSectorShare(), false);
        assertEquals("SectorShare incorrect: ", validatedRequest.isEIDSectorShare(), false);

    }

    /**
     * Test validate data authenticate request. Verified parameters after validation.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testValidateDataAuthnRequest() throws EIDASSAMLEngineException {

        ProtocolEngineI engine = ProtocolEngineFactory.getDefaultProtocolEngine("CONF2");

        IStorkAuthenticationRequest request = (IStorkAuthenticationRequest) engine.unmarshallRequestAndValidate(
                getDefaultTestStorkAuthnRequestTokenSaml(), "ES",Arrays.asList(ISSUER));

        assertEquals("Sestination incorrect: ", request.getDestination(), destination);

        assertEquals("CrossBorderShare incorrect: ", request.isEIDCrossBorderShare(), false);
        assertEquals("CrossSectorShare incorrect: ", request.isEIDCrossSectorShare(), false);
        assertEquals("SectorShare incorrect: ", request.isEIDSectorShare(), false);

        assertEquals("Service provider incorrect: ", request.getProviderName(), spName);
        assertEquals("QAAL incorrect: ", request.getQaa(), QAAL);
        assertEquals("SPSector incorrect: ", request.getSpSector(), spSector);
        assertEquals("SPInstitution incorrect: ", request.getSpInstitution(), null);
        assertEquals("SPApplication incorrect: ", request.getSpApplication(), spApplication);
        assertEquals("Asserition consumer URL incorrect: ", request.getAssertionConsumerServiceURL(),
                     assertConsumerUrl);

        assertEquals("SP Country incorrect: ", request.getServiceProviderCountryCode(), spCountry);
        assertEquals("SP Id incorrect: ", request.getSpId(), spId);
        assertEquals("CitizenCountryCode incorrect: ", request.getCitizenCountryCode(), "ES");

    }

    /**
     * Test validate file authentication request. Validate from XML file.
     *
     * @throws Exception the exception
     */
    @Test
    public final void testValidateFileAuthnRequest() throws Exception {

        final byte[] bytes = SSETestUtils.readSamlFromFile("/data/eu/eidas/EIDASSAMLEngine/AuthnRequest.xml");

        try {
            engine1.unmarshallRequestAndValidate(bytes, "ES",null);
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
    public final void testValidateFileAuthnRequestTagDelete() throws Exception {

        final byte[] bytes = SSETestUtils.readSamlFromFile("/data/eu/eidas/EIDASSAMLEngine/AuthnRequestTagDelete.xml");

        try {
            engine1.unmarshallRequestAndValidate(bytes, "ES",null);
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
    public final void testValidateAuthnRequestNotTrustedErr1() throws EIDASSAMLEngineException {

        try {
            final ProtocolEngineI engineNotTrusted = ProtocolEngineFactory.getDefaultProtocolEngine("CONF2");

            IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                    id(SAMLID).
                    issuer(ISSUER).
                    destination(DESTINATION).
                    assertionConsumerServiceURL(assertConsumerUrl).
                    destination(destination).
                    providerName(spName).
                    serviceProviderCountryCode(spCountry).
                    citizenCountryCode("ES").
                    spId(spId).
                    qaa(QAAL).
                    spSector(spSector).
                    spInstitution(spInstitution).
                    spApplication(spApplication).
                    requestedAttributes(REQUESTED_ATTRIBUTES).
                    levelOfAssurance("high").
                    build();

            final byte[] authReqNotTrust = engineNotTrusted.generateRequestMessage(request, null).getMessageBytes();
            final ProtocolEngineI engine = ProtocolEngineFactory.getDefaultProtocolEngine("CONF1");
            engine.unmarshallRequestAndValidate(authReqNotTrust, "ES",Arrays.asList(ISSUER));
            fail("validateEIDASAuthnRequestNotTrusted(...) should have thrown an EIDASSAMLEngineException!");
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error: " + e, e);
            assertEquals("Error (no. message.validation.error.code) processing request : message.validation.error.message - QAA Level attribute is the STORK 1 attribute", e.getMessage());
        }
    }

    /**
     * Test validate authentication request trusted.
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testValidateAuthnRequestTrusted() throws EIDASSAMLEngineException {

        final ProtocolEngineI engineTrusted = ProtocolEngineFactory.getDefaultProtocolEngine("CONF3");

        IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                providerName(spName).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("ES").
                spId(spId).
                qaa(QAAL).
                spSector(spSector).
                spInstitution(spInstitution).
                spApplication(spApplication).
                requestedAttributes(REQUESTED_ATTRIBUTES).
                levelOfAssurance("high").
                build();

        final byte[] authReqNotTrust = engineTrusted.generateRequestMessage(request, null).getMessageBytes();

        // engine ("CONF1") no have trust certificate from "CONF2"
        final ProtocolEngineI engineNotTrusted = ProtocolEngineFactory.getDefaultProtocolEngine("CONF2");
        engineNotTrusted.unmarshallRequestAndValidate(authReqNotTrust, "ES",Arrays.asList(ISSUER));

    }

    /**
     * Test generate authentication request service provider application not null.
     */
    @Test
    public final void testGenerateAuthnRequestNADA() {
        IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                providerName(spName).
                serviceProviderCountryCode(null).
                citizenCountryCode("ES").
                spId("TEST_SP").
                qaa(QAAL).
                spSector(null).
                spInstitution(null).
                spApplication(null).
                requestedAttributes(REQUESTED_ATTRIBUTES).
                levelOfAssurance("high").
                build();

        try {
            authRequest = engine1.generateRequestMessage(request, null).getMessageBytes();
            engine1.unmarshallRequestAndValidate(authRequest, "ES",null);
            assertNotNull(request.getSpId());
        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error");
        }
    }

    /**
     * Test generate authentication request service provider application not null.
     */
    @Test
    public final void testGenerateAuthnRequestWithVIDPAuthenticationBlockAbsent() {
        IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                providerName(spName).
                serviceProviderCountryCode(null).
                citizenCountryCode("ES").
                qaa(QAAL).
                spSector(null).
                spInstitution(null).
                spApplication(null).
                requestedAttributes(REQUESTED_ATTRIBUTES).
                levelOfAssurance("high").
                build();

        try {
            authRequest = engine1.generateRequestMessage(request, null).getMessageBytes();
            engine1.unmarshallRequestAndValidate(authRequest, "ES",null);
            assertNull(request.getSpId());
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
    public final void testBuildAuthnRequestWithUnknownElements() throws EIDASSAMLEngineException {

        ImmutableAttributeMap attributeMap = new ImmutableAttributeMap.Builder().
                put(UNKNWON).
                put(EIDENTIFIER).
                build();

        IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                providerName(spName).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("ES").
                spId(spId).
                qaa(QAAL).
                spSector(spSector).
                spInstitution(spInstitution).
                spApplication(spApplication).
                requestedAttributes(attributeMap).
                levelOfAssurance("high").
                build();

        IRequestMessage requestMessage = engine3.generateRequestMessage(request, null);

        byte[] tokenSaml = requestMessage.getMessageBytes();

        IStorkAuthenticationRequest authenticationRequest =
                (IStorkAuthenticationRequest) engine1.unmarshallRequestAndValidate(tokenSaml, "ES",Arrays.asList(ISSUER));

        assertNull("The value shouldn't exist",
                   authenticationRequest.getRequestedAttributes().getDefinitionsByFriendlyName("unknown"));
        assertNotNull("The value should exist",
                      authenticationRequest.getRequestedAttributes().getDefinitionsByFriendlyName("eIdentifier"));
    }

    /**
     * Test generate Request with required elements by default
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testGenerateAuthnRequestWithIsRequiredElementsByDefault() throws EIDASSAMLEngineException {

        ImmutableAttributeMap attributeMap = new ImmutableAttributeMap.Builder().
                put(EIDENTIFIER).
                build();

        IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                providerName(spName).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("ES").
                spId(spId).
                qaa(QAAL).
                spSector(spSector).
                spInstitution(spInstitution).
                spApplication(spApplication).
                requestedAttributes(attributeMap).
                levelOfAssurance("high").
                build();

        IRequestMessage reqTrue = engine1.generateRequestMessage(request, null);
        IRequestMessage reqFalse = engine2.generateRequestMessage(request, null);
        IRequestMessage req = engine3.generateRequestMessage(request, null);

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

        assertTrue("The token must contain the chain 'isRequired=\"false\"'",
                   reqFalseToken.contains("isRequired=\"false\""));

    }

    /**
     * Test generating/validating request with signedDoc
     *
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Test
    public final void testGenerateAuthnRequestWithSignedDoc() throws EIDASSAMLEngineException {
        String signedDocRequest =
                "<dss:SignRequest xmlns:dss=\"urn:oasis:names:tc:dss:1.0:core:schema\" RequestID=\"_d96b62a87d18f1095170c1f44c90b5fd\"><dss:InputDocuments>"
                        + "<dss:Document><dss:Base64Data MimeType=\"text/plain\">VGVzdCB0ZXh0</dss:Base64Data></dss:Document></dss:InputDocuments></dss:SignRequest>";

        ImmutableAttributeMap attributeMap = new ImmutableAttributeMap.Builder().
                put(EIDENTIFIER).
                put(IS_AGE_OVER, new StringAttributeValue("16", false), new StringAttributeValue("18", false)).
                put(SIGNED_DOC, signedDocRequest).
                build();

        IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                providerName(spName).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("ES").
                spId(spId).
                qaa(QAAL).
                spSector(spSector).
                spInstitution(spInstitution).
                spApplication(spApplication).
                requestedAttributes(attributeMap).
                levelOfAssurance("high").
                build();

        IRequestMessage req = engine1.generateRequestMessage(request, null);
        byte[] messageBytes = req.getMessageBytes();
        String asXml = EidasStringUtil.toString(messageBytes);
        IStorkAuthenticationRequest authenticationRequest =
                (IStorkAuthenticationRequest) engine1.unmarshallRequestAndValidate(messageBytes, "ES",Arrays.asList(ISSUER));

        assertTrue("SignedDoc request should be the same: " + asXml, authenticationRequest.getRequestedAttributes()
                .getAttributeValuesByFriendlyName("signedDoc")
                .getAttributeMap()
                .values()
                .iterator()
                .next()
                .contains(new StringAttributeValue(signedDocRequest, false)));

    }

    /**
     * Return the default EIDAS authRequest token used in the tests.
     *
     * @return default EIDAS authRequest token
     * @throws EIDASSAMLEngineException
     */
    private final byte[] getDefaultTestStorkAuthnRequestTokenSaml() throws EIDASSAMLEngineException {
        IStorkAuthenticationRequest IAuthenticationRequest = StorkAuthenticationRequest.builder().
                id(SAMLID).
                issuer(ISSUER).
                destination(DESTINATION).
                assertionConsumerServiceURL(assertConsumerUrl).
                destination(destination).
                providerName(spName).
                serviceProviderCountryCode(spCountry).
                citizenCountryCode("ES").
                spId(spId).
                qaa(QAAL).
                spSector(spSector).
                spInstitution(spInstitution).
                spApplication(spApplication).
                requestedAttributes(REQUESTED_ATTRIBUTES).
                levelOfAssurance("high").
                build();

        return engine1.generateRequestMessage(IAuthenticationRequest, null).getMessageBytes();
    }
}
