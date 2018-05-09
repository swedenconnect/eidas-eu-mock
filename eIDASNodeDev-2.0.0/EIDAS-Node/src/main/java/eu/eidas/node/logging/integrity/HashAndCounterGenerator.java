/*
 * Copyright (c) 2015 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.node.logging.integrity;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.bind.DatatypeConverter;

import eu.eidas.auth.commons.EidasStringUtil;

/**
 * Class used to generate hash in front of line in log files
 * The output generated will have the form of (log-output counter [hashValue] (for example : this is my log content #1# [ydBMlWX8ZlyAaB+x2CmTgCaHH2bhT1AeCFMd9mk4p4k=])
 *
 * @author vanegdi
 */
public class HashAndCounterGenerator extends OutputStream {

    private static final byte SEPARATOR = '#';
    private static final byte SPACE_SEPARATOR = ' ';
    public static final byte NEWLINE = '\n';
    private static final byte CARRIAGE = '\r';
    private static final byte BEGIN_SEPARATOR = '[';
    private static final byte END_SEPARATOR = ']';
    private final MessageDigest messageDigest;
    private final OutputStream outputStream;
    private AtomicLong counter;
    private String lastDigest;

    /**
     * Base constructor without initialisation salting.
     *
     * @param outputStream  the outputStream
     * @param isCounterUsed indicates if a counter is used
     * @param hashAlgorithm give the hash algorithm used
     */
    @SuppressWarnings("squid:S00112")
    public HashAndCounterGenerator(OutputStream outputStream, final boolean isCounterUsed, final String hashAlgorithm) {
        this.outputStream = outputStream;
        if (isCounterUsed) {
            counter = new AtomicLong(0);
        } else {
            counter = null;
        }
        try {
            messageDigest = MessageDigest.getInstance(hashAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            //TODO throw a specific exception instead of a generic one
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructor with initialisation salting.
     *
     * @param outputStream  the outputStream
     * @param isCounterUsed indicates if a counter is used
     * @param hashAlgorithm give the hash algorithm used
     */
    @SuppressWarnings("squid:S00112")
    public HashAndCounterGenerator(OutputStream outputStream, final String initSalting, final boolean isCounterUsed, final String hashAlgorithm) {
        this.outputStream = outputStream;
        if (isCounterUsed) {
            counter = new AtomicLong(0);
        } else {
            counter = null;
        }
        try {
            messageDigest = MessageDigest.getInstance(hashAlgorithm);
            messageDigest.update(initSalting.getBytes(Charset.forName("UTF-8")));
            messageDigest.update(NEWLINE);
        } catch (NoSuchAlgorithmException e) {
            //TODO throw a specific exception instead of a generic one
            throw new RuntimeException(e);
        }
    }

    private synchronized void writeAndUpdateMessageDigest(byte myValue) throws IOException {
        messageDigest.update(myValue);
        outputStream.write(myValue);
    }

    /**
     * Adds the following information to the log line : #xx# [hashValue]
     * where xx is the counter of the log line and hashValue is the SHA256 of the log line content
     *
     * @param byteToWrite bytes to write
     * @throws IOException error
     */
    @Override
    public void write(int byteToWrite) throws IOException {
        // New line
        if (byteToWrite == NEWLINE) {
            // increment the counter and add it to the hash
            byte[] base64HashBytes = computeHashBeforeLog();
            this.lastDigest = EidasStringUtil.toString(base64HashBytes);
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

    public String getLastDigest() {
        return lastDigest;
    }

    /**
     * Compute the hash with the counter and salting next hash
     *
     * @return hashbyte
     */
    private synchronized byte[] computeHashBeforeLog() throws IOException {
        writeAndUpdateMessageDigest(SPACE_SEPARATOR);
        // increment the counter, write it in the outputstream and add it to the hash
        if (counter != null) {
            writeAndUpdateMessageDigest(SEPARATOR);
            counter.getAndIncrement();
            outputStream.write(counter.toString().getBytes(Charset.forName("UTF-8")));
            messageDigest.update(counter.toString().getBytes(Charset.forName("UTF-8")));
            writeAndUpdateMessageDigest(SEPARATOR);
            writeAndUpdateMessageDigest(SPACE_SEPARATOR);
        }
        // computing the hash and convert it in Base64
        byte[] base64HashBytes = DatatypeConverter.printBase64Binary(messageDigest.digest()).getBytes(Charset.forName("UTF-8"));
        messageDigest.reset();
        // adding previous hash for next hash salting
        messageDigest.update(base64HashBytes);
        messageDigest.update(NEWLINE);
        return base64HashBytes;
    }

}