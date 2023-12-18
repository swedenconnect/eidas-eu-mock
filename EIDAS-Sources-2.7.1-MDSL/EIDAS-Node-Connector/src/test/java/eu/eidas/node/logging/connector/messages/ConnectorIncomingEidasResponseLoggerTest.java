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
package eu.eidas.node.logging.connector.messages;


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
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.logging.LoggingConstants;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.node.logging.messages.MessageLogTestUtils;
import eu.eidas.node.utils.ReflectionUtils;
import eu.eidas.node.utils.logger.LevelFilter;
import eu.eidas.node.utils.logger.LoggerTestData;
import eu.eidas.specificcommunication.CommunicationCache;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xml.security.utils.Base64;
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

import static eu.eidas.auth.commons.EIDASValues.REFERER;
import static eu.eidas.node.utils.logger.LoggerTestUtils.createStartListAppender;
import static eu.eidas.node.utils.logger.LoggerTestUtils.verifyILoggingEvent;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link ConnectorIncomingEidasResponseLogger}.
 *
 * @since 2.3
 */
public class ConnectorIncomingEidasResponseLoggerTest {

    private final static String ANY_STRING_VALUE = "anyString";

    private static final String CONNECTOR_INCOMING_RESPONSE_ORIGIN = "/EidasNode/SpecificProxyServiceResponse";
    private static final String CONNECTOR_INCOMING_RESPONSE_DESTINATION = "/EidasNode/ColleagueResponse";

    private final Logger logger = (Logger) LoggerFactory.getLogger(ConnectorIncomingEidasResponseLogger.class.getName());
    private ListAppender<ILoggingEvent> infoAppender;
    private ListAppender<ILoggingEvent> errorAppender;

    private MessageLoggerUtils mockMessageLoggerUtils;

    private ConnectorIncomingEidasResponseLogger connectorIncomingEidasResponseLogger;
    private ApplicationContext oldContext = null;

    @Before
    public void setup() {
        infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);

        errorAppender = createStartListAppender(new LevelFilter(Level.ERROR));
        logger.addAppender(errorAppender);

        mockMessageLoggerUtils = mock(MessageLoggerUtils.class);

        connectorIncomingEidasResponseLogger = new ConnectorIncomingEidasResponseLogger();
        connectorIncomingEidasResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils);
    }

    @After
    public void teardown() {
        logger.detachAndStopAllAppenders();
        Mockito.reset(mockMessageLoggerUtils);
        if (oldContext!=null){
            ReflectionUtils.setStaticField(BeanProvider.class,"CONTEXT",oldContext);
            oldContext = null;
        }
    }

    /**
     * Test method for {@link ConnectorIncomingEidasResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * Check no logging occurs when logging is deactivated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageInactive () throws Exception {
        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setDestination(StringUtils.EMPTY);

        ConnectorIncomingEidasResponseLogger connectorIncomingEidasResponseLogger = connectorIncomingEidasResponseLogger(testData);
        setUpBeanProvider(connectorIncomingEidasResponseLogger);
        String[] tokenBase64Array = {testData.tokenBase64};
        HashMap<String, String[]> httpParameterMap = createHttpParameterMap(tokenBase64Array);
        connectorIncomingEidasResponseLogger.logMessage(logger, mockHttpServletRequest(testData, httpParameterMap));

        assertThat(infoAppender.list.size(), is(0));
    }

    /**
     * Test method for {@link ConnectorIncomingEidasResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * Check logging occurs when logging is activated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageActive () throws Exception {
        String inResponseTo = String.format("inResponseTo_%s", RandomStringUtils.randomAlphabetic(10));
        String flowId = String.format("flowId_%s", RandomStringUtils.randomAlphabetic(10));
        Cache<String, String> flowIdCache = new CommunicationCache(new ConcurrentMapJcacheServiceDefaultImpl());
        flowIdCache.put(inResponseTo, flowId);

        String tokenBase64 = EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)) .getBytes());

        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(true)
                .setTokenBase64(tokenBase64)
                .setFlowIdCache(flowIdCache)
                .setOpType(EIDASValues.EIDAS_CONNECTOR_RESPONSE.toString())
                .setNodeId("/EidasNode/ServiceMetadata")
                .setOrigin("/EidasNode/SpecificProxyServiceResponse")
                .setDestination("/EidasNode/ColleagueResponse")
                .setMsgId("msgID_" + randomAlphabetic(10))
                .setInResponseTo(inResponseTo)
                .setStatusCode("StatusCode");

        ConnectorIncomingEidasResponseLogger connectorIncomingEidasResponseLogger = connectorIncomingEidasResponseLogger(testData);
        setUpBeanProvider(connectorIncomingEidasResponseLogger);
        String[] tokenBase64Array = {testData.tokenBase64};
        HashMap<String, String[]> httpParameterMap = createHttpParameterMap(tokenBase64Array);
        connectorIncomingEidasResponseLogger.logMessage(logger, mockHttpServletRequest(testData, httpParameterMap));

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
                MessageLogTestUtils.getTagValue("inResponseTo", testData.inResponseTo),
                MessageLogTestUtils.getTagValue("statusCode", testData.statusCode)
        );

        String loggedMessage = infoAppender.list.get(0).getMessage();
        assertThat(loggedMessage, stringContainsInOrder(expectedStrings));
    }

    /**
     * Test method for {@link ConnectorIncomingEidasResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * when in the {@link HttpServletRequest} instance for the key {EidasParameterKeys.SAML_RESPONSE} two tokens are available
     * the first one the bad token: not to be processed
     * the second and last one the good token: the one to be logged
     *
     * Must succeed.
     */
    @Test
    public void testTwoTokensHttpRequestGoodTokenLast() throws EIDASSAMLEngineException, NoSuchFieldException, IllegalAccessException {

        String inResponseTo = String.format("inResponseTo_%s", RandomStringUtils.randomAlphabetic(10));
        String flowId = String.format("flowId_%s", RandomStringUtils.randomAlphabetic(10));
        Cache<String, String> flowIdCache = new CommunicationCache(new ConcurrentMapJcacheServiceDefaultImpl());
        flowIdCache.put(inResponseTo, flowId);

        String tokenBase64 = EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)) .getBytes());

        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(true)
                .setTokenBase64(tokenBase64)
                .setFlowIdCache(flowIdCache)
                .setOpType(EIDASValues.EIDAS_CONNECTOR_RESPONSE.toString())
                .setNodeId("/EidasNode/ServiceMetadata")
                .setOrigin("/EidasNode/SpecificProxyServiceResponse")
                .setDestination("/EidasNode/ColleagueResponse")
                .setMsgId("msgID_")
                .setInResponseTo(inResponseTo)
                .setStatusCode("StatusCode");

        ConnectorIncomingEidasResponseLogger connectorIncomingEidasResponseLogger = connectorIncomingEidasResponseLogger(testData);
        setUpBeanProvider(connectorIncomingEidasResponseLogger);

        String goodToken = "goodToken";
        String badToken = "badToken";
        String[] tokenBase64Array = {badToken, goodToken};
        HashMap<String, String[]> httpParameterMap = createHttpParameterMap(tokenBase64Array);

        HttpServletRequest httpServletRequest = mockHttpServletRequest(testData, httpParameterMap);
        connectorIncomingEidasResponseLogger.logMessage(logger, httpServletRequest);

        assertThat(infoAppender.list.size(), is(1));

        String loggedMessage = infoAppender.list.get(0).getMessage();

        byte[] bytes = EidasStringUtil.decodeBytesFromBase64(goodToken);
        byte[] msgHashBytes = EidasDigestUtil.hashPersonalToken(bytes);
        String s = Base64.encode(msgHashBytes, 88);
        List<String> strings = Arrays.asList(s);
        assertThat(loggedMessage, stringContainsInOrder(strings));
    }

    /**
     * Test method for {@link ConnectorIncomingEidasResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * when in the {@link HttpServletRequest} instance for the key {EidasParameterKeys.SAML_RESPONSE} cannot be unmarshal
     * All information that can be logged should be logged anyway
     *
     * Error scenario
     */
    @Test
    public void testLogOfInvalidEidasResponse() throws EIDASSAMLEngineException {
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        Mockito.when(mockMessageLoggerUtils.getIAuthenticationResponse(Mockito.any())).thenThrow(EIDASSAMLEngineException.class);

        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        String[] tokenBase64Array = {"token"};
        HashMap<String, String[]> httpParametersMap = createHttpParameterMap(tokenBase64Array);
        Mockito.when(mockHttpServletRequest.getHeader(REFERER.toString())).thenReturn(CONNECTOR_INCOMING_RESPONSE_ORIGIN);
        Mockito.when(mockHttpServletRequest.getRequestURL()).thenReturn((new StringBuffer()).append(CONNECTOR_INCOMING_RESPONSE_DESTINATION));
        Mockito.when(mockHttpServletRequest.getParameterMap()).thenReturn(httpParametersMap);
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn(BindingMethod.POST.getValue());

        connectorIncomingEidasResponseLogger.logMessage(logger, mockHttpServletRequest);

        assertThat(infoAppender.list.size(), is(0));
        assertThat(errorAppender.list.size(), is(2));
        Level expectedLevel = Level.ERROR;
        String expectedMessage = "Incomplete log of the incoming response because of ";
        Class expectedException = EIDASSAMLEngineException.class;
        verifyILoggingEvent(errorAppender.list.get(0), expectedLevel, expectedMessage, expectedException);

        List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                MessageLogTestUtils.getTagValue("OpType", EIDASValues.EIDAS_CONNECTOR_RESPONSE.toString()),
                MessageLogTestUtils.getTagValue("NodeId", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("Origin", CONNECTOR_INCOMING_RESPONSE_ORIGIN),
                MessageLogTestUtils.getTagValue("Destination", CONNECTOR_INCOMING_RESPONSE_DESTINATION),
                MessageLogTestUtils.getTagValue("flowId", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("msgId", LoggingConstants.UNDEFINED),
                "msgHash",
                MessageLogTestUtils.getTagValue("inResponseTo", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("statusCode", LoggingConstants.UNDEFINED)
        );
        String actualMessage = errorAppender.list.get(1).getMessage();
        assertThat(actualMessage, stringContainsInOrder(expectedStrings));
    }

    private void setUpBeanProvider (ConnectorIncomingEidasResponseLogger connectorIncomingEidasResponseLogger) throws NoSuchFieldException, IllegalAccessException {
        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        oldContext = (ApplicationContext) contextField.get(null);
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
        Mockito.when(mockApplicationContext.getBean(anyString())).thenReturn(connectorIncomingEidasResponseLogger);
    }

    private HttpServletRequest mockHttpServletRequest(LoggerTestData testData, HashMap<String, String[]> httpParametersMap) {
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        Mockito.when(mockHttpServletRequest.getHeader(REFERER.toString())).thenReturn(testData.origin);
        Mockito.when(mockHttpServletRequest.getRequestURL()).thenReturn((new StringBuffer()).append(testData.destination));

        Mockito.when(mockHttpServletRequest.getParameterMap()).thenReturn(httpParametersMap);
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn(BindingMethod.POST.getValue());

        return mockHttpServletRequest;
    }

    private HashMap<String, String[]> createHttpParameterMap(String[] tokenBase64Array) {
        final String servletParameter = EidasParameterKeys.SAML_RESPONSE.toString();
        HashMap<String, String[]> httpParametersMap = new HashMap<>();
        httpParametersMap.put(servletParameter, tokenBase64Array);
        return httpParametersMap;
    }

    private ConnectorIncomingEidasResponseLogger connectorIncomingEidasResponseLogger (LoggerTestData testData) throws EIDASSAMLEngineException {
        ConnectorIncomingEidasResponseLogger ConnectorIncomingEidasResponseLogger = new ConnectorIncomingEidasResponseLogger();
        ConnectorIncomingEidasResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));
        ConnectorIncomingEidasResponseLogger.setFlowIdCache(testData.flowIdCache);
        return ConnectorIncomingEidasResponseLogger;
    }

    private MessageLoggerUtils mockMessageLoggerUtils(LoggerTestData testData) throws EIDASSAMLEngineException {
        IAuthenticationResponse mockAuthenticationResponse = mockAuthenticationResponse(testData);
        MessageLoggerUtils mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(testData.logMessage);
        Mockito.when(mockMessageLoggerUtils.getIAuthenticationResponse(any(byte[].class))).thenReturn(mockAuthenticationResponse);
        Mockito.when(mockMessageLoggerUtils.getConnectorEntityId(anyString())).thenReturn(testData.nodeId);
        return mockMessageLoggerUtils;
    }

    private IAuthenticationResponse mockAuthenticationResponse (LoggerTestData testData) {
        IAuthenticationResponse mockAuthenticationResponse = mock(IAuthenticationResponse.class);
        Mockito.when(mockAuthenticationResponse.getInResponseToId()).thenReturn(testData.inResponseTo);
        Mockito.when(mockAuthenticationResponse.getIssuer()).thenReturn((ANY_STRING_VALUE));
        Mockito.when(mockAuthenticationResponse.getId()).thenReturn(testData.msgId);
        Mockito.when(mockAuthenticationResponse.getStatusCode()).thenReturn(testData.statusCode);
        return mockAuthenticationResponse;
    }

}