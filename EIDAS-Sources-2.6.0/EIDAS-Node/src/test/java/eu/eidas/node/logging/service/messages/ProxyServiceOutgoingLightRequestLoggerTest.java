/*
 * Copyright (c) 2021 by European Commission
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
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.light.ILightRequest;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static eu.eidas.auth.commons.EidasParameterKeys.TOKEN;
import static eu.eidas.node.utils.logger.LoggerTestUtils.createStartListAppender;
import static eu.eidas.node.utils.logger.LoggerTestUtils.verifyILoggingEvent;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link ProxyServiceOutgoingLightRequestLogger}.
 *
 * @since 2.3
 */
public class ProxyServiceOutgoingLightRequestLoggerTest {

    private static final String PROXY_SERVICE_LIGHT_REQUEST_NODE_ID = "SpecificProxyService";
    private static final String PROXY_SERVICE_OUTGOING_REQUEST_DESTINATION = "/SpecificProxyService/ProxyServiceRequest";

    private final Logger logger = (Logger) LoggerFactory.getLogger(ProxyServiceOutgoingLightRequestLogger.class.getName());
    private ListAppender<ILoggingEvent> infoAppender;
    private ListAppender<ILoggingEvent> errorAppender;

    private MessageLoggerUtils mockMessageLoggerUtils;
    private SpecificCommunicationLoggingService mockSpecificCommunicationLoggingService;

    private ProxyServiceOutgoingLightRequestLogger proxyServiceOutgoingLightRequestLogger;
    private ApplicationContext oldContext = null;

    @Before
    public void setup() {
        infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);

        errorAppender = createStartListAppender(new LevelFilter(Level.ERROR));
        logger.addAppender(errorAppender);

        mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        mockSpecificCommunicationLoggingService = mock(SpecificCommunicationLoggingService.class);

        proxyServiceOutgoingLightRequestLogger = new ProxyServiceOutgoingLightRequestLogger();
        proxyServiceOutgoingLightRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils);
        proxyServiceOutgoingLightRequestLogger.setLightTokenProxyserviceRequestNodeId(PROXY_SERVICE_LIGHT_REQUEST_NODE_ID);
        proxyServiceOutgoingLightRequestLogger.setSpringManagedSpecificCommunicationLoggingService(mockSpecificCommunicationLoggingService);
    }

    @After
    public void teardown() {
        logger.detachAndStopAllAppenders();
        Mockito.reset(mockMessageLoggerUtils, mockSpecificCommunicationLoggingService);
        if (oldContext!=null){
            ReflectionUtils.setStaticField(BeanProvider.class,"CONTEXT",oldContext);
            oldContext = null;
        }
    }

    /**
     * Test method for {@link ProxyServiceOutgoingLightRequestLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * Check no logging occurs when logging is deactivated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageInactive () throws Exception {
        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setDestination(StringUtils.EMPTY);

        ProxyServiceOutgoingLightRequestLogger proxyServiceOutgoingLightRequestLogger = proxyServiceOutgoingLightRequestLogger(testData);
        setUpBeanProvider(proxyServiceOutgoingLightRequestLogger);
        proxyServiceOutgoingLightRequestLogger.logMessage(logger, mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

        assertThat(infoAppender.list.size(), is(0));
    }

    /**
     * Test method for {@link ProxyServiceOutgoingLightRequestLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * Check logging occurs when logging is activated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageActive () throws Exception {
        String msgId = String.format("msgID_%s", RandomStringUtils.randomAlphabetic(10));
        String flowId = String.format("flowId_%s", RandomStringUtils.randomAlphabetic(10));
        Cache<String, String> flowIdCache = new CommunicationCache(new ConcurrentMapJcacheServiceDefaultImpl());
        flowIdCache.put(msgId, flowId);
        String tokenBase64 = EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)) .getBytes());

        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(true)
                .setTokenBase64(tokenBase64)
                .setFlowIdCache(flowIdCache)
                .setOpType(EIDASValues.EIDAS_SERVICE_SPECIFIC_REQUEST.toString())
                .setNodeId("specificProxyService")
                .setOrigin(LoggingConstants.NOT_APPLICABLE)
                .setDestination("/SpecificProxyService/ProxyServiceRequest")
                .setMsgId(msgId);

        ProxyServiceOutgoingLightRequestLogger proxyServiceOutgoingLightRequestLogger = proxyServiceOutgoingLightRequestLogger(testData);
        setUpBeanProvider(proxyServiceOutgoingLightRequestLogger);
        String[] tokenBase64Array = {testData.tokenBase64};
        proxyServiceOutgoingLightRequestLogger.logMessage(logger, mockHttpServletRequest(testData, tokenBase64Array));

        assertThat(infoAppender.list.size(), is(1));
        List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                MessageLogTestUtils.getTagValue("OpType", testData.opType),
                MessageLogTestUtils.getTagValue("NodeId", testData.nodeId),
                MessageLogTestUtils.getTagValue("Origin", testData.origin),
                MessageLogTestUtils.getTagValue("Destination", testData.destination),
                MessageLogTestUtils.getTagValue("flowId", flowId),
                MessageLogTestUtils.getTagValue("msgId", testData.msgId),
                "msgHash",
                "bltHash"
        );
        String loggedMessage = infoAppender.list.get(0).getMessage();
        assertThat(loggedMessage, stringContainsInOrder(expectedStrings));
    }

    /**
     * Test method for {@link ProxyServiceOutgoingLightRequestLogger#logFullMessage(HttpServletRequest)}
     * when  {@link HttpServletRequest} is passed to getSerializedResponse() it should
     * call the {@link SpecificCommunicationLoggingService#getResponse(String)} to obtain the xml format of the LightRequestMessage
     * Log this information to the logger with {@link org.slf4j.Marker} of {@link LoggingMarkerMDC#FULL_MSG_EXCHANGE}
     */
    @Test
    public void logFullMessage() throws Exception {
        final String SERIALIZED_MESSAGE = RandomStringUtils.random(10);
        final String tokenBase64 = EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)).getBytes());
        final LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setTokenBase64(tokenBase64)
                .setDestination(StringUtils.EMPTY);
        final SpecificCommunicationLoggingService mockLoggingService = mock(SpecificCommunicationLoggingService.class);
        final ProxyServiceOutgoingLightRequestLogger proxyServiceOutgoingLightRequestLogger = proxyServiceOutgoingLightRequestLogger(testData);
        final org.slf4j.Logger mockLogger = mock(org.slf4j.Logger.class);
        final MessageLoggerUtils mockSettings = mock(MessageLoggerUtils.class);
        when(mockSettings.isLogCompleteMessage()).thenReturn(true);
        when(mockLoggingService.getRequest(any()))
                .thenReturn(SERIALIZED_MESSAGE);
        proxyServiceOutgoingLightRequestLogger.setSpringManagedSpecificCommunicationLoggingService(mockLoggingService);
        proxyServiceOutgoingLightRequestLogger.setMessageLoggerUtils(mockSettings);
        FieldUtils.writeField(proxyServiceOutgoingLightRequestLogger, "fullLogger", mockLogger, true);

        proxyServiceOutgoingLightRequestLogger.logFullMessage(mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

        verify(mockLoggingService).getRequest(eq(tokenBase64));
        verify(mockLogger).info(eq(LoggingMarkerMDC.FULL_MSG_EXCHANGE), eq(SERIALIZED_MESSAGE));
    }

    /**
     * Test method for {@link ProxyServiceOutgoingLightRequestLogger#logFullMessage(HttpServletRequest)}
     * call the {@link SpecificCommunicationLoggingService#getRequest(String)} with the expected Error of {@link SpecificCommunicationException}
     * Should handle the error and send and error message to the logger with {@link org.slf4j.Marker} of {@link LoggingMarkerMDC#FULL_MSG_EXCHANGE}
     */
    @Test
    public void logFullMessageError() throws Exception {
        final String ERROR_MESSAGE = "WHAT IS THE AVERAGE AIR VELOCITY OF AN UNLADEN SWALLOW";
        final String tokenBase64 = EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)).getBytes());
        final LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setTokenBase64(tokenBase64)
                .setDestination(StringUtils.EMPTY);
        final SpecificCommunicationLoggingService mockLoggingService = mock(SpecificCommunicationLoggingService.class);
        final ProxyServiceOutgoingLightRequestLogger proxyServiceOutgoingLightRequestLogger = proxyServiceOutgoingLightRequestLogger(testData);
        final org.slf4j.Logger mockLogger = mock(org.slf4j.Logger.class);
        final MessageLoggerUtils mockSettings = mock(MessageLoggerUtils.class);
        when(mockSettings.isLogCompleteMessage()).thenReturn(true);
        when(mockLoggingService.getRequest(any()))
                .thenThrow(new SpecificCommunicationException(ERROR_MESSAGE));
        proxyServiceOutgoingLightRequestLogger.setSpringManagedSpecificCommunicationLoggingService(mockLoggingService);
        proxyServiceOutgoingLightRequestLogger.setMessageLoggerUtils(mockSettings);
        FieldUtils.writeField(proxyServiceOutgoingLightRequestLogger, "fullLogger", mockLogger, true);

        proxyServiceOutgoingLightRequestLogger.logFullMessage(mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

        verify(mockLoggingService).getRequest(eq(tokenBase64));
        verify(mockLogger, never()).info(eq(LoggingMarkerMDC.FULL_MSG_EXCHANGE), anyString());
        verify(mockLogger).error(eq(LoggingMarkerMDC.FULL_MSG_EXCHANGE), anyString(), any(SpecificCommunicationException.class));
    }

    /**
     * Test method for {@link ProxyServiceOutgoingLightRequestLogger#logFullMessage(HttpServletRequest)}
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
        final ProxyServiceOutgoingLightRequestLogger proxyServiceOutgoingLightRequestLogger = proxyServiceOutgoingLightRequestLogger(testData);
        final org.slf4j.Logger mockLogger = mock(org.slf4j.Logger.class);
        final MessageLoggerUtils mockSettings = mock(MessageLoggerUtils.class);
        when(mockSettings.isLogCompleteMessage()).thenReturn(false);
        when(mockLoggingService.getResponse(any()))
                .thenReturn(SERIALIZED_MESSAGE);
        proxyServiceOutgoingLightRequestLogger.setSpringManagedSpecificCommunicationLoggingService(mockLoggingService);
        proxyServiceOutgoingLightRequestLogger.setMessageLoggerUtils(mockSettings);
        FieldUtils.writeField(proxyServiceOutgoingLightRequestLogger, "fullLogger", mockLogger, true);

        proxyServiceOutgoingLightRequestLogger.logFullMessage(mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

        verify(mockLoggingService, never()).getResponse(eq(tokenBase64));
        verify(mockLogger, never()).info(eq(LoggingMarkerMDC.FULL_MSG_EXCHANGE), eq(SERIALIZED_MESSAGE));
    }

    /**
     * Test method for {@link ProxyServiceOutgoingLightRequestLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * when the light response cannot be retrieved from the cache.
     * All information that can be logged should be logged anyway
     *
     * Error scenario.
     */
    @Test
    public void testLogOfInvalidLightRequest () throws SpecificCommunicationException, NoSuchMethodException {
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        Mockito.when(mockMessageLoggerUtils.getProxyServiceRedirectUrl())
                .thenReturn(PROXY_SERVICE_OUTGOING_REQUEST_DESTINATION);
        Mockito.when(mockSpecificCommunicationLoggingService.getRequest(anyString(), anyList()))
                .thenThrow(SpecificCommunicationException.class);

        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        String tokenBase64 = "token";
        Mockito.when(mockHttpServletRequest.getAttribute(TOKEN.toString())).thenReturn(tokenBase64);
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn(BindingMethod.POST.getValue());

        proxyServiceOutgoingLightRequestLogger.logMessage(logger, mockHttpServletRequest);

        assertThat(infoAppender.list.size(), is(0));
        assertThat(errorAppender.list.size(), is(2));
        Level expectedLevel = Level.ERROR;
        String expectedMessage = "Incomplete log of the outgoing light request because of ";
        Class expectedException = SpecificCommunicationException.class;
        verifyILoggingEvent(errorAppender.list.get(0), expectedLevel, expectedMessage, expectedException);

        List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                MessageLogTestUtils.getTagValue("OpType", EIDASValues.EIDAS_SERVICE_SPECIFIC_REQUEST.toString()),
                MessageLogTestUtils.getTagValue("NodeId", PROXY_SERVICE_LIGHT_REQUEST_NODE_ID),
                MessageLogTestUtils.getTagValue("Origin", LoggingConstants.NOT_APPLICABLE),
                MessageLogTestUtils.getTagValue("Destination", PROXY_SERVICE_OUTGOING_REQUEST_DESTINATION),
                MessageLogTestUtils.getTagValue("flowId", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("msgId", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("msgHash", LoggingConstants.UNDEFINED),
                "bltHash"
        );
        String loggedMessage = errorAppender.list.get(1).getMessage();
        assertThat(loggedMessage, stringContainsInOrder(expectedStrings));
    }

    private void setUpBeanProvider (ProxyServiceOutgoingLightRequestLogger proxyServiceOutgoingLightRequestLogger) throws NoSuchFieldException, IllegalAccessException {
        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        oldContext = (ApplicationContext) contextField.get(null);
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
        Mockito.when(mockApplicationContext.getBean(anyString())).thenReturn(proxyServiceOutgoingLightRequestLogger);
    }

    private HttpServletRequest mockHttpServletRequest(LoggerTestData testData, String[] tokenBase64Array) {
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        Mockito.when(mockHttpServletRequest.getAttribute(TOKEN.toString())).thenReturn(testData.tokenBase64);

        HashMap<String, String[]> httpParametersMap = createHttpParameterMap(tokenBase64Array);
        when(mockHttpServletRequest.getParameterMap()).thenReturn(httpParametersMap);
        when(mockHttpServletRequest.getMethod()).thenReturn(BindingMethod.POST.getValue());
        return mockHttpServletRequest;
    }

    private HashMap<String, String[]> createHttpParameterMap(String[] tokenBase64Array) {
        String servletParameter = EidasParameterKeys.TOKEN.toString();
        HashMap<String, String[]> httpParametersMap = new HashMap<>();
        httpParametersMap.put(servletParameter, tokenBase64Array);
        return httpParametersMap;
    }

    private ProxyServiceOutgoingLightRequestLogger proxyServiceOutgoingLightRequestLogger (LoggerTestData testData) throws SpecificCommunicationException {
        ProxyServiceOutgoingLightRequestLogger proxyServiceOutgoingLightRequestLogger = new ProxyServiceOutgoingLightRequestLogger();

        proxyServiceOutgoingLightRequestLogger.setLightTokenProxyserviceRequestNodeId(testData.nodeId);

        SpecificCommunicationLoggingService mockSpecificCommunicationLoggingService = mockSpecificCommunicationLoggingService(testData);
        proxyServiceOutgoingLightRequestLogger.setSpringManagedSpecificCommunicationLoggingService(mockSpecificCommunicationLoggingService);

        proxyServiceOutgoingLightRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));
        proxyServiceOutgoingLightRequestLogger.setFlowIdCache(testData.flowIdCache);
        return proxyServiceOutgoingLightRequestLogger;
    }

    private SpecificProxyserviceCommunicationServiceExtensionImpl mockSpecificProxyserviceCommunicationServiceExtension(LoggerTestData testData) {
        SpecificProxyserviceCommunicationServiceExtensionImpl mockSpecificProxyserviceCommunicationServiceExtension = mock(SpecificProxyserviceCommunicationServiceExtensionImpl.class);
        Mockito.when(mockSpecificProxyserviceCommunicationServiceExtension.getLightTokenRequestNodeId()).thenReturn(testData.nodeId);
        return mockSpecificProxyserviceCommunicationServiceExtension;
    }

    private SpecificCommunicationLoggingService mockSpecificCommunicationLoggingService(LoggerTestData testData) throws SpecificCommunicationException {
        SpecificCommunicationLoggingService mockSpecificCommunicationLoggingService = mock(SpecificCommunicationLoggingService.class);
        ILightRequest mockLightRequest = mockLightRequest(testData);
        Mockito.when(mockSpecificCommunicationLoggingService.getRequest(anyString(), any())).thenReturn(mockLightRequest);
        return mockSpecificCommunicationLoggingService;
    }

    private ILightRequest mockLightRequest (LoggerTestData  testData) {
        ILightRequest mockLightRequest = mock(ILightRequest.class);
        Mockito.when(mockLightRequest.getId()).thenReturn(testData.msgId);
        return mockLightRequest;
    }

    private MessageLoggerUtils mockMessageLoggerUtils(LoggerTestData testData) {
        MessageLoggerUtils mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(testData.logMessage);
        Mockito.when(mockMessageLoggerUtils.getProxyServiceRedirectUrl()).thenReturn(testData.destination);
        return mockMessageLoggerUtils;
    }
}