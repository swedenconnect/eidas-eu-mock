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

package eu.eidas.specificcommunication.protocol.impl;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.cache.ConcurrentCacheService;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.ILightToken;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.light.impl.LightResponse;
import eu.eidas.auth.commons.light.impl.LightToken;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.auth.commons.tx.LightTokenEncoder;
import eu.eidas.specificcommunication.BinaryLightTokenHelper;
import eu.eidas.specificcommunication.CommunicationCache;
import eu.eidas.specificcommunication.SpecificCommunicationApplicationContextProvider;
import eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import javax.cache.Cache;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractCollection;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:specificCommunicationDefinitionLoggerApplicationContext.xml"})
public class SpecificProxyserviceCommunicationLoggerServiceImplTest {

    private static final AbstractCollection<AttributeDefinition<?>> REGISTRY = EidasSpec.REGISTRY.getAttributes();

    private static final String ISSUER_NAME = "issuer";
    private static final String CORRECT_TOKEN_ID = "tokenId";
    private static final String WRONG_TOKEN_ID = "wrongToken";

    @Value("${lightToken.connector.request.secret}")
    private String LIGHT_TOKEN_REQUEST_SECRET;
    @Value("${lightToken.connector.request.algorithm}")
    private String LIGHT_TOKEN_REQUEST_ALGORITHM;
    @Value("${lightToken.connector.response.secret}")
    private String LIGHT_TOKEN_RESPONSE_SECRET;
    @Value("${lightToken.connector.response.algorithm}")
    private String LIGHT_TOKEN_RESPONSE_ALGORITHM;


    private static final String NODE_SPECIFIC_PROXYSERVICE_CACHE_IMPL = "nodeSpecificProxyserviceRequestCacheImpl";
    private static final String SPECIFIC_NODE_PROXYSERVICE_CACHE_IMPL = "specificNodeProxyserviceResponseCacheImpl";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SpecificProxyserviceCommunicationLoggerServiceImpl specificProxyserviceCommunicationLoggerServiceImpl;

    @Autowired
    @Qualifier(NODE_SPECIFIC_PROXYSERVICE_CACHE_IMPL)
    private ConcurrentCacheService nodeSpecificProxyserviceRequestCacheImpl;

    @Autowired
    @Qualifier(SPECIFIC_NODE_PROXYSERVICE_CACHE_IMPL)
    private ConcurrentCacheService specificNodeProxyserviceResponseCacheImpl;

    private LightJAXBCodec codec;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        Field appContextField = ReflectionUtils.findField(SpecificCommunicationApplicationContextProvider.class, "applicationContext");
        appContextField.setAccessible(true);
        ReflectionUtils.setField(appContextField, null, applicationContext);
        this.codec = LightJAXBCodec.buildDefault();
    }

    @AfterClass
    public static void tearDown() {
        Field appContextField = ReflectionUtils.findField(SpecificCommunicationApplicationContextProvider.class, "applicationContext");
        appContextField.setAccessible(true);
        ReflectionUtils.setField(appContextField, null, new ClassPathXmlApplicationContext("specificCommunicationDefinitionApplicationContext.xml"));
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationLoggerServiceImpl#getRequest(String, Collection)}
     * when input token corresponds with a request in the cache
     * <p>
     * Must succeed and return an ILightRequest object that is not null
     */
    @Test
    public void getRequestCorrectToken() throws SpecificCommunicationException, NoSuchAlgorithmException {

        LightRequest lRequest = createLightRequest();
        Cache mockCache = nodeSpecificProxyserviceRequestCacheImpl.getConfiguredCache();
        Mockito.when(mockCache.get(CORRECT_TOKEN_ID)).thenReturn(codec.marshall(lRequest));
        String tokenBase64 = createBase64RequestTokenFromId(CORRECT_TOKEN_ID);

        ILightRequest retrievedRequest = specificProxyserviceCommunicationLoggerServiceImpl.getRequest(tokenBase64, REGISTRY);

        assertNotNull(retrievedRequest);
        assertEquals(lRequest, retrievedRequest);
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationLoggerServiceImpl#getRequest(String, Collection)}
     * when input token does not correspond with a request in the cache
     * <p>
     * Must succeed and return an ILightRequest object that is null
     */
    @Test
    public void getRequestWrongToken() throws SpecificCommunicationException, NoSuchAlgorithmException {
        final String tokenBase64 = createBase64RequestTokenFromId(WRONG_TOKEN_ID);
        ILightRequest retrievedRequest = specificProxyserviceCommunicationLoggerServiceImpl.getRequest(tokenBase64, REGISTRY);
        assertNull(retrievedRequest);
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationLoggerServiceImpl#getResponse(String, Collection)}
     * when input token corresponds with a response in the cache
     * <p>
     * Must succeed and return an ILightResponse object
     */
    @Test
    public void getResponseCorrectToken() throws SpecificCommunicationException, NoSuchAlgorithmException {
        LightResponse lResponse = createLightResponse();
        Cache mockCache = specificNodeProxyserviceResponseCacheImpl.getConfiguredCache();
        Mockito.when(mockCache.get(CORRECT_TOKEN_ID)).thenReturn(codec.marshall(lResponse));
        String tokenBase64 = createBase64ResponseTokenFromId(CORRECT_TOKEN_ID);

        ILightResponse retrievedResponse = specificProxyserviceCommunicationLoggerServiceImpl.getResponse(tokenBase64, REGISTRY);

        assertNotNull(retrievedResponse);
        assertEquals(lResponse, retrievedResponse);
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationLoggerServiceImpl#getResponse(String, Collection)}
     * when input token does not correspond with a response in the cache
     * <p>
     * Must succeed and return an ILightResponse object that is null
     */
    @Test
    public void getResponseWrongToken() throws SpecificCommunicationException, NoSuchAlgorithmException {
        String tokenBase64 = createBase64ResponseTokenFromId(WRONG_TOKEN_ID);
        ILightResponse retrievedResponse = specificProxyserviceCommunicationLoggerServiceImpl.getResponse(tokenBase64, REGISTRY);
        assertNull(retrievedResponse);
    }

    /**
     * Test method for
     * {SpecificProxyserviceCommunicationLoggerServiceImpl#getRequestCommunicationCache()}
     * <p>
     * Must succeed and return an object of CommunicationCache
     */
    @Test
    public void getRequestCommunicationCache() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        SpecificProxyserviceCommunicationLoggerServiceImpl proxyserviceLoggerService = new SpecificProxyserviceCommunicationLoggerServiceImpl(LIGHT_TOKEN_REQUEST_SECRET,
                LIGHT_TOKEN_REQUEST_ALGORITHM, LIGHT_TOKEN_RESPONSE_SECRET, LIGHT_TOKEN_RESPONSE_ALGORITHM);
        SpecificProxyserviceCommunicationLoggerServiceImpl proxyserviceLoggerSpy = spy(proxyserviceLoggerService);
        Method getRequestCache = AbstractSpecificCommunicationLoggingService.class.getDeclaredMethod("getRequestCommunicationCache", null);
        getRequestCache.setAccessible(true);

        CommunicationCache cacheToTest = (CommunicationCache) getRequestCache.invoke(proxyserviceLoggerSpy, null);

        verify(proxyserviceLoggerSpy).getRequestCacheName();
        Assert.isInstanceOf(CommunicationCache.class, cacheToTest);
    }

    /**
     * Test method for
     * {SpecificProxyserviceCommunicationLoggerServiceImpl#getResponseCommunicationCache()}
     * <p>
     * Must succeed and return an object of CommunicationCache
     */
    @Test
    public void getResponseCommunicationCache() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        SpecificProxyserviceCommunicationLoggerServiceImpl proxyserviceLoggerService = new SpecificProxyserviceCommunicationLoggerServiceImpl(LIGHT_TOKEN_REQUEST_SECRET,
                LIGHT_TOKEN_REQUEST_ALGORITHM, LIGHT_TOKEN_RESPONSE_SECRET, LIGHT_TOKEN_RESPONSE_ALGORITHM);
        SpecificProxyserviceCommunicationLoggerServiceImpl proxyserviceLoggerSpy = spy(proxyserviceLoggerService);
        Method getResponseCache = AbstractSpecificCommunicationLoggingService.class.getDeclaredMethod("getResponseCommunicationCache", null);
        getResponseCache.setAccessible(true);

        CommunicationCache cacheToTest = (CommunicationCache) getResponseCache.invoke(proxyserviceLoggerSpy, null);

        verify(proxyserviceLoggerSpy).getResponseCacheName();
        Assert.isInstanceOf(CommunicationCache.class, cacheToTest);
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationLoggerServiceImpl#getRequestCacheName()}
     * <p>
     * Must succeed and return a String corresponding with SpecificCommunicationDefinitionBeanNames.SPECIFIC_NODE_CONNECTOR_CACHE.toString()
     */
    @Test
    public void getRequestCacheName() {

        SpecificProxyserviceCommunicationLoggerServiceImpl proxyserviceLoggerService = new SpecificProxyserviceCommunicationLoggerServiceImpl(LIGHT_TOKEN_REQUEST_SECRET,
                LIGHT_TOKEN_REQUEST_ALGORITHM, LIGHT_TOKEN_RESPONSE_SECRET, LIGHT_TOKEN_RESPONSE_ALGORITHM);

        String requestCacheName = proxyserviceLoggerService.getRequestCacheName();
        assertNotNull(proxyserviceLoggerService.getRequestCacheName());
        assertEquals(SpecificCommunicationDefinitionBeanNames.NODE_SPECIFIC_PROXYSERVICE_CACHE.toString(), requestCacheName);
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationLoggerServiceImpl#getResponseCacheName()}
     * <p>
     * Must succeed and return a String corresponding with SpecificCommunicationDefinitionBeanNames.NODE_SPECIFIC_CONNECTOR_CACHE.toString()
     */
    @Test
    public void getResponseCacheName() {

        SpecificProxyserviceCommunicationLoggerServiceImpl proxyserviceLoggerService = new SpecificProxyserviceCommunicationLoggerServiceImpl(LIGHT_TOKEN_REQUEST_SECRET,
                LIGHT_TOKEN_REQUEST_ALGORITHM, LIGHT_TOKEN_RESPONSE_SECRET, LIGHT_TOKEN_RESPONSE_ALGORITHM);

        String responseCacheName = proxyserviceLoggerService.getResponseCacheName();
        assertNotNull(proxyserviceLoggerService.getResponseCacheName());
        assertEquals(SpecificCommunicationDefinitionBeanNames.SPECIFIC_NODE_PROXYSERVICE_CACHE.toString(), responseCacheName);
    }

    private LightRequest createLightRequest() {
        LightRequest.Builder lightRequestBuilder = new LightRequest.Builder();

        lightRequestBuilder
                .id("id")
                .issuer("issuer")
                .levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue())
                .citizenCountryCode("CA");

        return lightRequestBuilder.build();
    }

    private String createBase64RequestTokenFromId(String idToken) throws NoSuchAlgorithmException {
        ILightToken lightToken = createLightToken(idToken);
        BinaryLightToken binaryToken = LightTokenEncoder.encode(lightToken, LIGHT_TOKEN_REQUEST_SECRET, LIGHT_TOKEN_REQUEST_ALGORITHM);
        return BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryToken);
    }

    private String createBase64ResponseTokenFromId(String idToken) throws NoSuchAlgorithmException {
        ILightToken lightToken = createLightToken(idToken);
        BinaryLightToken binaryToken = LightTokenEncoder.encode(lightToken, LIGHT_TOKEN_RESPONSE_SECRET, LIGHT_TOKEN_RESPONSE_ALGORITHM);
        return BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryToken);
    }

    private LightResponse createLightResponse() {
        final ResponseStatus responseStatus = new ResponseStatus.Builder()
                .statusCode("statusCode")
                .statusMessage("statusMessage")
                .build();
        return new LightResponse.Builder()
                .id("id")
                .issuer("issuer")
                .status(responseStatus)
                .subject("subject")
                .subjectNameIdFormat("subjectNameFormat")
                .inResponseToId("inResponseToId")
                .build();
    }

    private ILightToken createLightToken(String tokenId) {
        return LightToken.builder().id(tokenId).issuer("Issuer").createdOn(DateTime.now()).build();
    }

}


