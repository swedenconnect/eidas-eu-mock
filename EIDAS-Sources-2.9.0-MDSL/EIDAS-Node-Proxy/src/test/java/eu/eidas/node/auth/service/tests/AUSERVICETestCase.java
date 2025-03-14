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
package eu.eidas.node.auth.service.tests;

import eu.eidas.auth.cache.ConcurrentMapJcacheServiceDefaultImpl;
import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.light.impl.LightResponse;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.Gender;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.ProxyBeanNames;
import eu.eidas.node.auth.service.AUSERVICE;
import eu.eidas.node.auth.service.AUSERVICESAML;
import eu.eidas.node.auth.service.AUSERVICEUtil;
import eu.eidas.node.auth.service.ISERVICECitizenService;
import eu.eidas.node.auth.service.ISERVICESAMLService;
import eu.eidas.node.auth.util.tests.TestingConstants;
import eu.eidas.node.service.exceptions.ProxyServiceError;
import eu.eidas.node.utils.ReflectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ResourceBundleMessageSource;

import javax.annotation.Nonnull;
import javax.cache.Cache;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link AUSERVICE}
 */
public class AUSERVICETestCase {

    /**
     * Properties values for testing proposes.
     */
    private static Properties CONFIGS = new Properties();

    private AUSERVICEUtil auserviceUtil;

    private final static String CONSENT_UNSPECIFIED = "urn:oasis:names:tc:SAML:2.0:consent:unspecified";

    private final static String CONSENT_OBTAINED = "urn:oasis:names:tc:SAML:2.0:consent:obtained";

    private ApplicationContext oldContext = null;

    private ApplicationContext mockApplicationContext = Mockito.mock(ApplicationContext.class);

    /**
     * Initialising class variables.
     */
    @BeforeClass
    public static void runBeforeClass() {
        CONFIGS.setProperty(EidasErrorKey.SERVICE_REDIRECT_URL.errorCode(), "203006");
        CONFIGS.setProperty(EidasErrorKey.SERVICE_REDIRECT_URL.errorMessage(), "invalid.service.redirectUrl");

        CONFIGS.setProperty(EidasErrorKey.AUTHENTICATION_FAILED_ERROR.errorCode(), "003002");
        CONFIGS.setProperty(EidasErrorKey.AUTHENTICATION_FAILED_ERROR.errorMessage(), "authentication.failed");

        CONFIGS.setProperty(EidasErrorKey.INVALID_ATTRIBUTE_LIST.errorCode(), "203001");
        CONFIGS.setProperty(EidasErrorKey.INVALID_ATTRIBUTE_LIST.errorMessage(), "invalid.attrlist");
    }

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        auserviceUtil = new AUSERVICEUtil();
        auserviceUtil.setConfigs(CONFIGS);

        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        oldContext = (ApplicationContext) contextField.get(null);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);

        mockResourceBundleMessageSource();
    }

    @After
    public void tearDown() {
        if (oldContext != null) {
            ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", oldContext);
            oldContext = null;
        }
    }

    private static WebRequest newWebRequest(String paramName1,
                                            String paramValue1,
                                            String paramName2,
                                            String paramValue2) {
        return new IncomingRequest(
                BindingMethod.POST,
                Map.of(paramName1, List.of(paramValue1), paramName2, List.of(paramValue2)),
                "127.0.0.1",
                null);
    }

    /**
     * Test method for AUSERVICE.processAuthenticationRequest(Map)}. Testing an invalid saml token. Must
     * throw {@link ProxyServiceError}.
     */
    @Test(expected = ProxyServiceError.class)
    public void testProcessAuthenticationRequestInvalidSaml() {

        ISERVICESAMLService mockSamlService = mock(ISERVICESAMLService.class);
        WebRequest mockParameters = newWebRequest("http://www.stork.gov.eu/1.0/isAgeOver", "", "age", "");

        AUSERVICE auservice = new AUSERVICE();
        auservice.setSamlService(mockSamlService);

        Cache<String, StoredAuthenticationRequest> requestCorrelationCache = new ConcurrentMapJcacheServiceDefaultImpl().getConfiguredCache();

        auservice.processAuthenticationRequest(mockParameters, "relayState", requestCorrelationCache,
                TestingConstants.USER_IP_CONS.toString());
    }

    /**
     * Test method for AUSERVICE#processAuthenticationRequest(Map)}. Testing an invalid saml token. Must
     * throw {@link ProxyServiceError}.
     */
    @Test(expected = ProxyServiceError.class)
    public void testProcessAuthenticationRequestErrorValidatingSaml() {

        ISERVICESAMLService mockSamlService = mock(ISERVICESAMLService.class);
        //when(mockSamlService.getSAMLToken(anyString())).thenReturn(new byte[0]);

        when(mockSamlService.processConnectorRequest(eq("POST"), (byte[]) any(),
                eq(TestingConstants.USER_IP_CONS.toString()),
                eq("relayState"))).thenThrow(
                new InvalidParameterEIDASException(TestingConstants.ERROR_CODE_CONS.toString(),
                        TestingConstants.ERROR_MESSAGE_CONS.toString()));

        WebRequest mockParameters = mock(WebRequest.class);
        when(mockParameters.getMethod()).thenReturn(BindingMethod.POST);
        AUSERVICE auservice = new AUSERVICE();
        auservice.setSamlService(mockSamlService);

        Cache<String, StoredAuthenticationRequest> requestCorrelationCache = new ConcurrentMapJcacheServiceDefaultImpl().getConfiguredCache();

        auservice.processAuthenticationRequest(mockParameters, "relayState", requestCorrelationCache,
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
                .citizenCountryCode(TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH);

        IAuthenticationRequest authData = eidasAuthenticationRequestBuilder.build();

        when(mockSamlService.processConnectorRequest(eq("POST"), (byte[]) any(),
                eq(TestingConstants.USER_IP_CONS.toString()),
                eq("relayState"))).thenReturn(authData);

        WebRequest mockParameters = mock(WebRequest.class);
        when(mockParameters.getMethod()).thenReturn(BindingMethod.POST);
        when(mockParameters.getRemoteIpAddress()).thenReturn(TestingConstants.USER_IP_CONS.toString());
        when(mockParameters.getEncodedLastParameterValue(EidasParameterKeys.SAML_REQUEST)).thenReturn(
                EidasStringUtil.encodeToBase64("test".getBytes()));
        AUSERVICE auservice = new AUSERVICE();
        auservice.setSamlService(mockSamlService);

        ISERVICECitizenService mockCitizenService = mock(ISERVICECitizenService.class);
        auservice.setCitizenService(mockCitizenService);

        Cache<String, StoredAuthenticationRequest> requestCorrelationCache = new ConcurrentMapJcacheServiceDefaultImpl().getConfiguredCache();
        assertSame(authData, auservice.processAuthenticationRequest(mockParameters, "relayState", requestCorrelationCache,
                TestingConstants.USER_IP_CONS.toString()));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for {@link AUSERVICE#processIdpResponse(WebRequest, StoredAuthenticationRequest, ILightResponse)} to
     * check if the consent value is conveyed correctly to the SAML request
     * <p>
     * Must succeed.
     */
    @Test
    public void testProcessIdpResponseConsentObtained() {
        WebRequest webRequest = mock(IncomingRequest.class);
        String personIdentifier = "12345678";
        ImmutableAttributeMap attributes = getPersonIdentifierAttributeMap(personIdentifier);
        String responseStatusCode = TestingConstants.RESPONSE_STATUS_CODE_SUCCESS_CONS.toString();
        setConfigPublishedLoA(ILevelOfAssurance.EIDAS_LOA_HIGH, ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL, "loa:nonNotified");
        ILightResponse idpResponse = createDummyLightResponseNotifiedLoaSubstantial(responseStatusCode, attributes);
        ILightResponse consentResponse = LightResponse.builder(idpResponse)
                .consent(CONSENT_OBTAINED)
                .issuer(TestingConstants.REQUEST_ISSUER_CONS.toString())
                .build();

        IAuthenticationRequest originalRequest = createDummyAuthenticationRequestNotifiedLoa(attributes, NotifiedLevelOfAssurance.SUBSTANTIAL);
        StoredAuthenticationRequest storedRequest = StoredAuthenticationRequest.builder()
                .request(originalRequest)
                .remoteIpAddress("localhost:8080")
                .build();

        final AUSERVICE testedService = new AUSERVICE();
        testedService.setServiceMetadataUrl("localhost:8080");
        AUSERVICESAML auservicesaml = mockService();
        testedService.setSamlService(auservicesaml);
        testedService.setServiceUtil(auserviceUtil);
        ArgumentCaptor<AuthenticationResponse> authResponseCaptor = ArgumentCaptor.forClass(AuthenticationResponse.class);

        Mockito.when(webRequest.getRemoteIpAddress()).thenReturn("localhost:8080");

        testedService.processIdpResponse(webRequest, storedRequest, consentResponse);

        Mockito.verify(auservicesaml, times(1)).processIdpSpecificResponse(any(), authResponseCaptor.capture(), anyString());
        String actualConsent = authResponseCaptor.getValue().getConsent();
        Assert.assertEquals(CONSENT_OBTAINED, actualConsent);
    }

    /**
     * Test method for {@link AUSERVICE#validateIdpResponse(ILightResponse, IAuthenticationRequest)} to
     * check if the validation of the idpResponse passes for a Response
     * with a valid PersonIdentifier.
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidateIdpResponse() throws Exception {
        String personIdentifier = "12345678";
        ImmutableAttributeMap attributes = getPersonIdentifierAttributeMap(personIdentifier);
        String responseStatusCode = TestingConstants.RESPONSE_STATUS_CODE_SUCCESS_CONS.toString();

        setConfigPublishedLoA(ILevelOfAssurance.EIDAS_LOA_HIGH, ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL, "loa:nonNotified");
        ILightResponse idpResponse = createDummyLightResponseNotifiedLoaSubstantial(responseStatusCode, attributes);
        IAuthenticationRequest originalRequest = createDummyAuthenticationRequestNotifiedLoa(attributes, NotifiedLevelOfAssurance.SUBSTANTIAL);

        final AUSERVICE testedService = new AUSERVICE();
        AUSERVICESAML auservicesaml = mockService();
        testedService.setSamlService(auservicesaml);
        testedService.setServiceUtil(auserviceUtil);

        Optional<EidasErrorKey> validationError = getOptionalErrorFromIdpResponseValidation(testedService,
                idpResponse, originalRequest);

        Assert.assertFalse(validationError.isPresent());
    }

    /**
     * Test method for {@link AUSERVICE#validateIdpResponse(ILightResponse, IAuthenticationRequest)} to
     * check if the validation of the idpResponse passes for a Response
     * with a PersonIdentifier that includes a whitespace.
     * <p>
     * Must throw a ProxyServiceError.
     */
    @Test(expected = ProxyServiceError.class)
    public void testValidateIdpResponseWithWhitespaceInUID() throws Throwable {
        String personIdentifier = "1234 5678";
        ImmutableAttributeMap attributes = getPersonIdentifierAttributeMap(personIdentifier);

        String responseStatusCode = TestingConstants.RESPONSE_STATUS_CODE_SUCCESS_CONS.toString();
        ILightResponse idpResponse = createDummyLightResponseNotifiedLoaSubstantial(responseStatusCode, attributes);

        IAuthenticationRequest originalRequest = createDummyAuthenticationRequestNotifiedLoa(attributes, NotifiedLevelOfAssurance.SUBSTANTIAL);

        final AUSERVICE testedService = new AUSERVICE();
        AUSERVICESAML auservicesaml = mockService();
        testedService.setSamlService(auservicesaml);

        try {
            getOptionalErrorFromIdpResponseValidation(testedService, idpResponse, originalRequest);
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getCause() instanceof ProxyServiceError);
            String expectedErrorCode = EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_VALUE.errorCode());
            String actualErrorCode = ((ProxyServiceError) e.getCause()).getErrorCode();
            Assert.assertEquals(expectedErrorCode, actualErrorCode);
            throw e.getCause();
        }
    }

    /**
     * Test method for {@link AUSERVICE#validateIdpResponse(ILightResponse, IAuthenticationRequest)} to
     * check if the validation of the idpResponse passes for a Response
     * with an PersonIdentifier exceeding 256 characters.
     * <p>
     * Must throw a ProxyServiceError.
     */
    @Test(expected = ProxyServiceError.class)
    public void testValidateIdpResponseWithTooLongUID() throws Throwable {
        String personIdentifier = "12345678" + RandomStringUtils.randomAlphanumeric(256);
        ImmutableAttributeMap attributes = getPersonIdentifierAttributeMap(personIdentifier);

        String responseStatusCode = TestingConstants.RESPONSE_STATUS_CODE_SUCCESS_CONS.toString();
        ILightResponse idpResponse = createDummyLightResponseNotifiedLoaSubstantial(responseStatusCode, attributes);

        IAuthenticationRequest originalRequest = createDummyAuthenticationRequestNotifiedLoa(attributes, NotifiedLevelOfAssurance.SUBSTANTIAL);

        final AUSERVICE testedService = new AUSERVICE();
        AUSERVICESAML auservicesaml = mockService();
        testedService.setSamlService(auservicesaml);

        try {
            getOptionalErrorFromIdpResponseValidation(testedService, idpResponse, originalRequest);
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getCause() instanceof ProxyServiceError);
            String expectedErrorCode = EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_VALUE.errorCode());
            String actualErrorCode = ((ProxyServiceError) e.getCause()).getErrorCode();
            Assert.assertEquals(expectedErrorCode, actualErrorCode);
            throw e.getCause();
        }
    }

    /**
     * Test method for {@link AUSERVICE#validateIdpResponse(ILightResponse, IAuthenticationRequest)} to
     * check if the validation of the idpResponse passes for a Response
     * with an invalid Representation response (more than 2 MDS).
     * <p>
     * Must succeed
     */
    @Test
    public void testValidateIdpResponseWithInvalidRepresentationResponse() throws Throwable {
        AttributeDefinition identifier = EidasSpec.Definitions.PERSON_IDENTIFIER;
        AttributeDefinition representativePersonIdentifier = EidasSpec.Definitions.REPV_PERSON_IDENTIFIER;
        AttributeDefinition representativeLegalPersonIdentifier = EidasSpec.Definitions.REPV_LEGAL_PERSON_IDENTIFIER;
        StringAttributeValue identifierValue = new StringAttributeValue("1234 5678");

        ImmutableAttributeMap attributes = ImmutableAttributeMap.builder()
                .put(identifier, identifierValue)
                .put(representativePersonIdentifier, identifierValue)
                .put(representativeLegalPersonIdentifier, identifierValue)
                .build();

        String responseStatusCode = TestingConstants.RESPONSE_STATUS_CODE_SUCCESS_CONS.toString();
        ILightResponse idpResponse = createDummyLightResponseNotifiedLoaSubstantial(responseStatusCode, attributes);

        IAuthenticationRequest originalRequest = createDummyAuthenticationRequestNotifiedLoa(attributes, NotifiedLevelOfAssurance.SUBSTANTIAL);

        final AUSERVICE testedService = new AUSERVICE();
        AUSERVICESAML auservicesaml = Mockito.mock(AUSERVICESAML.class);
        Mockito.when(auservicesaml.checkRepresentationResponse(Mockito.any())).thenReturn(false);
        testedService.setSamlService(auservicesaml);

        Optional<EidasErrorKey> actualError = getOptionalErrorFromIdpResponseValidation(testedService, idpResponse, originalRequest);

        Assert.assertTrue(actualError.isPresent());

        String expectedErrorCode = EidasErrorKey.INVALID_ATTRIBUTE_LIST.errorCode();
        String actualErrorCode = actualError.get().errorCode();
        Assert.assertEquals(expectedErrorCode, actualErrorCode);
    }

    /**
     * Test method for {@link AUSERVICE#validateIdpResponse(ILightResponse, IAuthenticationRequest)} to
     * check if the validation of the idpResponse passes for a Response
     * with a non notified level of assurance
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidateIdpResponseWithNonNotifiedLoa() throws Exception {
        String personIdentifier = "12345678";
        ImmutableAttributeMap attributes = getPersonIdentifierAttributeMap(personIdentifier);
        String responseStatusCode = TestingConstants.RESPONSE_STATUS_CODE_SUCCESS_CONS.toString();

        setConfigPublishedLoA(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:nonNotified");
        ILightResponse idpResponse = createDummyLightResponseNonNotifiedLoa(responseStatusCode, attributes);
        IAuthenticationRequest originalRequest = createDummyAuthenticationRequestNonNotifiedLoa(attributes, "loa:nonNotified");

        final AUSERVICE testedService = new AUSERVICE();
        AUSERVICESAML auservicesaml = mockService();
        testedService.setSamlService(auservicesaml);
        testedService.setServiceUtil(auserviceUtil);

        Optional<EidasErrorKey> validationError = getOptionalErrorFromIdpResponseValidation(testedService,
                idpResponse, originalRequest);

        Assert.assertFalse(validationError.isPresent());
    }

    /**
     * Test method for {@link AUSERVICE#validateIdpResponse(ILightResponse, IAuthenticationRequest)} to
     * check if the validation of the idpResponse passes for a Response
     * with a non notified level of assurance that does not match the Level of assurance in the request
     * <p>
     * Must return a validation error.
     */
    @Test
    public void testValidateIdpResponseWithNonNotifiedLoaMisMatch() throws Exception {
        String personIdentifier = "12345678";
        ImmutableAttributeMap attributes = getPersonIdentifierAttributeMap(personIdentifier);
        String responseStatusCode = TestingConstants.RESPONSE_STATUS_CODE_SUCCESS_CONS.toString();

        setConfigPublishedLoA(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:nonNotified");
        ILightResponse idpResponse = createDummyLightResponseNonNotifiedLoa(responseStatusCode, attributes);
        IAuthenticationRequest originalRequest = createDummyAuthenticationRequestNonNotifiedLoa(attributes, "loa:wrongNonNotified");

        final AUSERVICE testedService = new AUSERVICE();
        AUSERVICESAML auservicesaml = mockService();
        testedService.setSamlService(auservicesaml);
        testedService.setServiceUtil(auserviceUtil);

        Optional<EidasErrorKey> validationError = getOptionalErrorFromIdpResponseValidation(testedService,
                idpResponse, originalRequest);

        Assert.assertTrue(validationError.isPresent());
        Assert.assertEquals(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE, validationError.get());
    }

    /**
     * Test method for {@link AUSERVICE#validateIdpResponse(ILightResponse, IAuthenticationRequest)} to
     * check if the validation of the idpResponse passes for a Response
     * with a notified level of assurance that is lower than the Level of assurance in the request
     * <p>
     * Must return a validation error.
     */
    @Test
    public void testValidateIdpResponseWithNotifiedLoaTooLow() throws Exception {
        String personIdentifier = "12345678";
        ImmutableAttributeMap attributes = getPersonIdentifierAttributeMap(personIdentifier);
        String responseStatusCode = TestingConstants.RESPONSE_STATUS_CODE_SUCCESS_CONS.toString();

        setConfigPublishedLoA(ILevelOfAssurance.EIDAS_LOA_HIGH, ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL, "loa:nonNotified");
        ILightResponse idpResponse = createDummyLightResponseNotifiedLoaSubstantial(responseStatusCode, attributes);
        IAuthenticationRequest originalRequest = createDummyAuthenticationRequestNotifiedLoa(attributes, NotifiedLevelOfAssurance.HIGH);

        final AUSERVICE testedService = new AUSERVICE();
        AUSERVICESAML auservicesaml = mockService();
        testedService.setSamlService(auservicesaml);
        testedService.setServiceUtil(auserviceUtil);

        Optional<EidasErrorKey> validationError = getOptionalErrorFromIdpResponseValidation(testedService,
                idpResponse, originalRequest);

        Assert.assertTrue(validationError.isPresent());
        Assert.assertEquals(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE, validationError.get());
    }

    /**
     * Test method for {@link AUSERVICE#validateIdpResponse(ILightResponse, IAuthenticationRequest)} to
     * check if the validation of the idpResponse passes for a Response
     * with a notified level of assurance that is equal or higher than the notified Level of assurance in the request
     * when the request contains both notified and non notified levels of assurance
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidateIdpResponseWithMixedLoaNotifiedResponse() throws Exception {
        String personIdentifier = "12345678";
        ImmutableAttributeMap attributes = getPersonIdentifierAttributeMap(personIdentifier);
        String responseStatusCode = TestingConstants.RESPONSE_STATUS_CODE_SUCCESS_CONS.toString();

        setConfigPublishedLoA(ILevelOfAssurance.EIDAS_LOA_HIGH, ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL, "loa:nonNotified");
        ILightResponse idpResponse = createDummyLightResponseNotifiedLoaSubstantial(responseStatusCode, attributes);
        IAuthenticationRequest originalRequest = createDummyAuthenticationRequestMixedLoa(attributes, NotifiedLevelOfAssurance.SUBSTANTIAL, "loa:nonNotified");

        final AUSERVICE testedService = new AUSERVICE();
        AUSERVICESAML auservicesaml = mockService();
        testedService.setSamlService(auservicesaml);
        testedService.setServiceUtil(auserviceUtil);

        Optional<EidasErrorKey> validationError = getOptionalErrorFromIdpResponseValidation(testedService,
                idpResponse, originalRequest);

        Assert.assertFalse(validationError.isPresent());
    }

    /**
     * Test method for {@link AUSERVICE#validateIdpResponse(ILightResponse, IAuthenticationRequest)} to
     * check if the validation of the idpResponse passes for a Response
     * with a non notified level of assurance that matches the non notified Level of assurance in the request
     * when the request contains both notified and non notified levels of assurance
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidateIdpResponseWithMixedLoaNonNotifiedResponse() throws Exception {
        String personIdentifier = "12345678";
        ImmutableAttributeMap attributes = getPersonIdentifierAttributeMap(personIdentifier);
        String responseStatusCode = TestingConstants.RESPONSE_STATUS_CODE_SUCCESS_CONS.toString();

        setConfigPublishedLoA(ILevelOfAssurance.EIDAS_LOA_HIGH, ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL, "loa:nonNotified");
        ILightResponse idpResponse = createDummyLightResponseNonNotifiedLoa(responseStatusCode, attributes);
        IAuthenticationRequest originalRequest = createDummyAuthenticationRequestMixedLoa(attributes, NotifiedLevelOfAssurance.SUBSTANTIAL, "loa:nonNotified");

        final AUSERVICE testedService = new AUSERVICE();
        AUSERVICESAML auservicesaml = mockService();
        testedService.setSamlService(auservicesaml);
        testedService.setServiceUtil(auserviceUtil);

        Optional<EidasErrorKey> validationError = getOptionalErrorFromIdpResponseValidation(testedService,
                idpResponse, originalRequest);

        Assert.assertFalse(validationError.isPresent());
    }

    /**
     * Test method for {@link AUSERVICE#validateIdpResponse(ILightResponse, IAuthenticationRequest)} to
     * check if the validation of the idpResponse passes for a Response
     * with a non notified level of assurance that does not match the non notified Level of assurance in the request
     * when the request contains both notified and non notified levels of assurance
     * <p>
     * Must return a validation error.
     */
    @Test
    public void testValidateIdpResponseWithMixedLoaNonNotifiedResponseMismatch() throws Exception {
        String personIdentifier = "12345678";
        ImmutableAttributeMap attributes = getPersonIdentifierAttributeMap(personIdentifier);
        String responseStatusCode = TestingConstants.RESPONSE_STATUS_CODE_SUCCESS_CONS.toString();

        setConfigPublishedLoA(ILevelOfAssurance.EIDAS_LOA_HIGH, ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL, "loa:nonNotified");
        final ILightResponse idpResponse = createDummyLightResponseNonNotifiedLoa(responseStatusCode, attributes);
        final IAuthenticationRequest originalRequest = createDummyAuthenticationRequestMixedLoa(attributes, NotifiedLevelOfAssurance.SUBSTANTIAL, "loa:wrongNotified");

        final AUSERVICE testedService = new AUSERVICE();
        AUSERVICESAML auservicesaml = mockService();
        testedService.setSamlService(auservicesaml);
        testedService.setServiceUtil(auserviceUtil);

        Optional<EidasErrorKey> validationError = getOptionalErrorFromIdpResponseValidation(testedService,
                idpResponse, originalRequest);

        Assert.assertTrue(validationError.isPresent());
        Assert.assertEquals(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE, validationError.get());
    }

    /**
     * Test method for {@link AUSERVICE#validateIdpResponse(ILightResponse, IAuthenticationRequest)} to
     * check if the validation of the idpResponse passes for a Response
     * with a notified level of assurance that is lower than the notified Level of assurance in the request
     * when the request contains both notified and non notified levels of assurance
     * <p>
     * Must return a validation error.
     */
    @Test
    public void testValidateIdpResponseWithMixedLoaNotifiedResponseTooLow() throws Exception {
        final String personIdentifier = "12345678";
        final ImmutableAttributeMap attributes = getPersonIdentifierAttributeMap(personIdentifier);
        final String responseStatusCode = TestingConstants.RESPONSE_STATUS_CODE_SUCCESS_CONS.toString();

        setConfigPublishedLoA(ILevelOfAssurance.EIDAS_LOA_HIGH, ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL, "loa:nonNotified");
        final ILightResponse idpResponse = createDummyLightResponseNotifiedLoaSubstantial(responseStatusCode, attributes);
        final IAuthenticationRequest originalRequest = createDummyAuthenticationRequestMixedLoa(attributes, NotifiedLevelOfAssurance.HIGH, "loa:nonNotified");

        final AUSERVICE testedService = new AUSERVICE();
        final AUSERVICESAML auservicesaml = mockService();
        testedService.setSamlService(auservicesaml);
        testedService.setServiceUtil(auserviceUtil);

        final Optional<EidasErrorKey> validationError = getOptionalErrorFromIdpResponseValidation(testedService,
                idpResponse, originalRequest);

        Assert.assertTrue(validationError.isPresent());
        Assert.assertEquals(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE, validationError.get());
    }

    /**
     * Test method for {@link AUSERVICE#validateIdpResponse(ILightResponse, IAuthenticationRequest)} to
     * check if the validation of the idpResponse returns a validation error
     * with a notified level of assurance that is not exact mentioned in the published loas
     * when the request contains both notified and non notified levels of assurance
     * <p>
     * Must succeed return a validation error.
     */
    @Test
    public void testValidateIdpResponseWithMixedLoaNotifiedNotExactInPublishedLoa() throws Exception {
        final String personIdentifier = "12345678";
        final ImmutableAttributeMap attributes = getPersonIdentifierAttributeMap(personIdentifier);
        final String responseStatusCode = TestingConstants.RESPONSE_STATUS_CODE_SUCCESS_CONS.toString();

        setConfigPublishedLoA(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:nonNotified");
        final ILightResponse idpResponse = createDummyLightResponseNotifiedLoaSubstantial(responseStatusCode, attributes);
        final IAuthenticationRequest originalRequest = createDummyAuthenticationRequestMixedLoa(attributes, NotifiedLevelOfAssurance.LOW, "loa:nonNotified");

        final AUSERVICE testedService = new AUSERVICE();
        final AUSERVICESAML auservicesaml = mockService();
        testedService.setSamlService(auservicesaml);
        testedService.setServiceUtil(auserviceUtil);

        final Optional<EidasErrorKey> validationError = getOptionalErrorFromIdpResponseValidation(testedService,
                idpResponse, originalRequest);

        Assert.assertTrue(validationError.isPresent());
        Assert.assertEquals(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE_UNPUBLISHED, validationError.get());
    }

    /**
     * Test method for {@link AUSERVICE#validateIdpResponse(ILightResponse, IAuthenticationRequest)} to
     * check if the validation of the idpResponse passes for a Response
     * with a notified level of assurance that is not published in the service metadata levels Of Assurance
     * <p>
     * Must return a validation error.
     */
    @Test
    public void testValidateIdpResponseWithResponseLoaNotInPublished() throws Exception {
        final String personIdentifier = "12345678";
        final ImmutableAttributeMap attributes = getPersonIdentifierAttributeMap(personIdentifier);
        final String responseStatusCode = TestingConstants.RESPONSE_STATUS_CODE_SUCCESS_CONS.toString();

        setConfigPublishedLoA(ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL);
        final ILightResponse idpResponse = createDummyLightResponse(responseStatusCode, attributes, "loa:nonNotified");
        final IAuthenticationRequest originalRequest = createDummyAuthenticationRequestMixedLoa(attributes, NotifiedLevelOfAssurance.SUBSTANTIAL, "loa:nonNotified");

        final AUSERVICE testedService = new AUSERVICE();
        final AUSERVICESAML auservicesaml = mockService();
        testedService.setSamlService(auservicesaml);
        testedService.setServiceUtil(auserviceUtil);

        final Optional<EidasErrorKey> validationError = getOptionalErrorFromIdpResponseValidation(testedService,
                idpResponse, originalRequest);

        Assert.assertTrue(validationError.isPresent());
        Assert.assertEquals(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE_UNPUBLISHED, validationError.get());
    }

    /**
     * Test method for {@link AUSERVICE#validateIdpResponse(ILightResponse, IAuthenticationRequest)} to
     * check if the validation of the idpResponse passes for a Response
     * with a notified level of assurance that is lower than the published Level of assurance in the service metadata
     * <p>
     * Must return a validation error.
     */
    @Test
    public void testValidateIdpResponseWithResponseLoaHigherThenPublished() throws Exception {
        final String personIdentifier = "12345678";
        final ImmutableAttributeMap attributes = getPersonIdentifierAttributeMap(personIdentifier);
        final String responseStatusCode = TestingConstants.RESPONSE_STATUS_CODE_SUCCESS_CONS.toString();

        setConfigPublishedLoA(ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL, "loa:nonNotified");
        final ILightResponse idpResponse = createDummyLightResponse(responseStatusCode, attributes, ILevelOfAssurance.EIDAS_LOA_HIGH);
        final IAuthenticationRequest originalRequest = createDummyAuthenticationRequestMixedLoa(attributes, NotifiedLevelOfAssurance.SUBSTANTIAL, "loa:nonNotified");

        final AUSERVICE testedService = new AUSERVICE();
        final AUSERVICESAML auservicesaml = mockService();
        testedService.setSamlService(auservicesaml);
        testedService.setServiceUtil(auserviceUtil);

        final Optional<EidasErrorKey> validationError = getOptionalErrorFromIdpResponseValidation(testedService,
                idpResponse, originalRequest);

        Assert.assertTrue(validationError.isPresent());
        Assert.assertEquals(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE_UNPUBLISHED, validationError.get());
    }


    private void setConfigPublishedLoA(String... loasStrings) {
        CONFIGS.setProperty(EIDASValues.EIDAS_SERVICE_LOA.toString(), String.join(";", loasStrings));
    }

    /**
     * Test method for
     * {@link AUSERVICE#validateIdpResponse(ILightResponse, IAuthenticationRequest)}
     * when idpResponse is null
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidateIdpResponseWithIdpResponseNull() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final AUSERVICE auService = new AUSERVICE();
        final AUSERVICESAML auServiceSaml = mockService();
        final ImmutableAttributeMap attributes = getPersonIdentifierAttributeMap("12345678");
        auService.setSamlService(auServiceSaml);
        auService.setServiceUtil(auserviceUtil);

        final IAuthenticationRequest originalRequest = createDummyAuthenticationRequestNonNotifiedLoa(attributes, "loa:wrongNonNotified");

        final Optional<EidasErrorKey> validationError = getOptionalErrorFromIdpResponseValidation(auService, null, originalRequest);
        Assert.assertTrue(validationError.isPresent());
        Assert.assertEquals(EidasErrorKey.INVALID_ATTRIBUTE_LIST, validationError.get());
    }

    /**
     * Test method for
     * {@link AUSERVICE#validateIdpResponse(ILightResponse, IAuthenticationRequest)}
     * when mandatory attribute is missing
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidateIdpResponseWithMissingMandatoryAttributes() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final AUSERVICE auService = new AUSERVICE();
        final AUSERVICESAML auServiceSaml = Mockito.mock(AUSERVICESAML.class);
        Mockito.when(auServiceSaml.checkRepresentationResponse(Mockito.any())).thenReturn(true);
        Mockito.when(auServiceSaml.checkMandatoryAttributes(Mockito.any(), Mockito.any())).thenReturn(false);

        final String responseStatusCode = TestingConstants.RESPONSE_STATUS_CODE_SUCCESS_CONS.toString();
        final ImmutableAttributeMap attributes = getPersonIdentifierAttributeMap("12345678");
        auService.setSamlService(auServiceSaml);
        auService.setServiceUtil(auserviceUtil);

        final ILightResponse idpResponse = createDummyLightResponse(responseStatusCode, attributes, ILevelOfAssurance.EIDAS_LOA_HIGH);
        final IAuthenticationRequest originalRequest = createDummyAuthenticationRequestNonNotifiedLoa(attributes, "loa:wrongNonNotified");

        final Optional<EidasErrorKey> validationError = getOptionalErrorFromIdpResponseValidation(auService, idpResponse, originalRequest);
        Assert.assertTrue(validationError.isPresent());
        Assert.assertEquals(EidasErrorKey.ATT_VERIFICATION_MANDATORY, validationError.get());
    }

    /**
     * Test method for
     * {@link AUSERVICE#validateIdpResponse(ILightResponse, IAuthenticationRequest)}
     * when mandatory attribute set is missing
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidateIdpResponseWithMissingMandatoryAttributeSet() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final AUSERVICE auService = new AUSERVICE();
        final AUSERVICESAML auServiceSaml = Mockito.mock(AUSERVICESAML.class);
        Mockito.when(auServiceSaml.checkRepresentationResponse(Mockito.any())).thenReturn(true);
        Mockito.when(auServiceSaml.checkMandatoryAttributes(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(auServiceSaml.checkMandatoryAttributeSet(Mockito.any())).thenReturn(false);

        final String responseStatusCode = TestingConstants.RESPONSE_STATUS_CODE_SUCCESS_CONS.toString();
        final ImmutableAttributeMap attributes = getPersonIdentifierAttributeMap("12345678");
        auService.setSamlService(auServiceSaml);
        auService.setServiceUtil(auserviceUtil);

        final ILightResponse idpResponse = createDummyLightResponse(responseStatusCode, attributes, ILevelOfAssurance.EIDAS_LOA_HIGH);
        final IAuthenticationRequest originalRequest = createDummyAuthenticationRequestNonNotifiedLoa(attributes, "loa:wrongNonNotified");

        final Optional<EidasErrorKey> validationError = getOptionalErrorFromIdpResponseValidation(auService, idpResponse, originalRequest);
        Assert.assertTrue(validationError.isPresent());
        Assert.assertEquals(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES, validationError.get());
    }

    @Nonnull
    private AUSERVICESAML mockService() {
        AUSERVICESAML auservicesaml = Mockito.mock(AUSERVICESAML.class);
        Mockito.when(auservicesaml.checkRepresentationResponse(Mockito.any())).thenReturn(true);
        Mockito.when(auservicesaml.checkMandatoryAttributes(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(auservicesaml.checkMandatoryAttributeSet(Mockito.any())).thenReturn(true);
        return auservicesaml;
    }

    private Optional<EidasErrorKey> getOptionalErrorFromIdpResponseValidation(AUSERVICE testedService,
                                                                              ILightResponse idpResponse, IAuthenticationRequest originalRequest) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Class[] parameterTypes = new Class[2];
        parameterTypes[0] = ILightResponse.class;
        parameterTypes[1] = IAuthenticationRequest.class;

        Method m = testedService.getClass().getDeclaredMethod("validateIdpResponse", ILightResponse.class, IAuthenticationRequest.class);
        m.setAccessible(true);

        Object[] methodParameters = new Object[2];
        methodParameters[0] = idpResponse;
        methodParameters[1] = originalRequest;

        return (Optional<EidasErrorKey>) m.invoke(testedService, methodParameters);

    }


    private IAuthenticationRequest createDummyAuthenticationRequestNotifiedLoa(ImmutableAttributeMap attributeMap, NotifiedLevelOfAssurance notifiedLoa) {
        return createDummyAuthenticationRequest(attributeMap, notifiedLoa.stringValue());
    }

    private IAuthenticationRequest createDummyAuthenticationRequestNonNotifiedLoa(ImmutableAttributeMap attributeMap, String nonNotifiedLoa) {
        return createDummyAuthenticationRequest(attributeMap, nonNotifiedLoa);
    }

    private IAuthenticationRequest createDummyAuthenticationRequestMixedLoa(ImmutableAttributeMap attributeMap, NotifiedLevelOfAssurance notifiedLoa, String nonNotifiedLoa) {
        return createDummyAuthenticationRequest(attributeMap, notifiedLoa.stringValue(), nonNotifiedLoa);
    }

    private IAuthenticationRequest createDummyAuthenticationRequest(ImmutableAttributeMap attributeMap, String... levelsOfAssurance) {
        return EidasAuthenticationRequest.builder()
                .id(TestingConstants.REQUEST_ID_CONS.toString())
                .issuer(TestingConstants.REQUEST_ISSUER_CONS.toString())
                .destination(TestingConstants.REQUEST_DESTINATION_CONS.toString())
                .citizenCountryCode(TestingConstants.REQUEST_CITIZEN_COUNTRY_CODE_CONS.toString())
                .requestedAttributes(attributeMap)
                .levelsOfAssurance(Arrays.asList(levelsOfAssurance).stream().map(LevelOfAssurance::build).collect(Collectors.toList()))
                .build();
    }

    private LightResponse createDummyLightResponseNotifiedLoaSubstantial(String responseStatusCode, ImmutableAttributeMap attributesMap) {
        return createDummyLightResponse(responseStatusCode, attributesMap, NotifiedLevelOfAssurance.SUBSTANTIAL.stringValue());
    }

    private LightResponse createDummyLightResponseNonNotifiedLoa(String responseStatusCode, ImmutableAttributeMap attributesMap) {
        return createDummyLightResponse(responseStatusCode, attributesMap, "loa:nonNotified");
    }

    private LightResponse createDummyLightResponse(String responseStatusCode, ImmutableAttributeMap attributesMap, String levelOfAssurance) {
        ResponseStatus responseStatus = ResponseStatus.builder()
                .statusCode(responseStatusCode)
                .build();
        return LightResponse.builder()
                .id(TestingConstants.RESPONSE_ID_CONS.toString())
                .inResponseToId(TestingConstants.REQUEST_ID_CONS.toString())
                .issuer(TestingConstants.RESPONSE_ISSUER_CONS.toString())
                .status(responseStatus)
                .subject("N/A")
                .subjectNameIdFormat("N/A")
                .attributes(attributesMap)
                .levelOfAssurance(levelOfAssurance)
                .consent(CONSENT_UNSPECIFIED)
                .build();
    }

    private ImmutableAttributeMap getPersonIdentifierAttributeMap(String personIdentifierValue) {
        AttributeDefinition identifier = EidasSpec.Definitions.PERSON_IDENTIFIER;
        StringAttributeValue identifierValue = new StringAttributeValue(personIdentifierValue);
        ImmutableAttributeMap attributeMap = ImmutableAttributeMap.of(identifier, identifierValue);
        return attributeMap;
    }

    /**
     * Test method for {@link AUSERVICE#updateResponseAttributes(IAuthenticationRequest, ImmutableAttributeMap)}  to
     * check if the attributes with AttributeDefinition equal to EidasSpec.Definitions.PERSON_IDENTIFIER and
     * EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER have the correct prefix got from EidasAuthenticationRequest
     * serviceProviderCountryCode and AUSERVICESAML countryCode values. Must succeed.
     */
    @Test
    public void testUpdateResponseAttributes()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final String countryCode = "ES";
        final String serviceProviderCountryCode = "PT";

        final String regex = "[A-Z]{2}/[zA-Z]{2}/.*";

        ImmutableAttributeMap result = getImmutableAttributeMapUpdatedResponseAttributes(
                countryCode, serviceProviderCountryCode, true, EidasSpec.Definitions.PERSON_IDENTIFIER);
        assertTrue(result.getFirstAttributeValue(EidasSpec.Definitions.PERSON_IDENTIFIER).getValue().matches(regex));
        assertEquals("ES/PT/12345678",
                result.getFirstAttributeValue(EidasSpec.Definitions.PERSON_IDENTIFIER).getValue());

        result = getImmutableAttributeMapUpdatedResponseAttributes(countryCode, serviceProviderCountryCode,
                true, EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER);
        assertTrue(
                result.getFirstAttributeValue(EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER).getValue().matches(regex));
        assertEquals("ES/PT/12345678",
                result.getFirstAttributeValue(EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER).getValue());
    }


    /**
     * Test method for {@link AUSERVICE#updateResponseAttributes(IAuthenticationRequest, ImmutableAttributeMap)}  to
     * check if the attributes with AttributeDefinition equal to EidasSpec.Definitions.PERSON_IDENTIFIER and
     * EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER have no prefix from EidasAuthenticationRequest.
     * Must succeed.
     */
    @Test
    public void testUpdateResponseAttributesNoPrefix()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final String countryCode = "ES";
        final String serviceProviderCountryCode = "PT";

        ImmutableAttributeMap result = getImmutableAttributeMapUpdatedResponseAttributes(
                countryCode, serviceProviderCountryCode, false, EidasSpec.Definitions.PERSON_IDENTIFIER);
        assertEquals("12345678",
                result.getFirstAttributeValue(EidasSpec.Definitions.PERSON_IDENTIFIER).getValue());

        result = getImmutableAttributeMapUpdatedResponseAttributes(countryCode, serviceProviderCountryCode,
                false, EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER);
        assertEquals("12345678",
                result.getFirstAttributeValue(EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER).getValue());
    }

    /**
     * Test method for {@link AUSERVICE#updateResponseAttributes(IAuthenticationRequest, ImmutableAttributeMap)}  to
     * check if the attributes with AttributeDefinition equal to EidasSpec.Definitions.PERSON_IDENTIFIER and
     * EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER have the correct prefix got from EidasAuthenticationRequest
     * serviceProviderCountryCode and AUSERVICESAML countryCode values. Must Fail.
     */
    @Test
    public void testUpdateResponseAttributesFail()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final String countryCode = "PT";
        final String serviceProviderCountryCode = null;

        final String regex = "[A-Z]{2}/[zA-Z]{2}/.*";

        final AttributeDefinition<String> personIdentifier = EidasSpec.Definitions.PERSON_IDENTIFIER;
        ImmutableAttributeMap result = getImmutableAttributeMapUpdatedResponseAttributes(
                countryCode, serviceProviderCountryCode, true, personIdentifier);
        assertFalse(result.getFirstAttributeValue(personIdentifier).getValue().matches(regex));
        assertEquals("PT/null/12345678", result.getFirstAttributeValue(personIdentifier).getValue());

        final AttributeDefinition<String> legalPersonIdentifier = EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER;
        result = getImmutableAttributeMapUpdatedResponseAttributes(
                countryCode, serviceProviderCountryCode, true, legalPersonIdentifier);
        assertFalse(result.getFirstAttributeValue(legalPersonIdentifier).getValue().matches(regex));
        assertEquals("PT/null/12345678", result.getFirstAttributeValue(legalPersonIdentifier).getValue());
    }

    /**
     * Test method for {@link AUSERVICE#updateResponseAttributes(IAuthenticationRequest, ImmutableAttributeMap)}  to
     * check if a GENDER attribute in the EidasAuthenticationResponse is changed depending of the protocol versions
     * Gender NOT_SPECIFIED should be replaced by Gender UNSPECIFIED for empty protocol version
     * <p>
     * Must succeed.
     */
    @Test
    public void testUpdateResponseAttributesGenderFemaleShouldNotBeAdapted()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, AttributeValueMarshallingException {
        final AUSERVICE underTest = new AUSERVICE();
        AUSERVICESAML auservicesaml = Mockito.mock(AUSERVICESAML.class);
        Mockito.when(auservicesaml.getCountryCode()).thenReturn("EU");
        ProtocolEngineI mockedSamlEngine = Mockito.mock(ProtocolEngineI.class);
        ProtocolProcessorI mockedProtocolProcessor = Mockito.mock(ProtocolProcessorI.class);
        Mockito.when(mockedSamlEngine.getProtocolProcessor()).thenReturn(mockedProtocolProcessor);
        Mockito.when(auservicesaml.getSamlEngine()).thenReturn(mockedSamlEngine);
        underTest.setSamlService(auservicesaml);

        IAuthenticationRequest authenticationRequest = EidasAuthenticationRequest.builder()
                .destination(TestingConstants.REQUEST_DESTINATION_CONS.toString())
                .id(TestingConstants.REQUEST_ID_CONS.toString())
                .issuer(TestingConstants.REQUEST_ISSUER_CONS.toString())
                .citizenCountryCode(TestingConstants.REQUEST_CITIZEN_COUNTRY_CODE_CONS.toString())
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH)
                .build();

        AttributeDefinition<Gender> genderAttributeDefinition = EidasSpec.Definitions.GENDER;
        AttributeValue<Gender> genderAttributeValue = genderAttributeDefinition.getAttributeValueMarshaller()
                .unmarshal(Gender.FEMALE.getValue(), false);
        ImmutableAttributeMap attributeMap =
                ImmutableAttributeMap.of(genderAttributeDefinition, genderAttributeValue);

        ImmutableAttributeMap resultMap = callUpdateResponseAttributes(underTest, authenticationRequest, attributeMap);

        Assert.assertEquals(1, resultMap.getAttributeValues(genderAttributeDefinition).size());
        Gender actualValue = resultMap.getAttributeValues(genderAttributeDefinition)
                .stream()
                .findFirst()
                .orElse(null)
                .getValue();
        Assert.assertEquals(Gender.FEMALE, actualValue);
    }

    /**
     * Auxiliar method to be used by test methods to invokes the method {@link AUSERVICE#updateResponseAttributes(IAuthenticationRequest,
     * ImmutableAttributeMap)}.
     *
     * @param countryCode         the country code of the AUSERVICESAML
     * @param serviceProviderCountryCode   the origin country code of the request
     * @param isPrefixIdentifiers boolean to indicate is identifiers should be prefixed
     * @param identifier
     * @return ImmutableAttributeMap with the value
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private ImmutableAttributeMap getImmutableAttributeMapUpdatedResponseAttributes(final String countryCode,
                                                                                    final String serviceProviderCountryCode, final boolean isPrefixIdentifiers, AttributeDefinition<String> identifier)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final AUSERVICE underTest = new AUSERVICE();
        AUSERVICESAML auservicesaml = new AUSERVICESAML();
        auservicesaml.setCountryCode(countryCode);
        underTest.setSamlService(auservicesaml);
        underTest.setIsPrefixIdentifiersCountryCode(isPrefixIdentifiers);

        IAuthenticationRequest authenticationRequest = EidasAuthenticationRequest.builder()
                .destination(TestingConstants.REQUEST_DESTINATION_CONS.toString())
                .id(TestingConstants.REQUEST_ID_CONS.toString())
                .issuer(TestingConstants.REQUEST_ISSUER_CONS.toString())
                .citizenCountryCode(TestingConstants.REQUEST_CITIZEN_COUNTRY_CODE_CONS.toString())
                .serviceProviderCountryCode(serviceProviderCountryCode)
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH)
                .build();

        ImmutableAttributeMap attributeMap =
                ImmutableAttributeMap.of(identifier, new StringAttributeValue("12345678"));

        return callUpdateResponseAttributes(underTest, authenticationRequest, attributeMap);
    }

    private ImmutableAttributeMap callUpdateResponseAttributes(AUSERVICE auService, IAuthenticationRequest authnRequest,
                                                               ImmutableAttributeMap attributesMap) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {

        Class[] parameterTypes = new Class[2];
        parameterTypes[0] = IAuthenticationRequest.class;
        parameterTypes[1] = ImmutableAttributeMap.class;

        Method m = auService.getClass().getDeclaredMethod("updateResponseAttributes", parameterTypes);
        m.setAccessible(true);

        Object[] methodParameters = new Object[2];
        methodParameters[0] = authnRequest;
        methodParameters[1] = attributesMap;

        return (ImmutableAttributeMap) m.invoke(auService, methodParameters);
    }

    private void mockResourceBundleMessageSource() {
        final ResourceBundleMessageSource mockResourceBundleMessageSource = Mockito.mock(ResourceBundleMessageSource.class);
        Mockito.when(mockApplicationContext.getBean(ProxyBeanNames.SYSADMIN_MESSAGE_RESOURCES.toString())).thenReturn(mockResourceBundleMessageSource);
    }

}
