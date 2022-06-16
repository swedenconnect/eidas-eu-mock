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

package eu.eidas.node.utils.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.Assert;

/**
 * Utils class for LoggerFilter classes.
 */
public class LoggerTestUtils {

    /**
     * Created and starts a {@link ListAppender}
     *
     * @param levelFilter the instance of the {@link LevelFilter}
     * @return the instance of  {@link ListAppender}
     */
    public static ListAppender<ILoggingEvent> createStartListAppender(LevelFilter levelFilter) {
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.addFilter(levelFilter);
        listAppender.start();

        return listAppender;
    }

    /**
     * Verification method to check if a logging event has the correct logging level.
     * @param loggingEvent  the logging event to check
     * @param expectedLevel the expected level of logging
     */
    private static void verifyILoggingEventLevel(ILoggingEvent loggingEvent, Level expectedLevel) {
        Assert.assertNotNull(loggingEvent);
        Assert.assertEquals(expectedLevel, loggingEvent.getLevel());
    }

    /**
     * Verification method to check if a logging event is a specific message
     * @param loggingEvent      the logging event to check
     * @param expectedMessage   the logging message expected
     */
    private static void verifyILoggingEventMessage(ILoggingEvent loggingEvent, String expectedMessage) {
        Assert.assertNotNull(loggingEvent);
        Assert.assertEquals(expectedMessage, loggingEvent.getMessage());
    }

    /**
     * Verification method to check if a logging event has the correct logging level.
     * @param loggingEvent      the logging event to check
     * @param expectedException the expected type of exception of to be logged
     */
    private static void verifyILoggingEventException(ILoggingEvent loggingEvent, Class<? extends Throwable> expectedException) {
        Assert.assertNotNull(loggingEvent);
        Assert.assertEquals(expectedException.getName(), loggingEvent.getThrowableProxy().getClassName());
    }

    /**
     * Verification method to check if a logging event has a specific message
     * @param loggingEvent      the logging event to check
     * @param expectedMessage   the logging message expected
     */
    public static void verifyILoggingEvent(ILoggingEvent loggingEvent, String expectedMessage) {
        verifyILoggingEventMessage(loggingEvent, expectedMessage);
    }

    /**
     * Verification method to check if a logging event has a specific level and a specific message
     * @param loggingEvent      the logging event to check
     * @param expectedLevel     the expected level of logging
     * @param expectedMessage   the logging message expected
     */
    public static void verifyILoggingEvent(ILoggingEvent loggingEvent, Level expectedLevel, String expectedMessage) {
        verifyILoggingEventLevel(loggingEvent, expectedLevel);
        verifyILoggingEventMessage(loggingEvent, expectedMessage);
    }

    /**
     * Verification method to check if a logging event contains information about a message and an exception
     * @param loggingEvent      the logging event to check
     * @param expectedLevel     the expected level of logging
     * @param expectedMessage   A message expected to be contained in the logging event message
     * @param expectedException An expected type of exception
     */
    public static void verifyILoggingEvent(ILoggingEvent loggingEvent, Level expectedLevel, String expectedMessage,
            Class<? extends Throwable> expectedException) {
        verifyILoggingEventLevel(loggingEvent, expectedLevel);
        Assert.assertTrue(loggingEvent.getMessage().contains(expectedMessage));
        verifyILoggingEventException(loggingEvent, expectedException);
    }

    private LoggerTestUtils(){}
}
