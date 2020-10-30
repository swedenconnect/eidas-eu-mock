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
package eu.eidas.node.logging.connector.messages;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import eu.eidas.auth.cache.ConcurrentMapJcacheServiceDefaultImpl;
import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.node.logging.LoggingConstants;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.node.logging.messages.MessageLogTestUtils;
import eu.eidas.node.utils.logger.ConnectorOutgoingLightResponseLoggerBuilder;
import eu.eidas.node.utils.logger.LevelFilter;
import eu.eidas.node.utils.logger.LoggerTestData;
import eu.eidas.specificcommunication.CommunicationCache;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationLoggingService;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static eu.eidas.auth.commons.EidasParameterKeys.TOKEN;
import static eu.eidas.node.utils.ReadFileUtils.readFileAsByteArray;
import static eu.eidas.node.utils.logger.LoggerTestUtils.createStartListAppender;
import static eu.eidas.node.utils.logger.LoggerTestUtils.verifyILoggingEvent;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link ConnectorOutgoingLightResponseLogger}.
 *
 * @since 2.3
 */
public class ConnectorOutgoingLightResponseLoggerTest extends AbstractLightMessageLoggerTest{

    private static final String CONNECTOR_SP_DESTINATION = "/SpecificConnector/ConnectorResponse";
    private static final String CONNECTOR_LIGHT_RESPONSE_NODE_ID = "NodeIDTest";
    private static final String BASE64_ENCODED_TOKEN = "c3BlY2lmaWNDb21tdW5pY2F0aW9uRGVmaW5pdGlvbkNvbm5lY3RvclJlcXVlc3R8Mjc3YTVlM2EtNWI5MS00ODY5LTk2NGMtNDg5YWVmN2ZmMGY1fDIwMjAtMDItMjAgMTY6MDE6MDkgMjgxfDZoaGhDSDFsYTk1NWo0QVBxT1pkMGczd3ROOFk3b3N1dUc4UjRQNlpJWVk9";

    private final Logger logger = (Logger) LoggerFactory.getLogger(ConnectorOutgoingLightResponseLogger.class.getName());
    private ListAppender<ILoggingEvent> infoAppender;
    private ListAppender<ILoggingEvent> errorAppender;

    private MessageLoggerUtils mockMessageLoggerUtils;
    private SpecificCommunicationLoggingService mockSpecificCommunicationLoggerService;

    private ConnectorOutgoingLightResponseLogger connectorOutgoingLightResponseLogger;

    @Before
    public void setup() {
        infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);

        errorAppender = createStartListAppender(new LevelFilter(Level.ERROR));
        logger.addAppender(errorAppender);

        mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        mockSpecificCommunicationLoggerService = mock(SpecificCommunicationLoggingService.class);

        connectorOutgoingLightResponseLogger = new ConnectorOutgoingLightResponseLogger();
        connectorOutgoingLightResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils);
        connectorOutgoingLightResponseLogger.setLightTokenConnectorResponseNodeId(CONNECTOR_LIGHT_RESPONSE_NODE_ID);
        connectorOutgoingLightResponseLogger.setSpringManagedSpecificCommunicationLoggingService(mockSpecificCommunicationLoggerService);
    }

    @After
    public void teardown() {
        logger.detachAndStopAllAppenders();
        Mockito.reset(mockMessageLoggerUtils, mockSpecificCommunicationLoggerService);
    }

    /**
     * Test method for {@link ConnectorOutgoingLightResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * Check no logging occurs when logging is deactivated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageInactive () throws Exception {
        final ConnectorOutgoingLightResponseLoggerBuilder loggerBuilder = standardFullLoggingConnectorOutgoingLightResponseLoggerBuilder()
                .withMessageLoggerUtils(new MsgUtilsTestBuilder().logMsg(false).build());
        final ConnectorOutgoingLightResponseLogger connectorOutgoingLightResponseLogger = loggerBuilder.build();
        final HttpServletRequest servletRequest = mockHttpServletRequest(BindingMethod.POST, StringUtils.EMPTY, StringUtils.EMPTY, BASE64_ENCODED_TOKEN);

        connectorOutgoingLightResponseLogger.logMessage(logger, servletRequest);

        assertThat(infoAppender.list.size(), is(0));
    }

    /**
     * Test method for {@link ConnectorOutgoingLightResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
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
                .setFlowIdCache(flowIdCache)
                .setTokenBase64(tokenBase64)
                .setOpType(EIDASValues.EIDAS_CONNECTOR_CONNECTOR_RESPONSE.toString())
                .setNodeId("specificConnector")
                .setOrigin(LoggingConstants.NOT_APPLICABLE)
                .setDestination("/SpecificConnector/ConnectorResponse")
                .setMsgId("msgID_" + randomAlphabetic(10))
                .setInResponseTo(inResponseTo)
                .setStatusCode("LightResponseStatusCode");


        ConnectorOutgoingLightResponseLogger connectorOutgoingLightResponseLogger = connectorOutgoingLightResponseLogger(testData);
        setUpBeanProvider(connectorOutgoingLightResponseLogger);
        final HttpServletRequest servletRequest = mockHttpServletRequest(BindingMethod.POST, StringUtils.EMPTY, StringUtils.EMPTY, BASE64_ENCODED_TOKEN);
        connectorOutgoingLightResponseLogger.logMessage(logger, servletRequest);

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
                "bltHash",
                MessageLogTestUtils.getTagValue("inResponseTo", testData.inResponseTo),
                MessageLogTestUtils.getTagValue("statusCode", testData.statusCode)
        );
        String loggedMessage = infoAppender.list.get(0).getMessage();
        assertThat(loggedMessage, stringContainsInOrder(expectedStrings));
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLogger#logFullMessage(HttpServletRequest)}
     * to assert if method {@link SpecificCommunicationLoggingService#getResponse(String)} is called
     * to assert if the {@link org.slf4j.Logger#info(Marker, String)} is called
     * * with {@link org.slf4j.Marker} of {@link LoggingMarkerMDC#FULL_MSG_EXCHANGE}
     */
    @Test
    public void logFullMessage() throws Exception {
        final String SERIALIZED_RESPONSE_MESSAGE = new String(readFileAsByteArray("logging/testFullLoggingLightResponse.xml"));
        final ConnectorOutgoingLightResponseLoggerBuilder loggerBuilder = standardFullLoggingConnectorOutgoingLightResponseLoggerBuilder()
                .forSpecificCommunicationLoggingService(service -> when(service.getResponse(any()))
                        .thenReturn(SERIALIZED_RESPONSE_MESSAGE));

        final HttpServletRequest servletRequest = mockHttpServletRequest(BindingMethod.POST, StringUtils.EMPTY, StringUtils.EMPTY, BASE64_ENCODED_TOKEN);
        final ConnectorOutgoingLightResponseLogger connectorOutgoingLightResponseLogger = loggerBuilder.build();

        connectorOutgoingLightResponseLogger.logFullMessage(servletRequest);

        verify(loggerBuilder.getSpecificCommunicationLoggingService()).getResponse(eq(BASE64_ENCODED_TOKEN));
        verify(loggerBuilder.getFullLogger()).info(eq(LoggingMarkerMDC.FULL_MSG_EXCHANGE), eq(SERIALIZED_RESPONSE_MESSAGE));
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLogger#logFullMessage(HttpServletRequest)}
     * when {@link SpecificCommunicationLoggingService#getResponse(String)} throws {@link SpecificCommunicationException}
     * <p>
     * Error must be caught and logged to {@link org.slf4j.Logger#error(Marker, String, Object)}) with Marker = {@link LoggingMarkerMDC#FULL_MSG_EXCHANGE}
     */
    @Test
    public void logFullMessageLogError() throws Exception {
        final ConnectorOutgoingLightResponseLoggerBuilder loggerBuilder = standardFullLoggingConnectorOutgoingLightResponseLoggerBuilder()
                .forSpecificCommunicationLoggingService(service -> when(service.getResponse(any()))
                        .thenThrow(SpecificCommunicationException.class));
        final ConnectorOutgoingLightResponseLogger connectorOutgoingLightResponseLogger = loggerBuilder.build();
        final HttpServletRequest servletRequest = mockHttpServletRequest(BindingMethod.POST, StringUtils.EMPTY, StringUtils.EMPTY, BASE64_ENCODED_TOKEN);

        connectorOutgoingLightResponseLogger.logFullMessage(servletRequest);

        verify(loggerBuilder.getSpecificCommunicationLoggingService()).getResponse(eq(BASE64_ENCODED_TOKEN));
        verify(loggerBuilder.getFullLogger(), never()).info(eq(LoggingMarkerMDC.FULL_MSG_EXCHANGE), anyString());
        verify(loggerBuilder.getFullLogger()).error(eq(LoggingMarkerMDC.FULL_MSG_EXCHANGE), anyString(), any(SpecificCommunicationException.class));
    }

    /**
     * Test method for {@link ConnectorOutgoingLightResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * when the light response cannot be retrieve from the cache.
     * All information that can be logged should be logged anyway
     *
     * Error scenario
     */
    @Test
    public void testLogOfInvalidLightResponse() throws SpecificCommunicationException {
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        Mockito.when(mockMessageLoggerUtils.getConnectorRedirectUrl()).thenReturn(CONNECTOR_SP_DESTINATION);

        Mockito.when(mockSpecificCommunicationLoggerService.getResponse(anyString(), anyList()))
                .thenThrow(SpecificCommunicationException.class);
        final HttpServletRequest mockHttpServletRequest = mockHttpServletRequest(BindingMethod.POST, StringUtils.EMPTY, StringUtils.EMPTY, "token");

        connectorOutgoingLightResponseLogger.logMessage(logger, mockHttpServletRequest);

        assertThat(infoAppender.list.size(), is(0));
        assertThat(errorAppender.list.size(), is(2));

        Level expectedLevel = Level.ERROR;
        String expectedMessage = "Incomplete log of the outgoing light response because of ";
        Class expectedException = SpecificCommunicationException.class;
        verifyILoggingEvent(errorAppender.list.get(0), expectedLevel, expectedMessage, expectedException);

        List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                MessageLogTestUtils.getTagValue("OpType", EIDASValues.EIDAS_CONNECTOR_CONNECTOR_RESPONSE.toString()),
                MessageLogTestUtils.getTagValue("NodeId", CONNECTOR_LIGHT_RESPONSE_NODE_ID),
                MessageLogTestUtils.getTagValue("Origin", LoggingConstants.NOT_APPLICABLE),
                MessageLogTestUtils.getTagValue("Destination", CONNECTOR_SP_DESTINATION),
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

    /**
     * Test method for {@link ConnectorOutgoingLightResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * when the token of the httpServletRequest is invalid.
     * All information that can be logged should be logged anyway
     *
     * Error scenario
     */
    @Test
    public void testLogOfInvalidLightResponseToken() {
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        Mockito.when(mockMessageLoggerUtils.getConnectorRedirectUrl()).thenReturn(CONNECTOR_SP_DESTINATION);

        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        Mockito.when(mockHttpServletRequest.getAttribute(TOKEN.toString())).thenReturn("");

        connectorOutgoingLightResponseLogger.logMessage(logger, mockHttpServletRequest);

        assertThat(infoAppender.list.size(), is(0));
        assertThat(errorAppender.list.size(), is(2));

        Level expectedLevel = Level.ERROR;
        String expectedMessage = "Incomplete log of the outgoing light response because of ";
        Class expectedException = InvalidParameterEIDASException.class;
        verifyILoggingEvent(errorAppender.list.get(0), expectedLevel, expectedMessage, expectedException);

        List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                MessageLogTestUtils.getTagValue("OpType", EIDASValues.EIDAS_CONNECTOR_CONNECTOR_RESPONSE.toString()),
                MessageLogTestUtils.getTagValue("NodeId", CONNECTOR_LIGHT_RESPONSE_NODE_ID),
                MessageLogTestUtils.getTagValue("Origin", LoggingConstants.NOT_APPLICABLE),
                MessageLogTestUtils.getTagValue("Destination", CONNECTOR_SP_DESTINATION),
                MessageLogTestUtils.getTagValue("flowId", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("msgId", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("msgHash", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("bltHash", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("inResponseTo", LoggingConstants.UNDEFINED),
                MessageLogTestUtils.getTagValue("statusCode", LoggingConstants.UNDEFINED)
        );
        String loggedMessage = errorAppender.list.get(1).getMessage();
        assertThat(loggedMessage, stringContainsInOrder(expectedStrings));
    }

    private ConnectorOutgoingLightResponseLoggerBuilder standardFullLoggingConnectorOutgoingLightResponseLoggerBuilder() {
        return new ConnectorOutgoingLightResponseLoggerBuilder()
                .withSpecificCommunicationLoggingService(mock(SpecificCommunicationLoggingService.class))
                .withLightTokenConnectorResponseNodeId("specificConnector")
                .withFullLogger(mock(org.slf4j.Logger.class))
                .withMessageLoggerUtils(new MsgUtilsTestBuilder().logCompleteMsg(true).build());
    }

    private ConnectorOutgoingLightResponseLogger connectorOutgoingLightResponseLogger (LoggerTestData testData) throws SpecificCommunicationException {
        ConnectorOutgoingLightResponseLogger connectorOutgoingLightResponseLogger = new ConnectorOutgoingLightResponseLogger();

        connectorOutgoingLightResponseLogger.setLightTokenConnectorResponseNodeId(testData.nodeId);

        SpecificCommunicationLoggingService mockSpecificCommunicationLoggerService =  mockSpecificCommunicationLoggerService(testData);
        connectorOutgoingLightResponseLogger.setSpringManagedSpecificCommunicationLoggingService(mockSpecificCommunicationLoggerService);

        connectorOutgoingLightResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));
        connectorOutgoingLightResponseLogger.setFlowIdCache(testData.flowIdCache);
        return connectorOutgoingLightResponseLogger;
    }

    private SpecificCommunicationLoggingService mockSpecificCommunicationLoggerService(LoggerTestData testData) throws SpecificCommunicationException {
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
        Mockito.when(mockMessageLoggerUtils.getConnectorRedirectUrl()).thenReturn(testData.destination);
        return mockMessageLoggerUtils;
    }

}