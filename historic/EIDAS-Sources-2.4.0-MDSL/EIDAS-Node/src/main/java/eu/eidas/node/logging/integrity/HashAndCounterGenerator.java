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

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Generator for generating the hash for a log line, either with or without usage of a counter.
 * This class makes only sense if used by the {@link HashPatternLayoutEncoder}.
 * To avoid concurrency issue, while different log event are handled by different threads, all writing performed on
 * the internal output stream MUST BE synchronized.
 */
public final class HashAndCounterGenerator {

    private static final byte SEPARATOR = '#';
    private static final byte SPACE_SEPARATOR = ' ';
    private static final byte NEWLINE = '\n';
    private static final byte CARRIAGE = '\r';
    private static final byte BEGIN_SEPARATOR = '[';
    private static final byte END_SEPARATOR = ']';
    private final MessageDigest messageDigest;

    /**
     * To prevent concurrent writing of different log event at the same time, all writing performed on
     * the used output stream MUST BE synchronized.
     */
    private final ByteArrayOutputStream outputStream;
    private AtomicLong counter;

    /**
     * Base constructor without initialisation salting.
     *
     * @param isCounterUsed indicates if a counter is used
     * @param hashAlgorithm give the hash algorithm used
     */
    public HashAndCounterGenerator(final boolean isCounterUsed, final String hashAlgorithm) {
        this(isCounterUsed, hashAlgorithm, new ByteArrayOutputStream());
    }

    /**
     * Base constructor, visible for testing purposes only.
     *
     * @param isCounterUsed indicates if a counter is used
     * @param hashAlgorithm give the hash algorithm used
     * @param outputStream a byte array output stream to be used internally, visible for testing.
     */
    protected HashAndCounterGenerator(final boolean isCounterUsed, final String hashAlgorithm, ByteArrayOutputStream outputStream) {
        this.outputStream = outputStream;
        counter = isCounterUsed ? new AtomicLong(0) : null;

        try {
            messageDigest = MessageDigest.getInstance(hashAlgorithm);
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Retrieves the altered log message, with the hash and optional counter.<br>
     * As an example, configured with a SHA-256 hash algorithm and with counter, the log message: <br>
     *     TestA should lead to: TestA #1# [kQYTvzcedONYovA2OkEpDihUOl1PyapPioklXt04RgE=]
     *
     * @param originalLoggedBytes the original data to log, as a char array
     * @return the modified logged data, with hash and optional counter
     */
    public final byte[] getModifiedLoggedBytes(byte[] originalLoggedBytes) {
        synchronized (outputStream) {
            try {
                for (byte originalByte : originalLoggedBytes) {
                    write(originalByte);
                }
                outputStream.flush();
                return outputStream.toByteArray();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            } finally {
                outputStream.reset();
            }
        }
    }

    /**
     * Adds the following information to the log line : #xx# [hashValue]
     * where xx is the counter of the log line and hashValue is the SHA256 of the log line content
     *
     * @param byteToWrite bytes to write
     * @throws IOException when writing to the internally used output stream fails
     */
    private void write(int byteToWrite) throws IOException {
        // New line
        if (byteToWrite == NEWLINE) {
            // increment the counter and add it to the hash
            byte[] base64HashBytes = computeHashBeforeLog();
            outputStream.write(BEGIN_SEPARATOR);
            outputStream.write(base64HashBytes);
            outputStream.write(END_SEPARATOR);
            outputStream.write(NEWLINE);
        } else {
            if (byteToWrite != CARRIAGE) {
                writeAndUpdateMessageDigest((byte) byteToWrite);
            }
        }
    }

    /**
     * Writes a single byte value to the stream and updates the message digest.
     *
     * @param byteValue  single byte value
     */
    private void writeAndUpdateMessageDigest(byte byteValue) {
        messageDigest.update(byteValue);
        outputStream.write(byteValue);
    }

    /**
     * Compute the hash with the counter and salting next hash/
     *
     * @return base64HashBytes the base 64 hash bytes
     * @throws IOException when writing to the internally used output stream fails
     */
    private byte[] computeHashBeforeLog() throws IOException {
        writeAndUpdateMessageDigest(SPACE_SEPARATOR);
//        // increment the counter, write it in the outputstream and add it to the hash
        if (counter != null) {
            writeAndUpdateMessageDigest(SEPARATOR);
            counter.getAndIncrement();
            byte[] counterAsUTF8Bytes = counter.toString().getBytes(UTF_8);
            outputStream.write(counterAsUTF8Bytes);
            messageDigest.update(counterAsUTF8Bytes);
            writeAndUpdateMessageDigest(SEPARATOR);
            writeAndUpdateMessageDigest(SPACE_SEPARATOR);
        }
        // computing the hash and convert it in Base64
        byte[] base64HashBytes = DatatypeConverter.printBase64Binary(messageDigest.digest()).getBytes(UTF_8);
        messageDigest.reset();
        // adding previous hash for next hash salting
        messageDigest.update(base64HashBytes);
        messageDigest.update(NEWLINE);
        return base64HashBytes;
    }

}
