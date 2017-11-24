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

import org.bouncycastle.util.encoders.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.cache.ConcurrentMapServiceDefaultImpl;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.light.impl.LightResponse;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.protocol.impl.BinaryResponseMessage;
import eu.eidas.auth.commons.tx.BinaryAuthenticationExchange;
import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequestCorrelationMap;
import eu.eidas.auth.commons.tx.StoredLightRequest;
import eu.eidas.auth.commons.tx.StoredLightRequestCorrelationMap;
import eu.eidas.auth.specific.IAUConnector;
import eu.eidas.node.SpecificConnectorBean;
import eu.eidas.node.SpecificParameterNames;
import eu.eidas.node.specificcommunication.exception.SpecificException;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * SpecificConnectorImplTest
 *
 * @since 2016-07-14
 */
@RunWith(MockitoJUnitRunner.class)
public class SpecificConnectorImplTest {

    private static final String PROVIDER_NAME = "DEMO-SP";
    private static final String CITIZEN_COUNTRY_CODE = "BE";
    private static final String REMOTE_ADDRESS = "127.0.0.1";
    private static final String ID = UUID.randomUUID().toString();
    private static final String ID_RESPONSE = UUID.randomUUID().toString();
    private static final String ISSUER = "http://localhost:7001/SP/metadata";
    private static final String ISSUER_CONNECTOR = "http://localhost:7001/EidasNode/ConnectorResponderMetadata";
    private static final String LEVEL_OF_ASSURANCE = "http://eidas.europa.eu/LoA/low";
    private static final String STATUS_CODE = "urn:oasis:names:tc:SAML:2.0:status:Success";
    private static final String SP_URL = "http://localhost:7001/SP/ReturnPage";
    private static final String NAME_ID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
    private static final String AUDIENCE_RESTRICTION = "http://localhost:7001/SP/metadata";
    private static final String DESTINATION = "http://localhost:7001/EidasNode/ServiceProvider";

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

    @Mock
    private IAUConnector specificNode;

    @Mock
    private Enumeration<String> enumeration;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private SpecificConnectorBean specificConnectorBean;

    @Mock
    private RequestDispatcher dispatcher;

    @InjectMocks
    private SpecificConnectorImpl specificConnector;

    @Test(expected = SpecificException.class)
    public void processRequestMissingSAMLRequest() throws Exception {
        Map<String, String[]> params = newHashMap();
        HttpServletRequest httpServletRequest = httpServletRequest(params);

        //test the bean
        specificConnector.processRequest(httpServletRequest, httpServletResponse);
        //verify
        verifyNoMoreInteractions(specificConnectorBean, specificNode);
    }

    @Test
    public void processRequest() throws Exception {
        Map<String, String[]> params = newHashMap();
        byte[] bytes = "<SAMLRequest>".getBytes("UTF-8");
        byte[] encodedSAMLRequest = Base64.encode(bytes);
        params.put("SAMLRequest", new String[] { new String(encodedSAMLRequest) });
        HttpServletRequest httpServletRequest = httpServletRequest(params);
        //expectation
        when(specificConnectorBean.getSpecificConnectorNode()).thenReturn(specificNode);
        when(specificNode.processAuthenticationRequest(any(WebRequest.class), eq(bytes))).thenReturn(lightRequest());
        //test the bean
        specificConnector.processRequest(httpServletRequest, httpServletResponse);
        //verify
        verify(specificConnectorBean).getSpecificConnectorNode();
        ArgumentCaptor<WebRequest> webRequestArgumentCaptor = ArgumentCaptor.forClass(WebRequest.class);
        verify(specificNode).processAuthenticationRequest(webRequestArgumentCaptor.capture(), eq(bytes));
        verifyNoMoreInteractions(specificNode, specificConnectorBean);
        //assert
        WebRequest actualWebRequest = webRequestArgumentCaptor.getValue();
        assertEquals("POST", actualWebRequest.getMethod().getValue());
        assertEquals(new String(encodedSAMLRequest), actualWebRequest.getEncodedLastParameterValue("SAMLRequest"));
    }

    @Test
    public void sendResponse() throws Exception {
        Map<String, String[]> params = newHashMap();
        HttpServletRequest httpServletRequest = httpServletRequest(params);

        ILightResponse lightResponse = LightResponse.builder(authenticationResponse()).build();
        IResponseMessage responseMessage = new BinaryResponseMessage(authenticationResponse(), "<Response>".getBytes());
        BinaryAuthenticationExchange binaryAuthenticationExchange = new BinaryAuthenticationExchange(storedAuthenticationRequest(), responseMessage);

        StoredAuthenticationRequest storedAuthenticationRequest = storedAuthenticationRequest();
        CorrelationMap<StoredAuthenticationRequest>
                storedAuthenticationRequestCorrelationMap = new StoredAuthenticationRequestCorrelationMap(new ConcurrentMapServiceDefaultImpl());
        storedAuthenticationRequestCorrelationMap.put(ID, storedAuthenticationRequest);

        StoredLightRequest storedLightRequest = StoredLightRequest.builder().request(lightRequest()).remoteIpAddress(REMOTE_ADDRESS).build();
        CorrelationMap<StoredLightRequest> storedLightRequestCorrelationMap = new StoredLightRequestCorrelationMap(new ConcurrentMapServiceDefaultImpl());
        storedLightRequestCorrelationMap.put(ID, storedLightRequest);
        //expectation
        when(specificConnectorBean.getSpecificConnectorNode()).thenReturn(specificNode);
        when(specificNode.generateAuthenticationResponse(lightResponse, false)).thenReturn(binaryAuthenticationExchange);
        when(specificNode.getSpecificSpRequestCorrelationMap()).thenReturn(storedAuthenticationRequestCorrelationMap);
        when(specificNode.getConnectorRequestCorrelationMap()).thenReturn(storedLightRequestCorrelationMap);
        //test the bean
        specificConnector.sendResponse(lightResponse, httpServletRequest, httpServletResponse);
        //verify
        verify(specificConnectorBean).getSpecificConnectorNode();
        verify(specificNode).generateAuthenticationResponse(eq(lightResponse), eq(false));
        verify(specificNode).getSpecificSpRequestCorrelationMap();
        verify(specificNode).getConnectorRequestCorrelationMap();
        verifyZeroInteractions(specificConnectorBean, specificNode);
        //assert
        assertEquals(new String(Base64.encode("<Response>".getBytes())),httpServletRequest.getAttribute("SAMLResponse"));
        assertEquals(SP_URL, httpServletRequest.getAttribute(EidasParameterKeys.SP_URL.toString()));
        assertNull(httpServletRequest.getAttribute(SpecificParameterNames.RELAY_STATE.toString()));
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

    private HttpServletRequest httpServletRequest(final Map<String, String[]> params) {
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
                return "POST";
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
            public String getParameter(String key) {
                return params.containsKey(key) ? params.get(key)[0] : null;
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
                .issuer(ISSUER_CONNECTOR)
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
                .assertionConsumerServiceURL(SP_URL)
                .destination(DESTINATION)
                .build();

        return StoredAuthenticationRequest.builder().remoteIpAddress(REMOTE_ADDRESS).request(authenticationRequest).build();
    }
}