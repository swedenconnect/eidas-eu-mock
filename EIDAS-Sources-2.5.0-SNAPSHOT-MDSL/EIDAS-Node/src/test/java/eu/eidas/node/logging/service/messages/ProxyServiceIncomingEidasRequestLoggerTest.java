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
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.logging.LoggingConstants;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.node.logging.connector.messages.ConnectorIncomingLightRequestLogger;
import eu.eidas.node.logging.messages.MessageLogTestUtils;
import eu.eidas.node.utils.ReflectionUtils;
import eu.eidas.node.utils.logger.LevelFilter;
import eu.eidas.node.utils.logger.LoggerTestData;
import eu.eidas.specificcommunication.CommunicationCache;
import org.apache.commons.lang.StringUtils;
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
import java.util.HashMap;
import java.util.List;

import static eu.eidas.node.utils.logger.LoggerTestUtils.createStartListAppender;
import static eu.eidas.node.utils.logger.LoggerTestUtils.verifyILoggingEvent;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link ProxyServiceIncomingEidasRequestLogger}.
 *
 * @since 2.3
 */
public class ProxyServiceIncomingEidasRequestLoggerTest {

    private static final String PROXY_SERVICE_REQUEST_ORIGIN = "/EidasNode/SpecificConnectorRequest";

    private final Logger logger = (Logger) LoggerFactory.getLogger(ConnectorIncomingLightRequestLogger.class.getName());
    private ListAppender<ILoggingEvent> infoAppender;
    private ListAppender<ILoggingEvent> errorAppender;

    private MessageLoggerUtils mockMessageLoggerUtils;

    private ProxyServiceIncomingEidasRequestLogger proxyServiceIncomingEidasRequestLogger;

    @Before
    public void setup() {
        infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);

        errorAppender = createStartListAppender(new LevelFilter(Level.ERROR));
        logger.addAppender(errorAppender);

        mockMessageLoggerUtils = mock(MessageLoggerUtils.class);

        proxyServiceIncomingEidasRequestLogger = new ProxyServiceIncomingEidasRequestLogger();
        proxyServiceIncomingEidasRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils);
    }

    @After
    public void teardown() {
        logger.detachAndStopAllAppenders();
        Mockito.reset(mockMessageLoggerUtils);
    }

    /**
     * Test method for {@link ProxyServiceIncomingEidasRequestLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * Check no logging occurs when logging is deactivated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageInactive () throws Exception {
        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setDestination(StringUtils.EMPTY);

        ProxyServiceIncomingEidasRequestLogger proxyServiceIncomingEidasRequestLogger = proxyServiceIncomingEidasRequestLogger(testData);
        setUpBeanProvider(proxyServiceIncomingEidasRequestLogger);
        proxyServiceIncomingEidasRequestLogger.logMessage(logger, mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

        assertThat(infoAppender.list.size(), is(0));
    }

    /**
     * Test method for {@link ProxyServiceIncomingEidasRequestLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * Check logging occurs when logging is activated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageActive () throws Exception {
        Cache<String, String> flowIdCache = new CommunicationCache(new ConcurrentMapJcacheServiceDefaultImpl());
        String tokenBase64 = EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)) .getBytes());

        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(true)
                .setTokenBase64(tokenBase64)
                .setFlowIdCache(flowIdCache)
                .setOpType(EIDASValues.EIDAS_SERVICE_REQUEST.toString())
                .setNodeId("/EidasNode/ConnectorMetadata")
                .setOrigin("/EidasNode/SpecificConnectorRequest")
                .setDestination("/EidasNode/ColleagueRequest")
                .setMsgId("msgID_" + randomAlphabetic(10));

        ProxyServiceIncomingEidasRequestLogger proxyServiceIncomingEidasRequestLogger = proxyServiceIncomingEidasRequestLogger(testData);
        setUpBeanProvider(proxyServiceIncomingEidasRequestLogger);
        proxyServiceIncomingEidasRequestLogger.logMessage(logger, mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

        assertThat(infoAppender.list.size(), is(1));
        String flowId = testData.flowIdCache.get(testData.msgId);
        assertTrue(StringUtils.isNotBlank(flowId));

        List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                MessageLogTestUtils.getTagValue("OpType", testData.opType),
                MessageLogTestUtils.getTagValue("NodeId", testData.nodeId),
                MessageLogTestUtils.getTagValue("Origin", testData.origin),
                MessageLogTestUtils.getTagValue("Destination", testData.destination),
                MessageLogTestUtils.getTagValue("flowId", flowId),
                MessageLogTestUtils.getTagValue("msgId", testData.msgId),
                "msgHash"
        );
        String loggedMessage = infoAppender.list.get(0).getMessage();
        assertThat(loggedMessage, stringContainsInOrder(expectedStrings));
    }

    /**
     * Test method for {@link ProxyServiceIncomingEidasRequestLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * when in the {@link HttpServletRequest} instance for the key {EidasParameterKeys.SAML_RESPONSE} two tokens are available
     * the first one the bad token: not to be processed
     * the second and last one the good token: the one to be logged
     *
     * Must succeed.
     */
    @Test
    public void testTwoTokensHttpRequestGoodTokenLast() throws Exception {
        Cache<String, String> flowIdCache = new CommunicationCache(new ConcurrentMapJcacheServiceDefaultImpl());
        String tokenBase64 = EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)) .getBytes());

        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(true)
                .setTokenBase64(tokenBase64)
                .setFlowIdCache(flowIdCache)
                .setOpType(EIDASValues.EIDAS_SERVICE_REQUEST.toString())
                .setNodeId("/EidasNode/ConnectorMetadata")
                .setOrigin("/EidasNode/SpecificConnectorRequest")
                .setDestination("/EidasNode/ColleagueRequest")
                .setMsgId("msgID_" + randomAlphabetic(10));

        ProxyServiceIncomingEidasRequestLogger proxyServiceIncomingEidasRequestLogger = proxyServiceIncomingEidasRequestLogger(testData);
        setUpBeanProvider(proxyServiceIncomingEidasRequestLogger);

        String goodToken = "goodToken";
        String badToken = "badToken";
        String[] tokenBase64Array = {badToken, goodToken};
        proxyServiceIncomingEidasRequestLogger.logMessage(logger, mockHttpServletRequest(testData, tokenBase64Array));

        assertThat(infoAppender.list.size(), is(1));
        String flowId = testData.flowIdCache.get(testData.msgId);
        assertTrue(StringUtils.isNotBlank(flowId));

        String loggedMessage = infoAppender.list.get(0).getMessage();

        byte[] bytes = EidasStringUtil.decodeBytesFromBase64(goodToken);
        byte[] msgHashBytes = EidasDigestUtil.hashPersonalToken(bytes);
        String s = Base64.encode(msgHashBytes, 88);
        List<String> strings = Arrays.asList(s);
        Assert.assertThat(loggedMessage, stringContainsInOrder(strings));
    }

    /**
     * Test method for {@link ProxyServiceIncomingEidasRequestLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * when in the {@link HttpServletRequest} instance for the key {EidasParameterKeys.SAML_REQUEST} cannot be marshall
     *
     * Error scenario.
     */
    @Test
    public void testLogOfInvalidRequest() throws EIDASSAMLEngineException {
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        Mockito.when(mockMessageLoggerUtils.getIAuthenticationProxyRequest(any())).thenThrow(EIDASSAMLEngineException.class);

        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        Mockito.when(mockHttpServletRequest.getHeader(EIDASValues.REFERER.toString()))
                .thenReturn(PROXY_SERVICE_REQUEST_ORIGIN);
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn(BindingMethod.POST.getValue());
        String tokenBase64 = "SuperTest";
        String[] tokenBase64Array = {"token"};
        HashMap<String, String[]> httpParametersMap = createHttpParameterMap(tokenBase64Array);
        Mockito.when(mockHttpServletRequest.getParameterMap()).thenReturn(httpParametersMap);

        proxyServiceIncomingEidasRequestLogger.logMessage(logger, mockHttpServletRequest);

        assertThat(infoAppender.list.size(), is(0));
        assertThat(errorAppender.list.size(), is(2));
        Level expectedLevel = Level.ERROR;
        String expectedMessage = "Incomplete log of the incoming request because of ";
        Class expectedException = EIDASSAMLEngineException.class;
        verifyILoggingEvent(errorAppender.list.get(0), expectedLevel, expectedMessage, expectedException);

        List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                MessageLogTestUtils.getTagValue("OpType", EIDASValues.EIDAS_SERVICE_REQUEST.toString()),
                MessageLogTestUtils.getTagValue("NodeId", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("Origin", PROXY_SERVICE_REQUEST_ORIGIN),
                MessageLogTestUtils.getTagValue("Destination", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("flowId", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("msgId", LoggingConstants.UNDEFINED),
                "msgHash"
        );
        String loggedMessage = errorAppender.list.get(1).getMessage();
        assertThat(loggedMessage, stringContainsInOrder(expectedStrings));
    }

    private void setUpBeanProvider (ProxyServiceIncomingEidasRequestLogger proxyServiceIncomingEidasRequestLogger) {
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
        Mockito.when(mockApplicationContext.getBean(anyString())).thenReturn(proxyServiceIncomingEidasRequestLogger);
    }

    private HttpServletRequest mockHttpServletRequest(LoggerTestData testData, String[] tokenBase64Array) {
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);

        HashMap<String, String[]> httpParametersMap = createHttpParameterMap(tokenBase64Array);
        Mockito.when(mockHttpServletRequest.getParameterMap()).thenReturn(httpParametersMap);
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn(BindingMethod.POST.getValue());
        Mockito.when(mockHttpServletRequest.getHeader(EIDASValues.REFERER.toString())).thenReturn(testData.origin);
        return mockHttpServletRequest;
    }

    private HashMap<String, String[]> createHttpParameterMap(String[] tokenBase64Array) {
        final String servletParameter = EidasParameterKeys.SAML_REQUEST.toString();
        HashMap<String, String[]> httpParametersMap = new HashMap<>();
        httpParametersMap.put(servletParameter, tokenBase64Array);
        return httpParametersMap;
    }

    private ProxyServiceIncomingEidasRequestLogger proxyServiceIncomingEidasRequestLogger (LoggerTestData testData) throws EIDASSAMLEngineException {
        ProxyServiceIncomingEidasRequestLogger proxyServiceIncomingEidasRequestLogger = new ProxyServiceIncomingEidasRequestLogger();

        proxyServiceIncomingEidasRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));
        proxyServiceIncomingEidasRequestLogger.setFlowIdCache(testData.flowIdCache);
        return proxyServiceIncomingEidasRequestLogger;
    }

    private MessageLoggerUtils mockMessageLoggerUtils(LoggerTestData testData) throws EIDASSAMLEngineException {
        IAuthenticationRequest mockAuthenticationRequest = mockAuthenticationRequest(testData);
        MessageLoggerUtils mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(testData.logMessage);
        Mockito.when(mockMessageLoggerUtils.getIAuthenticationProxyRequest(any(byte[].class))).thenReturn(mockAuthenticationRequest);
        Mockito.when(mockMessageLoggerUtils.getProxyServiceEntityId(anyString())).thenReturn(testData.nodeId);
        return mockMessageLoggerUtils;
    }

    private IAuthenticationRequest mockAuthenticationRequest (LoggerTestData testData) {
        IAuthenticationRequest mockAuthenticationRequest = mock(IAuthenticationRequest.class);
        Mockito.when(mockAuthenticationRequest.getId()).thenReturn(testData.msgId);
        Mockito.when(mockAuthenticationRequest.getIssuer()).thenReturn("anyStringValue");
        Mockito.when(mockAuthenticationRequest.getDestination()).thenReturn(testData.destination);
        return mockAuthenticationRequest;
    }

}