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

package eu.eidas.node.service.logger.request.outgoing;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.logging.IMessageLogger;
import eu.eidas.logging.LoggingConstants;
import eu.eidas.logging.LoggingMarkerMDC;
import eu.eidas.logging.MessageLoggerUtils;
import eu.eidas.logging.logger.LightRequestLogger;
import eu.eidas.node.service.logger.request.ProxyServiceOutgoingLightRequestLogger;
import eu.eidas.node.service.servlet.binding.ProxyLightRequestViewMapping;
import eu.eidas.node.utils.HelperUtil;
import eu.eidas.node.utils.logger.LevelFilter;
import eu.eidas.node.utils.logger.LoggerTestData;
import eu.eidas.node.utils.logger.LoggerTestUtils;
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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link ProxyServiceOutgoingLightRequestLogger}.
 */
public class IMessageLoggerLightRequestLoggerTest {

    private static final String PROXY_SERVICE_LIGHT_REQUEST_NODE_ID = "SpecificProxyService";
    private static final String PROXY_SERVICE_OUTGOING_REQUEST_DESTINATION = "/SpecificProxyService/ProxyServiceRequest";
    private static final String BASE64_ENCODED_TOKEN = "c3BlY2lmaWNDb21tdW5pY2F0aW9uRGVmaW5pdGlvbkNvbm5lY3RvclJlcXVlc3R8Mjc3YTVlM2EtNWI5MS00ODY5LTk2NGMtNDg5YWVmN2ZmMGY1fDIwMjAtMDItMjAgMTY6MDE6MDkgMjgxfDZoaGhDSDFsYTk1NWo0QVBxT1pkMGczd3ROOFk3b3N1dUc4UjRQNlpJWVk9";


    private final Logger commsLogger = (Logger) LoggerFactory.getLogger(IMessageLogger.getLoggerName(
            EIDASValues.EIDAS_PACKAGE_REQUEST_LOGGER_VALUE, ProxyServiceOutgoingLightRequestLogger.class));
    private ListAppender<ILoggingEvent> infoAppender;
    private ListAppender<ILoggingEvent> errorAppender;

    private MessageLoggerUtils mockMessageLoggerUtils;
    private SpecificCommunicationLoggingService mockSpecificCommunicationLoggingService;

    private LightRequestLogger lightRequestLogger;

    @Before
    public void setup() {
        infoAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.INFO));
        commsLogger.addAppender(infoAppender);

        errorAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.ERROR));
        commsLogger.addAppender(errorAppender);

        mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        mockSpecificCommunicationLoggingService = mock(SpecificCommunicationLoggingService.class);

        lightRequestLogger = new ProxyServiceOutgoingLightRequestLogger();
        lightRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils);
        lightRequestLogger.setLightTokenRequestNodeId(PROXY_SERVICE_LIGHT_REQUEST_NODE_ID);
        lightRequestLogger.setSpringManagedSpecificCommunicationLoggingService(mockSpecificCommunicationLoggingService);
    }

    @After
    public void teardown() {
        commsLogger.detachAndStopAllAppenders();
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLogger#logMessage(Logger, HttpServletRequest)}.
     * Check no logging occurs when logging is deactivated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageInactive() throws Exception {
        final LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setDestination(StringUtils.EMPTY);
        final HttpServletRequest servletRequest = mockHttpServletRequest(testData);
        when(mockMessageLoggerUtils.isLogMessages()).thenReturn(false);

        lightRequestLogger.logMessage(servletRequest);

        assertThat(infoAppender.list.size(), is(0));
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLogger#logMessage(Logger, HttpServletRequest)}.
     * Check logging occurs when logging is activated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageActive() throws Exception {
        final LoggerTestData testData = new LoggerTestData()
                .setLogMessage(true)
                .setTokenBase64(String.format("flowId_%s", RandomStringUtils.randomAlphabetic(10)))
                .setFlowIdCache(HelperUtil.createHashMapCommunicationCacheMock())
                .setOpType(EIDASValues.EIDAS_SERVICE_SPECIFIC_REQUEST.toString())
                .setNodeId("specificProxyService")
                .setOrigin(LoggingConstants.NOT_APPLICABLE)
                .setDestination("/SpecificProxyService/ProxyServiceRequest")
                .setMsgId("msgID_" + randomAlphabetic(10));

        testData.flowIdCache.put(testData.msgId, String.format("flowId_%s", RandomStringUtils.randomAlphabetic(10)));

        LightRequestLogger connectorIncomingLightRequestLogger = connectorIncomingLightRequestLogger(testData);

        HttpServletRequest servletRequest = mockHttpServletRequest(testData);
        connectorIncomingLightRequestLogger.logMessage(servletRequest);

        assertTrue(errorAppender.list.isEmpty());
        assertThat(infoAppender.list.size(), is(1));
        String flowId = testData.flowIdCache.get(testData.msgId);
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
                "bltHash"
        );

        String loggedMessage = infoAppender.list.get(0).getMessage();
        assertThat(loggedMessage, stringContainsInOrder(expectedStrings));
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLogger#logMessage(Logger, HttpServletRequest)}.
     * when the light request cannot be retrieved from the cache.
     * All information that can be logged should be logged anyway
     * <p>
     * Error scenario
     */
    @Test
    public void testLogOfInvalidLightRequest() throws SpecificCommunicationException {
        when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        when(mockSpecificCommunicationLoggingService.getRequest(anyString(), anyList()))
                .thenThrow(SpecificCommunicationException.class);

        HttpServletRequest mockHttpServletRequest = mockHttpServletRequest(
                BindingMethod.POST,
                PROXY_SERVICE_OUTGOING_REQUEST_DESTINATION,
                null,
                "token");

        lightRequestLogger.logMessage(mockHttpServletRequest);

        assertThat(infoAppender.list.size(), is(0));
        assertThat(errorAppender.list.size(), is(2));


        final ILoggingEvent errorLoggingEvent = errorAppender.list.get(0);
        Assert.assertEquals(LoggingMarkerMDC.SAML_EXCHANGE, errorLoggingEvent.getMarker());
        Assert.assertEquals(SpecificCommunicationException.class.getName(), errorLoggingEvent.getThrowableProxy().getClassName());
        String expectedMessage = "Incomplete log of the light request because of ";
        Assert.assertEquals(expectedMessage, errorLoggingEvent.getMessage());

        List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                "OpType", EIDASValues.EIDAS_SERVICE_SPECIFIC_REQUEST.toString(),
                "NodeId", PROXY_SERVICE_LIGHT_REQUEST_NODE_ID,
                "Origin", LoggingConstants.NOT_APPLICABLE,
                "Destination", PROXY_SERVICE_OUTGOING_REQUEST_DESTINATION,
                "flowId", LoggingConstants.UNDEFINED,
                "msgId", LoggingConstants.UNDEFINED,
                "msgHash", LoggingConstants.UNDEFINED,
                "bltHash"
        );
        String actualMessage = errorAppender.list.get(1).getMessage();
        assertThat(actualMessage, stringContainsInOrder(expectedStrings));
    }

    private LightRequestLogger connectorIncomingLightRequestLogger(LoggerTestData testData) throws SpecificCommunicationException {
        LightRequestLogger connectorIncomingLightRequestLogger = new ProxyServiceOutgoingLightRequestLogger();
        connectorIncomingLightRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));
        connectorIncomingLightRequestLogger.setSpringManagedSpecificCommunicationLoggingService(mockSpecificCommunicationLoggingService(testData));
        connectorIncomingLightRequestLogger.setLightTokenRequestNodeId(testData.nodeId);
        return connectorIncomingLightRequestLogger;
    }

    private MessageLoggerUtils mockMessageLoggerUtils(LoggerTestData testData) {
        MessageLoggerUtils mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        when(mockMessageLoggerUtils.isLogMessages()).thenReturn(testData.logMessage);
        Mockito.when(mockMessageLoggerUtils.trackMessageFlow(anyString()))
                .then(invocationOnMock -> testData.flowIdCache.get(invocationOnMock.getArgument(0)));
        return mockMessageLoggerUtils;
    }

    private ILightRequest mockLightRequest(String msgId) {
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

    protected HttpServletRequest mockHttpServletRequest(LoggerTestData testData) {
        return mockHttpServletRequest(BindingMethod.POST, testData.destination, testData.origin, testData.tokenBase64);
    }

    protected HttpServletRequest mockHttpServletRequest(BindingMethod method, String destination, String origin, String... tokenBase64Array) {
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        when(mockHttpServletRequest.getRequestURL()).thenReturn(new StringBuffer(destination));
        when(mockHttpServletRequest.getHeader(EIDASValues.REFERER.toString())).thenReturn(origin);

        when(mockHttpServletRequest.getMethod()).thenReturn(method.getValue());
        when(mockHttpServletRequest.getAttribute(ProxyLightRequestViewMapping.LIGHT_TOKEN)).thenReturn(tokenBase64Array[0]);
        when(mockHttpServletRequest.getAttribute(ProxyLightRequestViewMapping.REDIRECT_URL)).thenReturn(destination);

        return mockHttpServletRequest;
    }


}
