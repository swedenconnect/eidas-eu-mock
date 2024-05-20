/*
 * Copyright (c) 2024 by European Commission
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

package eu.eidas.node.connector.logger.response.outgoing;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.logging.IMessageLogger;
import eu.eidas.logging.LoggingConstants;
import eu.eidas.logging.LoggingMarkerMDC;
import eu.eidas.logging.MessageLoggerUtils;
import eu.eidas.logging.logger.LightResponseLogger;
import eu.eidas.node.connector.logger.LevelFilter;
import eu.eidas.node.connector.logger.LoggerTestData;
import eu.eidas.node.connector.logger.LoggerTestUtils;
import eu.eidas.node.connector.logger.response.ConnectorOutgoingLightResponseLogger;
import eu.eidas.node.connector.servlet.binding.ConnectorLightResponseViewMapping;
import eu.eidas.node.utils.HelperUtil;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationLoggingService;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link ConnectorOutgoingLightResponseLogger}.
 */
public class IMessageLoggerLightResponseLoggerTest {
    private static final String CONNECTOR_SP_DESTINATION = "/SpecificConnector/ConnectorResponse";
    private static final String CONNECTOR_LIGHT_RESPONSE_NODE_ID = "NodeIDTest";

    private final Logger commsLogger = (Logger) LoggerFactory.getLogger(IMessageLogger.getLoggerName(
            EIDASValues.EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE, ConnectorOutgoingLightResponseLogger.class));
    private ListAppender<ILoggingEvent> infoAppender;
    private ListAppender<ILoggingEvent> errorAppender;

    private MessageLoggerUtils mockMessageLoggerUtils;
    private SpecificCommunicationLoggingService mockSpecificCommunicationLoggingService;

    private LightResponseLogger proxyServiceIncomingLightResponseLogger;

    @Before
    public void setup() {
        infoAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.INFO));
        commsLogger.addAppender(infoAppender);

        errorAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.ERROR));
        commsLogger.addAppender(errorAppender);

        mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        mockSpecificCommunicationLoggingService = mock(SpecificCommunicationLoggingService.class);

        proxyServiceIncomingLightResponseLogger = new ConnectorOutgoingLightResponseLogger();
        proxyServiceIncomingLightResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils);
        proxyServiceIncomingLightResponseLogger.setSpringManagedSpecificCommunicationLoggingService(mockSpecificCommunicationLoggingService);
        ((ConnectorOutgoingLightResponseLogger)proxyServiceIncomingLightResponseLogger)
                .setLightTokenResponseNodeId(CONNECTOR_LIGHT_RESPONSE_NODE_ID);
    }

    @After
    public void teardown() {
        commsLogger.detachAndStopAllAppenders();
    }

    /**
     * Test method for {@link ProxyServiceIncomingLightResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * Check no logging occurs when logging is deactivated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageInactive() throws Exception {
        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setDestination(StringUtils.EMPTY);
        proxyServiceIncomingLightResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));

        proxyServiceIncomingLightResponseLogger.logMessage(mockHttpServletRequest(testData));

        assertThat(infoAppender.list.size(), is(0));
    }

    /**
     * Test method for {@link ProxyServiceIncomingLightResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * Check logging occurs when logging is activated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageActive() throws Exception {
        final LoggerTestData testData = new LoggerTestData()
                .setLogMessage(true)
                .setTokenBase64(EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)).getBytes()))
                .setFlowIdCache(HelperUtil.createHashMapCommunicationCacheMock())
                .setOpType(EIDASValues.EIDAS_CONNECTOR_CONNECTOR_RESPONSE.toString())
                .setNodeId(CONNECTOR_LIGHT_RESPONSE_NODE_ID)
                .setOrigin(LoggingConstants.NOT_APPLICABLE)
                .setDestination(CONNECTOR_SP_DESTINATION)
                .setMsgId("msgID_" + randomAlphabetic(10))
                .setInResponseTo(String.format("inResponseTo_%s", RandomStringUtils.randomAlphabetic(10)))
                .setStatusCode("ResponseStatusCode");

        final String flowId = String.format("flowId_%s", randomAlphabetic(10));
        testData.flowIdCache.put(testData.inResponseTo, flowId);
        proxyServiceIncomingLightResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));
        proxyServiceIncomingLightResponseLogger.setSpringManagedSpecificCommunicationLoggingService(mockSpecificCommunicationLoggingService(testData));

        proxyServiceIncomingLightResponseLogger.logMessage(mockHttpServletRequest(testData));

        assertThat(infoAppender.list.size(), is(1));
        assertTrue(StringUtils.isNotBlank(flowId));

        List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                "OpType", testData.opType,
                "NodeId", testData.nodeId,
                "Origin", testData.origin,
                "Destination", testData.destination,
                "flowId", flowId,
                "msgId", testData.msgId,
                "msgHash",
                "bltHash",
                "inResponseTo", testData.inResponseTo,
                "statusCode", testData.statusCode
        );
        String loggedMessage = infoAppender.list.get(0).getMessage();
        assertThat(loggedMessage, stringContainsInOrder(expectedStrings));
    }


    /**
     * Test method for {@link ProxyServiceIncomingLightResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * when the light response cannot be retrieved from the cache.
     * All information that can be logged should be logged anyway
     * <p>
     * Error scenario
     */
    @Test
    public void testLogOfInvalidLightResponse() throws SpecificCommunicationException {
        Mockito.when(mockSpecificCommunicationLoggingService.getResponse(anyString(), anyList()))
                .thenThrow(SpecificCommunicationException.class);

        final LoggerTestData testData = new LoggerTestData()
                .setDestination(CONNECTOR_SP_DESTINATION)
                .setLogMessage(true)
                .setTokenBase64("token");

        HttpServletRequest mockHttpServletRequest = mockHttpServletRequest(testData);
        Mockito.when(mockHttpServletRequest.getHeader(EIDASValues.REFERER.toString()))
                .thenReturn(StringUtils.EMPTY);
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn(BindingMethod.POST.getValue());
        Mockito.when(mockHttpServletRequest.getRequestURL())
                .thenReturn(new StringBuffer(CONNECTOR_SP_DESTINATION));

        proxyServiceIncomingLightResponseLogger.logMessage(mockHttpServletRequest);

        final ILoggingEvent errorLoggingEvent = errorAppender.list.get(0);
        Assert.assertEquals(LoggingMarkerMDC.SAML_EXCHANGE, errorLoggingEvent.getMarker());
        Assert.assertEquals(SpecificCommunicationException.class.getName(), errorLoggingEvent.getThrowableProxy().getClassName());
        String expectedMessage = "Incomplete log of the light response because of ";
        Assert.assertEquals(expectedMessage, errorLoggingEvent.getMessage());

        assertTrue(infoAppender.list.isEmpty());
        assertThat(errorAppender.list.size(), is(2));
        final List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                "OpType", EIDASValues.EIDAS_CONNECTOR_CONNECTOR_RESPONSE.toString(),
                "NodeId", CONNECTOR_LIGHT_RESPONSE_NODE_ID,
                "Origin", LoggingConstants.NOT_APPLICABLE,
                "Destination", CONNECTOR_SP_DESTINATION,
                "flowId", LoggingConstants.UNDEFINED,
                "msgId", LoggingConstants.UNDEFINED,
                "msgHash", LoggingConstants.UNDEFINED,
                "bltHash",
                "inResponseTo", LoggingConstants.UNDEFINED,
                "statusCode", LoggingConstants.UNDEFINED
        );
        String loggedMessage = errorAppender.list.get(1).getMessage();
        assertThat(loggedMessage, stringContainsInOrder(expectedStrings));
    }

    private HttpServletRequest mockHttpServletRequest(LoggerTestData testData) {
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        Mockito.when(mockHttpServletRequest.getAttribute(ConnectorLightResponseViewMapping.LIGHT_TOKEN))
                .thenReturn(testData.tokenBase64);
        Mockito.when(mockHttpServletRequest.getAttribute(ConnectorLightResponseViewMapping.REDIRECT_URL))
                .thenReturn(testData.destination);
        return mockHttpServletRequest;
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
        Mockito.when(mockMessageLoggerUtils.trackMessageFlow(anyString()))
                .then(invocationOnMock -> testData.flowIdCache.get(invocationOnMock.getArgument(0)));
        return mockMessageLoggerUtils;
    }
}
