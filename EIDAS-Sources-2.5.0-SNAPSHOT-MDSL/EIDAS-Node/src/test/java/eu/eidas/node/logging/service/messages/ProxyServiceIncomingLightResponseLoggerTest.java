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
package eu.eidas.node.logging.service.messages;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import eu.eidas.auth.cache.ConcurrentMapJcacheServiceDefaultImpl;
import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasDigestUtil;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.logging.LoggingConstants;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.node.logging.messages.MessageLogTestUtils;
import eu.eidas.node.utils.ReflectionUtils;
import eu.eidas.node.utils.logger.LevelFilter;
import eu.eidas.node.utils.logger.LoggerTestData;
import eu.eidas.specificcommunication.CommunicationCache;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationLoggingService;
import eu.eidas.specificcommunication.protocol.impl.SpecificProxyserviceCommunicationServiceExtensionImpl;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.xml.security.utils.Base64;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static eu.eidas.auth.commons.EidasParameterKeys.TOKEN;
import static eu.eidas.node.utils.ReadFileUtils.readFileAsByteArray;
import static eu.eidas.node.utils.logger.LoggerTestUtils.createStartListAppender;
import static eu.eidas.node.utils.logger.LoggerTestUtils.verifyILoggingEvent;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link ProxyServiceIncomingLightResponseLogger}.
 *
 * @since 2.3
 */
public class ProxyServiceIncomingLightResponseLoggerTest {

    private static final String PROXY_SERVICE_LIGHT_RESPONSE_NODE_ID = "SpecificProxyService";
    private static final String PROXY_SERVICE_INCOMING_RESPONSE_ORIGIN = "/SpecificProxyService/AfterCitizenConsentResponse";
    private static final String PROXY_SERVICE_INCOMING_RESPONSE_DESTINATION = "/EidasNode/SpecificProxyServiceResponse";

    private final Logger logger = (Logger) LoggerFactory.getLogger(ProxyServiceIncomingLightResponseLogger.class.getName());
    private ListAppender<ILoggingEvent> infoAppender;
    private ListAppender<ILoggingEvent> errorAppender;

    private MessageLoggerUtils mockMessageLoggerUtils;
    private SpecificCommunicationLoggingService mockSpecificCommunicationLoggingService;

    private ProxyServiceIncomingLightResponseLogger proxyServiceIncomingLightResponseLogger;

    @Before
    public void setup() {
        infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);

        errorAppender = createStartListAppender(new LevelFilter(Level.ERROR));
        logger.addAppender(errorAppender);

        mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        mockSpecificCommunicationLoggingService = mock(SpecificCommunicationLoggingService.class);

        proxyServiceIncomingLightResponseLogger = new ProxyServiceIncomingLightResponseLogger();
        proxyServiceIncomingLightResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils);
        proxyServiceIncomingLightResponseLogger.setSpringManagedSpecificCommunicationLoggingService(mockSpecificCommunicationLoggingService);
        proxyServiceIncomingLightResponseLogger.setLightTokenProxyserviceResponseNodeId(PROXY_SERVICE_LIGHT_RESPONSE_NODE_ID);
    }

    @After
    public void teardown() {
        logger.detachAndStopAllAppenders();
        Mockito.reset(mockMessageLoggerUtils, mockSpecificCommunicationLoggingService);
    }

    /**
     * Test method for {@link ProxyServiceIncomingLightResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * Check no logging occurs when logging is deactivated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageInactive () throws Exception {
        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setDestination(StringUtils.EMPTY);

        ProxyServiceIncomingLightResponseLogger proxyServiceIncomingLightResponseLogger = proxyServiceIncomingLightResponseLogger(testData);
        setUpBeanProvider(proxyServiceIncomingLightResponseLogger);
        proxyServiceIncomingLightResponseLogger.logMessage(logger, mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

        assertThat(infoAppender.list.size(), is(0));
    }

    /**
     * Test method for {@link ProxyServiceIncomingLightResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * Check logging occurs when logging is activated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageActive() throws Exception {
        String inResponseTo = String.format("inResponseTo_%s", RandomStringUtils.randomAlphabetic(10));
        String flowId = String.format("flowId_%s", RandomStringUtils.randomAlphabetic(10));
        Cache<String, String> flowIdCache = new CommunicationCache(new ConcurrentMapJcacheServiceDefaultImpl());
        flowIdCache.put(inResponseTo, flowId);

        String tokenBase64 = EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)) .getBytes());

        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(true)
                .setTokenBase64(tokenBase64)
                .setFlowIdCache(flowIdCache)
                .setOpType(EIDASValues.EIDAS_SERVICE_SPECIFIC_RESPONSE.toString())
                .setNodeId("specificProxyService")
                .setOrigin("/SpecificProxyService/AfterCitizenConsentResponse")
                .setDestination("/EidasNode/SpecificProxyServiceResponse")
                .setMsgId("msgID_" + randomAlphabetic(10))
                .setInResponseTo(inResponseTo)
                .setStatusCode("ResponseStatusCode");

        ProxyServiceIncomingLightResponseLogger proxyServiceIncomingLightResponseLogger = proxyServiceIncomingLightResponseLogger(testData);
        setUpBeanProvider(proxyServiceIncomingLightResponseLogger);
        proxyServiceIncomingLightResponseLogger.logMessage(logger, mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

        assertThat(infoAppender.list.size(), is(1));
        assertTrue(StringUtils.isNotBlank(flowId));

        List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                MessageLogTestUtils.getTagValue("OpType", testData.opType),
                MessageLogTestUtils.getTagValue("NodeId", testData.nodeId),
                MessageLogTestUtils.getTagValue("Origin", testData.origin),
                MessageLogTestUtils.getTagValue("Destination", testData.destination),
                MessageLogTestUtils.getTagValue("flowId", flowId),
                MessageLogTestUtils.getTagValue("msgId", testData.msgId),
                "msgHash",
                "bltHash",
                MessageLogTestUtils.getTagValue("inResponseTo", testData.inResponseTo),
                MessageLogTestUtils.getTagValue("statusCode", testData.statusCode)
        );
        String loggedMessage = infoAppender.list.get(0).getMessage();
        assertThat(loggedMessage, stringContainsInOrder(expectedStrings));
    }

    /**
     * Test method for {@link ProxyServiceIncomingLightResponseLogger#logFullMessage(HttpServletRequest)}
     * when  {@link HttpServletRequest} is passed to getFullResponse() it should
     * call the {@link SpecificCommunicationLoggingService#getResponse(String)} to obtain the xml format of the LightResponseMessage
     * Log this information to the logger with {@link org.slf4j.Marker} of {@link LoggingMarkerMDC#FULL_MSG_EXCHANGE}
     */
    @Test
    public void logFullMessage() throws Exception {
        final String SERIALIZED_RESPONSE_MESSAGE = new String(readFileAsByteArray("logging/testFullLoggingLightResponse.xml"));
        final String tokenBase64 = EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)).getBytes());
        final LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setTokenBase64(tokenBase64)
                .setDestination(StringUtils.EMPTY);
        final SpecificCommunicationLoggingService mockLoggingService = mock(SpecificCommunicationLoggingService.class);
        final ProxyServiceIncomingLightResponseLogger proxyServiceIncomingLightResponseLogger = proxyServiceIncomingLightResponseLogger(testData);
        final org.slf4j.Logger mockLogger = mock(org.slf4j.Logger.class);
        final MessageLoggerUtils mockSettings = mock(MessageLoggerUtils.class);
        when(mockSettings.isLogCompleteMessage()).thenReturn(true);
        when(mockLoggingService.getResponse(any())).thenReturn(SERIALIZED_RESPONSE_MESSAGE);
        proxyServiceIncomingLightResponseLogger.setSpringManagedSpecificCommunicationLoggingService(mockLoggingService);
        proxyServiceIncomingLightResponseLogger.setMessageLoggerUtils(mockSettings);
        FieldUtils.writeField(proxyServiceIncomingLightResponseLogger, "fullLogger", mockLogger, true);

        proxyServiceIncomingLightResponseLogger.logFullMessage(mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

        verify(mockLoggingService).getResponse(eq(tokenBase64));
        verify(mockLogger).info(eq(LoggingMarkerMDC.FULL_MSG_EXCHANGE), eq(SERIALIZED_RESPONSE_MESSAGE));
    }

    /**
     * Test method for {@link ProxyServiceIncomingLightResponseLogger#logFullMessage(HttpServletRequest)}
     * call the {@link SpecificCommunicationLoggingService#getSerializedRequest(String, Collection)} with the expected Error of {@link SpecificCommunicationException}
     * Should handle the error and send and error message to the logger with {@link org.slf4j.Marker} of {@link LoggingMarkerMDC#FULL_MSG_EXCHANGE}
     */
    @Test
    public void logFullMessageError() throws Exception {
        final String ERROR_MESSAGE = "NI!";
        final String tokenBase64 = EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)).getBytes());
        final LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setTokenBase64(tokenBase64)
                .setDestination(StringUtils.EMPTY);
        final SpecificCommunicationLoggingService mockLoggingService = mock(SpecificCommunicationLoggingService.class);
        final ProxyServiceIncomingLightResponseLogger proxyServiceIncomingLightResponseLogger = proxyServiceIncomingLightResponseLogger(testData);
        final org.slf4j.Logger mockLogger = mock(org.slf4j.Logger.class);
        final MessageLoggerUtils mockSettings = mock(MessageLoggerUtils.class);
        when(mockSettings.isLogCompleteMessage()).thenReturn(true);
        when(mockLoggingService.getResponse(any())).thenThrow(new SpecificCommunicationException(ERROR_MESSAGE));
        proxyServiceIncomingLightResponseLogger.setSpringManagedSpecificCommunicationLoggingService(mockLoggingService);
        proxyServiceIncomingLightResponseLogger.setMessageLoggerUtils(mockSettings);
        FieldUtils.writeField(proxyServiceIncomingLightResponseLogger, "fullLogger", mockLogger, true);

        proxyServiceIncomingLightResponseLogger.logFullMessage(mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

        verify(mockLoggingService).getResponse(eq(tokenBase64));
        //verify(mockLoggingService).getFullResponse(tokenBase64);
        verify(mockLogger, never()).info(eq(LoggingMarkerMDC.FULL_MSG_EXCHANGE), anyString());
        verify(mockLogger).error(eq(LoggingMarkerMDC.FULL_MSG_EXCHANGE), anyString(), any(SpecificCommunicationException.class));
    }

    /**
     * Test method for {@link ProxyServiceIncomingLightResponseLogger#logFullMessage(HttpServletRequest)}
     * when  {@link MessageLoggerUtils#isLogCompleteMessage()} is false it should
     * exit the method without logging
     */
    @Test
    public void logFullMessageConfigFalse() throws Exception {
        final String SERIALIZED_MESSAGE = RandomStringUtils.random(10);
        final String tokenBase64 = EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)).getBytes());
        final LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setTokenBase64(tokenBase64)
                .setDestination(StringUtils.EMPTY);
        final SpecificCommunicationLoggingService mockLoggingService = mock(SpecificCommunicationLoggingService.class);
        final ProxyServiceIncomingLightResponseLogger proxyServiceIncomingLightResponseLogger = proxyServiceIncomingLightResponseLogger(testData);
        final org.slf4j.Logger mockLogger = mock(org.slf4j.Logger.class);
        final MessageLoggerUtils mockSettings = mock(MessageLoggerUtils.class);
        when(mockSettings.isLogCompleteMessage()).thenReturn(false);
        when(mockLoggingService.getResponse(any())).thenReturn(SERIALIZED_MESSAGE);
        proxyServiceIncomingLightResponseLogger.setSpringManagedSpecificCommunicationLoggingService(mockLoggingService);
        proxyServiceIncomingLightResponseLogger.setMessageLoggerUtils(mockSettings);
        FieldUtils.writeField(proxyServiceIncomingLightResponseLogger, "fullLogger", mockLogger, true);

        proxyServiceIncomingLightResponseLogger.logFullMessage(mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

        verify(mockLoggingService, never()).getResponse(eq(tokenBase64));
        verify(mockLogger, never()).info(eq(LoggingMarkerMDC.FULL_MSG_EXCHANGE), eq(SERIALIZED_MESSAGE));
    }

    /**
     * Test method for {@link ProxyServiceIncomingLightResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * when in the {@link HttpServletRequest} instance for the key {EidasParameterKeys.SAML_RESPONSE} two tokens are available
     * the first one the bad token: not to be processed
     * the second and last one the good token: the one to be logged
     *
     * Must succeed.
     */
    @Test
    public void testTwoTokensHttpRequestGoodTokenLast() throws Exception {
        String inResponseTo = String.format("inResponseTo_%s", RandomStringUtils.randomAlphabetic(10));
        String flowId = String.format("flowId_%s", RandomStringUtils.randomAlphabetic(10));
        Cache<String, String> flowIdCache = new CommunicationCache(new ConcurrentMapJcacheServiceDefaultImpl());
        flowIdCache.put(inResponseTo, flowId);

        String tokenBase64 = EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)) .getBytes());

        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(true)
                .setTokenBase64(tokenBase64)
                .setFlowIdCache(flowIdCache)
                .setOpType(EIDASValues.EIDAS_SERVICE_SPECIFIC_RESPONSE.toString())
                .setNodeId("specificProxyService")
                .setOrigin("/SpecificProxyService/AfterCitizenConsentResponse")
                .setDestination("/EidasNode/SpecificProxyServiceResponse")
                .setMsgId("msgID_" + randomAlphabetic(10))
                .setInResponseTo(inResponseTo)
                .setStatusCode("ResponseStatusCode");

        ProxyServiceIncomingLightResponseLogger proxyServiceIncomingLightResponseLogger = proxyServiceIncomingLightResponseLogger(testData);
        setUpBeanProvider(proxyServiceIncomingLightResponseLogger);

        String goodToken = "goodToken";
        String badToken = "badToken";
        String[] tokenBase64Array = {badToken, goodToken};
        proxyServiceIncomingLightResponseLogger.logMessage(logger, mockHttpServletRequest(testData, tokenBase64Array));

        assertThat(infoAppender.list.size(), is(1));
        assertTrue(StringUtils.isNotBlank(flowId));

        String loggedMessage = infoAppender.list.get(0).getMessage();

        byte[] bytes = EidasStringUtil.decodeBytesFromBase64(goodToken);
        byte[] msgHashBytes = EidasDigestUtil.hashPersonalToken(bytes);
        String s = Base64.encode(msgHashBytes, 88);
        List<String> strings = Arrays.asList(s);
        Assert.assertThat(loggedMessage, stringContainsInOrder(strings));
    }

    /**
     * Test method for {@link ProxyServiceIncomingLightResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * when the light response cannot be retrieved from the cache.
     * All information that can be logged should be logged anyway
     *
     * Error scenario
     */
    @Test
    public void testLogOfInvalidLightResponse() throws SpecificCommunicationException {
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        Mockito.when(mockSpecificCommunicationLoggingService.getResponse(anyString(), anyList()))
                .thenThrow(SpecificCommunicationException.class);

        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        Mockito.when(mockHttpServletRequest.getHeader(EIDASValues.REFERER.toString()))
                .thenReturn(PROXY_SERVICE_INCOMING_RESPONSE_ORIGIN);
        String[] tokenBase64Array = {"token"};
        HashMap<String, String[]> httpParametersMap = createHttpParameterMap(tokenBase64Array);
        Mockito.when(mockHttpServletRequest.getParameterMap()).thenReturn(httpParametersMap);
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn(BindingMethod.POST.getValue());
        Mockito.when(mockHttpServletRequest.getRequestURL())
                .thenReturn(new StringBuffer(PROXY_SERVICE_INCOMING_RESPONSE_DESTINATION));

        proxyServiceIncomingLightResponseLogger.logMessage(logger, mockHttpServletRequest);

        assertThat(infoAppender.list.size(), is(0));
        assertThat(errorAppender.list.size(), is(2));
        Level expectedLevel = Level.ERROR;
        String expectedMessage = "Incomplete log of the incoming light response because of ";
        Class expectedException = SpecificCommunicationException.class;
        verifyILoggingEvent(errorAppender.list.get(0), expectedLevel, expectedMessage, expectedException);

        List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                MessageLogTestUtils.getTagValue("OpType", EIDASValues.EIDAS_SERVICE_SPECIFIC_RESPONSE.toString()),
                MessageLogTestUtils.getTagValue("NodeId", PROXY_SERVICE_LIGHT_RESPONSE_NODE_ID),
                MessageLogTestUtils.getTagValue("Origin", PROXY_SERVICE_INCOMING_RESPONSE_ORIGIN),
                MessageLogTestUtils.getTagValue("Destination", PROXY_SERVICE_INCOMING_RESPONSE_DESTINATION),
                MessageLogTestUtils.getTagValue("flowId", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("msgId", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("msgHash", LoggingConstants.UNDEFINED),
                "bltHash",
                MessageLogTestUtils.getTagValue("inResponseTo", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("statusCode", LoggingConstants.UNDEFINED)
        );
        String loggedMessage = errorAppender.list.get(1).getMessage();
        assertThat(loggedMessage, stringContainsInOrder(expectedStrings));
    }


    private void setUpBeanProvider (ProxyServiceIncomingLightResponseLogger proxyServiceIncomingLightResponseLogger) {
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
        Mockito.when(mockApplicationContext.getBean(anyString())).thenReturn(proxyServiceIncomingLightResponseLogger);
    }

    private HttpServletRequest mockHttpServletRequest(LoggerTestData testData, String[] tokenBase64Array) {
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        Mockito.when(mockHttpServletRequest.getAttribute(TOKEN.toString())).thenReturn(testData.tokenBase64);

        HashMap<String, String[]> httpParametersMap = createHttpParameterMap(tokenBase64Array);
        Mockito.when(mockHttpServletRequest.getParameterMap()).thenReturn(httpParametersMap);
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn(BindingMethod.POST.getValue());
        Mockito.when(mockHttpServletRequest.getHeader(EIDASValues.REFERER.toString())).thenReturn(testData.origin);
        Mockito.when(mockHttpServletRequest.getRequestURL()).thenReturn(new StringBuffer(testData.destination));
        return mockHttpServletRequest;
    }

    private HashMap<String, String[]> createHttpParameterMap(String[] tokenBase64Array) {
        final String servletParameter = EidasParameterKeys.TOKEN.toString();
        HashMap<String, String[]> httpParametersMap = new HashMap<>();
        httpParametersMap.put(servletParameter, tokenBase64Array);
        return httpParametersMap;
    }

    private ProxyServiceIncomingLightResponseLogger proxyServiceIncomingLightResponseLogger (LoggerTestData testData) throws SpecificCommunicationException {
        ProxyServiceIncomingLightResponseLogger proxyServiceIncomingLightResponseLogger = new ProxyServiceIncomingLightResponseLogger();

        proxyServiceIncomingLightResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));

        proxyServiceIncomingLightResponseLogger.setLightTokenProxyserviceResponseNodeId(testData.nodeId);

        SpecificCommunicationLoggingService mockSpecificCommunicationLoggingService = mockSpecificCommunicationLoggingService(testData);
        proxyServiceIncomingLightResponseLogger.setSpringManagedSpecificCommunicationLoggingService(mockSpecificCommunicationLoggingService);

        proxyServiceIncomingLightResponseLogger.setFlowIdCache(testData.flowIdCache);
        return proxyServiceIncomingLightResponseLogger;
    }

    private SpecificProxyserviceCommunicationServiceExtensionImpl mockSpecificProxyserviceCommunicationServiceExtension(LoggerTestData testData) {
        SpecificProxyserviceCommunicationServiceExtensionImpl mockSpecificProxyserviceCommunicationServiceExtension = mock(SpecificProxyserviceCommunicationServiceExtensionImpl.class);
        Mockito.when(mockSpecificProxyserviceCommunicationServiceExtension.getLightTokenResponseNodeId()).thenReturn(testData.nodeId);
        return mockSpecificProxyserviceCommunicationServiceExtension;
    }

    private SpecificCommunicationLoggingService mockSpecificCommunicationLoggingService(LoggerTestData testData) throws SpecificCommunicationException {
        SpecificCommunicationLoggingService mockSpecificCommunicationLoggingService = mock(SpecificCommunicationLoggingService.class);
        ILightResponse mockILightResponse = mockILightResponse(testData);
        Mockito.when(mockSpecificCommunicationLoggingService.getResponse(anyString(), any())).thenReturn(mockILightResponse);
        return mockSpecificCommunicationLoggingService;
    }

    private ILightResponse mockILightResponse(LoggerTestData testData) {
        ResponseStatus mockResponseStatus = mock(ResponseStatus.class);
        Mockito.when(mockResponseStatus.getStatusCode()).thenReturn(testData.statusCode);

        ILightResponse mockILightResponse = mock(ILightResponse.class);
        Mockito.when(mockILightResponse.getId()).thenReturn(testData.msgId);
        Mockito.when(mockILightResponse.getInResponseToId()).thenReturn(testData.inResponseTo);
        Mockito.when(mockILightResponse.getStatus()).thenReturn(mockResponseStatus);

        return mockILightResponse;
    }

    private MessageLoggerUtils mockMessageLoggerUtils(LoggerTestData testData) {
        MessageLoggerUtils mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(testData.logMessage);
        return mockMessageLoggerUtils;
    }
}
