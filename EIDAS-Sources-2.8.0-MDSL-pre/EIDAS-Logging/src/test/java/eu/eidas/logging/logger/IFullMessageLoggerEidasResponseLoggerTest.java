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
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.logging.IFullMessageLogger;
import eu.eidas.logging.MessageLoggerUtils;
import eu.eidas.logging.messages.EidasResponseMessageLog;
import eu.eidas.logging.utils.ReadFileUtils;
import eu.eidas.logging.utils.logger.LevelFilter;
import eu.eidas.logging.utils.logger.LoggerTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class IFullMessageLoggerEidasResponseLoggerTest {

    private EidasResponseLogger connectorIncomingEidasResponseLogger;
    private MessageLoggerUtils mockedMessageLoggerUtils;

    private final Logger fullMessageLogger = (Logger) LoggerFactory.getLogger(
            IFullMessageLogger.getLoggerName(TestEidasResponseLogger.class)
    );

    private ListAppender<ILoggingEvent> logAppender;

    @Before
    public void setup() {
        connectorIncomingEidasResponseLogger = new TestEidasResponseLogger();
        mockedMessageLoggerUtils = mockMessageLoggerUtils();
        connectorIncomingEidasResponseLogger.setMessageLoggerUtils(mockedMessageLoggerUtils);

        logAppender = LoggerTestUtils.createStartListAppender(new LevelFilter(Level.INFO, Level.ERROR));
        fullMessageLogger.addAppender(logAppender);
    }

    @After
    public void teardown() {
        fullMessageLogger.detachAndStopAllAppenders();
    }

    /**
     * Test method for {@link ConnectorIncomingEidasResponseLogger#logFullMessage(HttpServletRequest)}.
     * Check logging occurs when full logging flag is activated
     * Must succeed.
     */

    @Test
    public void logFullMessageActive() {
        String request = new String(ReadFileUtils.readFileAsByteArray("logging/testFullLoggingEidasResponse.xml"));
        HttpServletRequest testRequest = mockHttpServletRequest(request);

        Mockito.when(mockedMessageLoggerUtils.isLogCompleteMessage()).thenReturn(true);

        connectorIncomingEidasResponseLogger.logFullMessage(testRequest);

        Assert.assertFalse(logAppender.list.isEmpty());
        ILoggingEvent actualLogEvent = logAppender.list.get(0);
        LoggerTestUtils.verifyILoggingEvent(actualLogEvent, Level.INFO, request);
    }


    /**
     * Test method for {@link ConnectorIncomingEidasResponseLogger#logFullMessage(HttpServletRequest)}.
     * Check logging occurs and is correct when full logging flag is activated and request is encoded in UTF_16BE
     * Must succeed.
     */

    @Test
    public void logFullMessageUTF16_BE() {
        String request = new String(ReadFileUtils.readFileAsByteArray("logging/testFullLoggingEidasResponse.xml"));

        request = "<?xml version=\"1.0\" encoding=\"UTF-16BE\" ?>" + request;
        ByteBuffer requestTransformEncodingBuffer = StandardCharsets.UTF_16BE.encode(request);
        String requestInUTF16BE = new String(requestTransformEncodingBuffer.array());

        HttpServletRequest testRequest = mockHttpServletRequest(requestInUTF16BE, StandardCharsets.UTF_16BE);

        Mockito.when(mockedMessageLoggerUtils.isLogCompleteMessage()).thenReturn(true);

        connectorIncomingEidasResponseLogger.logFullMessage(testRequest);

        Assert.assertFalse(logAppender.list.isEmpty());
        ILoggingEvent actualLogEvent = logAppender.list.get(0);
        LoggerTestUtils.verifyILoggingEvent(actualLogEvent, Level.INFO, request);
    }


    /**
     * Test method for {@link ConnectorIncomingEidasResponseLogger#logFullMessage(HttpServletRequest)}.
     * Check logging doesn't occur when full logging flag is inactive
     * Must succeed.
     */
    @Test
    public void logFullMessageInactive() {
        String request = new String(ReadFileUtils.readFileAsByteArray("logging/testFullLoggingEidasResponse.xml"));
        HttpServletRequest testRequest = mockHttpServletRequest(request);

        Mockito.when(mockedMessageLoggerUtils.isLogCompleteMessage()).thenReturn(false);

        connectorIncomingEidasResponseLogger.logFullMessage(testRequest);

        Assert.assertTrue(logAppender.list.isEmpty());
    }


    /**
     * Test method for {@link ConnectorIncomingEidasResponseLogger#logFullMessage(HttpServletRequest)}.
     * Test full logging when receiving an invalid SAML in the request
     * By checking information concerning the failure of the logging that appears in the log.
     * <p>
     * Error scenario.
     */
    @Test
    public void logFullMessageInvalidSaml() {
        String request = new String(ReadFileUtils.readFileAsByteArray("logging/testFullLoggingEidasRequest.xml"));

        ByteBuffer requestTransformEncodingBuffer = StandardCharsets.UTF_16BE.encode(request);
        String requestInUTF16BE = new String(requestTransformEncodingBuffer.array());

        HttpServletRequest testRequest = mockHttpServletRequest(requestInUTF16BE, StandardCharsets.UTF_16BE);

        Mockito.when(mockedMessageLoggerUtils.isLogCompleteMessage()).thenReturn(true);

        connectorIncomingEidasResponseLogger.logFullMessage(testRequest);

        Assert.assertFalse(logAppender.list.isEmpty());
        ILoggingEvent actualLogEvent = logAppender.list.get(0);
        Level expectedErrorLevel = Level.ERROR;
        String expectedMessage = "SAML Response is not valid";

        LoggerTestUtils.verifyILoggingEvent(actualLogEvent, expectedErrorLevel, expectedMessage);
    }


    /**
     * Test method for {@link ConnectorIncomingEidasResponseLogger#logFullMessage(HttpServletRequest)}.
     * Test full logging when receiving an invalid request
     * By checking information concerning the failure of the logging that appears in the log.
     * <p>
     * Error scenario.
     */
    @Test
    public void logFullMessageInvalidRequest() {
        HttpServletRequest testRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(mockedMessageLoggerUtils.isLogCompleteMessage()).thenReturn(true);

        connectorIncomingEidasResponseLogger.logFullMessage(testRequest);

        Assert.assertFalse(logAppender.list.isEmpty());
        ILoggingEvent actualLogEvent = logAppender.list.get(0);
        Level expectedErrorLevel = Level.ERROR;
        String expectedMessage = "Could not log the incoming response because of ";
        Class expectedException = IllegalArgumentException.class;
        LoggerTestUtils.verifyILoggingEvent(actualLogEvent, expectedErrorLevel, expectedMessage, expectedException);
    }

    private static class TestEidasResponseLogger extends EidasResponseLogger {

        public static final String WHERE_TO_FIND_THE_SAML = "SAMLResponse";

        @Override
        protected byte[] getSamlResponseDecodedBytes(HttpServletRequest httpServletRequest) {
            final WebRequest webRequest = new IncomingRequest(httpServletRequest);
            final String samlResponseToken = webRequest.getEncodedLastParameterValue(WHERE_TO_FIND_THE_SAML);
            return EidasStringUtil.decodeBytesFromBase64(samlResponseToken);
        }

        @Override
        protected void setMessageVector(@Nonnull EidasResponseMessageLog.Builder messageLogBuilder, @Nonnull HttpServletRequest httpServletRequest, IAuthenticationResponse authenticationResponse) {
            // full message logging only uses raw SAML
        }
    }

    private MessageLoggerUtils mockMessageLoggerUtils() {
        MessageLoggerUtils mockMessageLoggerUtils = Mockito.mock(MessageLoggerUtils.class);
        return mockMessageLoggerUtils;
    }

    private HttpServletRequest mockHttpServletRequest(String testRequest) {
        return mockHttpServletRequest(testRequest, StandardCharsets.UTF_8);
    }

    private HttpServletRequest mockHttpServletRequest(String testRequest, Charset charset) {
        HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn(BindingMethod.GET.getValue());
        Map<String, String[]> parameterMap = new HashMap<>();
        final String tokenBase64 = EidasStringUtil.encodeToBase64(testRequest);
        String[] samlResponses = {tokenBase64};
        parameterMap.put(TestEidasResponseLogger.WHERE_TO_FIND_THE_SAML, samlResponses);
        Mockito.when(mockHttpServletRequest.getParameterMap()).thenReturn(parameterMap);
        Mockito.when(mockHttpServletRequest.getCharacterEncoding()).thenReturn(charset.toString());
        return mockHttpServletRequest;
    }
}
