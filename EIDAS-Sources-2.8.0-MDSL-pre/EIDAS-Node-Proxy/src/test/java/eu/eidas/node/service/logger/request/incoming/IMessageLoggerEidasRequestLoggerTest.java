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
package eu.eidas.node.service.logger.request.incoming;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.logging.IMessageLogger;
import eu.eidas.logging.LoggingConstants;
import eu.eidas.logging.LoggingMarkerMDC;
import eu.eidas.logging.MessageLoggerUtils;
import eu.eidas.logging.logger.EidasRequestLogger;
import eu.eidas.node.service.logger.request.ProxyServiceIncomingEidasRequestLogger;
import eu.eidas.node.utils.HelperUtil;
import eu.eidas.node.utils.logger.LevelFilter;
import eu.eidas.node.utils.logger.LoggerTestData;
import eu.eidas.node.utils.logger.LoggerTestUtils;
import org.apache.commons.lang.StringUtils;
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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link ProxyServiceIncomingEidasRequestLogger}.
 */
public class IMessageLoggerEidasRequestLoggerTest {

    private final static String ANY_STRING_VALUE = "anyString";

    private final Logger logger = (Logger) LoggerFactory.getLogger(
            IMessageLogger.getLoggerName(EIDASValues.EIDAS_PACKAGE_REQUEST_LOGGER_VALUE, ProxyServiceIncomingEidasRequestLogger.class)
    );
    private ListAppender<ILoggingEvent> infoAppender;
    private ListAppender<ILoggingEvent> errorAppender;

    private EidasRequestLogger eidasRequestLogger;

    @Before
    public void setup() {
        infoAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);

        errorAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.ERROR));
        logger.addAppender(errorAppender);

        eidasRequestLogger = new ProxyServiceIncomingEidasRequestLogger();
    }

    @After
    public void teardown() {
        logger.detachAndStopAllAppenders();
    }

    /**
     * Test method for {@link ConnectorOutgoingEidasRequestLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * Check no logging occurs when logging is deactivated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageInactive() throws Exception {
        LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setDestination(StringUtils.EMPTY);
        eidasRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));
        eidasRequestLogger.logMessage(mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

        assertThat(infoAppender.list.size(), is(0));
    }

    /**
     * Test method for {@link ConnectorOutgoingEidasRequestLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * Check logging occurs when logging is activated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageActive() throws Exception {
        final LoggerTestData testData = new LoggerTestData()
                .setLogMessage(true)
                .setTokenBase64(EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)).getBytes()))
                .setFlowIdCache(HelperUtil.createHashMapCommunicationCacheMock())
                .setOpType(EIDASValues.EIDAS_SERVICE_REQUEST.toString())
                .setNodeId("/EidasNode/ConnectorMetadata")
                .setOrigin("/EidasNode/SpecificConnectorRequest")
                .setDestination("/EidasNode/ColleagueRequest")
                .setMsgId("msgID_" + randomAlphabetic(10));

        final String flowId = "flowID_" + randomAlphabetic(10);
        testData.flowIdCache.put(testData.msgId, flowId);


        eidasRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));
        eidasRequestLogger.logMessage(mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

        assertThat(infoAppender.list.size(), is(1));
        List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                "OpType", testData.opType,
                "NodeId", testData.nodeId,
                "Origin", testData.origin,
                "Destination", testData.destination,
                "flowId", flowId,
                "msgId", testData.msgId,
                "msgHash"
        );

        String loggedMessage = infoAppender.list.get(0).getMessage();
        assertThat(loggedMessage, stringContainsInOrder(expectedStrings));
    }

    /**
     * Test method for {@link ConnectorOutgoingEidasRequestLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * when in the {@link HttpServletRequest} instance for the key {EidasParameterKeys.SAML_REQUEST} cannot be unmarshal
     * All information that can be logged should be logged anyway
     * <p>
     * Error scenario
     */
    @Test
    public void testLogOfInvalidEidasRequest() throws EIDASSAMLEngineException {
        MessageLoggerUtils mockMessageLoggerUtils = Mockito.mock(MessageLoggerUtils.class);
        eidasRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils);
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        Mockito.when(mockMessageLoggerUtils.getIAuthenticationProxyRequest(Mockito.any())).thenThrow(EIDASSAMLEngineException.class);

        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn(BindingMethod.POST.getValue());


        eidasRequestLogger.logMessage(mockHttpServletRequest);

        assertThat(infoAppender.list.size(), is(0));
        assertThat(errorAppender.list.size(), is(2));
        final ILoggingEvent errorLoggingEvent = errorAppender.list.get(0);
        Assert.assertEquals(LoggingMarkerMDC.SAML_EXCHANGE, errorLoggingEvent.getMarker());
        Assert.assertEquals(EIDASSAMLEngineException.class.getName(), errorLoggingEvent.getThrowableProxy().getClassName());
        Assert.assertEquals("Incomplete log of the eIDAS SAML request because of ", errorLoggingEvent.getMessage());

        List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                "OpType", EIDASValues.EIDAS_SERVICE_REQUEST.toString(),
                "NodeId", LoggingConstants.UNDEFINED,
                "Origin", LoggingConstants.UNDEFINED,
                "Destination", LoggingConstants.UNDEFINED,
                "flowId", LoggingConstants.UNDEFINED,
                "msgId", LoggingConstants.UNDEFINED,
                "msgHash"
        );
        String actualMessage = errorAppender.list.get(1).getMessage();
        assertThat(actualMessage, stringContainsInOrder(expectedStrings));
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

        return proxyServiceIncomingEidasRequestLogger;
    }

    private MessageLoggerUtils mockMessageLoggerUtils(LoggerTestData testData) throws EIDASSAMLEngineException {
        IAuthenticationRequest mockAuthenticationRequest = mockAuthenticationRequest(testData);
        MessageLoggerUtils mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(testData.logMessage);
        Mockito.when(mockMessageLoggerUtils.getIAuthenticationProxyRequest(any(byte[].class))).thenReturn(mockAuthenticationRequest);
        Mockito.when(mockMessageLoggerUtils.trackMessageFlow(anyString()))
                .then(invocationOnMock -> testData.flowIdCache.get(invocationOnMock.getArgument(0)));
        Mockito.when(mockMessageLoggerUtils.getEntityId(anyString())).thenReturn(testData.nodeId);
        return mockMessageLoggerUtils;
    }

    private IAuthenticationRequest mockAuthenticationRequest (LoggerTestData testData) {
        IAuthenticationRequest mockAuthenticationRequest = mock(IAuthenticationRequest.class);
        Mockito.when(mockAuthenticationRequest.getId()).thenReturn(testData.msgId);
        Mockito.when(mockAuthenticationRequest.getIssuer()).thenReturn("anyStringValue");
        Mockito.when(mockAuthenticationRequest.getDestination()).thenReturn(testData.destination);
        return mockAuthenticationRequest;
    }
    //
}