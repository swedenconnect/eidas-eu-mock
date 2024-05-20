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
package eu.eidas.node.service.utils;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.engine.ProtocolEngine;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.EidasMetadataRoleParametersI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.exceptions.SamlFailureResponseException;
import eu.eidas.node.service.exceptions.ProxyServiceError;
import eu.eidas.node.utils.ReflectionUtils;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static eu.eidas.auth.commons.EidasParameterKeys.SAML_REQUEST;

public class ProxyServiceSamlFailureUtilTest {

    private ProtocolEngineFactory mockProtocolEngineFactory = Mockito.mock(ProtocolEngineFactory.class);
    private ProtocolEngine mockProtocolEngine = Mockito.mock(ProtocolEngine.class);
    private MessageSource mockMessageSource= Mockito.mock(MessageSource.class);
    private MetadataFetcherI mockMetadataFetcher = Mockito.mock(MetadataFetcherI.class);
    private EidasMetadataParametersI mockMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);
    private EidasMetadataRoleParametersI mockMetadataRoleParameters = Mockito.mock(EidasMetadataRoleParametersI.class);
    private Set<EidasMetadataRoleParametersI> mockMetadataRoleParametersSet = new HashSet<>();
    private HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    private IResponseMessage mockResponseMessage = Mockito.mock(IResponseMessage.class);
    private String serviceMetadataUrl = "testURl";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        ReflectionUtils.setStaticField(ProxyServiceSamlFailureUtil.class,"nodeProtocolEngineFactory", mockProtocolEngineFactory);
        ReflectionUtils.setStaticField(ProxyServiceSamlFailureUtil.class,"metadataFetcher", mockMetadataFetcher);
        ReflectionUtils.setStaticField(ProxyServiceSamlFailureUtil.class,"messageSource", mockMessageSource);
        ReflectionUtils.setStaticField(ProxyServiceSamlFailureUtil.class,"serviceMetadataUrl", serviceMetadataUrl);
        mockMetadataRoleParametersSet.add(mockMetadataRoleParameters);

        Mockito.when(mockProtocolEngineFactory.getProtocolEngine(Mockito.any())).thenReturn(mockProtocolEngine);
        Mockito.when(mockMetadataFetcher.getEidasMetadata(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(mockMetadataParameters);
        Mockito.when(mockMetadataParameters.getRoleDescriptors()).thenReturn(mockMetadataRoleParametersSet);
    }

    @After
    public void tearDown(){
        ReflectionUtils.setStaticField(ProxyServiceSamlFailureUtil.class,"nodeProtocolEngineFactory", null);
        ReflectionUtils.setStaticField(ProxyServiceSamlFailureUtil.class,"metadataFetcher",null);
        ReflectionUtils.setStaticField(ProxyServiceSamlFailureUtil.class,"messageSource", null);
        ReflectionUtils.setStaticField(ProxyServiceSamlFailureUtil.class,"serviceMetadataUrl", null);
    }

    /**
     * Test method for {@link ProxyServiceSamlFailureUtil#generateSamlFailure(HttpServletRequest, IAuthenticationRequest, SamlFailureResponseException)}
     *
     * Must succeed
     */
    @Test
    public void testGenerateSamlFailure() throws EIDASSAMLEngineException {
        SamlFailureResponseException exception = new SamlFailureResponseException("1","test","failure","relayState");

        Mockito.when(mockProtocolEngine.generateResponseErrorMessage(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(mockResponseMessage);
        Mockito.when(mockResponseMessage.getMessageBytes()).thenReturn("test".getBytes(StandardCharsets.UTF_8));

        ProxyServiceSamlFailureUtil.generateSamlFailure(mockRequest, getDefaultTestAuthenticationRequest(), exception);
    }

    /**
     * Test method for {@link ProxyServiceSamlFailureUtil#generateSamlFailure(HttpServletRequest, IAuthenticationRequest, SamlFailureResponseException)}
     * When the error key is put in the error message as a string
     *
     * Must succeed
     */
    @Test
    public void testGenerateSamlFailureWithErrorKeyInMessage() throws EIDASSAMLEngineException {
        SamlFailureResponseException exception = new SamlFailureResponseException(
                EidasErrorKey.AUTHENTICATION_FAILED_ERROR.errorCode(),EidasErrorKey.AUTHENTICATION_FAILED_ERROR.toString(),
                "failure","relayState");

        Mockito.when(mockProtocolEngine.generateResponseErrorMessage(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(mockResponseMessage);
        Mockito.when(mockResponseMessage.getMessageBytes()).thenReturn("test".getBytes(StandardCharsets.UTF_8));

        ProxyServiceSamlFailureUtil.generateSamlFailure(mockRequest, getDefaultTestAuthenticationRequest(), exception);
    }

    /**
     * Test method for {@link ProxyServiceSamlFailureUtil#generateSamlFailure(HttpServletRequest, IAuthenticationRequest, SamlFailureResponseException)}
     * When messageSource throws NoSuchMessage exception and error code is not null
     *
     * Must succeed
     */
    @Test
    public void testGenerateSamlFailureWithNoSuchMessageExceptionWithCodeNotNull() throws EIDASSAMLEngineException {
        SamlFailureResponseException exception = new SamlFailureResponseException(
                EidasErrorKey.AUTHENTICATION_FAILED_ERROR.errorCode(),"test","failure","relayState");

        Mockito.when(mockProtocolEngine.generateResponseErrorMessage(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(mockResponseMessage);
        Mockito.when(mockResponseMessage.getMessageBytes()).thenReturn("test".getBytes(StandardCharsets.UTF_8));
        Mockito.when(mockMessageSource.getMessage(Mockito.any(),Mockito.any(),Mockito.any())).thenThrow(NoSuchMessageException.class);

        ProxyServiceSamlFailureUtil.generateSamlFailure(mockRequest, getDefaultTestAuthenticationRequest(), exception);
    }

    /**
     * Test method for {@link ProxyServiceSamlFailureUtil#generateSamlFailure(HttpServletRequest, IAuthenticationRequest, SamlFailureResponseException)}
     * When messageSource throws NoSuchMessage exception and error code is null
     *
     * Must succeed
     */
    @Test
    public void testGenerateSamlFailureWithNoSuchMessageExceptionWithCodeNull() throws EIDASSAMLEngineException {
        SamlFailureResponseException exception = new SamlFailureResponseException(null,"test","failure","relayState");

        Mockito.when(mockProtocolEngine.generateResponseErrorMessage(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(mockResponseMessage);
        Mockito.when(mockResponseMessage.getMessageBytes()).thenReturn("test".getBytes(StandardCharsets.UTF_8));
        Mockito.when(mockMessageSource.getMessage(Mockito.any(),Mockito.any(),Mockito.any())).thenThrow(NoSuchMessageException.class);

        ProxyServiceSamlFailureUtil.generateSamlFailure(mockRequest, getDefaultTestAuthenticationRequest(), exception);
    }

    /**
     * Test method for {@link ProxyServiceSamlFailureUtil#generateSamlFailure(HttpServletRequest, IAuthenticationRequest, SamlFailureResponseException)}
     * When protocolEngine throws EIDASSAMLEngineException
     *
     * Must fail
     */
    @Test
    public void testGenerateSamlFailureWithEidasSamlEngineException() throws EIDASSAMLEngineException {
        expectedException.expect(ProxyServiceError.class);

        SamlFailureResponseException exception = new SamlFailureResponseException("1","test","failure","relayState");


        Mockito.when(mockMetadataRoleParameters.getDefaultAssertionConsumerUrl()).thenReturn("assertionConsumer");
        Mockito.when(mockProtocolEngine.generateResponseErrorMessage(Mockito.any(),Mockito.any(),Mockito.any()))
                .thenThrow(EIDASSAMLEngineException.class);

        ProxyServiceSamlFailureUtil.generateSamlFailure(mockRequest, getDefaultTestAuthenticationRequest(), exception);
    }

    /**
     * Test method for {@link ProxyServiceSamlFailureUtil#generateSamlFailure(HttpServletRequest, IAuthenticationRequest, SamlFailureResponseException)}
     * When assertionConsumerUrl in AuthenticationRequest is empty
     *
     * Must succeed
     */
    @Test
    public void testGenerateSamlFailureWithAssertionConsumerUrlEmpty() throws EIDASSAMLEngineException {
        SamlFailureResponseException exception = new SamlFailureResponseException("1","test","failure","relayState");
        ImmutableAttributeMap mockImmutableAttributeMap = Mockito.mock(ImmutableAttributeMap.class);
        EidasAuthenticationRequest blankAssertionConsumerUrlRequest = new EidasAuthenticationRequest.Builder()
                        .id("f5e7e0f5-b9b8-4256-a7d0-4090141b326d")
                        .destination("postDestination")
                        .issuer("issuer")
                        .citizenCountryCode("EU")
                        .levelOfAssurance(NotifiedLevelOfAssurance.HIGH.stringValue())
                        .assertionConsumerServiceURL("")
                        .requestedAttributes(mockImmutableAttributeMap)
                        .build();

        Mockito.when(mockProtocolEngine.generateResponseErrorMessage(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(mockResponseMessage);
        Mockito.when(mockResponseMessage.getMessageBytes()).thenReturn("test".getBytes(StandardCharsets.UTF_8));

        ProxyServiceSamlFailureUtil.generateSamlFailure(mockRequest, blankAssertionConsumerUrlRequest, exception);
    }

    /**
     * Test method for {@link ProxyServiceSamlFailureUtil#generateSamlFailure(HttpServletRequest, IAuthenticationRequest, SamlFailureResponseException)}
     *
     * Must succeed
     */
    @Test
    public void testGenerateSamlFailureFromResponse() throws EIDASSAMLEngineException {

        StoredAuthenticationRequest mockOriginalRequest = Mockito.mock(StoredAuthenticationRequest.class);
        IAuthenticationRequest storedAuthRequest = getDefaultTestAuthenticationRequest();
        SamlFailureResponseException exception = new SamlFailureResponseException("1","test","failure","relayState");

        Mockito.when(mockProtocolEngine.generateResponseErrorMessage(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(mockResponseMessage);
        Mockito.when(mockResponseMessage.getMessageBytes()).thenReturn("test".getBytes(StandardCharsets.UTF_8));
        Mockito.when(mockOriginalRequest.getRequest()).thenReturn(storedAuthRequest);


        ProxyServiceSamlFailureUtil.generateSamlFailure(mockRequest, storedAuthRequest, exception);
    }

    /**
     * Test method for {@link ProxyServiceSamlFailureUtil#getAuthenticationRequestFromHttpServletRequest(HttpServletRequest)}
     *
     * Must succeed
     */
    @Test
    public void testGetAuthenticationRequestFromHttpServletRequest() throws EIDASSAMLEngineException {
        Mockito.when(mockRequest.getMethod()).thenReturn("POST");
        String[] tokenBase64 = {"token"};
        HashMap mockParameterMap = new HashMap();
        mockParameterMap.put(SAML_REQUEST.toString(),tokenBase64);
        Mockito.when(mockRequest.getParameterMap()).thenReturn(mockParameterMap);

        Mockito.when(mockProtocolEngine.unmarshallRequestAndValidate(Mockito.any(),Mockito.any())).thenReturn(getDefaultTestAuthenticationRequest());
        Mockito.when(mockMetadataRoleParameters.getRole()).thenReturn(MetadataRole.SP);
        Mockito.when(mockMetadataRoleParameters.getDefaultAssertionConsumerUrl()).thenReturn("assertionConsumer");

        ProxyServiceSamlFailureUtil.getAuthenticationRequestFromHttpServletRequest(mockRequest);
    }

    /**
     * Test method for {@link ProxyServiceSamlFailureUtil#getAuthenticationRequestFromHttpServletRequest(HttpServletRequest)}
     * When issuer is blank
     *
     * Must fail
     */
    @Test
    public void testGetAuthenticationRequestFromHttpServletRequestWithBlankIssuer() throws EIDASSAMLEngineException {
        expectedException.expect(ProxyServiceError.class);

        Mockito.when(mockRequest.getMethod()).thenReturn("POST");
        String[] tokenBase64 = {"token"};
        HashMap mockParameterMap = new HashMap();
        mockParameterMap.put(SAML_REQUEST.toString(),tokenBase64);
        Mockito.when(mockRequest.getParameterMap()).thenReturn(mockParameterMap);

        IAuthenticationRequest mockAuthRequest = Mockito.mock(IAuthenticationRequest.class);

        Mockito.when(mockProtocolEngine.unmarshallRequestAndValidate(Mockito.any(),Mockito.any())).thenReturn(mockAuthRequest);
        Mockito.when(mockMetadataRoleParameters.getRole()).thenReturn(MetadataRole.SP);
        Mockito.when(mockMetadataRoleParameters.getDefaultAssertionConsumerUrl()).thenReturn("assertionConsumer");

        ProxyServiceSamlFailureUtil.getAuthenticationRequestFromHttpServletRequest(mockRequest);
    }

    /**
     * Test method for {@link ProxyServiceSamlFailureUtil#getAuthenticationRequestFromHttpServletRequest(HttpServletRequest)}
     * When metadata fetcher throws EIDASMetadataException
     *
     * Must fail
     */
    @Test
    public void testGetAuthenticationRequestFromHttpServletRequestWithEIDASMetadataException() throws EIDASSAMLEngineException, EIDASMetadataException {
        expectedException.expect(ProxyServiceError.class);

        Mockito.when(mockRequest.getMethod()).thenReturn("POST");
        String[] tokenBase64 = {"token"};
        HashMap mockParameterMap = new HashMap();
        mockParameterMap.put(SAML_REQUEST.toString(),tokenBase64);
        Mockito.when(mockRequest.getParameterMap()).thenReturn(mockParameterMap);

        IAuthenticationRequest mockAuthRequest = Mockito.mock(IAuthenticationRequest.class);
        Mockito.when(mockAuthRequest.getIssuer()).thenReturn("http://127.0.0.1");

        Mockito.when(mockProtocolEngine.unmarshallRequestAndValidate(Mockito.any(),Mockito.any())).thenReturn(mockAuthRequest);
        Mockito.when(mockMetadataFetcher.getEidasMetadata(any(),any(),any())).thenThrow(EIDASMetadataException.class);
        Mockito.when(mockMetadataRoleParameters.getRole()).thenReturn(MetadataRole.SP);
        Mockito.when(mockMetadataRoleParameters.getDefaultAssertionConsumerUrl()).thenReturn("assertionConsumer");

        ProxyServiceSamlFailureUtil.getAuthenticationRequestFromHttpServletRequest(mockRequest);
    }

    /**
     * Test method for {@link ProxyServiceSamlFailureUtil#getAuthenticationRequestFromHttpServletRequest(HttpServletRequest)}
     * When AssertionConsumerServiceUrl in request is empty
     *
     * Must succeed
     */
    @Test
    public void testGetAuthenticationRequestFromHttpServletRequestWithAssertionConsumerServiceUrlEmpty() throws EIDASSAMLEngineException {
        Mockito.when(mockRequest.getMethod()).thenReturn("POST");
        String[] tokenBase64 = {"token"};
        HashMap mockParameterMap = new HashMap();
        mockParameterMap.put(SAML_REQUEST.toString(),tokenBase64);
        Mockito.when(mockRequest.getParameterMap()).thenReturn(mockParameterMap);

        ImmutableAttributeMap mockImmutableAttributeMap = Mockito.mock(ImmutableAttributeMap.class);
        EidasAuthenticationRequest blankAssertionConsumerUrlRequest = new EidasAuthenticationRequest.Builder()
                .id("f5e7e0f5-b9b8-4256-a7d0-4090141b326d")
                .destination("postDestination")
                .issuer("issuer")
                .citizenCountryCode("EU")
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH.stringValue())
                .assertionConsumerServiceURL("")
                .requestedAttributes(mockImmutableAttributeMap)
                .build();

        Mockito.when(mockProtocolEngine.unmarshallRequestAndValidate(Mockito.any(),Mockito.any())).thenReturn(blankAssertionConsumerUrlRequest);
        Mockito.when(mockMetadataRoleParameters.getRole()).thenReturn(MetadataRole.SP);
        Mockito.when(mockMetadataRoleParameters.getDefaultAssertionConsumerUrl()).thenReturn("assertionConsumer");

       IAuthenticationRequest result = ProxyServiceSamlFailureUtil.getAuthenticationRequestFromHttpServletRequest(mockRequest);
        Assert.assertNotNull(result.getAssertionConsumerServiceURL());
    }

    private EidasAuthenticationRequest getDefaultTestAuthenticationRequest() {
        return getTestAuthenticationRequestBuilder().build();
    }

    private EidasAuthenticationRequest.Builder getTestAuthenticationRequestBuilder() {
        ImmutableAttributeMap mockImmutableAttributeMap = Mockito.mock(ImmutableAttributeMap.class);
        return new EidasAuthenticationRequest.Builder()
                .id("f5e7e0f5-b9b8-4256-a7d0-4090141b326d")
                .destination("postDestination")
                .issuer("issuer")
                .citizenCountryCode("EU")
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH.stringValue())
                .assertionConsumerServiceURL("assertionConsumer")
                .requestedAttributes(mockImmutableAttributeMap);
    }
}