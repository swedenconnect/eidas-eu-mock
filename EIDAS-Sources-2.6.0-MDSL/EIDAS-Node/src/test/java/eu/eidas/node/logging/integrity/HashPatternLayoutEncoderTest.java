/*
 * Copyright (c) 2019 by European Commission
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
 *
 */

package eu.eidas.node.logging.integrity;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link HashPatternLayoutEncoder}
 *
 * To prevent superfluous logging while running this test class, an
 */
public class HashPatternLayoutEncoderTest {

    /**
     * Test method for {@link HashPatternLayoutEncoder#encode(ILoggingEvent)}
     * Test that an event is correctly logged, and that when a second event is logged, there is no longer an influence
     * from the first event.
     * <br>
     * Must succeed.
     */
    @Test
    public void testEncode() {

        LoggerContext loggerContext = new LoggerContext();
        Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        ILoggingEvent event1 = new LoggingEvent("", logger, Level.TRACE, "myMessage1", null, new Object[1]);
        ILoggingEvent event2 = new LoggingEvent("", logger, Level.TRACE, "myMessage2", null, new Object[1]);

        HashPatternLayoutEncoder hashPatternLayoutEncoder = new HashPatternLayoutEncoder();
        hashPatternLayoutEncoder.setPattern("%d{yyyy-MM-dd; HH:mm:ss.SSS} [%thread] %-5level %logger{66} %marker -%X{sessionId} -%X{remoteHost} -%msg%n");
        hashPatternLayoutEncoder.setContext(logger.getLoggerContext());
        hashPatternLayoutEncoder.start();

        byte[] encoded = hashPatternLayoutEncoder.encode(event1);
        String encodedString = new String(encoded, Charset.defaultCharset());

        assertTrue(encodedString.contains("myMessage1"));

        encoded = hashPatternLayoutEncoder.encode(event2);
        encodedString = new String(encoded, Charset.defaultCharset());

        assertTrue(encodedString.contains("myMessage2"));
        assertFalse("Second logging should no longer contain data from first event", encodedString.contains("myMessage1"));
    }
}