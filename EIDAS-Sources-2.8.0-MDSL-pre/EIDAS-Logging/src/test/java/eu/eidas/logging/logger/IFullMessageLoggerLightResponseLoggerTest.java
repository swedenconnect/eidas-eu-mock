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
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.logging.IFullMessageLogger;
import eu.eidas.logging.LoggingMarkerMDC;
import eu.eidas.logging.MessageLoggerUtils;
import eu.eidas.logging.messages.LightResponseMessageLog;
import eu.eidas.logging.utils.ReadFileUtils;
import eu.eidas.logging.utils.logger.LevelFilter;
import eu.eidas.logging.utils.logger.LoggerTestData;
import eu.eidas.logging.utils.logger.LoggerTestUtils;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationLoggingService;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashMap;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IFullMessageLoggerLightResponseLoggerTest {
    private static final String PROXY_SERVICE_LIGHT_RESPONSE_NODE_ID = "SpecificProxyService";
    private final Logger fullLogger = (Logger) LoggerFactory.getLogger(IFullMessageLogger.getLoggerName(TestLightResponseLogger.class));

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
        fullLogger.addAppender(infoAppender);

        errorAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.ERROR));
        fullLogger.addAppender(errorAppender);

        mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        when(mockMessageLoggerUtils.isLogCompleteMessage()).thenReturn(true);
        mockSpecificCommunicationLoggingService = mock(SpecificCommunicationLoggingService.class);

        proxyServiceIncomingLightResponseLogger = new TestLightResponseLogger();
        proxyServiceIncomingLightResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils);
        proxyServiceIncomingLightResponseLogger.setSpringManagedSpecificCommunicationLoggingService(mockSpecificCommunicationLoggingService);
    }

    @After
    public void teardown() {
        fullLogger.detachAndStopAllAppenders();
    }

    /**
     * Test method for {@link ProxyServiceIncomingLightResponseLogger#logFullMessage(HttpServletRequest)}
     * when  {@link HttpServletRequest} is passed to getFullResponse() it should
     * call the {@link SpecificCommunicationLoggingService#getResponse(String)} to obtain the xml format of the LightResponseMessage
     * Log this information to the logger with {@link org.slf4j.Marker} of {@link LoggingMarkerMDC#FULL_MSG_EXCHANGE}
     */
    @Test
    public void logFullMessage() throws Exception {
        final String SERIALIZED_RESPONSE_MESSAGE = new String(ReadFileUtils.readFileAsByteArray("logging/testFullLoggingLightResponse.xml"));
        final String tokenBase64 = EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)).getBytes());
        final LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setTokenBase64(tokenBase64)
                .setDestination(StringUtils.EMPTY);
        Mockito.when(mockSpecificCommunicationLoggingService.getResponse(any()))
                .thenReturn(SERIALIZED_RESPONSE_MESSAGE);

        proxyServiceIncomingLightResponseLogger.logFullMessage(mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

        verify(mockSpecificCommunicationLoggingService).getResponse(eq(tokenBase64));
        Assert.assertEquals(1, infoAppender.list.size());
        final ILoggingEvent infoLoggingEvent = infoAppender.list.get(0);
        Assert.assertEquals(LoggingMarkerMDC.FULL_MSG_EXCHANGE, infoLoggingEvent.getMarker());
        Assert.assertEquals(SERIALIZED_RESPONSE_MESSAGE, infoLoggingEvent.getMessage());
    }

    /**
     * Test method for {@link ProxyServiceIncomingLightResponseLogger#logFullMessage(HttpServletRequest)}
     * call the {@link SpecificCommunicationLoggingService#getSerializedRequest(String, Collection)} with the expected Error of {@link SpecificCommunicationException}
     * Should handle the error and send and error message to the logger with {@link org.slf4j.Marker} of {@link LoggingMarkerMDC#FULL_MSG_EXCHANGE}
     */
    @Test
    public void logFullMessageError() throws Exception {
        final SpecificCommunicationException specificCommunicationException = new SpecificCommunicationException("NI!");
        final LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setTokenBase64(EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)).getBytes()))
                .setDestination(StringUtils.EMPTY);
        Mockito.when(mockSpecificCommunicationLoggingService.getResponse(any()))
                .thenThrow(specificCommunicationException);

        proxyServiceIncomingLightResponseLogger.logFullMessage(mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

        verify(mockSpecificCommunicationLoggingService).getResponse(eq(testData.tokenBase64));
        Assert.assertTrue(infoAppender.list.isEmpty());
        final ILoggingEvent errorLoggingEvent = errorAppender.list.get(0);
        Assert.assertEquals(LoggingMarkerMDC.FULL_MSG_EXCHANGE, errorLoggingEvent.getMarker());
        Assert.assertEquals(SpecificCommunicationException.class.getName(), errorLoggingEvent.getThrowableProxy().getClassName());
        Assert.assertEquals(specificCommunicationException.getMessage(), errorLoggingEvent.getThrowableProxy().getMessage());
    }

    /**
     * Test method for {@link ProxyServiceIncomingLightResponseLogger#logFullMessage(HttpServletRequest)}
     * when  {@link MessageLoggerUtils#isLogCompleteMessage()} is false it should
     * exit the method without logging
     */
    @Test
    public void logFullMessageConfigFalse() throws Exception {
        final LoggerTestData testData = new LoggerTestData()
                .setLogMessage(false)
                .setTokenBase64(EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)).getBytes()))
                .setDestination(StringUtils.EMPTY);

        when(mockMessageLoggerUtils.isLogCompleteMessage()).thenReturn(false);

        proxyServiceIncomingLightResponseLogger.logFullMessage(mockHttpServletRequest(testData, new String[]{testData.tokenBase64}));

        verify(mockSpecificCommunicationLoggingService, never()).getResponse(eq(testData.tokenBase64));
        Assert.assertTrue(infoAppender.list.isEmpty());
        Assert.assertTrue(errorAppender.list.isEmpty());
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

    private ILightResponse mockILightResponse(LoggerTestData testData) {
        ResponseStatus mockResponseStatus = mock(ResponseStatus.class);
        Mockito.when(mockResponseStatus.getStatusCode()).thenReturn(testData.statusCode);

        ILightResponse mockILightResponse = mock(ILightResponse.class);
        Mockito.when(mockILightResponse.getId()).thenReturn(testData.msgId);
        Mockito.when(mockILightResponse.getInResponseToId()).thenReturn(testData.inResponseTo);
        Mockito.when(mockILightResponse.getStatus()).thenReturn(mockResponseStatus);

        return mockILightResponse;
    }
}
