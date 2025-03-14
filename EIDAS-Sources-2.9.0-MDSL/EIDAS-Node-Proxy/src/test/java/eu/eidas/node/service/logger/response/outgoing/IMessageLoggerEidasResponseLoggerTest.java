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
package eu.eidas.node.service.logger.response.outgoing;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.logging.IMessageLogger;
import eu.eidas.logging.LoggingConstants;
import eu.eidas.logging.LoggingMarkerMDC;
import eu.eidas.logging.MessageLoggerUtils;
import eu.eidas.logging.logger.EidasResponseLogger;
import eu.eidas.node.service.logger.response.ProxyServiceOutgoingEidasResponseLogger;
import eu.eidas.node.service.servlet.binding.ProxySamlResponseViewMapping;
import eu.eidas.node.utils.HelperUtil;
import eu.eidas.node.utils.logger.LevelFilter;
import eu.eidas.node.utils.logger.LoggerTestData;
import eu.eidas.node.utils.logger.LoggerTestUtils;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link ProxyServiceOutgoingEidasResponseLogger}.
 */
public class IMessageLoggerEidasResponseLoggerTest {

    private final static String PROXY_SERVICE_RESPONSE_DESTINATION = "/EidasNode/ColleagueResponse";

    private final Logger logger = (Logger) LoggerFactory.getLogger(IMessageLogger.getLoggerName(
            EIDASValues.EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE, ProxyServiceOutgoingEidasResponseLogger.class));

    private ListAppender<ILoggingEvent> infoAppender;
    private ListAppender<ILoggingEvent> errorAppender;
    private EidasResponseLogger eidasResponseLogger;

    @Before
    public void setup() {
        infoAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);

        errorAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.ERROR));
        logger.addAppender(errorAppender);

        eidasResponseLogger = new ProxyServiceOutgoingEidasResponseLogger();
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
        eidasResponseLogger.logMessage(mockHttpServletRequest(testData, testData.tokenBase64));

        assertTrue(infoAppender.list.isEmpty());
        assertTrue(errorAppender.list.isEmpty());
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
                .setOpType(EIDASValues.EIDAS_SERVICE_CONNECTOR_RESPONSE.toString())
                .setNodeId("specificProxyService")
                .setOrigin(LoggingConstants.NOT_APPLICABLE)
                .setDestination("/EidasNode/ColleagueResponse")
                .setMsgId("msgID_" + randomAlphabetic(10))
                .setInResponseTo(String.format("inResponseTo_%s", RandomStringUtils.randomAlphabetic(10)))
                .setStatusCode("ResponseStatusCode");

        final String flowId = String.format("flowId_%s", RandomStringUtils.randomAlphabetic(10));
        testData.flowIdCache.put(testData.inResponseTo, flowId);

        eidasResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));
        eidasResponseLogger.logMessage(mockHttpServletRequest(testData, testData.tokenBase64));

        assertThat(infoAppender.list.size(), is(1));
        final List<String> expectedStrings = Arrays.asList(
                "Timestamp",
                "OpType", testData.opType,
                "NodeId", testData.nodeId,
                "Origin", testData.origin,
                "Destination", testData.destination,
                "flowId", flowId,
                "msgId", testData.msgId,
                "msgHash",
                "inResponseTo", testData.inResponseTo,
                "statusCode", testData.statusCode
        );

        String loggedMessage = infoAppender.list.get(0).getMessage();
        assertThat(loggedMessage, stringContainsInOrder(expectedStrings));
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

        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        Mockito.when(mockHttpServletRequest.getAttribute(ProxySamlResponseViewMapping.REDIRECT_URL))
                .thenReturn(PROXY_SERVICE_RESPONSE_DESTINATION);
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
                "OpType", EIDASValues.EIDAS_SERVICE_CONNECTOR_RESPONSE.toString(),
                "NodeId", LoggingConstants.UNDEFINED,
                "Origin", LoggingConstants.NOT_APPLICABLE,
                "Destination", PROXY_SERVICE_RESPONSE_DESTINATION,
                "flowId", LoggingConstants.UNDEFINED,
                "msgId", LoggingConstants.UNDEFINED,
                "msgHash",
                "inResponseTo", LoggingConstants.UNDEFINED,
                "statusCode", LoggingConstants.UNDEFINED
        );
        String actualMessage = errorAppender.list.get(1).getMessage();
        assertThat(actualMessage, stringContainsInOrder(expectedStrings));
    }

    private MessageLoggerUtils mockMessageLoggerUtils(LoggerTestData testData) throws EIDASSAMLEngineException {
        IAuthenticationResponse mockAuthenticationResponse = mockAuthenticationResponse(testData);
        MessageLoggerUtils mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(testData.logMessage);
        Mockito.when(mockMessageLoggerUtils.getIAuthenticationResponse(any(byte[].class))).thenReturn(mockAuthenticationResponse);
        Mockito.when(mockMessageLoggerUtils.getIssuer(anyString(), any())).thenReturn(testData.nodeId);
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

    private HttpServletRequest mockHttpServletRequest(LoggerTestData testData, String base64SamlMessage) {
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        Mockito.when(mockHttpServletRequest.getAttribute(ProxySamlResponseViewMapping.REDIRECT_URL)).thenReturn(testData.destination);
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn(BindingMethod.POST.getValue());
        Mockito.when(mockHttpServletRequest.getAttribute(ProxySamlResponseViewMapping.SAML_TOKEN))
                .thenReturn(base64SamlMessage);

        return mockHttpServletRequest;
    }
}