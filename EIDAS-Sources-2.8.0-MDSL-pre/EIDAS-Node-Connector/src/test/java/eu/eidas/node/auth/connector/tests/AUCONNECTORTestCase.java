/*
 * Copyright (c) 2022 by European Commission
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

package eu.eidas.node.auth.connector.tests;

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.Country;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.SpType;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.tx.AuthenticationExchange;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredLightRequest;
import eu.eidas.node.auth.connector.AUCONNECTOR;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.auth.connector.ICONNECTORSAMLService;
import eu.eidas.node.auth.util.tests.TestingConstants;
import eu.eidas.node.connector.exceptions.ConnectorError;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.cache.Cache;
import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link AUCONNECTOR}
 */
@RunWith(MockitoJUnitRunner.class)
public class AUCONNECTORTestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Properties values for testing proposes.
     */
    private static final Properties CONFIGS = new Properties();

    /**
     * Country List dummy values for testing proposes.
     */
    private static final List<Country> COUNTRY_LIST = new ArrayList<Country>(1);

    /**
     * byte[] dummy SAML token.
     */
    private static final byte[] SAML_TOKEN_ARRAY = {
            1, 23, -86, -71, -21, 45, 0, 0, 0, 3, -12, 94, -86, -25, -84, 122, -53, 64};

    /**
     * byte[] dummy Native SAML token.
     */
    private static final byte[] SAML_NATIVE_TOKEN_ARRAY = {
            1, 23, 86, 71, 21, 45, 0, 0, 0, 3, 12, 94, 86, 25, 84, 122, 53, 64};

    private final String SP_DEMO_REQUESTER_ID = "http://localhost:8080/SP";
    private final String PRIVATE_SP_TYPE = SpType.PRIVATE.getValue();
    private final String PUBLIC_SP_TYPE = SpType.PUBLIC.getValue();

    /**
     * Initialising class variables.
     */
    @BeforeClass
    public static void runBeforeClass() {
        COUNTRY_LIST.add(new Country(TestingConstants.LOCAL_CONS.toString(), TestingConstants.LOCAL_CONS.toString()));

        CONFIGS.setProperty(EidasErrorKey.INVALID_SESSION.errorCode(), TestingConstants.ERROR_CODE_CONS.toString());
        CONFIGS.setProperty(EidasErrorKey.INVALID_SESSION.errorMessage(), TestingConstants.ERROR_MESSAGE_CONS.toString());
        //EIDASUtil.setConfigs(CONFIGS);
    }

    @InjectMocks
    private AUCONNECTOR auconnector;

    @Mock
    private ICONNECTORSAMLService mockSamlService;
    @Mock
    private AUCONNECTORUtil mockConnectorUtil;
    @Mock
    private Cache<String, StoredLightRequest> mockSpecificSpRequestCorrelationCache;
    @Mock
    private Cache<String, StoredAuthenticationRequest> mockConnectorRequestCorrelationCache;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        auconnector.setSpecificSpRequestCorrelationCache(mockSpecificSpRequestCorrelationCache);
        auconnector.setConnectorRequestCorrelationCache(mockConnectorRequestCorrelationCache);
    }

    /**
     * Test method for {
     *
     * @link eu.eidas.node.auth.connector.AUCONNECTOR#sendRedirect(byte[])}.
     * when samlToken is null
     * <p>
     * Must fail.
     */
    @Test(expected = NullPointerException.class)
    public void testSendRedirectNullToken() {
        AUCONNECTOR auconnector = new AUCONNECTOR();
        auconnector.sendRedirect(null);
    }

    /**
     * Test method for
     * {@link eu.eidas.node.auth.connector.AUCONNECTOR#sendRedirect(byte[])}.
     * <p>
     * Must succeed.
     */
    @Test
    public void testSendRedirect() {
        AUCONNECTOR auconnector = new AUCONNECTOR();
        assertEquals("ARequestAAAAA/RequesestA", EidasStringUtil.toString(auconnector.sendRedirect(SAML_TOKEN_ARRAY)));
    }

    /**
     * Test for
     * {@link AUCONNECTOR#getAuthenticationRequest(WebRequest, ILightRequest)}
     * when same request is submitted
     * <p>
     * Must fail
     */
    @Test(expected = ConnectorError.class)
    public void doPostWithAntiReplayTriggered() {
        LightRequest mockLightRequest = Mockito.mock(LightRequest.class);
        when(mockLightRequest.getId()).thenReturn("lightRequestId");
        when(mockLightRequest.getCitizenCountryCode()).thenReturn("CA");

        boolean notPresentInCache = false;
        AUCONNECTORUtil mockAuconnectorUtil = Mockito.mock(AUCONNECTORUtil.class);
        when(mockAuconnectorUtil.checkNotPresentInCache(anyString(), anyString())).thenReturn(notPresentInCache);

        AUCONNECTOR auconnector = new AUCONNECTOR();
        auconnector.setConnectorUtil(mockAuconnectorUtil);
        auconnector.getAuthenticationRequest(Mockito.mock(WebRequest.class), mockLightRequest);
    }

    /**
     * Test method for
     * {@link AUCONNECTOR#getAuthenticationRequest(WebRequest, ILightRequest)}
     * Ensure happy flow is working correctly
     * <p>
     * Must succeed
     */
    @Test
    public void testGetAuthenticationRequest() {
        WebRequest mockWebRequest = Mockito.mock(WebRequest.class);
        Mockito.when(mockWebRequest.getRemoteIpAddress()).thenReturn("IpAddress");

        ILightRequest mockLightRequest = mockLightRequest();

        ImmutableAttributeMap mockRequestedAttributes = setUpMockRequestedAttributes("NaturalPerson");

        Mockito.when(mockConnectorUtil.checkNotPresentInCache(TestingConstants.REQUEST_ID_CONS.toString(),
                TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())).thenReturn(true);
        Mockito.when(mockConnectorUtil.getSPType()).thenReturn(SpType.PRIVATE.getValue());

        IAuthenticationRequest mockAuthenticationRequest = mockAuthenticationRequest();
        Mockito.when(mockSamlService.processSpRequest(mockLightRequest, mockWebRequest))
                .thenReturn(mockAuthenticationRequest);
        IRequestMessage mockRequestMessage = Mockito.mock(IRequestMessage.class);
        Mockito.when(mockSamlService.generateServiceAuthnRequest(Mockito.any(), Mockito.any()))
                .thenReturn(mockRequestMessage);
        setSamlServiceMockAttributeChecksToReturnTrue();

        Mockito.when(mockRequestMessage.getRequest()).thenReturn(mockAuthenticationRequest);
        Mockito.when(mockRequestMessage.getMessageBytes()).thenReturn(TestingConstants.SAML_TOKEN_CONS.toString().getBytes());

        Mockito.when(mockLightRequest.getRequestedAttributes()).thenReturn(mockRequestedAttributes);

        IRequestMessage requestMessage = auconnector.getAuthenticationRequest(mockWebRequest, mockLightRequest);

        Mockito.verify(mockConnectorRequestCorrelationCache).put(Mockito.any(), Mockito.any());
        Mockito.verify(mockSpecificSpRequestCorrelationCache).put(Mockito.any(), Mockito.any());

        Assert.assertNotNull(requestMessage);
        Assert.assertEquals(mockAuthenticationRequest, requestMessage.getRequest());
    }

    /**
     * Test method for
     * {@link AUCONNECTOR#prepareEidasRequest(IEidasAuthenticationRequest)}
     * when connector metadata spType and request spType are both private
     * <p>
     * Must succeed
     */

    @Test
    public void testPrepareEidasRequestWithPrivateConnectorPrivateRequestSPType() {
        Mockito.when(mockConnectorUtil.getSPType()).thenReturn(PRIVATE_SP_TYPE);
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, PRIVATE_SP_TYPE);

        setSamlServiceMockAttributeChecksToReturnTrue();
        final IEidasAuthenticationRequest actualIEidasAuthenticationRequest = prepareEidasRequest(authenticationRequest);

        Assert.assertNotNull(actualIEidasAuthenticationRequest.getRequesterId());
        Assert.assertNull(actualIEidasAuthenticationRequest.getSpType());
    }

    /**
     * Test method for
     * {@link AUCONNECTOR#prepareEidasRequest(IEidasAuthenticationRequest)}
     * when connector metadata spType and request spType are both public
     * <p>
     * Must succeed
     */
    @Test
    public void testPrepareEidasRequestWithPublicConnectorPublicRequestSPType() {
        Mockito.when(mockConnectorUtil.getSPType()).thenReturn(PUBLIC_SP_TYPE);
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, PUBLIC_SP_TYPE);

        setSamlServiceMockAttributeChecksToReturnTrue();
        final IEidasAuthenticationRequest actualIEidasAuthenticationRequest = prepareEidasRequest(authenticationRequest);

        Assert.assertNull(actualIEidasAuthenticationRequest.getRequesterId());
        Assert.assertNull(actualIEidasAuthenticationRequest.getSpType());
    }

    /**
     * Test method for
     * {@link AUCONNECTOR#prepareEidasRequest(IEidasAuthenticationRequest)}
     * when representative attributes are requested
     * <p>
     * Must fail
     */
    @Test
    public void testPrepareEidasRequestWithRepresentativeAttributes() {
        expectedException.expect(ConnectorError.class);

        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, PUBLIC_SP_TYPE);

        Mockito.when(mockSamlService.checkRepresentativeAttributes(Mockito.any()))
                .thenReturn(false);

        prepareEidasRequest(authenticationRequest);
    }

    /**
     * Test method for
     * {@link AUCONNECTOR#prepareEidasRequest(IEidasAuthenticationRequest)}
     * when connector metadata spType is null and request spType is public
     * <p>
     * Must succeed
     */

    @Test
    public void testPrepareEidasRequestWithUndefinedConnectorPublicRequestSPType() {
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, PUBLIC_SP_TYPE);

        setSamlServiceMockAttributeChecksToReturnTrue();
        final IEidasAuthenticationRequest actualIEidasAuthenticationRequest = prepareEidasRequest(authenticationRequest);

        Assert.assertNull(actualIEidasAuthenticationRequest.getRequesterId());
        Assert.assertEquals(PUBLIC_SP_TYPE, actualIEidasAuthenticationRequest.getSpType());
    }

    /**
     * Test method for
     * {@link AUCONNECTOR#prepareEidasRequest(IEidasAuthenticationRequest)}
     * when connector metadata spType is null and request spType is private
     * <p>
     * Must succeed
     */
    @Test
    public void testPrepareEidasRequestWithUndefinedConnectorPrivateRequestSPType() {
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, PRIVATE_SP_TYPE);

        setSamlServiceMockAttributeChecksToReturnTrue();
        final IEidasAuthenticationRequest actualIEidasAuthenticationRequest = prepareEidasRequest(authenticationRequest);

        Assert.assertNotNull(actualIEidasAuthenticationRequest.getRequesterId());
        Assert.assertEquals(PRIVATE_SP_TYPE, actualIEidasAuthenticationRequest.getSpType());
    }

    /**
     * Test method for
     * {@link AUCONNECTOR#prepareEidasRequest(IEidasAuthenticationRequest)}
     * when connector metadata spType is public and request spType is null
     * <p>
     * Must succeed
     */
    @Test
    public void testPrepareEidasRequestWithPublicConnectorUndefinedRequestSPType() {
        Mockito.when(mockConnectorUtil.getSPType()).thenReturn(PUBLIC_SP_TYPE);
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, null);

        setSamlServiceMockAttributeChecksToReturnTrue();
        final IEidasAuthenticationRequest actualIEidasAuthenticationRequest = prepareEidasRequest(authenticationRequest);

        Assert.assertNull(actualIEidasAuthenticationRequest.getRequesterId());
        Assert.assertNull(actualIEidasAuthenticationRequest.getSpType());
    }

    /**
     * Test method for
     * {@link AUCONNECTOR#prepareEidasRequest(IEidasAuthenticationRequest)}
     * when connector metadata spType is private and request spType is null
     * <p>
     * Must succeed
     */
    @Test
    public void testPrepareEidasRequestWithPrivateConnectorUndefinedRequestSPType() {
        Mockito.when(mockConnectorUtil.getSPType()).thenReturn(PRIVATE_SP_TYPE);
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, null);

        setSamlServiceMockAttributeChecksToReturnTrue();
        final IEidasAuthenticationRequest actualIEidasAuthenticationRequest = prepareEidasRequest(authenticationRequest);

        Assert.assertNotNull(actualIEidasAuthenticationRequest.getRequesterId());
        Assert.assertNull(actualIEidasAuthenticationRequest.getSpType());
    }

    /**
     * Test method for
     * {@link AUCONNECTOR#getAuthenticationRequest(WebRequest, ILightRequest)}
     * when connector metadata spType is null and request spType is null
     * <p>
     * Must fail.
     */
    @Test(expected = ConnectorError.class)
    public void testGetAuthenticationRequestWithNullSpTypes() {
        final WebRequest mockWebRequest = Mockito.mock(WebRequest.class);
        final ILightRequest mockLightRequest = mockLightRequest();
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, null);
        final ImmutableAttributeMap mockRequestedAttributes = setUpMockRequestedAttributes("NaturalPerson");
        Mockito.when(mockWebRequest.getRemoteIpAddress()).thenReturn("IpAddress");
        Mockito.when(mockSamlService.processSpRequest(mockLightRequest, mockWebRequest)).thenReturn(authenticationRequest);
        Mockito.when(mockLightRequest.getRequestedAttributes()).thenReturn(mockRequestedAttributes);
        Mockito.when(mockConnectorUtil.checkNotPresentInCache(TestingConstants.REQUEST_ID_CONS.toString(),
                TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())).thenReturn(true);

        auconnector.getAuthenticationRequest(mockWebRequest, mockLightRequest);
    }

    /**
     * Test method for
     * {@link AUCONNECTOR#getAuthenticationRequest(WebRequest, ILightRequest)}
     * when connector metadata spType is private and request spType is public
     * <p>
     * Must fail.
     */
    @Test(expected = ConnectorError.class)
    public void testGetAuthenticationRequestWithPrivateConfigPublicRequestSpTypes() {
        final WebRequest mockWebRequest = Mockito.mock(WebRequest.class);
        final ILightRequest mockLightRequest = mockLightRequest();
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, PUBLIC_SP_TYPE);
        final ImmutableAttributeMap mockRequestedAttributes = setUpMockRequestedAttributes("NaturalPerson");
        Mockito.when(mockWebRequest.getRemoteIpAddress()).thenReturn("IpAddress");
        Mockito.when(mockSamlService.processSpRequest(mockLightRequest, mockWebRequest)).thenReturn(authenticationRequest);
        Mockito.when(mockLightRequest.getRequestedAttributes()).thenReturn(mockRequestedAttributes);
        Mockito.when(mockConnectorUtil.checkNotPresentInCache(TestingConstants.REQUEST_ID_CONS.toString(),
                TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())).thenReturn(true);

        auconnector.getAuthenticationRequest(mockWebRequest, mockLightRequest);
    }

    /**
     * Test method for
     * {@link AUCONNECTOR#getAuthenticationRequest(WebRequest, ILightRequest)}
     * when connector metadata spType is public and request spType is private
     * <p>
     * Must fail.
     */
    @Test(expected = ConnectorError.class)
    public void testGetAuthenticationRequestWithPublicConfigPrivateRequestSpTypes() {
        final WebRequest mockWebRequest = Mockito.mock(WebRequest.class);
        final ILightRequest mockLightRequest = mockLightRequest();
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, PRIVATE_SP_TYPE);
        final ImmutableAttributeMap mockRequestedAttributes = setUpMockRequestedAttributes("NaturalPerson");
        Mockito.when(mockWebRequest.getRemoteIpAddress()).thenReturn("IpAddress");
        Mockito.when(mockSamlService.processSpRequest(mockLightRequest, mockWebRequest)).thenReturn(authenticationRequest);
        Mockito.when(mockLightRequest.getRequestedAttributes()).thenReturn(mockRequestedAttributes);
        Mockito.when(mockConnectorUtil.checkNotPresentInCache(TestingConstants.REQUEST_ID_CONS.toString(),
                TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())).thenReturn(true);

        auconnector.getAuthenticationRequest(mockWebRequest, mockLightRequest);
    }

    /**
     * Test method for
     * {@link AUCONNECTOR#prepareEidasRequest(IEidasAuthenticationRequest)}
     * when IEidasAuthenticationRequest is returned
     * <p>
     * Must succeed.
     */
    @Test
    public void testPrepareEidasRequest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method prepareEidasRequest = AUCONNECTOR.class.getDeclaredMethod("prepareEidasRequest", IEidasAuthenticationRequest.class);
        prepareEidasRequest.setAccessible(true);

        setSamlServiceMockAttributeChecksToReturnTrue();

        final IAuthenticationRequest authenticationRequest = getMinimumAuthenticationRequest(PRIVATE_SP_TYPE);
        final IEidasAuthenticationRequest eidasAuthenticationRequest = (IEidasAuthenticationRequest) prepareEidasRequest.invoke(auconnector, authenticationRequest);

        Assert.assertNotNull(eidasAuthenticationRequest);
        Assert.assertTrue(eidasAuthenticationRequest instanceof IEidasAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link AUCONNECTOR#prepareEidasRequest(IEidasAuthenticationRequest)}
     * when AssertionConsumerServiceURL and Binding from IEidasAuthenticationRequest are not null
     * and EidasAuthenticationRequest.Builder is returned
     * <p>
     * Must succeed.
     */
    @Test
    public void testPrepareEidasRequestWithAssertionConsumerServiceURLAndBinding() {
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, PRIVATE_SP_TYPE);
        setSamlServiceMockAttributeChecksToReturnTrue();

        final IEidasAuthenticationRequest eidasAuthenticationRequest = prepareEidasRequest(authenticationRequest);

        Assert.assertNotNull(eidasAuthenticationRequest);
        Assert.assertTrue(eidasAuthenticationRequest instanceof IEidasAuthenticationRequest);
    }

    /**
     * Test method for
     * {@link AUCONNECTOR#validateAuthenticationRequestSPType(IAuthenticationRequest)}
     * when Node SP type is different than SP type from the request
     * <p>
     * Must fail.
     */
    @Test
    public void testValidateAuthenticationRequestSPTypeWithDifferentSpType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(ConnectorError.class));

        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, PRIVATE_SP_TYPE);
        final Method validateAuthenticationRequestSPType = AUCONNECTOR.class.getDeclaredMethod("validateAuthenticationRequestSPType", IAuthenticationRequest.class);

        Mockito.when(mockConnectorUtil.getSPType()).thenReturn(PUBLIC_SP_TYPE);

        validateAuthenticationRequestSPType.setAccessible(true);
        validateAuthenticationRequestSPType.invoke(auconnector, authenticationRequest);
    }

    /**
     * Test method for
     * {@link AUCONNECTOR#validateAuthenticationRequestSPType(IAuthenticationRequest)}
     * when Node SP type is not set, the request has no SP type
     * <p>
     * Must fail.
     */
    @Test
    public void testValidateAuthenticationRequestSPTypeWithNoSpType() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        expectedException.expectCause(isA(ConnectorError.class));

        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, null);
        final Method validateAuthenticationRequestSPType = AUCONNECTOR.class.getDeclaredMethod("validateAuthenticationRequestSPType", IAuthenticationRequest.class);

        validateAuthenticationRequestSPType.setAccessible(true);
        validateAuthenticationRequestSPType.invoke(auconnector, authenticationRequest);
    }

    /**
     * Test method for
     * {@link AUCONNECTOR#getAuthenticationResponse(WebRequest)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetAuthenticationResponse() {
        final AUCONNECTOR auConnector = new AUCONNECTOR();
        auConnector.setSamlService(mockSamlService);

        final WebRequest mockWebRequest = Mockito.mock(WebRequest.class);
        final AuthenticationExchange mockAuthenticationExchange = Mockito.mock(AuthenticationExchange.class);
        final IAuthenticationResponse mockIAuthenticationResponse = Mockito.mock(IAuthenticationResponse.class);

        Mockito.when(mockSamlService.processProxyServiceResponse(any(), any(), any())).thenReturn(mockAuthenticationExchange);
        Mockito.when(mockAuthenticationExchange.getConnectorResponse()).thenReturn(mockIAuthenticationResponse);
        Mockito.when(mockSamlService.checkMandatoryAttributes(any())).thenReturn(true);

        final AuthenticationExchange authenticationExchange = auConnector.getAuthenticationResponse(mockWebRequest);
        Assert.assertNotNull(authenticationExchange);
    }

    /**
     * Test method for
     * {@link AUCONNECTOR#getAuthenticationResponse(WebRequest)}
     * when the attribute map does not contains at least one of the mandatory eIDAS attribute set
     * <p>
     * Must fail.
     */
    @Test
    public void testGetAuthenticationResponseWithNoAttributes() {
        expectedException.expect(ConnectorError.class);

        final AUCONNECTOR auConnector = new AUCONNECTOR();
        auConnector.setSamlService(mockSamlService);

        final WebRequest mockWebRequest = Mockito.mock(WebRequest.class);
        final AuthenticationExchange mockAuthenticationExchange = Mockito.mock(AuthenticationExchange.class);
        final IAuthenticationResponse mockIAuthenticationResponse = Mockito.mock(IAuthenticationResponse.class);

        Mockito.when(mockSamlService.processProxyServiceResponse(any(), any(), any())).thenReturn(mockAuthenticationExchange);
        Mockito.when(mockAuthenticationExchange.getConnectorResponse()).thenReturn(mockIAuthenticationResponse);
        Mockito.when(mockSamlService.checkMandatoryAttributes(any())).thenReturn(false);

        auConnector.getAuthenticationResponse(mockWebRequest);
    }

    @Test
    public void testRequestNaturalAndLegalDatasets() {
        expectedException.expect(ConnectorError.class);

        final WebRequest mockWebRequest = Mockito.mock(WebRequest.class);
        final ILightRequest mockLightRequest = mockLightRequest();
        final ImmutableAttributeMap mockRequestedAttributes = setUpMockRequestedAttributes("both");
        Mockito.when(mockLightRequest.getRequestedAttributes()).thenReturn(mockRequestedAttributes);

        auconnector.getAuthenticationRequest(mockWebRequest, mockLightRequest);
    }

    private IAuthenticationRequest getAuthenticationRequest(String requesterId, String spType) {
        return EidasAuthenticationRequest.builder()
                .id(TestingConstants.REQUEST_ID_CONS.toString())
                .assertionConsumerServiceURL(TestingConstants.ASSERTION_URL_CONS.toString())
                .binding(BindingMethod.POST.toString())
                .issuer(TestingConstants.ISSUER_CONS.toString())
                .destination(TestingConstants.DESTINATION_CONS.toString())
                .citizenCountryCode(TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())
                .spType(spType)
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH.stringValue())
                .requesterId(requesterId)
                .build();
    }

    private IAuthenticationRequest getMinimumAuthenticationRequest(String spType) {
        return EidasAuthenticationRequest.builder()
                .id(TestingConstants.REQUEST_ID_CONS.toString())
                .issuer(TestingConstants.ISSUER_CONS.toString())
                .destination(TestingConstants.DESTINATION_CONS.toString())
                .citizenCountryCode(TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())
                .spType(spType)
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH.stringValue())
                .build();
    }

    private ILightRequest mockLightRequest() {
        ILightRequest mockLightRequest = Mockito.mock(ILightRequest.class, Mockito.withSettings().lenient());

        Mockito.when(mockLightRequest.getId()).thenReturn(TestingConstants.REQUEST_ID_CONS.toString());
        Mockito.when(mockLightRequest.getCitizenCountryCode()).thenReturn(TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString());

        return mockLightRequest;
    }

    private IAuthenticationRequest mockAuthenticationRequest() {
        IEidasAuthenticationRequest authenticationRequest = EidasAuthenticationRequest.builder()
                .id(TestingConstants.REQUEST_ID_CONS.toString())
                .issuer(TestingConstants.ISSUER_CONS.toString())
                .destination(TestingConstants.DESTINATION_CONS.toString())
                .citizenCountryCode(TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())
                .spType(SpType.PRIVATE.getValue())
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH.stringValue())
                .build();
        return authenticationRequest;
    }

    private void setSamlServiceMockAttributeChecksToReturnTrue() {
        Mockito.when(mockSamlService.checkMandatoryAttributes(Mockito.any()))
                .thenReturn(true);
        Mockito.when(mockSamlService.checkRepresentativeAttributes(Mockito.any()))
                .thenReturn(true);
    }

    /**
     * Reflection for * {@link AUCONNECTOR#prepareEidasRequest(IEidasAuthenticationRequest)}
     */
    private IEidasAuthenticationRequest prepareEidasRequest(IAuthenticationRequest authenticationRequest) {
        try {
            Method prepareEidasRequest = AUCONNECTOR.class.getDeclaredMethod("prepareEidasRequest", IEidasAuthenticationRequest.class);
            prepareEidasRequest.setAccessible(true);
            return (IEidasAuthenticationRequest) prepareEidasRequest.invoke(auconnector, authenticationRequest);
        } catch (InvocationTargetException targetException) {
            throw (RuntimeException) targetException.getCause(); // method signature has no exceptions defined
        } catch (ReflectiveOperationException testException) {
            Assert.fail("Reflection failed for AUCONNECTOR.prepareEidasRequest(IEidasAuthenticationRequest) " +
                    testException.getMessage());
            return null;
        }
    }

    private ImmutableAttributeMap setUpMockRequestedAttributes(String type){
        final ImmutableAttributeMap mockRequestedAttributes = Mockito.mock(ImmutableAttributeMap.class);
        final AttributeDefinition<?> mockAttributeDefinition = Mockito.mock(AttributeDefinition.class, Mockito.withSettings().lenient());
        final ImmutableSet<AttributeDefinition<?>> mockAttributeDefinitions;
        if ("both".equals(type)){
            final AttributeDefinition<?> mockAttributeDefinitionTwo = Mockito.mock(AttributeDefinition.class, Mockito.withSettings().lenient());
            mockAttributeDefinitions = ImmutableSet.of(mockAttributeDefinition,mockAttributeDefinitionTwo);
            Mockito.when(mockAttributeDefinition.getPersonType()).thenReturn(PersonType.NATURAL_PERSON);
            Mockito.when(mockAttributeDefinitionTwo.getPersonType()).thenReturn(PersonType.LEGAL_PERSON);
        }
        else{
            mockAttributeDefinitions = ImmutableSet.of(mockAttributeDefinition);
            Mockito.when(mockAttributeDefinition.getPersonType()).thenReturn(PersonType.fromString(type));
        }

        Mockito.when(mockRequestedAttributes.getDefinitions()).thenReturn(mockAttributeDefinitions);

        return mockRequestedAttributes;
    }

}
