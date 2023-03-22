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
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import eu.eidas.auth.cache.ConcurrentMapJcacheServiceDefaultImpl;
import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasDigestUtil;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.node.logging.LoggingConstants;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.node.logging.messages.MessageLogTestUtils;
import eu.eidas.node.utils.logger.ConnectorIncomingLightRequestLoggerBuilder;
import eu.eidas.node.utils.logger.LevelFilter;
import eu.eidas.node.utils.logger.LoggerTestData;
import eu.eidas.specificcommunication.CommunicationCache;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationLoggingService;
import org.apache.commons.lang.StringUtils;
import org.apache.xml.security.utils.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static eu.eidas.node.utils.ReadFileUtils.readFileAsByteArray;
import static eu.eidas.node.utils.logger.LoggerTestUtils.createStartListAppender;
import static eu.eidas.node.utils.logger.LoggerTestUtils.verifyILoggingEvent;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;
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
 * Tests for the {@link ConnectorIncomingLightRequestLogger}.
 *
 * @since 2.3
 */
public class ConnectorIncomingLightRequestLoggerTest extends AbstractLightMessageLoggerTest{

    private static final String CONNECTOR_LIGHT_REQUEST_NODE_ID = "TestNodeID";
    private static final String CONNECTOR_INCOMING_REQUEST_ORIGIN = "http://origin";
    private static final String CONNECTOR_INCOMING_REQUEST_DESTINATION = "http://destination";
    private static final String BASE64_ENCODED_TOKEN = "c3BlY2lmaWNDb21tdW5pY2F0aW9uRGVmaW5pdGlvbkNvbm5lY3RvclJlcXVlc3R8Mjc3YTVlM2EtNWI5MS00ODY5LTk2NGMtNDg5YWVmN2ZmMGY1fDIwMjAtMDItMjAgMTY6MDE6MDkgMjgxfDZoaGhDSDFsYTk1NWo0QVBxT1pkMGczd3ROOFk3b3N1dUc4UjRQNlpJWVk9";

    private final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ConnectorIncomingLightRequestLogger.class.getName());
    private ListAppender<ILoggingEvent> infoAppender;
    private ListAppender<ILoggingEvent> errorAppender;

    private MessageLoggerUtils mockMessageLoggerUtils;
    private SpecificCommunicationLoggingService mockSpecificCommunicationLoggingService;

    private ConnectorIncomingLightRequestLogger connectorIncomingLightRequestLogger;

    @Before
    public void setup() {
        infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);

        errorAppender = createStartListAppender(new LevelFilter(Level.ERROR));
        logger.addAppender(errorAppender);

        mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        mockSpecificCommunicationLoggingService = mock(SpecificCommunicationLoggingService.class);

        connectorIncomingLightRequestLogger = new ConnectorIncomingLightRequestLogger();
        connectorIncomingLightRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils);
        connectorIncomingLightRequestLogger.setLightTokenConnectorRequestNodeId(CONNECTOR_LIGHT_REQUEST_NODE_ID);
        connectorIncomingLightRequestLogger.setSpringManagedSpecificCommunicationLoggingService(mockSpecificCommunicationLoggingService);
    }

    @After
    public void teardown() {
        logger.detachAndStopAllAppenders();
        Mockito.reset(mockMessageLoggerUtils, mockSpecificCommunicationLoggingService);
        tearDownBeanProvider();
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLogger#logMessage(Logger, HttpServletRequest)}.
     * Check no logging occurs when logging is deactivated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageInactive () throws Exception {
        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setDestination(StringUtils.EMPTY);

        ConnectorIncomingLightRequestLogger connectorIncomingLightRequestLogger = connectorIncomingLightRequestLogger(testData);
        setUpBeanProvider(connectorIncomingLightRequestLogger);
        final HttpServletRequest servletRequest = mockHttpServletRequest(BindingMethod.POST,testData.destination, testData.origin, testData.tokenBase64);

        connectorIncomingLightRequestLogger.logMessage(logger, servletRequest);

        assertThat(infoAppender.list.size(), is(0));
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLogger#logMessage(Logger, HttpServletRequest)}.
     * Check logging occurs when logging is activated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageActive () throws Exception {
        Cache<String, String> flowIdCache = new CommunicationCache(new ConcurrentMapJcacheServiceDefaultImpl());
        String tokenBase64 = BASE64_ENCODED_TOKEN;

        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(true)
                .setTokenBase64(tokenBase64)
                .setFlowIdCache(flowIdCache)
                .setOpType(EIDASValues.SPECIFIC_EIDAS_CONNECTOR_REQUEST.toString())
                .setNodeId("SpecificConnector")
                .setOrigin("http://origin")
                .setDestination("http://destination")
                .setMsgId("msgID_" + randomAlphabetic(10));

        ConnectorIncomingLightRequestLogger connectorIncomingLightRequestLogger = connectorIncomingLightRequestLogger(testData);
        setUpBeanProvider(connectorIncomingLightRequestLogger);

        HttpServletRequest servletRequest = mockHttpServletRequest(BindingMethod.POST,testData.destination, testData.origin, testData.tokenBase64);
        connectorIncomingLightRequestLogger.logMessage(logger, servletRequest);

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
                "msgHash",
                "bltHash"
        );

        String loggedMessage = infoAppender.list.get(0).getMessage();
        assertThat(loggedMessage, stringContainsInOrder(expectedStrings));
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLogger#logFullMessage(HttpServletRequest)}
     * when serializedRequest is a available from service
     * <p>
     * message must be send to {@link Logger#info(Marker, String)} with {@link LoggingMarkerMDC#FULL_MSG_EXCHANGE} marker
     */
    @Test
    public void logFullMessage() throws Exception {
        final String SERIALIZED_MESSAGE = new String(readFileAsByteArray("logging/testFullLoggingLightRequest.xml"));
        final HttpServletRequest mockHttpServletRequest = mockHttpServletRequest(BindingMethod.POST,"", "", BASE64_ENCODED_TOKEN);
        final ConnectorIncomingLightRequestLoggerBuilder loggerBuilder = standardFullLoggingConnectorIncomingLightRequestLoggerBuilder()
                .forSpecificCommunicationLoggingService( service -> when(service.getRequest(any()))
                        .thenReturn(SERIALIZED_MESSAGE));
        final ConnectorIncomingLightRequestLogger connectorIncomingLightRequestLogger = loggerBuilder.build();

        connectorIncomingLightRequestLogger.logFullMessage(mockHttpServletRequest);

        verify(loggerBuilder.getSpecificCommunicationLoggingService()).getRequest(eq(BASE64_ENCODED_TOKEN));
        verify(loggerBuilder.getFullLogger()).info(eq(LoggingMarkerMDC.FULL_MSG_EXCHANGE), eq(SERIALIZED_MESSAGE));
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLogger#logFullMessage(HttpServletRequest)}
     * when serializedRequest is throwing an error
     * <p>
     * error must be send to {@link Logger#error(Marker, String)} with {@link LoggingMarkerMDC#FULL_MSG_EXCHANGE} marker
     */
    @Test
    public void logFullMessageError() throws Exception {
        final String ERROR_MESSAGE = "RUN AWAY";
        final HttpServletRequest mockHttpServletRequest = mockHttpServletRequest(BindingMethod.POST,"", "", BASE64_ENCODED_TOKEN);
        final ConnectorIncomingLightRequestLoggerBuilder loggerBuilder = standardFullLoggingConnectorIncomingLightRequestLoggerBuilder()
                .forSpecificCommunicationLoggingService( service -> when(service.getRequest(any()))
                        .thenThrow(new SpecificCommunicationException(ERROR_MESSAGE)));
        final ConnectorIncomingLightRequestLogger connectorIncomingLightRequestLogger = loggerBuilder.build();

        connectorIncomingLightRequestLogger.logFullMessage(mockHttpServletRequest);

        verify(loggerBuilder.getSpecificCommunicationLoggingService()).getRequest(eq(BASE64_ENCODED_TOKEN));
        verify(loggerBuilder.getFullLogger(), never()).info(eq(LoggingMarkerMDC.FULL_MSG_EXCHANGE), anyString());
        verify(loggerBuilder.getFullLogger()).error(eq(LoggingMarkerMDC.FULL_MSG_EXCHANGE), anyString(), any(SpecificCommunicationException.class));
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLogger#logMessage(Logger, HttpServletRequest)}.
     * when in the {@link HttpServletRequest} instance for the key {EidasParameterKeys.SAML_RESPONSE} two tokens are available
     * the first one the bad token: not to be processed
     * the second and last one the good token: the one to be logged
     *
     * Must succeed.
     */
    @Test
    public void testTwoTokensHttpRequestGoodTokenLast() throws Exception {
        Cache<String, String> flowIdCache = new CommunicationCache(new ConcurrentMapJcacheServiceDefaultImpl());
        String tokenBase64 = BASE64_ENCODED_TOKEN;

        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(true)
                .setTokenBase64(tokenBase64)
                .setFlowIdCache(flowIdCache)
                .setOpType(EIDASValues.SPECIFIC_EIDAS_CONNECTOR_REQUEST.toString())
                .setNodeId("SpecificConnector")
                .setOrigin("http://origin")
                .setDestination("http://destination")
                .setMsgId("msgID_" + randomAlphabetic(10));

        ConnectorIncomingLightRequestLogger connectorIncomingLightRequestLogger = connectorIncomingLightRequestLogger(testData);
        setUpBeanProvider(connectorIncomingLightRequestLogger);

        String goodToken = "goodToken";
        String badToken = "badToken";
        final HttpServletRequest mockHttpServletRequest = mockHttpServletRequest(BindingMethod.POST, testData.destination, testData.origin, badToken, goodToken);

        connectorIncomingLightRequestLogger.logMessage(logger, mockHttpServletRequest);

        assertThat(infoAppender.list.size(), is(1));
        String flowId = testData.flowIdCache.get(testData.msgId);
        assertTrue(StringUtils.isNotBlank(flowId));

        String loggedMessage = infoAppender.list.get(0).getMessage();

        byte[] bytes = EidasStringUtil.decodeBytesFromBase64(goodToken);
        byte[] msgHashBytes = EidasDigestUtil.hashPersonalToken(bytes);
        String s = Base64.encode(msgHashBytes, 88);
        List<String> strings = Arrays.asList(s);
        assertThat(loggedMessage, stringContainsInOrder(strings));
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLogger#logMessage(Logger, HttpServletRequest)}.
     * when the light request cannot be retrieved from the cache.
     * All information that can be logged should be logged anyway
     *
     * Error scenario
     */
    @Test
    public void testLogOfInvalidLightRequest() throws SpecificCommunicationException {
        when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        when(mockSpecificCommunicationLoggingService.getRequest(anyString(), anyList()))
                .thenThrow(SpecificCommunicationException.class);

        HttpServletRequest mockHttpServletRequest = mockHttpServletRequest(
                BindingMethod.POST,
                CONNECTOR_INCOMING_REQUEST_DESTINATION,
                CONNECTOR_INCOMING_REQUEST_ORIGIN,
                "token");

        connectorIncomingLightRequestLogger.logMessage(logger, mockHttpServletRequest);

        assertThat(infoAppender.list.size(), is(0));
        assertThat(errorAppender.list.size(), is(2));

        Level expectedLevel = Level.ERROR;
        String expectedMessage = "Incomplete log of the incoming light request because of ";
        Class expectedException = SpecificCommunicationException.class;
        verifyILoggingEvent(errorAppender.list.get(0), expectedLevel, expectedMessage, expectedException);

        List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                MessageLogTestUtils.getTagValue("OpType", EIDASValues.SPECIFIC_EIDAS_CONNECTOR_REQUEST.toString()),
                MessageLogTestUtils.getTagValue("NodeId", CONNECTOR_LIGHT_REQUEST_NODE_ID),
                MessageLogTestUtils.getTagValue("Origin", CONNECTOR_INCOMING_REQUEST_ORIGIN),
                MessageLogTestUtils.getTagValue("Destination", CONNECTOR_INCOMING_REQUEST_DESTINATION),
                MessageLogTestUtils.getTagValue("flowId", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("msgId", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("msgHash", LoggingConstants.UNDEFINED),
                "bltHash"
        );
        String actualMessage = errorAppender.list.get(1).getMessage();
        assertThat(actualMessage, stringContainsInOrder(expectedStrings));
    }

    private ConnectorIncomingLightRequestLogger connectorIncomingLightRequestLogger (LoggerTestData testData) throws SpecificCommunicationException {
        ConnectorIncomingLightRequestLogger connectorIncomingLightRequestLogger = new ConnectorIncomingLightRequestLogger();
        connectorIncomingLightRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));
        connectorIncomingLightRequestLogger.setSpringManagedSpecificCommunicationLoggingService(mockSpecificCommunicationLoggingService(testData));
        connectorIncomingLightRequestLogger.setLightTokenConnectorRequestNodeId(testData.nodeId);
        connectorIncomingLightRequestLogger.setFlowIdCache(testData.flowIdCache);
        return connectorIncomingLightRequestLogger;
    }

    private ConnectorIncomingLightRequestLoggerBuilder standardFullLoggingConnectorIncomingLightRequestLoggerBuilder() {
        return new ConnectorIncomingLightRequestLoggerBuilder()
                .withLightTokenConnectorRequestNodeId("SpecificConnector")
                .withFullLogger(mock(Logger.class))
                .withMessageLoggerUtils(new MsgUtilsTestBuilder().logCompleteMsg(true).logMsg(true).build())
                .withSpecificCommunicationLoggingService(mock(SpecificCommunicationLoggingService.class));
    }

    private MessageLoggerUtils mockMessageLoggerUtils(LoggerTestData testData) {
        MessageLoggerUtils mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        when(mockMessageLoggerUtils.isLogMessages()).thenReturn(testData.logMessage);
        return mockMessageLoggerUtils;
    }

    private ILightRequest mockLightRequest (String  msgId) {
        ILightRequest mockLightRequest = mock(ILightRequest.class);
        when(mockLightRequest.getId()).thenReturn(msgId);
        return mockLightRequest;
    }

    private SpecificCommunicationLoggingService mockSpecificCommunicationLoggingService(LoggerTestData testData) throws SpecificCommunicationException {
        ILightRequest mockLightRequest = mockLightRequest(testData.msgId);
        SpecificCommunicationLoggingService mockSpecificCommunicationLoggingService = mock(SpecificCommunicationLoggingService.class);
        when(mockSpecificCommunicationLoggingService.getRequest(anyString(), anyList())).thenReturn(mockLightRequest);
        return mockSpecificCommunicationLoggingService;
    }

}
