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
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.logging.LoggingConstants;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.node.logging.messages.MessageLogTestUtils;
import eu.eidas.node.utils.ReflectionUtils;
import eu.eidas.node.utils.logger.LevelFilter;
import eu.eidas.node.utils.logger.LoggerTestData;
import eu.eidas.specificcommunication.CommunicationCache;
import org.apache.commons.lang.StringUtils;
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
import java.util.List;

import static eu.eidas.auth.commons.EidasParameterKeys.SAML_REQUEST;
import static eu.eidas.node.NodeParameterNames.CITIZEN_COUNTRY_CODE;
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
 * Tests for the {@link ConnectorOutgoingEidasRequestLogger}.
 *
 * @since 2.3
 */
public class ConnectorOutgoingEidasRequestLoggerTest {

    private final static String ANY_STRING_VALUE = "anyString";

    private final Logger logger = (Logger) LoggerFactory.getLogger(ConnectorIncomingLightRequestLogger.class.getName());
    private ListAppender<ILoggingEvent> infoAppender;
    private ListAppender<ILoggingEvent> errorAppender;

    private MessageLoggerUtils mockMessageLoggerUtils;
    private AUCONNECTORUtil mockAuconnectorUtil;

    private ConnectorOutgoingEidasRequestLogger connectorOutgoingEidasRequestLogger;
    private ApplicationContext oldContext = null;

    @Before
    public void setup() {
        infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);

        errorAppender = createStartListAppender(new LevelFilter(Level.ERROR));
        logger.addAppender(errorAppender);

        mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        mockAuconnectorUtil = mock(AUCONNECTORUtil.class);

        connectorOutgoingEidasRequestLogger = new ConnectorOutgoingEidasRequestLogger();
        connectorOutgoingEidasRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils);
        connectorOutgoingEidasRequestLogger.setSpringManagedAUCONNECTORUtil(mockAuconnectorUtil);
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
     * Test method for {@link ConnectorOutgoingEidasRequestLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * Check no logging occurs when logging is deactivated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageInactive () throws Exception {
        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setDestination(StringUtils.EMPTY);

        ConnectorOutgoingEidasRequestLogger connectorOutgoingEidasRequestLogger = connectorOutgoingEidasRequestLogger(testData);
        setUpBeanProvider(connectorOutgoingEidasRequestLogger);
        connectorOutgoingEidasRequestLogger.logMessage(logger, mockHttpServletRequest(testData));

        assertThat(infoAppender.list.size(), is(0));
    }

    /**
     * Test method for {@link ConnectorOutgoingEidasRequestLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * Check logging occurs when logging is activated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageActive () throws Exception {
        Cache<String, String> flowIdCache = new CommunicationCache(new ConcurrentMapJcacheServiceDefaultImpl());
        String msgId = "msgID_" + randomAlphabetic(10);
        String flowId = "flowID_" + randomAlphabetic(10);
        flowIdCache.put(msgId, flowId);

        String tokenBase64 = EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)) .getBytes());

        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(true)
                .setTokenBase64(tokenBase64)
                .setFlowIdCache(flowIdCache)
                .setOpType(EIDASValues.CONNECTOR_SERVICE_REQUEST.toString())
                .setNodeId("/EidasNode/ServiceMetadata")
                .setOrigin(LoggingConstants.NOT_APPLICABLE)
                .setDestination("/EidasNode/ColleagueRequest")
                .setMsgId(msgId);

        ConnectorOutgoingEidasRequestLogger connectorOutgoingEidasRequestLogger = connectorOutgoingEidasRequestLogger(testData);
        setUpBeanProvider(connectorOutgoingEidasRequestLogger);
        connectorOutgoingEidasRequestLogger.logMessage(logger, mockHttpServletRequest(testData));

        assertThat(infoAppender.list.size(), is(1));
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
     * Test method for {@link ConnectorOutgoingEidasRequestLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * when in the {@link HttpServletRequest} instance for the key {EidasParameterKeys.SAML_REQUEST} cannot be unmarshal
     * All information that can be logged should be logged anyway
     *
     * Error scenario
     */
    @Test
    public void testLogOfInvalidEidasRequest() throws EIDASSAMLEngineException {
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        String serviceUrl = "serviceUrl";
        Mockito.when(mockAuconnectorUtil.loadConfigServiceMetadataURL(Mockito.anyString())).thenReturn(serviceUrl);
        String expectedNodeId = "testNodeId";
        Mockito.when(mockMessageLoggerUtils.getConnectorEntityId(serviceUrl)).thenReturn(expectedNodeId);
        Mockito.when(mockMessageLoggerUtils.getIAuthenticationProxyRequest(Mockito.any())).thenThrow(EIDASSAMLEngineException.class);

        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        String tokenBase64 = "token";
        Mockito.when(mockHttpServletRequest.getAttribute(SAML_REQUEST.toString())).thenReturn(tokenBase64);
        Mockito.when(mockHttpServletRequest.getAttribute(CITIZEN_COUNTRY_CODE.toString())).thenReturn(ANY_STRING_VALUE);

        connectorOutgoingEidasRequestLogger.logMessage(logger, mockHttpServletRequest);

        assertThat(infoAppender.list.size(), is(0));
        assertThat(errorAppender.list.size(), is(2));
        Level expectedLevel = Level.ERROR;
        String expectedMessage = "Incomplete log of the outgoing request because of ";
        Class expectedException = EIDASSAMLEngineException.class;
        verifyILoggingEvent(errorAppender.list.get(0), expectedLevel, expectedMessage, expectedException);

        List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                MessageLogTestUtils.getTagValue("OpType", EIDASValues.CONNECTOR_SERVICE_REQUEST.toString()),
                MessageLogTestUtils.getTagValue("NodeId", expectedNodeId),
                MessageLogTestUtils.getTagValue("Origin", LoggingConstants.NOT_APPLICABLE),
                MessageLogTestUtils.getTagValue("Destination", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("flowId", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("msgId", LoggingConstants.UNDEFINED),
                "msgHash"
        );
        String actualMessage = errorAppender.list.get(1).getMessage();
        assertThat(actualMessage, stringContainsInOrder(expectedStrings));
    }

    /**
     * Test method for {@link ConnectorOutgoingEidasRequestLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * when an error occurs within {@link AUCONNECTORUtil#loadConfigServiceMetadataURL(String)}
     * All information that can be logged should be logged anyway
     *
     * Error scenario
     */
    @Test
    public void testLogWhenLoadingServiceMetadataURLFails() {
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        Mockito.when(mockAuconnectorUtil.loadConfigServiceMetadataURL(Mockito.anyString())).thenThrow(NumberFormatException.class);

        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        String tokenBase64 = "token";
        Mockito.when(mockHttpServletRequest.getAttribute(SAML_REQUEST.toString())).thenReturn(tokenBase64);
        Mockito.when(mockHttpServletRequest.getAttribute(CITIZEN_COUNTRY_CODE.toString())).thenReturn(ANY_STRING_VALUE);

        connectorOutgoingEidasRequestLogger.logMessage(logger, mockHttpServletRequest);

        assertThat(infoAppender.list.size(), is(0));
        assertThat(errorAppender.list.size(), is(2));
        Level expectedLevel = Level.ERROR;
        String expectedMessage = "Incomplete log of the outgoing request because of ";
        Class expectedException = NumberFormatException.class;
        verifyILoggingEvent(errorAppender.list.get(0), expectedLevel, expectedMessage, expectedException);

        List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                MessageLogTestUtils.getTagValue("OpType", EIDASValues.CONNECTOR_SERVICE_REQUEST.toString()),
                MessageLogTestUtils.getTagValue("NodeId", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("Origin", LoggingConstants.NOT_APPLICABLE),
                MessageLogTestUtils.getTagValue("Destination", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("flowId", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("msgId", LoggingConstants.UNDEFINED),
                "msgHash"
        );
        String actualMessage = errorAppender.list.get(1).getMessage();
        assertThat(actualMessage, stringContainsInOrder(expectedStrings));
    }

    private ConnectorOutgoingEidasRequestLogger connectorOutgoingEidasRequestLogger (LoggerTestData testData) throws EIDASSAMLEngineException {
        ConnectorOutgoingEidasRequestLogger connectorOutgoingEidasRequestLogger = new ConnectorOutgoingEidasRequestLogger();
        connectorOutgoingEidasRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));
        connectorOutgoingEidasRequestLogger.setSpringManagedAUCONNECTORUtil(mockAuconnectorUtil());
        connectorOutgoingEidasRequestLogger.setFlowIdCache(testData.flowIdCache);
        return connectorOutgoingEidasRequestLogger;
    }

    private AUCONNECTORUtil mockAuconnectorUtil () {
        AUCONNECTORUtil mockAuconnectorUtil = mock(AUCONNECTORUtil.class);
        Mockito.when(mockAuconnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn(ANY_STRING_VALUE);
        return mockAuconnectorUtil;
    }

    private void setUpBeanProvider (ConnectorOutgoingEidasRequestLogger connectorOutgoingEidasRequestLogger) throws NoSuchFieldException, IllegalAccessException {
        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        oldContext = (ApplicationContext) contextField.get(null);
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
        Mockito.when(mockApplicationContext.getBean(anyString())).thenReturn(connectorOutgoingEidasRequestLogger);
    }

    private MessageLoggerUtils mockMessageLoggerUtils(LoggerTestData testData) throws EIDASSAMLEngineException {
        IAuthenticationRequest mockAuthenticationRequest = mockAuthenticationRequest(testData);
        MessageLoggerUtils mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(testData.logMessage);
        Mockito.when(mockMessageLoggerUtils.getIAuthenticationProxyRequest(any(byte[].class))).thenReturn(mockAuthenticationRequest);
        Mockito.when(mockMessageLoggerUtils.getConnectorEntityId(anyString())).thenReturn(testData.nodeId);
        return mockMessageLoggerUtils;
    }

    private IAuthenticationRequest mockAuthenticationRequest (LoggerTestData testData) {
        IAuthenticationRequest mockAuthenticationRequest = mock(IAuthenticationRequest.class);
        Mockito.when(mockAuthenticationRequest.getId()).thenReturn(testData.msgId);
        Mockito.when(mockAuthenticationRequest.getDestination()).thenReturn(testData.destination);
        return mockAuthenticationRequest;
    }

    private HttpServletRequest mockHttpServletRequest(LoggerTestData testData) {
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        Mockito.when(mockHttpServletRequest.getAttribute(SAML_REQUEST.toString())).thenReturn(testData.tokenBase64);
        Mockito.when(mockHttpServletRequest.getAttribute(CITIZEN_COUNTRY_CODE.toString())).thenReturn(ANY_STRING_VALUE);
        return mockHttpServletRequest;
    }

}