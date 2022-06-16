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

package eu.eidas.node.logging.service.messages;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.node.utils.logger.LevelFilter;
import eu.eidas.node.utils.logger.LoggerTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static eu.eidas.auth.commons.EidasParameterKeys.SAML_REQUEST;
import static eu.eidas.node.utils.ReadFileUtils.readFileAsByteArray;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link ProxyServiceIncomingEidasRequestLogger}.
 *
 * @since 2.5
 */
public class ProxyServiceIncomingEidasRequestFullLoggerTest {

    private ProxyServiceIncomingEidasRequestLogger proxyServiceIncomingEidasRequestLogger;
    private MessageLoggerUtils mockedMessageLoggerUtils;

    private Logger testedLogger;
    private ListAppender<ILoggingEvent> logAppender;

    @Before
    public void setup() {
        proxyServiceIncomingEidasRequestLogger = new ProxyServiceIncomingEidasRequestLogger();
        mockedMessageLoggerUtils = mockMessageLoggerUtils();
        proxyServiceIncomingEidasRequestLogger.setMessageLoggerUtils(mockedMessageLoggerUtils);

        logAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.INFO, Level.ERROR));
        testedLogger = (Logger) ReflectionTestUtils.getField(proxyServiceIncomingEidasRequestLogger, "fullLogger");
        testedLogger.addAppender(logAppender);
    }

    @After
    public void teardown() {
        testedLogger.detachAndStopAllAppenders();
    }

    /**
     * Test method for {@link ProxyServiceIncomingEidasRequestLogger#logFullMessage(HttpServletRequest)}.
     * Check logging occurs when full logging flag is activated
     * Must succeed.
     */
    @Test
    public void logFullMessageActive() {
        String request = new String(readFileAsByteArray("logging/testFullLoggingEidasRequest.xml"));
        HttpServletRequest testRequest = mockHttpServletRequest(request);

        Mockito.when(mockedMessageLoggerUtils.isLogCompleteMessage()).thenReturn(true);

        proxyServiceIncomingEidasRequestLogger.logFullMessage(testRequest);

        Assert.assertFalse(logAppender.list.isEmpty());
        ILoggingEvent actualLogEvent = logAppender.list.get(0);
        LoggerTestUtils.verifyILoggingEvent(actualLogEvent, Level.INFO, request);
    }

    /**
     * Test method for {@link ProxyServiceIncomingEidasRequestLogger#logFullMessage(HttpServletRequest)}.
     * Check logging occurs and is correct when full logging flag is activated and request is encoded in UTF_16BE
     * Must succeed.
     */
    @Test
    public void logFullMessageUTF16_BE () {
        String request = new String(readFileAsByteArray("logging/testFullLoggingEidasRequest.xml"));

        request = "<?xml version=\"1.0\" encoding=\"UTF-16BE\"?>" + request;
        ByteBuffer requestTransformEncodingBuffer = StandardCharsets.UTF_16BE.encode(request);
        String requestInUTF16BE = new String(requestTransformEncodingBuffer.array());

        HttpServletRequest testRequest = mockHttpServletRequest(requestInUTF16BE, StandardCharsets.UTF_16BE);

        Mockito.when(mockedMessageLoggerUtils.isLogCompleteMessage()).thenReturn(true);

        proxyServiceIncomingEidasRequestLogger.logFullMessage(testRequest);

        Assert.assertFalse(logAppender.list.isEmpty());
        ILoggingEvent actualLogEvent = logAppender.list.get(0);
        LoggerTestUtils.verifyILoggingEvent(actualLogEvent, Level.INFO, request);
    }

    /**
     * Test method for {@link ProxyServiceIncomingEidasRequestLogger#logFullMessage(HttpServletRequest)}.
     * Check logging doesn't occur when full logging flag is inactive
     * Must succeed.
     */
    @Test
    public void logFullMessageInactive() {
        String request = new String(readFileAsByteArray("logging/testFullLoggingEidasRequest.xml"));
        HttpServletRequest testRequest = mockHttpServletRequest(request);

        Mockito.when(mockedMessageLoggerUtils.isLogCompleteMessage()).thenReturn(false);

        proxyServiceIncomingEidasRequestLogger.logFullMessage(testRequest);

        Assert.assertTrue(logAppender.list.isEmpty());
    }

    /**
     * Test method for {@link ProxyServiceIncomingEidasRequestLogger#logFullMessage(HttpServletRequest)}.
     * Test full logging when receiving an invalid SAML in the request (wrong encoding information)
     * By checking information concerning the failure of the logging that appears in the log.
     * <p>
     * Error scenario.
     */
    @Test
    public void logFullMessageInvalidSaml() {
        String request = new String(readFileAsByteArray("logging/testFullLoggingEidasRequest.xml"));

        ByteBuffer requestTransformEncodingBuffer = StandardCharsets.UTF_16BE.encode(request);
        String requestInUTF16BE = new String(requestTransformEncodingBuffer.array());

        HttpServletRequest testRequest = mockHttpServletRequest(requestInUTF16BE, StandardCharsets.UTF_16BE);

        Mockito.when(mockedMessageLoggerUtils.isLogCompleteMessage()).thenReturn(true);

        proxyServiceIncomingEidasRequestLogger.logFullMessage(testRequest);

        Assert.assertFalse(logAppender.list.isEmpty());
        ILoggingEvent actualLogEvent = logAppender.list.get(0);
        Level expectedErrorLevel = Level.ERROR;
        String expectedMessage = "SAML Request is not valid";
        ;
        LoggerTestUtils.verifyILoggingEvent(actualLogEvent, expectedErrorLevel, expectedMessage);
    }

    /**
     * Test method for {@link ProxyServiceIncomingEidasRequestLogger#logFullMessage(HttpServletRequest)}.
     * Test full logging when receiving an invalid request
     * By checking information concerning the failure of the logging that appears in the log.
     * <p>
     * Error scenario.
     */
    @Test
    public void logFullMessageInvalidRequest() {
        HttpServletRequest testRequest = mockHttpServletRequest(null);

        Mockito.when(mockedMessageLoggerUtils.isLogCompleteMessage()).thenReturn(true);

        proxyServiceIncomingEidasRequestLogger.logFullMessage(testRequest);

        Assert.assertFalse(logAppender.list.isEmpty());
        ILoggingEvent actualLogEvent = logAppender.list.get(0);
        Level expectedErrorLevel = Level.ERROR;
        String expectedMessage = "SAML Request is not valid";

        LoggerTestUtils.verifyILoggingEvent(actualLogEvent, expectedErrorLevel, expectedMessage);
    }

    private MessageLoggerUtils mockMessageLoggerUtils() {
        MessageLoggerUtils mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        return mockMessageLoggerUtils;
    }

    private HttpServletRequest mockHttpServletRequest(String testRequest) {
        return mockHttpServletRequest(testRequest, StandardCharsets.UTF_8);
    }

    private HttpServletRequest mockHttpServletRequest(String testRequest, Charset encodingCharset) {
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn(BindingMethod.GET.getValue());
        Map<String, String[]> parameterMap = new HashMap<>();
        final String tokenBase64 = testRequest != null ? EidasStringUtil.encodeToBase64(testRequest) : null;
        if (tokenBase64 != null) {
            String[] samlRequests = {tokenBase64};
            parameterMap.put(SAML_REQUEST.toString(), samlRequests);
        }
        Mockito.when(mockHttpServletRequest.getParameterMap()).thenReturn(parameterMap);
        Mockito.when(mockHttpServletRequest.getCharacterEncoding()).thenReturn(encodingCharset.toString());
        return mockHttpServletRequest;
    }
}
