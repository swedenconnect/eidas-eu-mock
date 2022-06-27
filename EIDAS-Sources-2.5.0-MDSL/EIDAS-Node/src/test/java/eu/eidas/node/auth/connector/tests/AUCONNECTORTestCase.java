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

package eu.eidas.node.auth.connector.tests;

import eu.eidas.auth.commons.Country;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.commons.exceptions.SecurityEIDASException;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.SpType;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredLightRequest;
import eu.eidas.node.auth.connector.AUCONNECTOR;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.auth.connector.ICONNECTORSAMLService;
import eu.eidas.node.auth.util.tests.TestingConstants;
import org.hamcrest.core.IsInstanceOf;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Functional testing class to {@link AUCONNECTOR}.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com, luis.felix@multicert.com
 * @version $Revision: $, $Date:$
 */
@RunWith(MockitoJUnitRunner.class)
public class AUCONNECTORTestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();


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

    private String SP_DEMO_REQUESTER_ID = "http://localhost:8080/SP";
    private final String PRIVATE_SPTYPE = SpType.PRIVATE.getValue();
    private final String PUBLIC_SPTYPE = SpType.PUBLIC.getValue();

    /**
     * Initialising class variables.
     *
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void runBeforeClass() throws Exception {
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
    public void setup() {
        auconnector.setSpecificSpRequestCorrelationCache(mockSpecificSpRequestCorrelationCache);
        auconnector.setConnectorRequestCorrelationCache(mockConnectorRequestCorrelationCache);

        Mockito.when(mockConnectorUtil.getConfigs()).thenReturn(mockProperties());
    }

    /**
     * Test method for {@link eu.eidas.node.auth.connector.AUCONNECTOR#sendRedirect(byte[])}. Testing null value. Must
     * throw {@link NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void testSendRedirectNullToken() {
        AUCONNECTOR auconnector = new AUCONNECTOR();
        auconnector.sendRedirect(null);
    }

    /**
     * Test method for {@link eu.eidas.node.auth.connector.AUCONNECTOR#sendRedirect(byte[])}. Must succeed.
     */
    @Test
    public void testSendRedirect() {
        AUCONNECTOR auconnector = new AUCONNECTOR();
        assertEquals("ARequestAAAAA/RequesestA", EidasStringUtil.toString(auconnector.sendRedirect(SAML_TOKEN_ARRAY)));
    }

    @Test(expected = SecurityEIDASException.class)
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
     * Test method for {@link AUCONNECTOR#getAuthenticationRequest(WebRequest, ILightRequest)}
     * Ensure happy flow is working correctly
     *
     * Must succeed
     */
    @Test
    public void testGetAuthenticationRequest() {
        WebRequest mockWebRequest = Mockito.mock(WebRequest.class);
        Mockito.when(mockWebRequest.getRemoteIpAddress()).thenReturn("IpAddress");

        ILightRequest mockLightRequest = mockLightRequest();

        Mockito.when(mockConnectorUtil.checkNotPresentInCache(TestingConstants.REQUEST_ID_CONS.toString(),
                TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())).thenReturn(true);
        Properties mockProperties = mockProperties();
        Mockito.when(mockConnectorUtil.getConfigs()).thenReturn(mockProperties);
        Mockito.when(mockConnectorUtil.getSPType()).thenReturn(SpType.PRIVATE.getValue());

        IAuthenticationRequest mockAuthenticationRequest = mockAuthenticationRequest();
        Mockito.when(mockSamlService.processSpRequest(mockLightRequest, mockWebRequest))
                .thenReturn(mockAuthenticationRequest);
        IRequestMessage mockRequestMessage = Mockito.mock(IRequestMessage.class);
        Mockito.when(mockSamlService.generateServiceAuthnRequest(Mockito.any(), Mockito.any()))
                .thenReturn(mockRequestMessage);

        Mockito.when(mockSamlService.checkRepresentativeAttributes(Mockito.any()))
                .thenReturn(true);

        Mockito.when(mockRequestMessage.getRequest()).thenReturn(mockAuthenticationRequest);
        Mockito.when(mockRequestMessage.getMessageBytes()).thenReturn(TestingConstants.SAML_TOKEN_CONS.toString().getBytes());

        IRequestMessage requestMessage = auconnector.getAuthenticationRequest(mockWebRequest, mockLightRequest);

        Mockito.verify(mockConnectorRequestCorrelationCache).put(Mockito.any(), Mockito.any());
        Mockito.verify(mockSpecificSpRequestCorrelationCache).put(Mockito.any(), Mockito.any());

        Assert.assertNotNull(requestMessage);
        Assert.assertEquals(mockAuthenticationRequest, requestMessage.getRequest());
    }

    /**
     * Test method for {@link AUCONNECTOR#prepareEidasRequest(IEidasAuthenticationRequest)}
     * when connector metadata spType and request spType are both private
     * <p>
     * Must succeed
     */

    @Test
    public void testPrepareEidasRequestWithPrivateConnectorPrivateRequestSPType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Mockito.when(mockConnectorUtil.getSPType()).thenReturn(PRIVATE_SPTYPE);
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, PRIVATE_SPTYPE);

        final IEidasAuthenticationRequest actualIEidasAuthenticationRequest = prepareEidasRequest(authenticationRequest);

        Assert.assertNotNull(actualIEidasAuthenticationRequest.getRequesterId());
        Assert.assertNull(actualIEidasAuthenticationRequest.getSpType());
    }

    /**
     * Test method for {@link AUCONNECTOR#prepareEidasRequest(IEidasAuthenticationRequest)}
     * when connector metadata spType and request spType are both public
     * <p>
     * Must succeed
     */
    @Test
    public void testPrepareEidasRequestWithPublicConnectorPublicRequestSPType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Mockito.when(mockConnectorUtil.getSPType()).thenReturn(PUBLIC_SPTYPE);
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, PUBLIC_SPTYPE);

        final IEidasAuthenticationRequest actualIEidasAuthenticationRequest = prepareEidasRequest(authenticationRequest);

        Assert.assertNull(actualIEidasAuthenticationRequest.getRequesterId());
        Assert.assertNull(actualIEidasAuthenticationRequest.getSpType());
    }

    /**
     * Test method for {@link AUCONNECTOR#prepareEidasRequest(IEidasAuthenticationRequest)}
     * when representative attributes are requested
     * <p>
     * Must fail
     */
    @Test
    public void testPrepareEidasRequestWithRepresentativeAttributes() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        thrown.expect(InvocationTargetException.class);
        thrown.expectCause(IsInstanceOf.instanceOf(EidasNodeException.class));

        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, PUBLIC_SPTYPE);

        prepareEidasRequestWithRepresentative(authenticationRequest);
    }

    /**
     * Test method for {@link AUCONNECTOR#prepareEidasRequest(IEidasAuthenticationRequest)}
     * when connector metadata spType is null and request spType is public
     * <p>
     * Must succeed
     */

    @Test
    public void testPrepareEidasRequestWithUndefinedConnectorPublicRequestSPType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, PUBLIC_SPTYPE);

        final IEidasAuthenticationRequest actualIEidasAuthenticationRequest = prepareEidasRequest(authenticationRequest);

        Assert.assertNull(actualIEidasAuthenticationRequest.getRequesterId());
        Assert.assertEquals(PUBLIC_SPTYPE, actualIEidasAuthenticationRequest.getSpType());
    }

    /**
     * Test method for {@link AUCONNECTOR#prepareEidasRequest(IEidasAuthenticationRequest)}
     * when connector metadata spType is null and request spType is private
     * <p>
     * Must succeed
     */
    @Test
    public void testPrepareEidasRequestWithUndefinedConnectorPrivateRequestSPType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, PRIVATE_SPTYPE);

        final IEidasAuthenticationRequest actualIEidasAuthenticationRequest = prepareEidasRequest(authenticationRequest);

        Assert.assertNotNull(actualIEidasAuthenticationRequest.getRequesterId());
        Assert.assertEquals(PRIVATE_SPTYPE, actualIEidasAuthenticationRequest.getSpType());
    }

    /**
     * Test method for {@link AUCONNECTOR#prepareEidasRequest(IEidasAuthenticationRequest)}
     * when connector metadata spType is public and request spType is null
     * <p>
     * Must succeed
     */
    @Test
    public void testPrepareEidasRequestWithPublicConnectorUndefinedRequestSPType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Mockito.when(mockConnectorUtil.getSPType()).thenReturn(PUBLIC_SPTYPE);
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, null);

        final IEidasAuthenticationRequest actualIEidasAuthenticationRequest = prepareEidasRequest(authenticationRequest);

        Assert.assertNull(actualIEidasAuthenticationRequest.getRequesterId());
        Assert.assertNull(actualIEidasAuthenticationRequest.getSpType());
    }

    /**
     * Test method for {@link AUCONNECTOR#prepareEidasRequest(IEidasAuthenticationRequest)}
     * when connector metadata spType is private and request spType is null
     * <p>
     * Must succeed
     */
    @Test
    public void testPrepareEidasRequestWithPrivateConnectorUndefinedRequestSPType() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Mockito.when(mockConnectorUtil.getSPType()).thenReturn(PRIVATE_SPTYPE);
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, null);

        final IEidasAuthenticationRequest actualIEidasAuthenticationRequest = prepareEidasRequest(authenticationRequest);

        Assert.assertNotNull(actualIEidasAuthenticationRequest.getRequesterId());
        Assert.assertNull(actualIEidasAuthenticationRequest.getSpType());
    }

    /**
     * Test method for {@link AUCONNECTOR#getAuthenticationRequest(WebRequest, ILightRequest)}
     * when connector metadata spType is null and request spType is null
     * <p>
     * Must fail.
     */
    @Test(expected = EidasNodeException.class)
    public void testGetAuthenticationRequestWithNullSpTypes() {
        final WebRequest mockWebRequest = Mockito.mock(WebRequest.class);
        final ILightRequest mockLightRequest = mockLightRequest();
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, null);
        Mockito.when(mockWebRequest.getRemoteIpAddress()).thenReturn("IpAddress");
        Mockito.when(mockSamlService.processSpRequest(mockLightRequest, mockWebRequest)).thenReturn(authenticationRequest);
        Mockito.when(mockConnectorUtil.checkNotPresentInCache(TestingConstants.REQUEST_ID_CONS.toString(),
                TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())).thenReturn(true);

        auconnector.getAuthenticationRequest(mockWebRequest, mockLightRequest);
    }

    /**
     * Test method for {@link AUCONNECTOR#getAuthenticationRequest(WebRequest, ILightRequest)}
     * when connector metadata spType is private and request spType is public
     * <p>
     * Must fail.
     */
    @Test(expected = EidasNodeException.class)
    public void testGetAuthenticationRequestWithPrivateConfigPublicRequestSpTypes() {
        final WebRequest mockWebRequest = Mockito.mock(WebRequest.class);
        final ILightRequest mockLightRequest = mockLightRequest();
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, PUBLIC_SPTYPE);
        Mockito.when(mockWebRequest.getRemoteIpAddress()).thenReturn("IpAddress");
        Mockito.when(mockSamlService.processSpRequest(mockLightRequest, mockWebRequest)).thenReturn(authenticationRequest);
        Mockito.when(mockConnectorUtil.checkNotPresentInCache(TestingConstants.REQUEST_ID_CONS.toString(),
                TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())).thenReturn(true);

        auconnector.getAuthenticationRequest(mockWebRequest, mockLightRequest);
    }

    /**
     * Test method for {@link AUCONNECTOR#getAuthenticationRequest(WebRequest, ILightRequest)}
     * when connector metadata spType is public and request spType is private
     * <p>
     * Must fail.
     */
    @Test(expected = EidasNodeException.class)
    public void testGetAuthenticationRequestWithPublicConfigPrivateRequestSpTypes() {
        final WebRequest mockWebRequest = Mockito.mock(WebRequest.class);
        final ILightRequest mockLightRequest = mockLightRequest();
        final IAuthenticationRequest authenticationRequest = getAuthenticationRequest(SP_DEMO_REQUESTER_ID, PRIVATE_SPTYPE);
        Mockito.when(mockWebRequest.getRemoteIpAddress()).thenReturn("IpAddress");
        Mockito.when(mockSamlService.processSpRequest(mockLightRequest, mockWebRequest)).thenReturn(authenticationRequest);
        Mockito.when(mockConnectorUtil.checkNotPresentInCache(TestingConstants.REQUEST_ID_CONS.toString(),
                TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())).thenReturn(true);

        auconnector.getAuthenticationRequest(mockWebRequest, mockLightRequest);
    }

    private IAuthenticationRequest getAuthenticationRequest(String requesterId, String spType) {
        return EidasAuthenticationRequest.builder()
                .id(TestingConstants.REQUEST_ID_CONS.toString())
                .issuer(TestingConstants.ISSUER_CONS.toString())
                .destination(TestingConstants.DESTINATION_CONS.toString())
                .citizenCountryCode(TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())
                .spType(spType)
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH.stringValue())
                .requesterId(requesterId)
                .build();
    }

    private ILightRequest mockLightRequest() {
        ILightRequest mockLightRequest = Mockito.mock(ILightRequest.class);

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

    private Properties mockProperties() {
        Properties properties = new Properties();

        properties.put(EIDASValues.DISABLE_CHECK_MANDATORY_ATTRIBUTES.toString(), "true");

        return properties;
    }

    private IEidasAuthenticationRequest prepareEidasRequest(IAuthenticationRequest authenticationRequest) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method prepareEidasRequest = AUCONNECTOR.class.getDeclaredMethod("prepareEidasRequest", IEidasAuthenticationRequest.class);
        prepareEidasRequest.setAccessible(true);
        Mockito.when(mockSamlService.checkRepresentativeAttributes(Mockito.any()))
                .thenReturn(true);
        return (IEidasAuthenticationRequest) prepareEidasRequest.invoke(auconnector, authenticationRequest);
    }

    private IEidasAuthenticationRequest prepareEidasRequestWithRepresentative(IAuthenticationRequest authenticationRequest) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method prepareEidasRequest = AUCONNECTOR.class.getDeclaredMethod("prepareEidasRequest", IEidasAuthenticationRequest.class);
        prepareEidasRequest.setAccessible(true);
        Mockito.when(mockSamlService.checkRepresentativeAttributes(Mockito.any()))
                .thenReturn(false);
        return (IEidasAuthenticationRequest) prepareEidasRequest.invoke(auconnector, authenticationRequest);
    }
}
