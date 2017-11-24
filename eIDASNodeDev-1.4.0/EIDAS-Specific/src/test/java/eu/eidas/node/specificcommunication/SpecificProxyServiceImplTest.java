package eu.eidas.node.specificcommunication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.collect.Maps;

import org.bouncycastle.util.encoders.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.cache.ConcurrentMapServiceDefaultImpl;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.tx.AuthenticationExchange;
import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredLightRequest;
import eu.eidas.auth.commons.tx.StoredLightRequestCorrelationMap;
import eu.eidas.auth.specific.IAUService;
import eu.eidas.node.CitizenAuthenticationBean;
import eu.eidas.node.SpecificIdPBean;
import eu.eidas.node.SpecificParameterNames;
import eu.eidas.node.SpecificViewNames;

import static java.lang.Boolean.TRUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * SpecificProxyServiceImplTest
 *
 * @since 2016-07-12
 */
@RunWith(MockitoJUnitRunner.class)
public class SpecificProxyServiceImplTest {

    private static final String PROVIDER_NAME = "DEMO-SP";
    private static final String CITIZEN_COUNTRY_CODE = "BE";
    private static final String REMOTE_ADDRESS = "127.0.0.1";
    private static final String ID = UUID.randomUUID().toString();
    private static final String ID_RESPONSE = UUID.randomUUID().toString();
    private static final String ISSUER = "http://localhost:7001/EidasNode/ServiceRequesterMetadata";
    private static final String ISSUER_IDP = "http://localhost:7001/IdP/metadata";
    private static final String CALLBACK_URL = "http://localhost:7001/EidasNode/IdpResponse";
    private static final String LEVEL_OF_ASSURANCE = "http://eidas.europa.eu/LoA/low";
    private static final String IDP_URL = "http://localhost:7001/IdP/AuthenticateCitizen";
    private static final String STATUS_CODE = "urn:oasis:names:tc:SAML:2.0:status:Success";
    private static final String NAME_ID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
    private static final String AUDIENCE_RESTRICTION = "http://localhost:7001/EidasNode/ServiceRequesterMetadata";
    private static final String DESTINATION = "http://localhost:7001/IdP/AuthenticateCitizen";

    private static final AttributeDefinition<String> CURRENT_FAMILY_NAME =
            new AttributeDefinition.Builder<String>().nameUri(
                    "http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName")
                    .friendlyName("FamilyName")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentFamilyNameType",
                             "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .transliterationMandatory(true)
                    .build();

    private static final AttributeDefinition<String> CURRENT_GIVEN_NAME =
            new AttributeDefinition.Builder<String>().nameUri(
                    "http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName")
                    .friendlyName("FirstName")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "CurrentGivenNameType", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    @InjectMocks
    private SpecificProxyServiceImpl specificProxyService;

    @Mock
    private CitizenAuthenticationBean citizenAuthentication;

    @Mock
    private SpecificIdPBean specificIdPResponse;

    @Mock
    private IAUService specificService;

    @Mock
    private Enumeration<String> enumeration;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private RequestDispatcher dispatcher;


    @Test
    public void sendRequest() throws Exception {
        byte[] bytes = "SAMLRequest".getBytes("UTF-8");
        ILightRequest lightRequest = lightRequest();
        HttpServletRequest httpServletRequest = httpServletRequest(Maps.<String, String[]>newHashMap());
        //expectation
        when(citizenAuthentication.isExternalAuth()).thenReturn(TRUE);
        when(citizenAuthentication.getIdpUrl()).thenReturn(IDP_URL);
        when(citizenAuthentication.getSpecAuthenticationNode()).thenReturn(specificService);
        when(specificService.getCallBackURL()).thenReturn(CALLBACK_URL);
        when(specificService.prepareCitizenAuthentication(eq(lightRequest), eq(lightRequest.getRequestedAttributes()), (Map<String, Object>) anyObject(), (Map<String, Object>) anyObject())).thenReturn(bytes);
        when(httpServletResponse.encodeURL(eq(SpecificViewNames.IDP_REDIRECT.toString()))).thenReturn(SpecificViewNames.IDP_REDIRECT.toString());
        doNothing().when(dispatcher).forward(eq(httpServletRequest), eq(httpServletResponse));
        //test the bean
        specificProxyService.sendRequest(lightRequest, httpServletRequest, httpServletResponse);
        //verify
        verify(citizenAuthentication).getSpecAuthenticationNode();
        verify(citizenAuthentication).isExternalAuth();
        verify(citizenAuthentication, times(3)).getIdpUrl();
        verify(specificService).getCallBackURL();
        verify(specificService).prepareCitizenAuthentication(eq(lightRequest), eq(lightRequest.getRequestedAttributes()), (Map<String, Object>) anyObject(), (Map<String, Object>) anyObject());
        verify(httpServletResponse).encodeURL(eq(SpecificViewNames.IDP_REDIRECT.toString()));
        verify(dispatcher).forward(eq(httpServletRequest), eq(httpServletResponse));
        verifyNoMoreInteractions(citizenAuthentication, specificService, httpServletResponse, dispatcher);
        //assert
        assertEquals(new String(Base64.encode(bytes), "UTF-8"), httpServletRequest.getAttribute(SpecificParameterNames.SAML_TOKEN.toString()));
        assertEquals(IDP_URL, httpServletRequest.getAttribute(SpecificParameterNames.IDP_URL.toString()));
        assertEquals("eidas", httpServletRequest.getAttribute(EidasParameterKeys.REQUEST_FORMAT.toString()));
    }

    @Test(expected = InvalidParameterEIDASException.class)
    public void processResponseMissingSamlResponse() throws Exception {
        HttpServletRequest httpServletRequest = httpServletRequest(Maps.<String, String[]>newHashMap());
        specificProxyService.processResponse(httpServletRequest, httpServletResponse);
    }

    @Test
    public void processResponseSuccessfull() throws Exception {
        byte[] bytes = "Response".getBytes("UTF-8");
        byte[] encodedSamlResponse = Base64.encode(bytes);
        Map<String, String[]> params = new HashMap<>();
        params.put("SAMLResponse", new String[] {new String(encodedSamlResponse)});
        HttpServletRequest httpServletRequest = httpServletRequest(params);

        StoredLightRequest storedLightRequest = StoredLightRequest.builder().request(lightRequest()).remoteIpAddress(REMOTE_ADDRESS).build();
        CorrelationMap<StoredLightRequest> correlationMap = new StoredLightRequestCorrelationMap(new ConcurrentMapServiceDefaultImpl());
        correlationMap.put(ID, storedLightRequest);

        AuthenticationExchange authenticationExchange = new AuthenticationExchange(storedAuthenticationRequest(), authenticationResponse());
        //expectation
        when(specificIdPResponse.getSpecificNode()).thenReturn(specificService);
        when(specificService.processAuthenticationResponse(bytes)).thenReturn(authenticationExchange);
        when(specificService.getProxyServiceRequestCorrelationMap()).thenReturn(correlationMap);
        when(specificService.compareAttributeLists(ImmutableAttributeMap.builder().put(CURRENT_FAMILY_NAME).build(), immutableAttributeMap())).thenReturn(true);
        //test the bean
        ILightResponse lightResponse = specificProxyService.processResponse(httpServletRequest, httpServletResponse);
        //verify
        verify(specificIdPResponse).getSpecificNode();
        verify(specificService).processAuthenticationResponse(eq(bytes));
        verify(specificService).getProxyServiceRequestCorrelationMap();
        verify(specificService).compareAttributeLists(eq(ImmutableAttributeMap.builder().put(CURRENT_FAMILY_NAME).build()), eq(immutableAttributeMap()));
        verifyNoMoreInteractions(specificIdPResponse, specificService);
        //assert
        assertEquals(ID, lightResponse.getInResponseToId());
        assertEquals(ID_RESPONSE, lightResponse.getId());
        assertEquals(REMOTE_ADDRESS, lightResponse.getIPAddress());
        assertEquals(ISSUER_IDP, lightResponse.getIssuer());
        assertEquals(LEVEL_OF_ASSURANCE, lightResponse.getLevelOfAssurance());
        assertEquals(STATUS_CODE, lightResponse.getStatus().getStatusCode());
        assertEquals(immutableAttributeMap(), lightResponse.getAttributes());
    }

    @Test
    public void processResponseFailure() throws Exception {
        byte[] bytes = "Response".getBytes("UTF-8");
        byte[] encodedSamlResponse = Base64.encode(bytes);
        Map<String, String[]> params = new HashMap<>();
        params.put("SAMLResponse", new String[] {new String(encodedSamlResponse)});
        HttpServletRequest httpServletRequest = httpServletRequest(params);

        StoredLightRequest storedLightRequest = StoredLightRequest.builder().request(lightRequest()).remoteIpAddress(REMOTE_ADDRESS).build();
        CorrelationMap<StoredLightRequest> correlationMap = new StoredLightRequestCorrelationMap(new ConcurrentMapServiceDefaultImpl());
        correlationMap.put(ID, storedLightRequest);

        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder(authenticationResponse())
                .statusCode("urn:oasis:names:tc:SAML:2.0:status:Responder").failure(true).build();

        AuthenticationExchange authenticationExchange = new AuthenticationExchange(storedAuthenticationRequest(), authenticationResponse);
        //expectation
        when(specificIdPResponse.getSpecificNode()).thenReturn(specificService);
        when(specificService.processAuthenticationResponse(bytes)).thenReturn(authenticationExchange);
        when(specificService.getProxyServiceRequestCorrelationMap()).thenReturn(correlationMap);
        //test the bean
        ILightResponse lightResponse = specificProxyService.processResponse(httpServletRequest, httpServletResponse);
        //verify
        verify(specificIdPResponse).getSpecificNode();
        verify(specificService).processAuthenticationResponse(eq(bytes));
        verify(specificService).getProxyServiceRequestCorrelationMap();
        verifyNoMoreInteractions(specificIdPResponse, specificService);
        //assert
        assertTrue(lightResponse.getStatus().isFailure());
        assertEquals(ID, lightResponse.getInResponseToId());
        assertEquals(ID_RESPONSE, lightResponse.getId());
        assertEquals(ISSUER_IDP, lightResponse.getIssuer());
    }

    @Test
    public void processResponseInvalidAttrList() throws Exception {
        byte[] bytes = "Response".getBytes("UTF-8");
        byte[] encodedSamlResponse = Base64.encode(bytes);
        Map<String, String[]> params = new HashMap<>();
        params.put("SAMLResponse", new String[] {new String(encodedSamlResponse)});
        HttpServletRequest httpServletRequest = httpServletRequest(params);

        StoredLightRequest storedLightRequest = StoredLightRequest.builder().request(lightRequest()).remoteIpAddress(REMOTE_ADDRESS).build();
        CorrelationMap<StoredLightRequest> correlationMap = new StoredLightRequestCorrelationMap(new ConcurrentMapServiceDefaultImpl());
        correlationMap.put(ID, storedLightRequest);

        ImmutableAttributeMap responseAttributeMap =
                ImmutableAttributeMap.of(CURRENT_GIVEN_NAME, new StringAttributeValue("value", false));

        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder(authenticationResponse())
                .attributes(responseAttributeMap).build();

        AuthenticationExchange authenticationExchange = new AuthenticationExchange(storedAuthenticationRequest(), authenticationResponse);
        //expectation
        when(specificIdPResponse.getSpecificNode()).thenReturn(specificService);
        when(specificService.processAuthenticationResponse(bytes)).thenReturn(authenticationExchange);
        when(specificService.getProxyServiceRequestCorrelationMap()).thenReturn(correlationMap);
        when(specificService.compareAttributeLists(ImmutableAttributeMap.builder().put(CURRENT_FAMILY_NAME).build(), responseAttributeMap)).thenReturn(false);
        //test the bean
        ILightResponse lightResponse = specificProxyService.processResponse(httpServletRequest, httpServletResponse);
        //verify
        verify(specificIdPResponse).getSpecificNode();
        verify(specificService).processAuthenticationResponse(eq(bytes));
        verify(specificService).getProxyServiceRequestCorrelationMap();
        verify(specificService).compareAttributeLists(eq(ImmutableAttributeMap.builder().put(CURRENT_FAMILY_NAME).build()), eq(responseAttributeMap));
        verifyNoMoreInteractions(specificIdPResponse, specificService);
        //assert
        assertTrue(lightResponse.getStatus().isFailure());
        assertEquals(ID, lightResponse.getInResponseToId());
        assertEquals(ID_RESPONSE, lightResponse.getId());
        assertEquals(ISSUER_IDP, lightResponse.getIssuer());
        assertEquals("203001", lightResponse.getStatus().getStatusCode());
        assertEquals("invalid.attrlist", lightResponse.getStatus().getStatusMessage());
    }

    private LightRequest lightRequest() {
        return LightRequest.builder()
                .id(ID)
                .issuer(ISSUER)
                .providerName(PROVIDER_NAME)
                .nameIdFormat(NAME_ID_FORMAT)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .citizenCountryCode(CITIZEN_COUNTRY_CODE)
                .requestedAttributes(ImmutableAttributeMap.builder().put(CURRENT_FAMILY_NAME).build())
                .build();
    }

    private HttpServletRequest httpServletRequest(final  Map<String, String[]> params) {
        final Map<String, Object> attributes = new HashMap<>();

        return new HttpServletRequest() {
            @Override
            public String getAuthType() {
                return null;
            }

            @Override
            public Cookie[] getCookies() {
                return new Cookie[0];
            }

            @Override
            public long getDateHeader(String s) {
                return 0;
            }

            @Override
            public String getHeader(String s) {
                return null;
            }

            @Override
            public Enumeration getHeaders(String s) {
                return null;
            }

            @Override
            public Enumeration getHeaderNames() {
                return enumeration;
            }

            @Override
            public int getIntHeader(String s) {
                return 0;
            }

            @Override
            public String getMethod() {
                return null;
            }

            @Override
            public String getPathInfo() {
                return null;
            }

            @Override
            public String getPathTranslated() {
                return null;
            }

            @Override
            public String getContextPath() {
                return null;
            }

            @Override
            public String getQueryString() {
                return null;
            }

            @Override
            public String getRemoteUser() {
                return null;
            }

            @Override
            public boolean isUserInRole(String s) {
                return false;
            }

            @Override
            public Principal getUserPrincipal() {
                return null;
            }

            @Override
            public String getRequestedSessionId() {
                return null;
            }

            @Override
            public String getRequestURI() {
                return null;
            }

            @Override
            public StringBuffer getRequestURL() {
                return null;
            }

            @Override
            public String getServletPath() {
                return null;
            }

            @Override
            public HttpSession getSession(boolean b) {
                return null;
            }

            @Override
            public HttpSession getSession() {
                return null;
            }

            @Override
            public boolean isRequestedSessionIdValid() {
                return false;
            }

            @Override
            public boolean isRequestedSessionIdFromCookie() {
                return false;
            }

            @Override
            public boolean isRequestedSessionIdFromURL() {
                return false;
            }

            @Override
            public boolean isRequestedSessionIdFromUrl() {
                return false;
            }

            @Override
            public Object getAttribute(String key) {
                return attributes.get(key);
            }

            @Override
            public Enumeration getAttributeNames() {
                return enumeration;
            }

            @Override
            public String getCharacterEncoding() {
                return null;
            }

            @Override
            public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

            }

            @Override
            public int getContentLength() {
                return 0;
            }

            @Override
            public String getContentType() {
                return null;
            }

            @Override
            public ServletInputStream getInputStream() throws IOException {
                return null;
            }

            @Override
            public String getParameter(String s) {
                return params.containsKey(s) ? params.get(s)[0] : null;
            }

            @Override
            public Enumeration getParameterNames() {
                return null;
            }

            @Override
            public String[] getParameterValues(String s) {
                return new String[0];
            }

            @Override
            public Map getParameterMap() {
                return params;
            }

            @Override
            public String getProtocol() {
                return null;
            }

            @Override
            public String getScheme() {
                return null;
            }

            @Override
            public String getServerName() {
                return null;
            }

            @Override
            public int getServerPort() {
                return 0;
            }

            @Override
            public BufferedReader getReader() throws IOException {
                return null;
            }

            @Override
            public String getRemoteAddr() {
                return REMOTE_ADDRESS;
            }

            @Override
            public String getRemoteHost() {
                return null;
            }

            @Override
            public void setAttribute(String key, Object value) {
                attributes.put(key, value);
            }

            @Override
            public void removeAttribute(String s) {

            }

            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public Enumeration getLocales() {
                return null;
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public RequestDispatcher getRequestDispatcher(String s) {
                return dispatcher;
            }

            @Override
            public String getRealPath(String s) {
                return null;
            }

            @Override
            public int getRemotePort() {
                return 0;
            }

            @Override
            public String getLocalName() {
                return null;
            }

            @Override
            public String getLocalAddr() {
                return null;
            }

            @Override
            public int getLocalPort() {
                return 0;
            }
        };
    }

    private IAuthenticationResponse authenticationResponse() {

        return AuthenticationResponse.builder()
                .id(ID_RESPONSE)
                .inResponseTo(ID)
                .responseStatus(ResponseStatus.builder().statusCode(STATUS_CODE).build())
                .audienceRestriction(AUDIENCE_RESTRICTION)
                .ipAddress(REMOTE_ADDRESS)
                .issuer(ISSUER_IDP)
                .levelOfAssurance(LEVEL_OF_ASSURANCE)
                .attributes(immutableAttributeMap())
                .build();
    }

    private ImmutableAttributeMap immutableAttributeMap() {
        return ImmutableAttributeMap.of(CURRENT_FAMILY_NAME, new StringAttributeValue("Garcia"));
    }

    private StoredAuthenticationRequest storedAuthenticationRequest() {
        EidasAuthenticationRequest authenticationRequest = EidasAuthenticationRequest.builder()
                .lightRequest(lightRequest())
                .destination(DESTINATION)
                .build();

        return StoredAuthenticationRequest.builder().remoteIpAddress(REMOTE_ADDRESS).request(authenticationRequest).build();
    }
}