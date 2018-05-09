/*
 * Copyright (c) 2017 by European Commission
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

package eu.eidas.node.auth.service.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.eidas.auth.commons.*;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.cache.ConcurrentMapServiceDefaultImpl;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequestCorrelationMap;
import eu.eidas.node.auth.service.AUSERVICE;
import eu.eidas.node.auth.service.AUSERVICESAML;
import eu.eidas.node.auth.service.ISERVICECitizenService;
import eu.eidas.node.auth.service.ISERVICESAMLService;
import eu.eidas.node.auth.util.tests.TestingConstants;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Functional testing class to {@link AUSERVICE}.
 */
public class AUSERVICETestCase {

    /**
     * Properties values for testing proposes.
     */
    private static Properties CONFIGS = new Properties();

    /**
     * Initialising class variables.
     *
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void runBeforeClass() throws Exception {
        CONFIGS.setProperty(EidasErrorKey.SERVICE_REDIRECT_URL.errorCode(), "203006");
        CONFIGS.setProperty(EidasErrorKey.SERVICE_REDIRECT_URL.errorMessage(), "invalid.service.redirectUrl");

        CONFIGS.setProperty(EidasErrorKey.AUTHENTICATION_FAILED_ERROR.errorCode(), "003002");
        CONFIGS.setProperty(EidasErrorKey.AUTHENTICATION_FAILED_ERROR.errorMessage(), "authentication.failed");

        CONFIGS.setProperty(EidasErrorKey.INVALID_ATTRIBUTE_LIST.errorCode(), "203001");
        CONFIGS.setProperty(EidasErrorKey.INVALID_ATTRIBUTE_LIST.errorMessage(), "invalid.attrlist");

        //EIDASUtil.createInstance(CONFIGS);
    }

    private static WebRequest newWebRequest(String paramName1,
                                            String paramValue1,
                                            String paramName2,
                                            String paramValue2) {
        return new IncomingRequest(
                BindingMethod.POST,
                ImmutableMap.of(paramName1, ImmutableList.of(paramValue1), paramName2, ImmutableList.of(paramValue2)),
                "127.0.0.1",
                null);
    }

    /**
     * Test method for AUSERVICE.processAuthenticationRequest(Map)}. Testing an invalid saml token. Must
     * throw {@link InvalidParameterEIDASException}.
     */
    @Test(expected = InvalidParameterEIDASException.class)
    public void testProcessAuthenticationRequestInvalidSaml() {

        ISERVICESAMLService mockSamlService = mock(ISERVICESAMLService.class);
        WebRequest mockParameters = newWebRequest("http://www.stork.gov.eu/1.0/isAgeOver", "", "age", "");

        AUSERVICE auservice = new AUSERVICE();
        auservice.setSamlService(mockSamlService);

        CorrelationMap<StoredAuthenticationRequest> correlationMap =
                new StoredAuthenticationRequestCorrelationMap(new ConcurrentMapServiceDefaultImpl());

        auservice.processAuthenticationRequest(mockParameters, "relayState", correlationMap,
                                               TestingConstants.USER_IP_CONS.toString());
    }

    /**
     * Test method for AUSERVICE#processAuthenticationRequest(Map)}. Testing an invalid saml token. Must
     * throw {@link InvalidParameterEIDASException}.
     */
    @Test(expected = InvalidParameterEIDASException.class)
    public void testProcessAuthenticationRequestErrorValidatingSaml() {

        ISERVICESAMLService mockSamlService = mock(ISERVICESAMLService.class);
        //when(mockSamlService.getSAMLToken(anyString())).thenReturn(new byte[0]);

        when(mockSamlService.processConnectorRequest(eq("POST"), (byte[]) any(),
                                                     eq(TestingConstants.USER_IP_CONS.toString()),
                                                     eq("relayState"))).thenThrow(
                new InvalidParameterEIDASException(TestingConstants.ERROR_CODE_CONS.toString(),
                                                   TestingConstants.ERROR_MESSAGE_CONS.toString()));

        CorrelationMap<StoredAuthenticationRequest> correlationMap =
                new StoredAuthenticationRequestCorrelationMap(new ConcurrentMapServiceDefaultImpl());
        WebRequest mockParameters = mock(WebRequest.class);
        when(mockParameters.getMethod()).thenReturn(BindingMethod.POST);
        AUSERVICE auservice = new AUSERVICE();
        auservice.setSamlService(mockSamlService);
        auservice.processAuthenticationRequest(mockParameters, "relayState", correlationMap,
                                               TestingConstants.USER_IP_CONS.toString());
    }

    /**
     * Test method for processAuthenticationRequest(Map)}. Testing an invalid qaa level. Must throw
     * {@link EIDASServiceException}.
     */
    @Test(expected = InvalidParameterEIDASException.class)
    public void testProcessAuthenticationRequestInvalidQAA() {

        ISERVICESAMLService mockSamlService = mock(ISERVICESAMLService.class);
        /*when(mockSamlService.getSAMLToken(anyString())).thenReturn(new byte[0]);*/

        when(mockSamlService.processConnectorRequest(eq("POST"), (byte[]) any(),
                                                     eq(TestingConstants.USER_IP_CONS.toString()),

                                                     eq("relayState"))).thenThrow(
                new EIDASServiceException(TestingConstants.ERROR_CODE_CONS.toString(),
                                          TestingConstants.ERROR_MESSAGE_CONS.toString(),
                                          TestingConstants.SAML_TOKEN_CONS.toString()));

        CorrelationMap<StoredAuthenticationRequest> correlationMap =
                new StoredAuthenticationRequestCorrelationMap(new ConcurrentMapServiceDefaultImpl());
        WebRequest mockParameters = mock(WebRequest.class);
        when(mockParameters.getMethod()).thenReturn(BindingMethod.POST);
        AUSERVICE auservice = new AUSERVICE();
        auservice.setSamlService(mockSamlService);
        auservice.processAuthenticationRequest(mockParameters, "relayState", correlationMap,
                                               TestingConstants.USER_IP_CONS.toString());
    }

    /**
     * Test method for processAuthenticationRequest(Map)}. Must succeed.
     */
    @Test
    public void testProcessAuthenticationRequest() {

        ISERVICESAMLService mockSamlService = mock(ISERVICESAMLService.class);

        final EidasAuthenticationRequest.Builder eidasAuthenticationRequestBuilder =
                EidasAuthenticationRequest.builder();
        eidasAuthenticationRequestBuilder.id(TestingConstants.SAML_ID_CONS.toString())
                .destination(TestingConstants.REQUEST_DESTINATION_CONS.toString())
                .issuer(TestingConstants.REQUEST_ISSUER_CONS.toString())
                .citizenCountryCode(TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString());

        IAuthenticationRequest authData = eidasAuthenticationRequestBuilder.build();

        when(mockSamlService.processConnectorRequest(eq("POST"), (byte[]) any(),
                                                     eq(TestingConstants.USER_IP_CONS.toString()),
                                                     eq("relayState"))).thenReturn(authData);

        CorrelationMap<StoredAuthenticationRequest> correlationMap =
                new StoredAuthenticationRequestCorrelationMap(new ConcurrentMapServiceDefaultImpl());
        WebRequest mockParameters = mock(WebRequest.class);
        when(mockParameters.getMethod()).thenReturn(BindingMethod.POST);
        when(mockParameters.getRemoteIpAddress()).thenReturn(TestingConstants.USER_IP_CONS.toString());
        when(mockParameters.getEncodedLastParameterValue(EidasParameterKeys.SAML_REQUEST)).thenReturn(
                EidasStringUtil.encodeToBase64("test".getBytes()));
        AUSERVICE auservice = new AUSERVICE();
        auservice.setSamlService(mockSamlService);

        ISERVICECitizenService mockCitizenService = mock(ISERVICECitizenService.class);
        auservice.setCitizenService(mockCitizenService);
        assertSame(authData, auservice.processAuthenticationRequest(mockParameters, "relayState", correlationMap,
                                                                    TestingConstants.USER_IP_CONS.toString()));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Test
    public void testGenerateSamlTokenFail() {
        AUSERVICE auservice = new AUSERVICE();

        IAuthenticationRequest authData = EidasAuthenticationRequest.builder()
                .destination(TestingConstants.REQUEST_DESTINATION_CONS.toString())
                .id(TestingConstants.REQUEST_ID_CONS.toString())
                .issuer(TestingConstants.ISSUER_CONS.toString())
                .citizenCountryCode(TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())
                .build();

        ISERVICESAMLService mockSamlService = mock(ISERVICESAMLService.class);
        when(mockSamlService.generateErrorAuthenticationResponse((IAuthenticationRequest) any(), anyString(),
                                                                 anyString(), anyString(), anyString(), anyString(),
                                                                 anyBoolean())).thenReturn(new byte[0]);
        auservice.setSamlService(mockSamlService);
        assertEquals("", auservice.generateSamlTokenFail(authData, TestingConstants.ERROR_CODE_CONS.toString(),
                                                         EidasErrorKey.AUTHENTICATION_FAILED_ERROR,
                                                         TestingConstants.USER_IP_CONS.toString()));
    }

    /**
     * Test method for {@link AUSERVICE#updateResponseAttributes(IAuthenticationRequest, ImmutableAttributeMap)}  to
     * check if the attributes with AttributeDefinition equal to EidasSpec.Definitions.PERSON_IDENTIFIER and
     * EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER have the correct prefix got from EidasAuthenticationRequest
     * originCountryCode and AUSERVICESAML countryCode values. Must succeed.
     */
    @Test
    public void testUpdateResponseAttributes()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final String countryCode = "ES";
        final String originCountryCode = "PT";

        final String regex = "[A-Z]{2}/[zA-Z]{2}/.*";

        ImmutableAttributeMap result = getImmutableAttributeMapUpdatedResponseAttributes(countryCode, originCountryCode,
                                                                                         EidasSpec.Definitions.PERSON_IDENTIFIER);
        assertTrue(result.getFirstAttributeValue(EidasSpec.Definitions.PERSON_IDENTIFIER).getValue().matches(regex));
        assertEquals("ES/PT/12345678",
                     result.getFirstAttributeValue(EidasSpec.Definitions.PERSON_IDENTIFIER).getValue());

        result = getImmutableAttributeMapUpdatedResponseAttributes(countryCode, originCountryCode,
                                                                   EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER);
        assertTrue(
                result.getFirstAttributeValue(EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER).getValue().matches(regex));
        assertEquals("ES/PT/12345678",
                     result.getFirstAttributeValue(EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER).getValue());
    }

    /**
     * Test method for {@link AUSERVICE#updateResponseAttributes(IAuthenticationRequest, ImmutableAttributeMap)}  to
     * check if the attributes with AttributeDefinition equal to EidasSpec.Definitions.PERSON_IDENTIFIER and
     * EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER have the correct prefix got from EidasAuthenticationRequest
     * originCountryCode and AUSERVICESAML countryCode values. Must Fail.
     */
    @Test
    public void testUpdateResponseAttributesFail()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final String countryCode = "PT";
        final String originCountryCode = null;

        final String regex = "[A-Z]{2}/[zA-Z]{2}/.*";

        final AttributeDefinition<String> personIdentifier = EidasSpec.Definitions.PERSON_IDENTIFIER;
        ImmutableAttributeMap result =
                getImmutableAttributeMapUpdatedResponseAttributes(countryCode, originCountryCode, personIdentifier);
        assertFalse(result.getFirstAttributeValue(personIdentifier).getValue().matches(regex));
        assertEquals("PT/null/12345678", result.getFirstAttributeValue(personIdentifier).getValue());

        final AttributeDefinition<String> legalPersonIdentifier = EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER;
        result = getImmutableAttributeMapUpdatedResponseAttributes(countryCode, originCountryCode,
                                                                   legalPersonIdentifier);
        assertFalse(result.getFirstAttributeValue(legalPersonIdentifier).getValue().matches(regex));
        assertEquals("PT/null/12345678", result.getFirstAttributeValue(legalPersonIdentifier).getValue());
    }

    /**
     * Auxiliar method to be used by test methods to invokes the method {@link AUSERVICE#updateResponseAttributes(IAuthenticationRequest,
     * ImmutableAttributeMap)}.
     *
     * @param countryCode the country code of the AUSERVICESAML
     * @param originCountryCode the origin country code of the request
     * @param identifier
     * @return ImmutableAttributeMap with the value
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private ImmutableAttributeMap getImmutableAttributeMapUpdatedResponseAttributes(final String countryCode,
                                                                                    final String originCountryCode,
                                                                                    AttributeDefinition<String> identifier)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final AUSERVICE underTest = new AUSERVICE();
        AUSERVICESAML auservicesaml = new AUSERVICESAML();
        auservicesaml.setCountryCode(countryCode);
        underTest.setSamlService(auservicesaml);

        Class[] parameterTypes = new Class[2];
        parameterTypes[0] = IAuthenticationRequest.class;
        parameterTypes[1] = ImmutableAttributeMap.class;

        Method m = underTest.getClass().getDeclaredMethod("updateResponseAttributes", parameterTypes);
        m.setAccessible(true);

        Object[] methodParameters = new Object[2];
        methodParameters[0] = EidasAuthenticationRequest.builder()
                .destination(TestingConstants.REQUEST_DESTINATION_CONS.toString())
                .id(TestingConstants.REQUEST_ID_CONS.toString())
                .issuer(TestingConstants.REQUEST_ISSUER_CONS.toString())
                .citizenCountryCode(TestingConstants.REQUEST_CITIZEN_COUNTRY_CODE_CONS.toString())
                .originCountryCode(originCountryCode)
                .build();

        methodParameters[1] = ImmutableAttributeMap.of(identifier, new StringAttributeValue("12345678", false));

        return (ImmutableAttributeMap) m.invoke(underTest, methodParameters);
    }

}
