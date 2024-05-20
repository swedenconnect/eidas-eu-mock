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
package eu.eidas.node.connector.logger.request.incoming;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.logging.IFullMessageLogger;
import eu.eidas.logging.LoggingMarkerMDC;
import eu.eidas.logging.MessageLoggerUtils;
import eu.eidas.logging.logger.LightRequestLogger;
import eu.eidas.node.connector.logger.LevelFilter;
import eu.eidas.node.connector.logger.LoggerTestData;
import eu.eidas.node.connector.logger.LoggerTestUtils;
import eu.eidas.node.connector.logger.request.ConnectorIncomingLightRequestLogger;
import eu.eidas.node.utils.ReadFileUtils;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationLoggingService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link ConnectorIncomingLightRequestLogger}.
 */
public class IFullMessageLoggerLightRequestLoggerTest {

    private static final String CONNECTOR_LIGHT_REQUEST_NODE_ID = "TestNodeID";
    private static final String CONNECTOR_INCOMING_REQUEST_ORIGIN = "http://origin";
    private static final String CONNECTOR_INCOMING_REQUEST_DESTINATION = "http://destination";
    private static final String BASE64_ENCODED_TOKEN = "c3BlY2lmaWNDb21tdW5pY2F0aW9uRGVmaW5pdGlvbkNvbm5lY3RvclJlcXVlc3R8Mjc3YTVlM2EtNWI5MS00ODY5LTk2NGMtNDg5YWVmN2ZmMGY1fDIwMjAtMDItMjAgMTY6MDE6MDkgMjgxfDZoaGhDSDFsYTk1NWo0QVBxT1pkMGczd3ROOFk3b3N1dUc4UjRQNlpJWVk9";

    private final Logger fullLogger = (Logger) LoggerFactory.getLogger(IFullMessageLogger.getLoggerName(ConnectorIncomingLightRequestLogger.class));

    private ListAppender<ILoggingEvent> infoAppender;
    private ListAppender<ILoggingEvent> errorAppender;

    private MessageLoggerUtils mockMessageLoggerUtils;
    private SpecificCommunicationLoggingService mockSpecificCommunicationLoggingService;

    private LightRequestLogger lightRequestLogger;

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

        lightRequestLogger = new ConnectorIncomingLightRequestLogger();
        lightRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils);
        lightRequestLogger.setLightTokenRequestNodeId(CONNECTOR_LIGHT_REQUEST_NODE_ID);
        lightRequestLogger.setSpringManagedSpecificCommunicationLoggingService(mockSpecificCommunicationLoggingService);
    }

    @After
    public void teardown() {
        fullLogger.detachAndStopAllAppenders();
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLogger#logFullMessage(HttpServletRequest)}
     * when serializedRequest is a available from service
     * <p>
     * message must be send to {@link Logger#info(Marker, String)} with {@link LoggingMarkerMDC#FULL_MSG_EXCHANGE} marker
     */
    @Test
    public void logFullMessage() throws Exception {
        final String SERIALIZED_MESSAGE = new String(ReadFileUtils.readFileAsByteArray("logging/testFullLoggingLightRequest.xml"));
        final HttpServletRequest mockHttpServletRequest = mockHttpServletRequest(new LoggerTestData()
                .setTokenBase64(BASE64_ENCODED_TOKEN)
                .setDestination("")
                .setOrigin("")
        );

        Mockito.when(mockSpecificCommunicationLoggingService.getRequest(any()))
                .thenReturn(SERIALIZED_MESSAGE);

        lightRequestLogger.logFullMessage(mockHttpServletRequest);

        verify(mockSpecificCommunicationLoggingService).getRequest(eq(BASE64_ENCODED_TOKEN));
        Assert.assertEquals(1, infoAppender.list.size());
        final ILoggingEvent infoLoggingEvent = infoAppender.list.get(0);
        Assert.assertEquals(LoggingMarkerMDC.FULL_MSG_EXCHANGE, infoLoggingEvent.getMarker());
        Assert.assertEquals(SERIALIZED_MESSAGE, infoLoggingEvent.getMessage());
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLogger#logFullMessage(HttpServletRequest)}
     * when serializedRequest is throwing an error
     * <p>
     * error must be send to {@link Logger#error(Marker, String)} with {@link LoggingMarkerMDC#FULL_MSG_EXCHANGE} marker
     */
    @Test
    public void logFullMessageError() throws Exception {
        final HttpServletRequest mockHttpServletRequest = mockHttpServletRequest(new LoggerTestData()
                .setTokenBase64(BASE64_ENCODED_TOKEN)
                .setDestination("")
                .setOrigin("")
        );
        final SpecificCommunicationException specificCommunicationException = new SpecificCommunicationException("RUN AWAY");
        Mockito.when(mockSpecificCommunicationLoggingService.getRequest(any()))
                .thenThrow(specificCommunicationException);

        lightRequestLogger.logFullMessage(mockHttpServletRequest);

        verify(mockSpecificCommunicationLoggingService).getRequest(eq(BASE64_ENCODED_TOKEN));
        Assert.assertTrue(infoAppender.list.isEmpty());
        final ILoggingEvent errorLoggingEvent = errorAppender.list.get(0);
        Assert.assertEquals(LoggingMarkerMDC.FULL_MSG_EXCHANGE, errorLoggingEvent.getMarker());
        Assert.assertEquals(SpecificCommunicationException.class.getName(), errorLoggingEvent.getThrowableProxy().getClassName());
        Assert.assertEquals(specificCommunicationException.getMessage(), errorLoggingEvent.getThrowableProxy().getMessage());
    }

    /**
     * Test method for {@link ConnectorIncomingLightRequestLogger#logFullMessage(HttpServletRequest)}
     * when serializedRequest is throwing IllegalArgumentException
     * <p>
     * error must be sent to {@link Logger#error(Marker, String)} with {@link LoggingMarkerMDC#FULL_MSG_EXCHANGE} marker
     */
    @Test
    public void logFullMessageThrowingIllegalArgumentException() throws Exception {
        final HttpServletRequest mockHttpServletRequest = mockHttpServletRequest(new LoggerTestData()
                .setTokenBase64(BASE64_ENCODED_TOKEN)
                .setDestination("")
                .setOrigin("")
        );
        final IllegalArgumentException illegalArgumentException = new IllegalArgumentException("invalid Light Request");
        Mockito.when(mockSpecificCommunicationLoggingService.getRequest(any()))
                .thenThrow(illegalArgumentException);

        lightRequestLogger.logFullMessage(mockHttpServletRequest);

        verify(mockSpecificCommunicationLoggingService).getRequest(eq(BASE64_ENCODED_TOKEN));
        Assert.assertTrue(infoAppender.list.isEmpty());

        final ILoggingEvent errorLoggingEvent = errorAppender.list.get(0);
        Assert.assertEquals(LoggingMarkerMDC.FULL_MSG_EXCHANGE, errorLoggingEvent.getMarker());
        Assert.assertEquals(IllegalArgumentException.class.getName(), errorLoggingEvent.getThrowableProxy().getClassName());
        Assert.assertEquals(illegalArgumentException.getMessage(), errorLoggingEvent.getThrowableProxy().getMessage());
    }

    private LightRequestLogger connectorIncomingLightRequestLogger(LoggerTestData testData) throws SpecificCommunicationException {
        LightRequestLogger connectorIncomingLightRequestLogger = new ConnectorIncomingLightRequestLogger();
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

        HashMap<String, String[]> httpParametersMap = createHttpParameterMap(tokenBase64Array);
        when(mockHttpServletRequest.getParameterMap()).thenReturn(httpParametersMap);
        when(mockHttpServletRequest.getMethod()).thenReturn(method.getValue());
        //when(mockHttpServletRequest.getAttribute(TestLightRequestLogger.WHERE_TO_FIND_THE_LIGHT_TOKEN)).thenReturn(tokenBase64Array[0]);

        return mockHttpServletRequest;
    }

    protected HashMap<String, String[]> createHttpParameterMap(String[] tokenBase64Array) {
        String servletParameter = EidasParameterKeys.TOKEN.toString();
        HashMap<String, String[]> httpParametersMap = new HashMap<>();
        httpParametersMap.put(servletParameter, tokenBase64Array);
        return httpParametersMap;
    }

}
