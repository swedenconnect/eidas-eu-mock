/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the
 * "NOTICE" text file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution.
 * Any derivative works that you distribute must include a readable
 * copy of the "NOTICE" text file.
 */
package eu.eidas.engine.test.simple;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.impl.DateTimeAttributeValue;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.configuration.dom.ReloadableProtocolConfigurationInvocationHandler;
import eu.eidas.auth.engine.core.eidas.spec.NaturalPersonSpec;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.fail;

/**
 *
 */
public class SAMLEngineTimeSkewTest {

    private static final Logger LOG = LoggerFactory.getLogger(SAMLEngineTimeSkewTest.class.getName());

    private SamlEngineTestClock clock;

    /**
     * The engines.
     */
    private ProtocolEngineI engine;

    @Before
    public void setUp() throws Exception {
        // inject a test clock to do some  time shifting
        engine =
                ProtocolEngineFactory.getDefaultProtocolEngine("SkewTest");
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(engine.getClock());
        ReloadableProtocolConfigurationInvocationHandler ih = (ReloadableProtocolConfigurationInvocationHandler) invocationHandler;
        clock = (SamlEngineTestClock) ih.getProxiedObject();
    }

    /**
     * Normal behaviour of validation : no time skew, no clock change. Expected : no error
     *
     * @throws EIDASSAMLEngineException
     */
    @Test
    public void testValidateResponseWithNoTimeSkew() throws EIDASSAMLEngineException {
        LOG.info("testValidateResponseWithNoTimeSkew");
        clock.setDelta(0);
        byte[] samlResponse = generateTestSamlResponse();
        engine.unmarshallResponseAndValidate(samlResponse, "", 0, 0, null, Arrays.asList(RESPONSE_ISSUER),true);
    }

    /**
     * Clock change to one hour later and no time skew Expected : exception thrown
     *
     * @throws EIDASSAMLEngineException
     */
    @Test(expected = EIDASSAMLEngineException.class)
    public void testValidateResponseWithTestClockOneHourLaterAndNoTimeSkew() throws EIDASSAMLEngineException {
        LOG.info("testValidateResponseWithTestClockOneHourLaterAndNoTimeSkew");
        byte[] samlResponse = generateTestSamlResponse();
        clock.setDelta(600000);              // clock is now one hour later
        engine.unmarshallResponseAndValidate(samlResponse, "", 0, 0, null,Arrays.asList(RESPONSE_ISSUER),true);
    }

    /**
     * Clock change to one hour before and no time skew Expected : exception thrown
     *
     * @throws EIDASSAMLEngineException
     */
    @Test(expected = EIDASSAMLEngineException.class)
    public void testValidateResponseWithTestClockOneHourBeforeAndNoTimeSkew() throws EIDASSAMLEngineException {
        LOG.info("testValidateResponseWithTestClockOneHourBeforeAndNoTimeSkew");
        byte[] samlResponse = generateTestSamlResponse();
        clock.setDelta(-600000);              // clock is now one hour before
        engine.unmarshallResponseAndValidate(samlResponse, "", 0, 0, null,Arrays.asList(RESPONSE_ISSUER),true);
    }

    /**
     * Clock change to one hour after and time skew one hour later Expected : no error
     *
     * @throws EIDASSAMLEngineException
     */
    @Test
    public void testValidateResponseWithTestClockOneHourLaterAndTimeSkew() throws EIDASSAMLEngineException {
        LOG.info("testValidateResponseWithTestClockOneHourLaterAndTimeSkew");
        clock.setDelta(600000);              // clock is now one hour later
        byte[] samlResponse = generateTestSamlResponse();
        engine.unmarshallResponseAndValidate(samlResponse, "", -600000, 600000, null,Arrays.asList(RESPONSE_ISSUER),true);
    }

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

    private static ImmutableAttributeMap newResponseImmutableAttributeMap() {
        ImmutableAttributeMap.Builder builder = ImmutableAttributeMap.builder();

        builder.put(NaturalPersonSpec.Definitions.PERSON_IDENTIFIER);
        builder.put(NaturalPersonSpec.Definitions.CURRENT_FAMILY_NAME);
        builder.put(NaturalPersonSpec.Definitions.CURRENT_GIVEN_NAME);
        builder.put(NaturalPersonSpec.Definitions.DATE_OF_BIRTH);

        return builder.build();
    }

    final static String REQUEST_ISSUER = "https://testIssuer".toLowerCase();
    static final String RESPONSE_ISSUER = "http://Responder".toLowerCase();
    private byte[] generateTestSamlResponse() throws EIDASSAMLEngineException {

        String destination = "http://proxyservice.gov.xx/EidasNode/ColleagueRequest";
        String assertConsumerUrl = "http://connector.gov.xx/EidasNode/ColleagueResponse";

        String spName = "University of Oxford";
        String spSector = "EDU001";
        String spInstitution = "OXF001";
        String spApplication = "APP001";
        String spCountry = "EN";

        String spId = "EDU001-APP001-APP001";
        int QAAL = 3;

		EidasAuthenticationRequest request = EidasAuthenticationRequest.builder()
                .id("QDS2QFD") // Common part
                .assertionConsumerServiceURL(assertConsumerUrl)
                .destination(destination)
                .issuer(REQUEST_ISSUER)
                .providerName(spName)
                .serviceProviderCountryCode(spCountry)
                .citizenCountryCode("ES")
                .requestedAttributes(newResponseImmutableAttributeMap())
                .levelOfAssurance(LevelOfAssurance.HIGH)
                .build();

        byte[] authRequest;
        IAuthenticationRequest authenRequest = null;

        try {
            authRequest = engine.generateRequestMessage(request, null).getMessageBytes();

            authenRequest = engine.unmarshallRequestAndValidate(authRequest, "ES",Arrays.asList(REQUEST_ISSUER));

        } catch (EIDASSAMLEngineException e) {
            fail("Error create EidasAuthenticationRequest");
        }

        String ipAddress = "111.222.333.444";

        ImmutableAttributeMap.Builder attributeMapBuilder = ImmutableAttributeMap.builder();

        attributeMapBuilder.put(NaturalPersonSpec.Definitions.PERSON_IDENTIFIER,
                new StringAttributeValue("personIdentifierValue"));
        attributeMapBuilder.put(NaturalPersonSpec.Definitions.CURRENT_FAMILY_NAME,
                new StringAttributeValue("currentFamilyNameValue"));
        attributeMapBuilder.put(NaturalPersonSpec.Definitions.CURRENT_GIVEN_NAME,
                new StringAttributeValue("currentGivenNameValue"));

        DateTime birthDate = new DateTime()
                .withDayOfMonth(16)
                .withMonthOfYear(12)
                .withYear(2008);

        attributeMapBuilder.put(NaturalPersonSpec.Definitions.DATE_OF_BIRTH,
                new DateTimeAttributeValue(birthDate));

		AuthenticationResponse response = new AuthenticationResponse.Builder().id("RESPONSE_ID_TO_QDS2QFD")
                .inResponseTo("QDS2QFD")
                .issuer(RESPONSE_ISSUER)
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .levelOfAssurance("high")
                .ipAddress("123.123.123.123")
                .attributes(attributeMapBuilder.build())
                .build();

        IResponseMessage responseMessage = engine.generateResponseMessage(authenRequest, response, false, ipAddress);

        return responseMessage.getMessageBytes();
    }
}
