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
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.logging.IMessageLogger;
import eu.eidas.logging.LoggingConstants;
import eu.eidas.logging.LoggingMarkerMDC;
import eu.eidas.logging.MessageLoggerUtils;
import eu.eidas.logging.messages.EidasResponseMessageLog;
import eu.eidas.logging.messages.MessageLogTestUtils;
import eu.eidas.logging.utils.HelperUtil;
import eu.eidas.logging.utils.logger.LevelFilter;
import eu.eidas.logging.utils.logger.LoggerTestData;
import eu.eidas.logging.utils.logger.LoggerTestUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static eu.eidas.auth.commons.EIDASValues.REFERER;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Tests for the {@link EidasResponseLogger}.
 *
 * @since 2.3
 */
public class IMessageLoggerEidasResponseLoggerTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(IMessageLogger.getLoggerName(
            EIDASValues.EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE, TestEidasResponseLogger.class));

    private ListAppender<ILoggingEvent> infoAppender;
    private ListAppender<ILoggingEvent> errorAppender;
    private EidasResponseLogger eidasResponseLogger;

    @Before
    public void setup() {
        infoAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);

        errorAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.ERROR));
        logger.addAppender(errorAppender);

        eidasResponseLogger = new TestEidasResponseLogger();
    }

    @After
    public void teardown() {
        logger.detachAndStopAllAppenders();
    }

    /**
     * Test method for {@link ConnectorIncomingEidasResponseLogger#logMessage(HttpServletRequest)}.
     * Check no logging occurs when logging is deactivated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageInactive() throws Exception {
        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setDestination(StringUtils.EMPTY);

        eidasResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));
        HashMap<String, String[]> httpParameterMap = createHttpParameterMap(new String[]{testData.tokenBase64});
        eidasResponseLogger.logMessage(mockHttpServletRequest(testData, httpParameterMap));

        assertThat(infoAppender.list.size(), is(0));
    }

    /**
     * Test method for {@link ConnectorIncomingEidasResponseLogger#logMessage(HttpServletRequest)}.
     * Check logging occurs when logging is activated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageActive() throws Exception {
        final LoggerTestData testData = new LoggerTestData()
                .setLogMessage(true)
                .setTokenBase64(EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)).getBytes()))
                .setFlowIdCache(HelperUtil.createHashMapCommunicationCacheMock())
                .setOpType(EIDASValues.EIDAS_CONNECTOR_RESPONSE.toString())
                .setNodeId("/EidasNode/ServiceMetadata")
                .setOrigin("/EidasNode/SpecificProxyServiceResponse")
                .setDestination("/EidasNode/ColleagueResponse")
                .setMsgId("msgID_" + randomAlphabetic(10))
                .setInResponseTo(String.format("inResponseTo_%s", RandomStringUtils.randomAlphabetic(10)))
                .setStatusCode("StatusCode");

        final String flowId = String.format("flowId_%s", RandomStringUtils.randomAlphabetic(10));
        testData.flowIdCache.put(testData.inResponseTo, flowId);

        eidasResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));
        HashMap<String, String[]> httpParameterMap = createHttpParameterMap(new String[]{testData.tokenBase64});
        eidasResponseLogger.logMessage(mockHttpServletRequest(testData, httpParameterMap));

        assertThat(infoAppender.list.size(), is(1));
        final List<String> expectedStrings = Arrays.asList(
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
     * <p>
     * Must succeed.
     */
    @Test
    public void testTwoTokensHttpRequestGoodTokenLast() throws EIDASSAMLEngineException, NoSuchFieldException, IllegalAccessException {
        final LoggerTestData testData = new LoggerTestData()
                .setLogMessage(true)
                .setTokenBase64(EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)).getBytes()))
                .setFlowIdCache(HelperUtil.createHashMapCommunicationCacheMock())
                .setOpType(EIDASValues.EIDAS_CONNECTOR_RESPONSE.toString())
                .setNodeId("/EidasNode/ServiceMetadata")
                .setOrigin("/EidasNode/SpecificProxyServiceResponse")
                .setDestination("/EidasNode/ColleagueResponse")
                .setMsgId("msgID_" + randomAlphabetic(10))
                .setInResponseTo(String.format("inResponseTo_%s", RandomStringUtils.randomAlphabetic(10)))
                .setStatusCode("StatusCode");

        final String flowId = String.format("flowId_%s", RandomStringUtils.randomAlphabetic(10));
        testData.flowIdCache.put(testData.inResponseTo, flowId);
        eidasResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));

        HashMap<String, String[]> httpParameterMap = createHttpParameterMap(new String[]{"badToken", "goodToken"});
        HttpServletRequest httpServletRequest = mockHttpServletRequest(testData, httpParameterMap);

        eidasResponseLogger.logMessage(httpServletRequest);

        assertThat(infoAppender.list.size(), is(1));
        String loggedMessage = infoAppender.list.get(0).getMessage();

        byte[] bytes = EidasStringUtil.decodeBytesFromBase64("goodToken");
        byte[] msgHashBytes = EidasDigestUtil.hashPersonalToken(bytes);
        String s = new String(java.util.Base64.getEncoder().encode(msgHashBytes));
        List<String> strings = List.of(s);
        assertThat(loggedMessage, stringContainsInOrder(strings));
    }

    /**
     * Test method for {@link ConnectorIncomingEidasResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * when in the {@link HttpServletRequest} instance for the key {EidasParameterKeys.SAML_RESPONSE} cannot be unmarshal
     * All information that can be logged should be logged anyway
     * <p>
     * Error scenario
     */
    @Test
    public void testLogOfInvalidEidasResponse() throws EIDASSAMLEngineException {
        MessageLoggerUtils mockMessageLoggerUtils = Mockito.mock(MessageLoggerUtils.class);
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        Mockito.when(mockMessageLoggerUtils.getIAuthenticationResponse(Mockito.any())).thenThrow(EIDASSAMLEngineException.class);
        eidasResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils);


        final String CONNECTOR_INCOMING_RESPONSE_ORIGIN = "/EidasNode/SpecificProxyServiceResponse";
        final String CONNECTOR_INCOMING_RESPONSE_DESTINATION = "/EidasNode/ColleagueResponse";

        HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
        String[] tokenBase64Array = {"token"};
        HashMap<String, String[]> httpParametersMap = createHttpParameterMap(tokenBase64Array);
        Mockito.when(mockHttpServletRequest.getHeader(REFERER.toString())).thenReturn(CONNECTOR_INCOMING_RESPONSE_ORIGIN);
        Mockito.when(mockHttpServletRequest.getRequestURL()).thenReturn((new StringBuffer()).append(CONNECTOR_INCOMING_RESPONSE_DESTINATION));
        Mockito.when(mockHttpServletRequest.getParameterMap()).thenReturn(httpParametersMap);
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn(BindingMethod.POST.getValue());

        eidasResponseLogger.logMessage(mockHttpServletRequest);

        assertThat(infoAppender.list.size(), is(0));
        assertThat(errorAppender.list.size(), is(2));
        final ILoggingEvent errorLoggingEvent = errorAppender.list.get(0);
        Assert.assertEquals(LoggingMarkerMDC.SAML_EXCHANGE, errorLoggingEvent.getMarker());
        Assert.assertEquals(EIDASSAMLEngineException.class.getName(), errorLoggingEvent.getThrowableProxy().getClassName());
        Assert.assertEquals("Incomplete log of the eIDAS SAML response because of ", errorLoggingEvent.getMessage());


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

    static class TestEidasResponseLogger extends EidasResponseLogger {

        public static final String WHERE_TO_FIND_THE_SAML = "SAMLResponse";

        @Override
        protected byte[] getSamlResponseDecodedBytes(HttpServletRequest httpServletRequest) {
            final WebRequest webRequest = new IncomingRequest(httpServletRequest);
            final String samlResponseToken = webRequest.getEncodedLastParameterValue(WHERE_TO_FIND_THE_SAML);
            return EidasStringUtil.decodeBytesFromBase64(samlResponseToken);
        }

        @Override
        protected void setMessageVector(@Nonnull EidasResponseMessageLog.Builder messageLogBuilder, @Nonnull HttpServletRequest httpServletRequest, IAuthenticationResponse authenticationResponse) {
            Optional.ofNullable(authenticationResponse)
                    .map(ILightResponse::getIssuer)
                    .map(messageLoggerUtils::getEntityId)
                    .map(messageLogBuilder::setNodeId);
            messageLogBuilder.setOpType(EIDASValues.EIDAS_CONNECTOR_RESPONSE.toString());
            messageLogBuilder.setOrigin(httpServletRequest.getHeader(EIDASValues.REFERER.toString()));
            messageLogBuilder.setDestination(httpServletRequest.getRequestURL().toString());
        }
    }

    private HttpServletRequest mockHttpServletRequest(LoggerTestData testData, HashMap<String, String[]> httpParametersMap) {
        HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
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

    private MessageLoggerUtils mockMessageLoggerUtils(LoggerTestData testData) throws EIDASSAMLEngineException {
        IAuthenticationResponse mockAuthenticationResponse = mockAuthenticationResponse(testData);
        MessageLoggerUtils mockMessageLoggerUtils = Mockito.mock(MessageLoggerUtils.class);
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(testData.logMessage);
        Mockito.when(mockMessageLoggerUtils.getIAuthenticationResponse(any(byte[].class))).thenReturn(mockAuthenticationResponse);
        Mockito.when(mockMessageLoggerUtils.getEntityId(anyString())).thenReturn(testData.nodeId);
        Mockito.when(mockMessageLoggerUtils.trackMessageFlow(anyString()))
                .then(invocationOnMock -> testData.flowIdCache.get(invocationOnMock.getArgument(0)));

        return mockMessageLoggerUtils;
    }

    private IAuthenticationResponse mockAuthenticationResponse(LoggerTestData testData) {
        IAuthenticationResponse mockAuthenticationResponse = Mockito.mock(IAuthenticationResponse.class);
        Mockito.when(mockAuthenticationResponse.getInResponseToId()).thenReturn(testData.inResponseTo);
        Mockito.when(mockAuthenticationResponse.getIssuer()).thenReturn(("anyString"));
        Mockito.when(mockAuthenticationResponse.getId()).thenReturn(testData.msgId);
        Mockito.when(mockAuthenticationResponse.getStatusCode()).thenReturn(testData.statusCode);
        return mockAuthenticationResponse;
    }
}