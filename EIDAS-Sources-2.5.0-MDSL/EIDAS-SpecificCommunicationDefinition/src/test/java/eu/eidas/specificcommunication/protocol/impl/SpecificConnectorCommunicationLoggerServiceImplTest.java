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
import org.mockito.stubbing.Answer;
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
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames.NODE_SPECIFIC_CONNECTOR_CACHE;
import static eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames.NODE_SPECIFIC_PROXYSERVICE_CACHE;
import static eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames.SPECIFIC_NODE_CONNECTOR_CACHE;
import static eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames.SPECIFIC_NODE_PROXYSERVICE_CACHE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:specificCommunicationDefinitionLoggerApplicationContext.xml"})
public class SpecificConnectorCommunicationLoggerServiceImplTest {

    private static final AbstractCollection<AttributeDefinition<?>> REGISTRY = EidasSpec.REGISTRY.getAttributes();

    private static final String ISSUER_NAME = "issuer";
    private static final String CORRECT_TOKEN_ID = "tokenId";
    private static final String WRONG_TOKEN_ID = "wrongToken";
    private final String SERIALIZED_REQUEST_MESSAGE =  readXmlTextFileAfterTag("src/test/resources/lightRequest.xml", "<lightRequest>");
    private  String SERIALIZED_RESPONSE_MESSAGE =  readXmlTextFileAfterTag("src/test/resources/lightResponse.xml", "<lightResponse>");

    private final ILightRequest LIGHT_REQUEST = createLightRequest();
    private final ILightResponse LIGHT_RESPONSE = createLightResponse();

    @Value("${lightToken.connector.request.secret}")
    private String LIGHT_TOKEN_REQUEST_SECRET;
    @Value("${lightToken.connector.request.algorithm}")
    private String LIGHT_TOKEN_REQUEST_ALGORITHM;
    @Value("${lightToken.connector.response.secret}")
    private String LIGHT_TOKEN_RESPONSE_SECRET;
    @Value("${lightToken.connector.response.algorithm}")
    private String LIGHT_TOKEN_RESPONSE_ALGORITHM;

    private static final String SPECIFIC_NODE_CONNECTOR_CACHE_IMPL = "specificNodeConnectorRequestCacheImpl";
    private static final String NODE_SPECIFIC_CONNECTOR_CACHE_IMPL = "nodeSpecificConnectorResponseCacheImpl";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SpecificConnectorCommunicationLoggerServiceImpl connectorLoggerService;

    @Autowired
    @Qualifier(SPECIFIC_NODE_CONNECTOR_CACHE_IMPL)
    private ConcurrentCacheService specificNodeConnectorRequestCacheImpl;

    @Autowired
    @Qualifier(NODE_SPECIFIC_CONNECTOR_CACHE_IMPL)
    private ConcurrentCacheService nodeSpecificConnectorResponseCacheImpl;

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
     * {@link SpecificConnectorCommunicationLoggerServiceImpl#getRequest(String, Collection)}
     * when input token corresponds with a request in the cache
     * <p>
     * Must succeed and return an ILightRequest object that is not null
     */
    @Test
    public void getRequestCorrectToken() throws SpecificCommunicationException, NoSuchAlgorithmException {

        LightRequest lRequest = createLightRequest();
        Cache mockCache = specificNodeConnectorRequestCacheImpl.getConfiguredCache();
        Mockito.when(mockCache.get(CORRECT_TOKEN_ID)).thenReturn(codec.marshall(lRequest));
        String tokenBase64 = createBase64RequestTokenFromId(CORRECT_TOKEN_ID);

        ILightRequest retrievedRequest = connectorLoggerService.getRequest(tokenBase64, REGISTRY);

        assertNotNull(retrievedRequest);
        assertEquals(lRequest, retrievedRequest);
    }

    /**
     * Test method for
     * {@link SpecificConnectorCommunicationLoggerServiceImpl#getRequest(String, Collection)}
     * when input token does not correspond with a request in the cache
     * <p>
     * Must succeed and return an ILightRequest object that is null
     */
    @Test
    public void getRequestWrongToken() throws SpecificCommunicationException, NoSuchAlgorithmException {
        String tokenBase64 = createBase64RequestTokenFromId(WRONG_TOKEN_ID);
        ILightRequest retrievedRequest = connectorLoggerService.getRequest(tokenBase64, REGISTRY);
        assertNull(retrievedRequest);
    }

    /**
     * Test method for {@link SpecificConnectorCommunicationLoggerServiceImpl#getRequest(String)}
     * prove that the method is calling required method {@link LightJAXBCodec#marshall(Object)}
     */
    @Test
    public void getRequest() throws SpecificCommunicationException, NoSuchAlgorithmException {
        ApplicationContext applicationContextSpy = createAppContextSpy();
        interceptCachesInApplicationContextSpy(applicationContextSpy);
        insertIntoCache(SPECIFIC_NODE_CONNECTOR_CACHE, CORRECT_TOKEN_ID, SERIALIZED_REQUEST_MESSAGE);
        String base64Token = createBase64RequestTokenFromId(CORRECT_TOKEN_ID);

        String serializedRequest = connectorLoggerService.getRequest(base64Token);

        assertEquals(SERIALIZED_REQUEST_MESSAGE, serializedRequest);
    }

    /**
     * Test for method {@link SpecificConnectorCommunicationLoggerServiceImpl#getResponse(String)}
     * to assert method {@link LightJAXBCodec#marshall(Object)} is called
     * to assert method {@link SpecificConnectorCommunicationLoggerServiceImpl#getResponse(String, Collection)} is called
     */
    @Test
    public void getResponse() throws SpecificCommunicationException, NoSuchAlgorithmException {
        ApplicationContext applicationContextSpy = createAppContextSpy();
        interceptCachesInApplicationContextSpy(applicationContextSpy);
        insertIntoCache(NODE_SPECIFIC_CONNECTOR_CACHE, CORRECT_TOKEN_ID, SERIALIZED_RESPONSE_MESSAGE);
        String base64Token = createBase64ResponseTokenFromId(CORRECT_TOKEN_ID);

        String serializedResponse = connectorLoggerService.getResponse(base64Token);

        assertEquals(SERIALIZED_RESPONSE_MESSAGE, serializedResponse);
    }

    /**
     * Test method for
     * {@link SpecificConnectorCommunicationLoggerServiceImpl#getResponse(String, Collection)}
     * when input token corresponds with a response in the cache
     * <p>
     * Must succeed and return an ILightResponse object
     */
    @Test
    public void getResponseCorrectToken() throws SpecificCommunicationException, NoSuchAlgorithmException {

        LightResponse lResponse = createLightResponse();
        Cache mockCache = nodeSpecificConnectorResponseCacheImpl.getConfiguredCache();
        Mockito.when(mockCache.get(CORRECT_TOKEN_ID)).thenReturn(codec.marshall(lResponse));
        String tokenBase64 = createBase64ResponseTokenFromId(CORRECT_TOKEN_ID);

        ILightResponse retrievedResponse = connectorLoggerService.getResponse(tokenBase64, REGISTRY);

        assertNotNull(retrievedResponse);
        assertEquals(lResponse, retrievedResponse);
    }

    /**
     * Test method for
     * {@link SpecificConnectorCommunicationLoggerServiceImpl#getResponse(String, Collection)}
     * when input token does not correspond with a response in the cache
     * <p>
     * Must succeed and return an ILightResponse object that is null
     */
    @Test
    public void getResponseWrongToken() throws SpecificCommunicationException, NoSuchAlgorithmException {
        String tokenBase64 = createBase64ResponseTokenFromId(WRONG_TOKEN_ID);
        ILightResponse retrievedResponse = connectorLoggerService.getResponse(tokenBase64, REGISTRY);
        assertNull(retrievedResponse);
    }

    /**
     * Test method for
     * {SpecificConnectorCommunicationLoggerServiceImpl#getRequestCommunicationCache()}
     * <p>
     * Must succeed and return an object of CommunicationCache
     */
    @Test
    public void getRequestCommunicationCache() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        SpecificConnectorCommunicationLoggerServiceImpl connectorLoggerService = new SpecificConnectorCommunicationLoggerServiceImpl(LIGHT_TOKEN_REQUEST_SECRET,
                LIGHT_TOKEN_REQUEST_ALGORITHM, LIGHT_TOKEN_RESPONSE_SECRET, LIGHT_TOKEN_RESPONSE_ALGORITHM);
        SpecificConnectorCommunicationLoggerServiceImpl connectorLoggerServiceSpy = spy(connectorLoggerService);
        Method getRequestCache = AbstractSpecificCommunicationLoggingService.class.getDeclaredMethod("getRequestCommunicationCache", null);
        getRequestCache.setAccessible(true);

        CommunicationCache cacheToTest = (CommunicationCache) getRequestCache.invoke(connectorLoggerServiceSpy, null);
        verify(connectorLoggerServiceSpy).getRequestCacheName();
        Assert.isInstanceOf(CommunicationCache.class, cacheToTest);
    }

    /**
     * Test method for
     * {SpecificConnectorCommunicationLoggerServiceImpl#getResponseCommunicationCache()}
     * <p>
     * Must succeed and return an object of CommunicationCache
     */
    @Test
    public void getResponseCommunicationCache() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        SpecificConnectorCommunicationLoggerServiceImpl connectorLoggerService = new SpecificConnectorCommunicationLoggerServiceImpl(LIGHT_TOKEN_REQUEST_SECRET,
                LIGHT_TOKEN_REQUEST_ALGORITHM, LIGHT_TOKEN_RESPONSE_SECRET, LIGHT_TOKEN_RESPONSE_ALGORITHM);
        SpecificConnectorCommunicationLoggerServiceImpl connectorLoggerServiceSpy = spy(connectorLoggerService);
        Method getResponseCache = AbstractSpecificCommunicationLoggingService.class.getDeclaredMethod("getResponseCommunicationCache", null);
        getResponseCache.setAccessible(true);

        CommunicationCache cacheToTest = (CommunicationCache) getResponseCache.invoke(connectorLoggerServiceSpy, null);
        verify(connectorLoggerServiceSpy).getResponseCacheName();
        Assert.isInstanceOf(CommunicationCache.class, cacheToTest);
    }

    /**
     * Test method for
     * {@link SpecificConnectorCommunicationLoggerServiceImpl#getRequestCacheName()}
     * <p>
     * Must succeed and return a String corresponding with SpecificCommunicationDefinitionBeanNames.SPECIFIC_NODE_CONNECTOR_CACHE.toString()
     */
    @Test
    public void getRequestCacheName() {

        SpecificConnectorCommunicationLoggerServiceImpl connectorLoggerService = new SpecificConnectorCommunicationLoggerServiceImpl(LIGHT_TOKEN_REQUEST_SECRET,
                LIGHT_TOKEN_REQUEST_ALGORITHM, LIGHT_TOKEN_RESPONSE_SECRET, LIGHT_TOKEN_RESPONSE_ALGORITHM);

        String requestCacheName = connectorLoggerService.getRequestCacheName();
        assertNotNull(requestCacheName);
        assertEquals(SpecificCommunicationDefinitionBeanNames.SPECIFIC_NODE_CONNECTOR_CACHE.toString(), requestCacheName);
    }

    /**
     * Test method for
     * {@link SpecificConnectorCommunicationLoggerServiceImpl#getResponseCacheName()}
     * <p>
     * Must succeed and return a String corresponding with SpecificCommunicationDefinitionBeanNames.NODE_SPECIFIC_CONNECTOR_CACHE.toString()
     */
    @Test
    public void getResponseCacheName() {

        SpecificConnectorCommunicationLoggerServiceImpl connectorLoggerService = new SpecificConnectorCommunicationLoggerServiceImpl(LIGHT_TOKEN_REQUEST_SECRET,
                LIGHT_TOKEN_REQUEST_ALGORITHM, LIGHT_TOKEN_RESPONSE_SECRET, LIGHT_TOKEN_RESPONSE_ALGORITHM);

        String responseCacheName = connectorLoggerService.getResponseCacheName();
        assertNotNull(responseCacheName);
        assertEquals(SpecificCommunicationDefinitionBeanNames.NODE_SPECIFIC_CONNECTOR_CACHE.toString(), responseCacheName);
    }

    private LightRequest createLightRequest() {
        return new LightRequest.Builder()
                .id("id")
                .issuer("issuer")
                .citizenCountryCode("CA")
                .levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue())
                .build();
    }


    private LightResponse createLightResponse() {
        LightResponse.Builder lightResponseBuilder = new LightResponse.Builder();

        ResponseStatus.Builder responseStatusBuilder = ResponseStatus.builder();
        responseStatusBuilder.statusCode("statusCode").statusMessage("statusMessage");
        ResponseStatus responseStatus = responseStatusBuilder.build();

        return lightResponseBuilder
                .id("id")
                .issuer("issuer")
                .status(responseStatus)
                .subject("subject")
                .subjectNameIdFormat("subjectNameFormat")
                .inResponseToId("inResponseToId")
                .build();
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

    private ILightToken createLightToken(String tokenId) {
        return LightToken.builder().id(tokenId).issuer("Issuer").createdOn(DateTime.now()).build();
    }

    private void insertIntoCache(SpecificCommunicationDefinitionBeanNames cachename, String key, String value) {
        CommunicationCache communicationCache = (CommunicationCache) SpecificCommunicationApplicationContextProvider.getApplicationContext().getBean(cachename.toString());
        communicationCache.put(key, value);
    }

    private ApplicationContext createAppContextSpy() {
        ApplicationContext applicationContext = SpecificCommunicationApplicationContextProvider.getApplicationContext();
        ApplicationContext applicationContextSpy = spy(applicationContext);
        new SpecificCommunicationApplicationContextProvider().setApplicationContext(applicationContextSpy);
        return applicationContextSpy;
    }

    private void interceptCachesInApplicationContextSpy(ApplicationContext applicationContextSpy) {
        Arrays.asList(
                SPECIFIC_NODE_CONNECTOR_CACHE,
                NODE_SPECIFIC_CONNECTOR_CACHE,
                SPECIFIC_NODE_PROXYSERVICE_CACHE,
                NODE_SPECIFIC_PROXYSERVICE_CACHE).stream().map(SpecificCommunicationDefinitionBeanNames::toString).forEach(cacheBeanName -> {
            CommunicationCache cacheToTest = createCommunicationCache();
            when(applicationContextSpy.getBean(cacheBeanName)).thenReturn(cacheToTest);
        });
    }

    private CommunicationCache createCommunicationCache() {
        Cache cache = createCacheMockUsingMap();
        ConcurrentCacheService concurrentCacheService = mock(ConcurrentCacheService.class);
        when(concurrentCacheService.getConfiguredCache()).thenReturn(cache);
        return new CommunicationCache(concurrentCacheService);
    }

    private Cache createCacheMockUsingMap() {
        Map<String,String> map  = new HashMap();
        Cache cache = mock(Cache.class);
        when(cache.get(anyString())).thenAnswer((Answer<String>) invocationOnMock -> {
            String key = invocationOnMock.getArgument(0);
            return map.get(key);
        });

        when(cache.getAndRemove(anyString())).thenAnswer((Answer<String>) invocationOnMock -> {
            String key = invocationOnMock.getArgument(0);
            String value = map.get(key);
            map.remove(key);
            return value;
        });

        doAnswer( invocationOnMock -> {
            String key = invocationOnMock.getArgument(0);
            String value = invocationOnMock.getArgument(1);
            map.put(key, value);
            return null;
        }).when(cache).put(anyString(),anyString());

        return cache;
    }

    private LightJAXBCodec codecMockFakedMarshalFunction() throws SpecificCommunicationException {
        final LightJAXBCodec mockCodec = mock(LightJAXBCodec.class);
        when(mockCodec.marshall(any(ILightRequest.class))).thenReturn(SERIALIZED_REQUEST_MESSAGE);
        when(mockCodec.marshall(any(ILightResponse.class))).thenReturn(SERIALIZED_RESPONSE_MESSAGE);
        return mockCodec;
    }

    /**
     * very basic file reader for testing where files can have a copyright header
     *
     * @param file location starting in the folder above src/
     * @param tag  line to start reading from
     * @return
     */
    private String readXmlTextFileAfterTag(String file, String tag) {
        try {
            return Files.lines(Paths.get(file))
                    .reduce("", (String buffer, String el) -> {
                        if (el.equals(tag)) buffer = "";
                        buffer += el + "\n";
                        return buffer;
                    }).trim();
        } catch (IOException ex) {
            fail("Bad test setup! Cannot read from file:  " + file);
            return null;
        }
    }
}


