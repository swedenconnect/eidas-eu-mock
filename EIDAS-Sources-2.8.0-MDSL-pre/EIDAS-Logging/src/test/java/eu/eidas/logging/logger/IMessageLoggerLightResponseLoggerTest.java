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

package eu.eidas.logging.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasDigestUtil;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.logging.IMessageLogger;
import eu.eidas.logging.LoggingConstants;
import eu.eidas.logging.LoggingMarkerMDC;
import eu.eidas.logging.MessageLoggerUtils;
import eu.eidas.logging.messages.LightResponseMessageLog;
import eu.eidas.logging.messages.MessageLogTestUtils;
import eu.eidas.logging.utils.HelperUtil;
import eu.eidas.logging.utils.logger.LevelFilter;
import eu.eidas.logging.utils.logger.LoggerTestData;
import eu.eidas.logging.utils.logger.LoggerTestUtils;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationLoggingService;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xml.security.utils.Base64;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
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

public class IMessageLoggerLightResponseLoggerTest {
    private static final String PROXY_SERVICE_LIGHT_RESPONSE_NODE_ID = "SpecificProxyService";
    private static final String PROXY_SERVICE_INCOMING_RESPONSE_ORIGIN = "/SpecificProxyService/AfterCitizenConsentResponse";
    private static final String PROXY_SERVICE_INCOMING_RESPONSE_DESTINATION = "/EidasNode/SpecificProxyServiceResponse";

    private final Logger commsLogger = (Logger) LoggerFactory.getLogger(IMessageLogger.getLoggerName(
            EIDASValues.EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE, TestLightResponseLogger.class));
    private ListAppender<ILoggingEvent> infoAppender;
    private ListAppender<ILoggingEvent> errorAppender;

    private MessageLoggerUtils mockMessageLoggerUtils;
    private SpecificCommunicationLoggingService mockSpecificCommunicationLoggingService;

    private LightResponseLogger proxyServiceIncomingLightResponseLogger;

    private static class TestLightResponseLogger extends LightResponseLogger {

        public static final String WHERE_TO_FIND_THE_LIGHT_TOKEN = "KeyForSomelightTOken";

        @Override
        protected void setMessageVector(LightResponseMessageLog.Builder messageLogBuilder, HttpServletRequest httpServletRequest) {
            messageLogBuilder.setOpType(EIDASValues.EIDAS_SERVICE_SPECIFIC_RESPONSE.toString());
            messageLogBuilder.setNodeId(PROXY_SERVICE_LIGHT_RESPONSE_NODE_ID);
            messageLogBuilder.setOrigin(httpServletRequest.getHeader(EIDASValues.REFERER.toString()));
            messageLogBuilder.setDestination(httpServletRequest.getRequestURL().toString());
        }

        @Override
        protected String getLightToken(HttpServletRequest httpServletRequest) {
            final WebRequest webRequest = new IncomingRequest(httpServletRequest);
            return webRequest.getEncodedLastParameterValue(WHERE_TO_FIND_THE_LIGHT_TOKEN);
        }
    }

    @Before
    public void setup() {
        infoAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.INFO));
        commsLogger.addAppender(infoAppender);

        errorAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.ERROR));
        commsLogger.addAppender(errorAppender);

        mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        mockSpecificCommunicationLoggingService = mock(SpecificCommunicationLoggingService.class);

        proxyServiceIncomingLightResponseLogger = new TestLightResponseLogger();
        proxyServiceIncomingLightResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils);
        proxyServiceIncomingLightResponseLogger.setSpringManagedSpecificCommunicationLoggingService(mockSpecificCommunicationLoggingService);
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

        proxyServiceIncomingLightResponseLogger.logMessage(mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

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
                .setOpType(EIDASValues.EIDAS_SERVICE_SPECIFIC_RESPONSE.toString())
                .setNodeId(PROXY_SERVICE_LIGHT_RESPONSE_NODE_ID)
                .setOrigin(PROXY_SERVICE_INCOMING_RESPONSE_ORIGIN)
                .setDestination(PROXY_SERVICE_INCOMING_RESPONSE_DESTINATION)
                .setMsgId("msgID_" + randomAlphabetic(10))
                .setInResponseTo(String.format("inResponseTo_%s", RandomStringUtils.randomAlphabetic(10)))
                .setStatusCode("ResponseStatusCode");

        final String flowId = String.format("flowId_%s", randomAlphabetic(10));
        testData.flowIdCache.put(testData.inResponseTo, flowId);
        proxyServiceIncomingLightResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));
        proxyServiceIncomingLightResponseLogger.setSpringManagedSpecificCommunicationLoggingService(mockSpecificCommunicationLoggingService(testData));

        proxyServiceIncomingLightResponseLogger.logMessage(mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

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
     * Test method for {@link ProxyServiceIncomingLightResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * when in the {@link HttpServletRequest} instance for the key {EidasParameterKeys.SAML_RESPONSE} two tokens are available
     * the first one the bad token: not to be processed
     * the second and last one the good token: the one to be logged
     * <p>
     * Must succeed.
     */
    @Test
    public void testTwoTokensHttpRequestGoodTokenLast() throws Exception {
        final LoggerTestData testData = new LoggerTestData()
                .setLogMessage(true)
                .setTokenBase64(EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)).getBytes()))
                .setFlowIdCache(HelperUtil.createHashMapCommunicationCacheMock())
                .setOpType(EIDASValues.EIDAS_SERVICE_SPECIFIC_RESPONSE.toString())
                .setNodeId(PROXY_SERVICE_LIGHT_RESPONSE_NODE_ID)
                .setOrigin(PROXY_SERVICE_INCOMING_RESPONSE_ORIGIN)
                .setDestination(PROXY_SERVICE_INCOMING_RESPONSE_DESTINATION)
                .setMsgId("msgID_" + randomAlphabetic(10))
                .setInResponseTo(String.format("inResponseTo_%s", RandomStringUtils.randomAlphabetic(10)))
                .setStatusCode("ResponseStatusCode");
        String flowId = String.format("flowId_%s", RandomStringUtils.randomAlphabetic(10));
        testData.flowIdCache.put(testData.inResponseTo, flowId);
        commsLogger.addAppender(infoAppender);

        final ILightResponse iLightResponse = mockILightResponse(testData);
        Mockito.when(mockSpecificCommunicationLoggingService.getResponse(anyString(), any()))
                .thenReturn(iLightResponse);


        String goodToken = "goodToken";
        String badToken = "badToken";
        String[] tokenBase64Array = {badToken, goodToken};

        proxyServiceIncomingLightResponseLogger.logMessage(mockHttpServletRequest(testData, tokenBase64Array));

        assertThat(infoAppender.list.size(), is(1));
        assertTrue(StringUtils.isNotBlank(flowId));

        final String loggedMessage = infoAppender.list.get(0).getMessage();
        byte[] bytes = EidasStringUtil.decodeBytesFromBase64(goodToken);
        byte[] msgHashBytes = EidasDigestUtil.hashPersonalToken(bytes);
        String s = Base64.encode(msgHashBytes, 88);
        List<String> strings = List.of(s);
        Assert.assertThat(loggedMessage, stringContainsInOrder(strings));
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

        proxyServiceIncomingLightResponseLogger.logMessage(commsLogger, mockHttpServletRequest);

        final ILoggingEvent errorLoggingEvent = errorAppender.list.get(0);
        Assert.assertEquals(LoggingMarkerMDC.SAML_EXCHANGE, errorLoggingEvent.getMarker());
        Assert.assertEquals(SpecificCommunicationException.class.getName(), errorLoggingEvent.getThrowableProxy().getClassName());
        String expectedMessage = "Incomplete log of the light response because of ";
        Assert.assertEquals(expectedMessage, errorLoggingEvent.getMessage());

        assertTrue(infoAppender.list.isEmpty());
        assertThat(errorAppender.list.size(), is(2));
        final List<String> expectedStrings = Arrays.asList(
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

    private HttpServletRequest mockHttpServletRequest(LoggerTestData testData, String[] tokenBase64Array) {
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        Mockito.when(mockHttpServletRequest.getAttribute(TestLightResponseLogger.WHERE_TO_FIND_THE_LIGHT_TOKEN))
                .thenReturn(testData.tokenBase64);
        HashMap<String, String[]> httpParametersMap = createHttpParameterMap(tokenBase64Array);
        Mockito.when(mockHttpServletRequest.getParameterMap()).thenReturn(httpParametersMap);
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn(BindingMethod.POST.getValue());
        Mockito.when(mockHttpServletRequest.getHeader(EIDASValues.REFERER.toString())).thenReturn(testData.origin);
        Mockito.when(mockHttpServletRequest.getRequestURL()).thenReturn(new StringBuffer(testData.destination));
        return mockHttpServletRequest;
    }

    private HashMap<String, String[]> createHttpParameterMap(String[] tokenBase64Array) {
        final String servletParameter = TestLightResponseLogger.WHERE_TO_FIND_THE_LIGHT_TOKEN;
        HashMap<String, String[]> httpParametersMap = new HashMap<>();
        httpParametersMap.put(servletParameter, tokenBase64Array);
        return httpParametersMap;
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
