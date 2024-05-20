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
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.logging.IMessageLogger;
import eu.eidas.logging.LoggingConstants;
import eu.eidas.logging.LoggingMarkerMDC;
import eu.eidas.logging.MessageLoggerUtils;
import eu.eidas.logging.messages.EidasRequestMessageLog;
import eu.eidas.logging.messages.MessageLogTestUtils;
import eu.eidas.logging.utils.HelperUtil;
import eu.eidas.logging.utils.logger.LevelFilter;
import eu.eidas.logging.utils.logger.LoggerTestData;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static eu.eidas.logging.utils.logger.LoggerTestUtils.createStartListAppender;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link EidasRequestLogger}.
 *
 * @since 2.3
 */
public class IMessageLoggerEidasRequestLoggerTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(
            IMessageLogger.getLoggerName(EIDASValues.EIDAS_PACKAGE_REQUEST_LOGGER_VALUE, TestEidasRequestLogger.class)
    );
    private ListAppender<ILoggingEvent> infoAppender;
    private ListAppender<ILoggingEvent> errorAppender;

    private EidasRequestLogger eidasRequestLogger;

    private static class TestEidasRequestLogger extends EidasRequestLogger {

        public static final String WHERE_TO_FIND_THE_SAML = "SAMLRequestViewMapping";

        @Override
        protected byte[] getSamlRequestDecodedBytes(HttpServletRequest httpServletRequest) {
            final String samlRequestTokenSaml = (String) httpServletRequest.getAttribute(WHERE_TO_FIND_THE_SAML);
            if (samlRequestTokenSaml == null) {
                return new byte[0];
            }
            return EidasStringUtil.decodeBytesFromBase64(samlRequestTokenSaml);
        }

        @Override
        protected void setMessageVector(@Nonnull EidasRequestMessageLog.Builder messageLogBuilder, @Nonnull HttpServletRequest httpServletRequest, @Nullable IAuthenticationRequest authenticationRequest) {
            final Optional<String> destination = Optional.ofNullable(authenticationRequest)
                    .map(IAuthenticationRequest::getDestination);

            destination.map(messageLogBuilder::setDestination);
            messageLogBuilder.setOpType(EIDASValues.CONNECTOR_SERVICE_REQUEST.toString());
            messageLogBuilder.setOrigin(LoggingConstants.NOT_APPLICABLE);
            Optional.ofNullable(authenticationRequest).map(
                            auth -> messageLoggerUtils.getEntityId("/EidasNode/ServiceMetadata"))
                    .map(messageLogBuilder::setNodeId);
            destination.map(messageLogBuilder::setDestination);
        }
    }

    @Before
    public void setup() {
        infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);

        errorAppender = createStartListAppender(new LevelFilter(Level.ERROR));
        logger.addAppender(errorAppender);

        eidasRequestLogger = new TestEidasRequestLogger();
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
        eidasRequestLogger.logMessage(logger, mockHttpServletRequest(testData));

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
                .setOpType(EIDASValues.CONNECTOR_SERVICE_REQUEST.toString())
                .setNodeId("/EidasNode/ServiceMetadata")
                .setOrigin(LoggingConstants.NOT_APPLICABLE)
                .setDestination("/EidasNode/ColleagueRequest")
                .setMsgId("msgID_" + randomAlphabetic(10));

        final String flowId = "flowID_" + randomAlphabetic(10);
        testData.flowIdCache.put(testData.msgId, flowId);


        eidasRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));
        eidasRequestLogger.logMessage(mockHttpServletRequest(testData));

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
     * <p>
     * Error scenario
     */
    @Test
    public void testLogOfInvalidEidasRequest() throws EIDASSAMLEngineException {
        MessageLoggerUtils mockMessageLoggerUtils = Mockito.mock(MessageLoggerUtils.class);
        eidasRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils);
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        String expectedNodeId = "/EidasNode/ServiceMetadata";
        Mockito.when(mockMessageLoggerUtils.getIAuthenticationProxyRequest(Mockito.any())).thenThrow(EIDASSAMLEngineException.class);

        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);

        eidasRequestLogger.logMessage(logger, mockHttpServletRequest);

        assertThat(infoAppender.list.size(), is(0));
        assertThat(errorAppender.list.size(), is(2));
        final ILoggingEvent errorLoggingEvent = errorAppender.list.get(0);
        Assert.assertEquals(LoggingMarkerMDC.SAML_EXCHANGE, errorLoggingEvent.getMarker());
        Assert.assertEquals(EIDASSAMLEngineException.class.getName(), errorLoggingEvent.getThrowableProxy().getClassName());
        Assert.assertEquals("Incomplete log of the eIDAS SAML request because of ", errorLoggingEvent.getMessage());

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

    /**
     * Test method for {@link ConnectorOutgoingEidasRequestLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * when an error occurs within {@link AUCONNECTORUtil#loadConfigServiceMetadataURL(String)}
     * All information that can be logged should be logged anyway
     * <p>
     * Error scenario
     */
    @Test
    public void testLogWhenLoadingServiceMetadataURLFails() throws EIDASSAMLEngineException {
        MessageLoggerUtils mockMessageLoggerUtils = mockMessageLoggerUtils(new LoggerTestData());
        eidasRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils);
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        Mockito.when(mockMessageLoggerUtils.getEntityId(Mockito.anyString())).thenThrow(NumberFormatException.class);
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        String tokenBase64 = "token";
        Mockito.when(mockHttpServletRequest.getAttribute(TestEidasRequestLogger.WHERE_TO_FIND_THE_SAML)).thenReturn(tokenBase64);

        eidasRequestLogger.logMessage(logger, mockHttpServletRequest);

        assertThat(infoAppender.list.size(), is(0));
        assertThat(errorAppender.list.size(), is(2));
        final ILoggingEvent errorLoggingEvent = errorAppender.list.get(0);
        Assert.assertEquals(LoggingMarkerMDC.SAML_EXCHANGE, errorLoggingEvent.getMarker());
        Assert.assertEquals(NumberFormatException.class.getName(), errorLoggingEvent.getThrowableProxy().getClassName());
        Assert.assertEquals("Incomplete log of the eIDAS SAML request because of ", errorLoggingEvent.getMessage());

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

    private MessageLoggerUtils mockMessageLoggerUtils(LoggerTestData testData) throws EIDASSAMLEngineException {
        IAuthenticationRequest mockAuthenticationRequest = mockAuthenticationRequest(testData);
        MessageLoggerUtils mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(testData.logMessage);
        Mockito.when(mockMessageLoggerUtils.getIAuthenticationProxyRequest(any(byte[].class))).thenReturn(mockAuthenticationRequest);
        Mockito.when(mockMessageLoggerUtils.getEntityId(anyString())).thenReturn(testData.nodeId);
        Mockito.when(mockMessageLoggerUtils.trackMessageFlow(anyString()))
                .then(invocationOnMock -> testData.flowIdCache.get(invocationOnMock.getArgument(0)));
        return mockMessageLoggerUtils;
    }


    private IAuthenticationRequest mockAuthenticationRequest(LoggerTestData testData) {
        IAuthenticationRequest mockAuthenticationRequest = mock(IAuthenticationRequest.class);
        Mockito.when(mockAuthenticationRequest.getId()).thenReturn(testData.msgId);
        Mockito.when(mockAuthenticationRequest.getDestination()).thenReturn(testData.destination);
        return mockAuthenticationRequest;
    }

    private HttpServletRequest mockHttpServletRequest(LoggerTestData testData) {
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        Mockito.when(mockHttpServletRequest.getAttribute(TestEidasRequestLogger.WHERE_TO_FIND_THE_SAML)).thenReturn(testData.tokenBase64);
        return mockHttpServletRequest;
    }

}