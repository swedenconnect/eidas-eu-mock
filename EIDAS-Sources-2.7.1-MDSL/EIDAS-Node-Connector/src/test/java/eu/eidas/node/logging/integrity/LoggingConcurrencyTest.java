/*
 * Copyright (c) 2022 by European Commission
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
package eu.eidas.node.logging.integrity;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.util.StatusPrinter;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.rightPad;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Detects concurrency and duplication issue when multiple threads are logging to the same logging file.
 *
 * The test launches multiple logging threads.
 * The logged lines contain repetitive information.
 * As each launched logging thread will log a different repetitive information, it is easy to predict
 * for each logging thread how many time the expected information has been logged.
 *
 * To avoid superfluous logging activity while running this test, an empty configuration logback-test.xml file must
 * exist vin the test classpath (in the resource directory). As the logback-test.wml is empty, the logback configuration
 * is set up programmatically by the test.
 *
 */
public class LoggingConcurrencyTest {

    private final static String LOG_EVENT_ENDLINE = "--------------------------";

    private final static char[] USED_CHARS = "abcdefghijklmnaopqrstu".toCharArray();
    private final static String pickUpChar(int index) {
        if (index > USED_CHARS.length) {
            throw new RuntimeException("Maximum number of simultaneous logging thread is " + USED_CHARS.length);
        }
        return Character.toString(USED_CHARS[index]);
    }

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /** Number of launched logging threads */
    private int loggingThreadsNumber;

    /** Number of lines in a log event */
    private int numberOfLinesPerLogEvent = 10;

    /** Number of logging Event generated by each logging threads*/
    private int numberOfLogEventPerThread = 5;

    /** Concurrency:  allows to wait for all logging thead completion */
    private CountDownLatch countDownLatch; //

    /** Concurrency:  Allows to start all logging threads almost simultaneously */
    private CyclicBarrier cyclicBarrier;

    /** Holds the information that must be retrieved in the generated logs */
    private List<ExpectedData> expected = Collections.synchronizedList(new ArrayList<>());

    /** Logger build and used by the test*/
    private Logger logger;

    /** Temporary logging file generated by unit test*/
    private File tempFile;

    @Before
    public void setUp() throws IOException {
        loggingThreadsNumber = 5;
        countDownLatch = new CountDownLatch(loggingThreadsNumber);
        cyclicBarrier = new CyclicBarrier(loggingThreadsNumber + 1); // +1 is for the launching thread.
        tempFile = tempFolder.newFile("tempLogFile3.log");
        logger = createTestLogger();
    }

    /**
     * Check if the eidas {@link HashPatternLayoutEncoder} is properly synchronized
     * This test may fail if a synchronisation issue is occurring or
     * if the amount of repetitive information does not match the expectation
     *
     * Must succeed
     * @throws BrokenBarrierException
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void hashPatternlayoutEncoderSynchronisationTest() throws BrokenBarrierException, InterruptedException, IOException {

        List<Thread> loggingThreads = createAllLoggingThreads();
        loggingThreads.forEach(Thread::start);

        // opening the cyclic barrier, all logging threads will run
        cyclicBarrier.await();

        // Waiting for the completion of all the looging threads.
        countDownLatch.await();

        // Reading all logging events from temporary file
        String loggedEvents = FileUtils.readFileToString(tempFile);

        for (ExpectedData expectedData : expected) {
            assertOccurences(loggedEvents, expectedData.threadName);
            assertOccurences(loggedEvents, expectedData.threadLine);
        }
    }

    /**
     * Programmatically build a logback {@link FileAppender} to be used by the test
     * @return the build logger
     */
    private Logger createTestLogger () {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder logEncoder = new HashPatternLayoutEncoder();
        logEncoder.setContext(lc);
        logEncoder.setPattern("%d{yyyy-MM-dd'T'HH:mm:ss.SSS'Z',GMT} [%thread] %-5level %logger{66} %marker -%X{sessionId} -%X{remoteHost} -%msg%n");
        logEncoder.start();

        FileAppender logFileAppender = new FileAppender();
        logFileAppender.setContext(lc);
        logFileAppender.setName("tempLogFile");
        logFileAppender.setEncoder(logEncoder);
        logFileAppender.setAppend(true);
        logFileAppender.setFile(tempFile.getPath());
        logFileAppender.start();

        Logger logger = lc.getLogger("tempLogger");
        logger.setLevel(Level.INFO);
        logger.addAppender(logFileAppender);
        StatusPrinter.print(lc);

        return logger;
    }

    /**
     * Creates all the logging threads used by the test
     * @return list of the logging threads.
     */
    private List<Thread> createAllLoggingThreads () {
        List<Thread> loggingThreads = new ArrayList<>();
        for (int i = 0; i < loggingThreadsNumber; i++) {
            String threadChar= pickUpChar(i);
            loggingThreads.add(createLoggingThread(threadChar));
        }
        return loggingThreads;
    }

    /**
     * Builds a logging thread
     *
     * @param threadChar alphabetical character used to build the thread name and the thread repetitively logged informations
     * @return the builmd thread
     */
    private Thread createLoggingThread(String threadChar) {
        return new Thread() {
            public void run() {
                String threadName = rightPad(EMPTY, 5, threadChar);
                String threadLine = rightPad(EMPTY, 11, threadChar.toUpperCase());
                String msgToLog = buildMsgToLog(threadName, threadLine);
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e.getMessage());
                }
                for (int i = 0; i < numberOfLogEventPerThread; i++) {
                    logger.info(msgToLog);
                }
                ExpectedData expectedData = new ExpectedData(threadName, threadLine);
                expected.add(expectedData);
                countDownLatch.countDown();
            }
        };
    }

    /**
     * Builds the message that a specific thread will repetitively log in a single log event
     * @param threadName used for recognizing which thread logged what
     * @param lineContent thread duplicated logged information
     * @return the logged message to repeat
     */
    private String buildMsgToLog(String threadName, String lineContent) {
        String linePrefix = threadName + ": ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfLinesPerLogEvent; i++) {
            sb.append(linePrefix)
                    .append(i)
                    .append(' ')
                    .append(lineContent)
                    .append('\n');
        }
        sb.append(LOG_EVENT_ENDLINE);
        return sb.toString();
    }

    /**
     * If for each thread the number of its name in the logged events does not match, a concurrency issue occurred.
     * @param loggedEvents generated output of all the logging threads
     * @param expected used to verify concurrency issue on a specific thread.
     */
    private void assertOccurences(String loggedEvents, String expected) {
        int actualOccurences = loggedEvents.split(expected).length - 1;
        int expectedOccurences = numberOfLogEventPerThread * numberOfLinesPerLogEvent;
        assertThat(loggedEvents, actualOccurences, is(expectedOccurences));
    }

    private class ExpectedData {
        private String threadName;
        private String threadLine;

        private ExpectedData (String threadName, String threadLine) {
            this.threadName = threadName;
            this.threadLine = threadLine;
        }
    }
}
